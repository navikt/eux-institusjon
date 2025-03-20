FROM gcr.io/distroless/java21
COPY target/eux-institusjon.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
