# SandCastle
 
Building a sample app with mircoservice by following Microservices with Spring boot and Spring cloud,  a book by Magnus Larsson.

# Overview

... coming soon ...

# Building the source

The project uses gradle as the build tool. Multi project build was set up for the ease of development.

Use the follwing command to build all the services

```
./gradlew build
```

# Run the services

To run the any service, run the following command 
```shell
java -jar services/product/build/libs/product-0.0.1-SNAPSHOT.jar
java -jar services/review/build/libs/review-0.0.1-SNAPSHOT.jar
java -jar services/recommendation/build/libs/recommendation-0.0.1-SNAPSHOT.jar

```