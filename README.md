#dokarkiv

Arkivtjenester

For mer informasjon: [confluence](https://confluence.adeo.no/display/BOA/dokarkiv)

----------

## Tilgangsstyring

Ved behov for tilgang til Dokarkiv kan andre team selv gjøre nødvendige kodeendringer:
- Lag en ny branch med endringene du ønsker for tilgang. Endringene må legges til i alle ønskede miljø i filene: /nais/<miljø>-config.json.
- Legg til audience i ```NO_NAV_SECURITY_JWT_ISSUER_OPENAM_ACCEPTED_AUDIENCE``` eller ```NO_NAV_SECURITY_JWT_ISSUER_RESTSTS_ACCEPTED_AUDIENCE```, avhengig om du bruker OpenAm eller Rest sts.
- Push endringene og lag en pull request. 
- Pull requesten vil bli gått gjennom og merget av noen i Team Dokumentløsninger.

----------

## Henvendelser
Spørsmål til koden eller prosjektet kan rettes til Team Dokumentløsninger på:
[\#Team Dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)
