# SandCastle
 
Building a sample app with mircoservice by following Microservices with Spring boot and Spring cloud,  a book by Magnus Larsson.

# Overview

... coming soon ...

# Building the source

The project uses gradle as the build tool. Multi project build was set up for the ease of development.

Use the following command to build all the services

```
./gradlew build
```

# Run the services

To run any service, run the following command 
```shell
java -jar services/product/build/libs/product-0.0.1-SNAPSHOT.jar
java -jar services/review/build/libs/review-0.0.1-SNAPSHOT.jar
java -jar services/recommendation/build/libs/recommendation-0.0.1-SNAPSHOT.jar

```

Start all the services
```shell
java -jar services/product-composite/build/libs/product-composite-0.0.1-SNAPSHOT.jar &
java -jar services/product/build/libs/product-0.0.1-SNAPSHOT.jar &
java -jar services/recommendation/build/libs/recommendation-0.0.1-SNAPSHOT.jar &
java -jar services/review/build/libs/review-0.0.1-SNAPSHOT.jar &
```

Stop the services with

```shell
kill $(jobs -p)
```
### Endpoints


[Product service](http://localhost:7001/product/1)

[Recommendation service](http://localhost:7002/recommendation?productId=1)

[Review service](http://localhost:7003/review?productId=1)

[Product-composite service](http://localhost:7000/product-composite/1)

# Docker

### Product service
First build the project using
```shell
./gradlew :services:product:build
```
```shell
cd services/product
docker build -t product .
# run
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" product
```

[Browse the endpoint for products](http://localhost:8080/product/1)

### Review service

```shell
./gradlew :services:review:build
cd services/review
docker build -t review .
# run
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" review

```
[Browse the endpoint for reviews](http://localhost:8080/review?productId=1)


### Recommendation service

```shell
./gradlew :services:recommendation:build
cd services/recommendation
docker build -t recommendation .
# run
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" recommendation

```

[Browse the endpoint for recommendations](http://localhost:8080/recommendation?productId=1)

### Product composite service

Since this service depends on the other services, make sure they are running or use docker-compose to run all the services.

```shell
# build
./gradlew :services:product-composite:build
# create the docker image
cd services/product-composite
docker build -t product-composite .
# run the image
docker run --rm -p8080:8080 -e "SPRING_PROFILES_ACTIVE=docker" product-composite
```

[Browse the endpoint for product-composite](http://localhost:8080/product-composite/1)

# Docker compose

```shell
./gradlew build
docker-compose build
docker images | grep sandcastle

# start the services
docker-compose up -d
docker-compose logs -f
docker-compose down

```




## TODO ::

- Write an overview of the project
- add test-em-all script
