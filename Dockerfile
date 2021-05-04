FROM navikt/java:11-appdynamics
ENV APPD_ENABLED=true
ENV APPD_TIER="${NAMESPACE}_dokarkiv"

COPY app/target/app.jar /app/app.jar
COPY export-vault-secrets.sh /init-scripts/10-export-vault-secrets.sh

ENV JAVA_OPTS="-Xmx1536m \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=nais"
