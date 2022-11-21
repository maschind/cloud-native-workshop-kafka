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

## Build inventory service

Add jdbc-postgresql plugin
```
mvn quarkus:add-extension -Dextensions="jdbc-postgresql" -f inventory-service
```
Add postgresql db 
sdsd
sdsd


Deploy service: 
```
oc project cloudnativeapps && \
mvn clean compile package -DskipTests -f inventory-service
```
Add labels:
```
oc label dc/inventory app.kubernetes.io/part-of=inventory --overwrite && \
oc label dc/inventory-database app.kubernetes.io/part-of=inventory app.openshift.io/runtime=postgresql --overwrite && \
oc annotate dc/inventory app.openshift.io/connects-to=inventory-database --overwrite && \
oc annotate dc/inventory app.openshift.io/vcs-ref=ocp-4.9 --overwrite
```

---

# Credits
This cloud native workshop is based on CCN Roadshow (Dev Track) https://github.com/RedHat-Middleware-Workshops/cloud-native-workshop-v2m4-labs
