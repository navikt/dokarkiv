# Dokarkiv
Dokarkiv består av arkivtjenester til fagarkivet,og inneholder både dokumentlager og metadata om dokumentene og deres journalposter.

Mer informasjon om appen og Rest-endepunktene finner du på henholdsvis [Confluence-doken for Dokarkiv](https://confluence.adeo.no/display/BOA/dokarkiv) 
og [Swagger-doken](https://dokarkiv.dev.intern.nav.no/swagger-ui/index.html).

## Distribusjon av tjenesten (deployment)
Distribusjon av tjenesten er gjort av [Github Actions](https://github.com/navikt/dokarkiv/actions).
Push/merge til branch vil teste, bygge og deploye til testmiljø. Ved merge til master lages det et utkast til release på
Github. Deploy til prod skjer ved å fullføre
[det release-utkastet på github.](https://github.com/navikt/dokarkiv/releases)

## Kjøre prosjektet lokalt
For å kjøre opp applikasjonen lokalt, bruk profile `nais` og systemvariabler hentet fra vault: [System variabler](https://vault.adeo.no/ui/vault/secrets/secret/list/dokument/dokarkiv/) 

## Tilgangsstyring
Ved behov for tilgang til Dokarkiv kan andre team selv gjøre nødvendige kodeendringer:
- Lag en ny branch med endringene du ønsker for tilgang. Endringene må legges til i alle ønskede miljø i filene: /nais/<miljø>-config.json.
- Legg til audience i ```NO_NAV_SECURITY_JWT_ISSUER_RESTSTS_ACCEPTED_AUDIENCE``` om du bruker REST-STS.
- Push endringene og lag en pull request. 
- Pull requesten vil bli gått gjennom og merget av noen i Team Dokumentløsninger.

### Henvendelser
Spørsmål om koden eller prosjektet kan rettes til [Slack-kanalen for \#Team Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ).

