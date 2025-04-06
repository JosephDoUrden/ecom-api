FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu as build
WORKDIR /workspace/app

# Copy only pom.xml first to cache dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests

FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
