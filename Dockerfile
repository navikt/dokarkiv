FROM navikt/java:8

COPY app/target/app.jar /app/app.jar

ENV JAVA_OPTS="-Xmx512m \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"
