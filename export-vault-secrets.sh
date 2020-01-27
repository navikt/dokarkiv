#!/usr/bin/env sh

if test -f /secrets/serviceuser/srvdokarkiv/username;
then
    echo "Setting serviceuser_username"
    export  serviceuser_username=$(cat /secrets/serviceuser/srvdokarkiv/username)
fi
if test -f /secrets/serviceuser/srvdokarkiv/password;
then
    echo "Setting serviceuser_password"
    export  serviceuser_***passord=gammelt_passord***)
fi
if test -f /var/run/secrets/nais.io/dokarkivDS/username;
then
    echo "Setting SPRING_DATASOURCE_USERNAME"
    export  SPRING_DATASOURCE_USERNAME=$(cat /var/run/secrets/nais.io/dokarkivDS/username)
fi
if test -f /var/run/secrets/nais.io/dokarkivDS/password;
then
    echo "Setting SPRING_DATASOURCE_PASSWORD"
    export  SPRING_DATASOURCE_***passord=gammelt_passord***)
fi

if test -d /var/run/secrets/nais.io/vault;
then
    echo "Setting dokarkiv_s3_creds_password"
    export  dokarkiv_s3_creds_***passord=gammelt_passord***)
    echo "Setting dokarkiv_s3_creds_username"
    export  dokarkiv_s3_creds_username=$(cat /var/run/secrets/nais.io/vault/dokarkiv_s3_creds_username)
    echo "Setting dokprodmellomlager_s3_storage_crypto_password"
    export  dokprodmellomlager_s3_storage_crypto_***passord=gammelt_passord***)
fi
