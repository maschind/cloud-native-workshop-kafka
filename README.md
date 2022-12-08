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

![Target Scenario](/images/lab3-goal.png)

# Installation 
Couple of Red Hat tech needs to be installed: 
1. Get a OpenShift cluster v4.10 (or greater)
2. Install Serverless 1.25 Operator 
3. Install AMQ Streams 2.2 Operator
4. Make sure oc cli and maven, jdk11, mandrel is installed
5. Add project/namespace _cloudnativeapps_

## Setup App Infra

---
**_INFO_** 
Some sources are configured to make use of namespace cloudnativeapps. If you choose for another name please modify the sources. 

```
sed ... 
```

---

Setup a new project/namespace
```
oc new-project cloudnativeapps
oc project cloudnativeapps
```

Deploy some needed databases, cache and Kafka
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

oc apply -f resources/Kafka.yaml
oc apply -f resources/KafkaTopic.yaml

oc apply -f resources/KnativeEventing.yaml 
oc apply -f resources/KnativeServing.yaml
oc apply -f resources/KnativeKafka.yaml 
```

## Build inventory service

Build and deploy inventory service: 

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

Add network policies (in case of deny default)
```
oc apply -f resources/....
```

## Build catalog service

Build and deploy catalog service: 
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

## Build order service

Build and deploy order service: 

```
mvn clean package -DskipTests -f order-service && oc rollout status -w dc/order
```
Add labels:
```
oc label dc/order app.kubernetes.io/part-of=order --overwrite && \
oc label dc/order-database app.kubernetes.io/part-of=order app.openshift.io/runtime=mongodb --overwrite && \
oc annotate dc/order app.openshift.io/connects-to=order-database --overwrite && \
oc annotate dc/order app.openshift.io/vcs-ref=ocp-4.9 --overwrite
```


## Build Web-UI service

Build and deploy web-ui service: 

```
npm run nodeshift && oc expose svc/coolstore-ui && \
oc label dc/coolstore-ui app.kubernetes.io/part-of=coolstore --overwrite && \
oc annotate dc/coolstore-ui app.openshift.io/connects-to=order-cart,catalog,inventory,order --overwrite && \
oc annotate dc/coolstore-ui app.openshift.io/vcs-uri=https://github.com/maschind/cloud-native-workshop-kafka.git --overwrite && \
oc annotate dc/coolstore-ui app.openshift.io/vcs-ref=ocp-4.9 --overwrite
cd ..
```

## Build payment service

Build and deploy payment service: 

```
mvn clean package -Pnative -DskipTests -Dnative-image.docker-build=true  -Dquarkus.native.native-image-xmx=2g -f payment-service && oc rollout status -w dc/payment
```
Add labels:
```
oc label Revision/payment-00001 app.openshift.io/runtime=quarkus --overwrite
```

```
oc apply -f resources/KafkaSource.yaml
```


---

# Credits
This cloud native workshop is based on CCN Roadshow (Dev Track) https://github.com/RedHat-Middleware-Workshops/cloud-native-workshop-v2m4-labs


