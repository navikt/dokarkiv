#dokarkiv

* [Funksjonelle Krav](#1-funksjonelle-krav)
* [Distribusjon av tjenesten (deployment)](#2-distribusjon-av-tjenesten-deployment)
* [Utviklingsmiljø](#3-utviklingsmilj)
* [Drift og støtte](#4-drift-og-sttte)

## Funksjonelle krav
Dokarkiv består av arkivtjenester til fagarkivet,og inneholder både dokumentlager og metadata om dokumentene og deres journalposter.

For mer informasjon: [confluence](https://confluence.adeo.no/display/BOA/dokarkiv)


## Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort av Jenkins:
[regoppslag CI / CD](https://dok-jenkins.adeo.no/job/dokarkiv/job/master/)
Push/merge til masterbranch vil teste, bygge og deploye til produksjonsmiljø og testmiljø.


## Utviklingsmiljø
### Forutsetninger
* Java 11
* Kubectl
* Maven

### Kjøre prosjektet lokalt
For å kjøre opp applikasjonen lokal, bruk profile `nais` og systemvariabler hentet fra vault: [System variabler](https://vault.adeo.no/ui/vault/secrets/secret/list/dokument/dokarkiv/) 

### Bygge app.jar og kjøre tester
`mvn clean package`/`mvn clean install`


## Drift og støtte
### Logging
Loggene til tjenesten kan leses på to måter:

### Kibana
For [dev-fss](https://logs.adeo.no/goto/25ee1a4d3a207f9bfb97c9d7abbadf72)

For [prod-fss](https://logs.adeo.no/goto/df4f7496e3b7a603efa49ca05a761aa5)

### Kubectl
For dev-fss:
```shell script
kubectl config use-context dev-fss
kubectl get pods -n q1 -l app=dokarkiv
kubectl logs -f dokarkiv-<POD-ID> -n teamdokumenthandtering -c dokarkiv
```

For prod-fss:
```shell script
kubectl config use-context prod-fss
kubectl get pods -l app=dokarkiv
kubectl logs -f dokarkiv-<POD-ID> -n teamdokumenthandtering -c dokarkiv
```

### Tilgangsstyring
Ved behov for tilgang til Dokarkiv kan andre team selv gjøre nødvendige kodeendringer:
- Lag en ny branch med endringene du ønsker for tilgang. Endringene må legges til i alle ønskede miljø i filene: /nais/<miljø>-config.json.
- Legg til audience i ```NO_NAV_SECURITY_JWT_ISSUER_OPENAM_ACCEPTED_AUDIENCE``` eller ```NO_NAV_SECURITY_JWT_ISSUER_RESTSTS_ACCEPTED_AUDIENCE```, avhengig om du bruker OpenAm eller Rest sts.
- Push endringene og lag en pull request. 
- Pull requesten vil bli gått gjennom og merget av noen i Team Dokumentløsninger.

### Henvendelser
Spørsmål til koden eller prosjektet kan rettes til Team Dokumentløsninger på:
* [\#Team Dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)



