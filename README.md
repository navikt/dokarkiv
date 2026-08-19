# Dokarkiv

Dokarkiv består av arkivtjenester til fagarkivet, og inneholder både dokumentlager og metadata om dokumentene og deres journalposter.

Mer informasjon om appen og Rest-endepunktene finner du på henholdsvis [Confluence-doken for Dokarkiv (Nav-internt)](https://confluence.adeo.no/display/BOA/dokarkiv)
og [Swagger-doken](https://dokarkiv.intern.dev.nav.no/swagger-ui/index.html).

## Komme i gang

Kjør tester og bygg appen

```
mvn clean verify
```

### Distribusjon av tjenesten (deployment)

Distribusjon av tjenesten er gjort av [Github Actions](https://github.com/navikt/dokarkiv/actions).
Push/merge til branch vil teste, bygge og deploye til testmiljø. Ved merge til master lages det et utkast til release på
Github. Deploy til prod skjer ved å fullføre
[det release-utkastet på github.](https://github.com/navikt/dokarkiv/releases)

### Tilgangsstyring

Ved behov for tilgang til Dokarkiv kan andre team selv gjøre nødvendige kodeendringer:
1. Lag en ny branch med endringene du ønsker for tilgang. Endringene må legges til i alle ønskede miljø i filene: /nais/<miljø>-config.json 
2. Legg til audience i ```NO_NAV_SECURITY_JWT_ISSUER_RESTSTS_ACCEPTED_AUDIENCE``` om du bruker REST-STS
3. Push endringene til remote
4. Lag en pull request
5. Pull requesten vil bli gått gjennom og merget av noen i Team Dokumentløsninger

---

## Henvendelser

Lag en issue i repository.

### For Nav-ansatte

Spørsmål om appen kan stilles på [#team_dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ)

## Lisens

[MIT](LICENSE.md)
