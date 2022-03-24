#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvdokarkiv/username;
then
    echo "Setting serviceuser_username"
    export serviceuser_username=$(cat /secrets/serviceuser/srvdokarkiv/username)
fi
if test -f /secrets/serviceuser/srvdokarkiv/password;
then
    echo "Setting serviceuser_password"
    export serviceuser_password=$(cat /secrets/serviceuser/srvdokarkiv/password)
fi
if test -f /var/run/secrets/nais.io/dokarkivDS/username;
then
    echo "Setting SPRING_DATASOURCE_USERNAME"
    export SPRING_DATASOURCE_USERNAME=$(cat /var/run/secrets/nais.io/dokarkivDS/username)
fi
if test -f /var/run/secrets/nais.io/dokarkivDS/password;
then
    echo "Setting SPRING_DATASOURCE_PASSWORD"
    export SPRING_DATASOURCE_PASSWORD=$(cat /var/run/secrets/nais.io/dokarkivDS/password)
fi
if test -f /var/run/secrets/nais.io/ldap/username;
then
    echo "Setting SPRING_LDAP_USERNAME"
    export SPRING_LDAP_USERNAME=$(cat /var/run/secrets/nais.io/ldap/username)
fi
if test -f /var/run/secrets/nais.io/ldap/password;
then
    echo "Setting SPRING_LDAP_PASSWORD"
    export SPRING_LDAP_PASSWORD=$(cat /var/run/secrets/nais.io/ldap/password)
fi
echo "Exporting appdynamics environment variables"
if test -f /var/run/secrets/nais.io/appdynamics/appdynamics.env;
then
    export $(cat /var/run/secrets/nais.io/appdynamics/appdynamics.env)
    export APPDYNAMICS_AGENT_BASE_DIR=/tmp/appdynamics
    echo "Appdynamics environment variables exported"
else
    echo "No such file or directory found at /var/run/secrets/nais.io/appdynamics/appdynamics.env"
fi

if test -f /var/run/secrets/nais.io/vault/gcloud_serviceaccount
then
    echo "Setting GOOGLE_APPLICATION_CREDENTIALS"
    export GOOGLE_APPLICATION_CREDENTIALS=/var/run/secrets/nais.io/vault/gcloud_serviceaccount
fi