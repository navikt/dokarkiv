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
if test -f /var/run/secrets/nais.io/ldap/username;
then
    echo "Setting SPRING_LDAP_USERNAME"
    export  SPRING_LDAP_USERNAME=$(cat /var/run/secrets/nais.io/dokarkivDS/username)
fi
if test -f /var/run/secrets/nais.io/ldap/password;
then
    echo "Setting SPRING_LDAP_PASSWORD"
    export  SPRING_LDAP_***passord=gammelt_passord***)
fi