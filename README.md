Cloud Native Workshop: auto scaling with kafka client application
=== 

# Introduction 
This cloud native workshop demonstrates knative capabilities using quarkus and an auto scaled kafka consumer application 

This repo provides templates, generated Java codes, empty configuration for each labs that developers need to implement cloud-native microservices in workshop. 

The included Java projects and/or installation files are here:

* Catalog Service - A Spring boot application running on JBoss Web Server (Tomcat) and PostgreSQL, serves products and prices for retail products
* Cart Service - Quarkus application running on OpenJDK and native which manages shopping cart for each customer, together with infinispan/JDG
* Inventory Service - Quarkus application running on OpenJDK and PostgreSQL, serves inventory and availability data for retail products
* Order service  - Quarkus application service running on OpenJDK or native for writing and displaying reviews for products
* User Service - Vert.x service running on JDK for managing users
* Payment Service  - A Quarkus based FaaS with Knative 

# Installation 

1. Get a cluster: OCP 4.10 (or greater)
2. Install Serverless Operator 
1. 
3. make sure oc cli and maven is installed
4. oc new-project cloudnativeapps

## Setup App Infra

```
oc new-app \
    --name=inventory-database \
    -e POSTGRESQL_USER=inventory \
    -e POSTGRESQL_PASSWORD=mysecretpassword \
    -e POSTGRESQL_DATABASE=inventory \
    registry.redhat.io/rhel8/postgresql-10

oc new-app \
    --name=catalog-database \
    -e POSTGRESQL_USER=catalog \
    -e POSTGRESQL_PASSWORD=mysecretpassword \
    -e POSTGRESQL_DATABASE=catalog \
    registry.redhat.io/rhel8/postgresql-10

oc new-app --as-deployment-config quay.io/openshiftlabs/ccn-infinispan:12.0.0.Final-1 --name=datagrid-service -e USER=user -e PASS=pass

oc new-app --as-deployment-config --docker-image quay.io/openshiftlabs/ccn-mongo:4.0 --name=order-database
```

## Build inventory service

Add jdbc-postgresql plugin
```
mvn quarkus:add-extension -Dextensions="jdbc-postgresql" -f inventory-service
```

Deploy service: 

```
mvn clean compile package -DskipTests -f inventory-service
```
Add labels:
```
oc label dc/inventory app.kubernetes.io/part-of=inventory --overwrite && \
oc label deployment/inventory-database app.kubernetes.io/part-of=inventory app.openshift.io/runtime=postgresql --overwrite && \
oc annotate dc/inventory app.openshift.io/connects-to=inventory-database --overwrite && \
oc annotate dc/inventory app.openshift.io/vcs-ref=ocp-4.9 --overwrite
```

## Build catalog service

build catalog:
```
mvn clean package spring-boot:repackage -DskipTests -f catalog-service
```

Deploy service: 

```
oc new-build registry.access.redhat.com/ubi8/openjdk-11 --binary --name=catalog -l app=catalog
oc start-build catalog --from-file=catalog-service/target/catalog-1.0.0-SNAPSHOT.jar --follow
```
Add labels:
```
oc new-app catalog --as-deployment-config -e JAVA_OPTS_APPEND='-Dspring.profiles.active=openshift' && oc expose service catalog && \
oc label dc/catalog app.kubernetes.io/part-of=catalog app.openshift.io/runtime=rh-spring-boot --overwrite && \
oc label deployment/catalog-database app.kubernetes.io/part-of=catalog app.openshift.io/runtime=postgresql --overwrite && \
oc annotate dc/catalog app.openshift.io/connects-to=inventory,catalog-database --overwrite && \
oc annotate dc/catalog app.openshift.io/vcs-uri=https://github.com/maschind/cloud-native-workshop-kafka.git --overwrite && \
oc annotate dc/catalog app.openshift.io/vcs-ref=ocp-4.9 --overwrite
```

## Build cart service

add openshift extension: 
```
mvn quarkus:add-extension -Dextensions="openshift" -f cart-service
```

Build and deploy cart service: 

```
mvn clean package -DskipTests -f cart-service && oc rollout status -w dc/cart
```
Add labels:
```
oc label dc/cart app.kubernetes.io/part-of=cart app.openshift.io/runtime=quarkus --overwrite && \
oc label dc/datagrid-service app.kubernetes.io/part-of=cart app.openshift.io/runtime=datagrid --overwrite && \
oc annotate dc/cart app.openshift.io/connects-to=catalog,datagrid-service --overwrite && \
oc annotate dc/cart app.openshift.io/vcs-ref=ocp-4.9 --overwrite
```

---

# Credits
This cloud native workshop is based on CCN Roadshow (Dev Track) https://github.com/RedHat-Middleware-Workshops/cloud-native-workshop-v2m4-labs




oc new-app \
    --name=inventory-database \
    -e POSTGRESQL_USER=inventory \
    -e POSTGRESQL_PASSWORD=mysecretpassword \
    -e POSTGRESQL_DATABASE=inventory \
    registry.redhat.io/rhel8/postgresql-10

oc new-app \
    --name=catalog-database \
    -e POSTGRESQL_USER=catalog \
    -e POSTGRESQL_PASSWORD=mysecretpassword \
    -e POSTGRESQL_DATABASE=catalog \
    registry.redhat.io/rhel8/postgresql-10

oc new-app --as-deployment-config quay.io/openshiftlabs/ccn-infinispan:12.0.0.Final-1 --name=datagrid-service -e USER=user -e PASS=pass

oc new-app --as-deployment-config --docker-image quay.io/openshiftlabs/ccn-mongo:4.0 --name=order-database