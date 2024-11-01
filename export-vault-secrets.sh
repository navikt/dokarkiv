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
if test -f /var/run/secrets/nais.io/db_creds/username;
then
    echo "Setting SPRING_DATASOURCE_USERNAME"
    export SPRING_DATASOURCE_USERNAME=$(cat /var/run/secrets/nais.io/db_creds/username)
fi
if test -f /var/run/secrets/nais.io/db_creds/password;
then
    echo "Setting SPRING_DATASOURCE_PASSWORD"
    export SPRING_DATASOURCE_PASSWORD=$(cat /var/run/secrets/nais.io/db_creds/password)
fi
if test -f /var/run/secrets/nais.io/db_config/jdbc_url;
then
    export SPRING_DATASOURCE_URL=$(cat /var/run/secrets/nais.io/db_config/jdbc_url)
    echo "Setting SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL"
fi

if test -f /var/run/secrets/nais.io/db_config/ons_host;
then
    export DATABASE_ONSHOSTS=$(cat /var/run/secrets/nais.io/db_config/ons_host)
    echo "Setting DATABASE_ONSHOSTS=$DATABASE_ONSHOSTS"
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

if test -f /var/run/secrets/nais.io/vault/gcloud_serviceaccount
then
    echo "Setting GOOGLE_APPLICATION_CREDENTIALS"
    export GOOGLE_APPLICATION_CREDENTIALS=/var/run/secrets/nais.io/vault/gcloud_serviceaccount
fi