FROM openjdk:11-jre
MAINTAINER Alex Funk <mykotlin@alexfunk.de>

ENTRYPOINT ["/usr/local/openjdk-11/bin/java", "-jar", "/usr/share/myservice/myservice.jar"]

EXPOSE 7070/tcp

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
# ADD target/lib           /usr/share/myservice/lib
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/myservice/myservice.jar