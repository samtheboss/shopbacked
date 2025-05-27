FROM maven:3.9.6-eclipse-temurin-22-jammy AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:22-jdk
COPY --from=build /target/shop-0.0.1-SNAPSHOT.jar shop.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","shop.jar"]