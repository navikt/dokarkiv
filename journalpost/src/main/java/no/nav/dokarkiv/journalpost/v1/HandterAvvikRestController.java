package no.nav.dokarkiv.journalpost.v1;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.FEILREGISTRER;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.UPDATE_ACTION;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.AVBRYT;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.FEILREGISTRER_SAKSRELASJON;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.OPPHEV_FEILREGISTRERING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.UKJENT_BRUKER;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateId;
import static no.nav.dokarkiv.journalpost.v1.validators.HandterAvvikValidator.validateAvvikstype;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.services.AvbrytService;
import no.nav.dokarkiv.journalpost.v1.services.FeilregistrerSaksrelasjonService;
import no.nav.dokarkiv.journalpost.v1.services.OpphevFeilregistreringService;
import no.nav.dokarkiv.journalpost.v1.services.SettTilUkjentBrukerService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFerdigstillJournalpost;
import no.nav.freg.abac.core.annotation.Abac;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
@Api
public class HandterAvvikRestController{

    private final FeilregistrerSaksrelasjonService feilregistrerSaksrelasjonService;
    private final OpphevFeilregistreringService opphevFeilregistreringService;
    private final SettTilUkjentBrukerService settTilUkjentBrukerService;
    private final AvbrytService avbrytService;
    private final AbacSecurityService abacSecurityService;
    private final AksjonsLoggService aksjonsLoggService;

    @Inject
    public HandterAvvikRestController(final FeilregistrerSaksrelasjonService feilregistrerSaksrelasjonService,
                                                final OpphevFeilregistreringService opphevFeilregistreringService,
                                                final SettTilUkjentBrukerService settTilUkjentBrukerService,
                                                final AvbrytService avbrytService,
                                                final AbacSecurityService abacSecurityService,
                                                final AksjonsLoggService aksjonsLoggService){
        this.feilregistrerSaksrelasjonService = feilregistrerSaksrelasjonService;
        this.opphevFeilregistreringService = opphevFeilregistreringService;
        this.settTilUkjentBrukerService = settTilUkjentBrukerService;
        this.avbrytService = avbrytService;
        this.abacSecurityService = abacSecurityService;
        this.aksjonsLoggService = aksjonsLoggService;
    }

    @Transactional
    @SwaggerFerdigstillJournalpost
    @PatchMapping("/{journalpostId}/handterAvvik/{avvikstype}")
    @Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)},
            actions = @Abac.Attr(key = ACTION_ID, value = UPDATE_ACTION))
    @RestMetrics(value = "dok_request", extraTags = {"process_code", "feilregistrer"}, percentiles = {0.5, 0.95})
    public ResponseEntity<String> handterAvvik(
            @PathVariable @ApiParam(value = "IDen til journalposten som skal feilregistreres", required = true, example = "77778888") String journalpostId,
            @PathVariable @ApiParam(value = "", required = true, example = "feilregistrerSaksrelasjon") String avvikstype)
            throws UgyldigAksjonsLoggException {
        MDC.put(MDC_REQUEST_ID, "feilregistrer");
        log.info(MDC.get(MDC_REQUEST_ID) + " har mottatt kall for feilregistrering av journalpost med journalpostId={}", journalpostId);
        validateRequest(journalpostId, avvikstype);
        abacSecurityService.assertAccessToJournalpost(journalpostId);
        RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDCConstants.MDC_CONSUMER_ID));

        if (avvikstype.equalsIgnoreCase(FEILREGISTRER_SAKSRELASJON)) {
            List<ArkivElementEndringTO> arkivElementEndringTOList = feilregistrerSaksrelasjonService.feilregistrerSaksrelasjon(journalpostId);

            populerAksjonslogg(journalpostId, FEILREGISTRER, arkivElementEndringTOList, "Saksrelasjonen ble feilregistrert");

            log.info(MDC.get(MDC_REQUEST_ID) + " har feilregistrert journalpost med journalpostId={}", journalpostId);

            return ResponseEntity.ok().body("Saksrelasjonen ble feilregistrert");
        }

        else if (avvikstype.equalsIgnoreCase(OPPHEV_FEILREGISTRERING)) {
            List<ArkivElementEndringTO> arkivElementEndringTOList = opphevFeilregistreringService.opphevFeilregistrering(journalpostId);

            populerAksjonslogg(journalpostId, AksjonsTypeCode.OPPHEV_FEILREGISTRERING ,arkivElementEndringTOList, "Feilregistreringen ble opphevet");

            log.info(MDC.get(MDC_REQUEST_ID) + " har opphevet feilregistrering av journalpost med journalpostId={}", journalpostId);

            return ResponseEntity.ok().body("Feilregistreringen ble opphevet");
        }

        else if (avvikstype.equalsIgnoreCase(UKJENT_BRUKER)) {
            List<ArkivElementEndringTO> arkivElementEndringTOList = settTilUkjentBrukerService.settTilUkjentBruker(journalpostId);

            populerAksjonslogg(journalpostId, AksjonsTypeCode.UKJENT_BRUKER ,arkivElementEndringTOList, "Journalposten fikk status Ukjent Bruker");

            log.info(MDC.get(MDC_REQUEST_ID) + " har satt status til Ukjent Bruker for journalpost med journalpostId={}", journalpostId);

            return ResponseEntity.ok().body("Journalposten fikk status Ukjent Bruker");
        }

        else if (avvikstype.equalsIgnoreCase(AVBRYT)) {
            List<ArkivElementEndringTO> arkivElementEndringTOList = avbrytService.avbryt(journalpostId);

            populerAksjonslogg(journalpostId, AksjonsTypeCode.AVBRYT ,arkivElementEndringTOList, "Journalposten ble satt til avbrutt / utgår");

            log.info(MDC.get(MDC_REQUEST_ID) + " har satt status til avbrutt / utgår for journalpost med journalpostId={}", journalpostId);

            return ResponseEntity.ok().body("Journalposten ble satt til avbrutt / utgår");
        }

        else {
            return ResponseEntity.badRequest().body("Ugyldig avvikstype");
        }
    }

    private void validateRequest(String journalpostId, String avvikstype) {
        validateId(journalpostId, "journalpostId");
        validateAvvikstype(avvikstype);
    }

    private void populerAksjonslogg(String journalpostId, AksjonsTypeCode aksjon, List<ArkivElementEndringTO> arkivElementEndringTOList, String melding) throws UgyldigAksjonsLoggException {
        AksjonsLoggTO aksjonsLoggTo;
        aksjonsLoggTo = AksjonsLoggTO.builder()
                .aksjon(aksjon)
                .journalpostId(Long.parseLong(journalpostId))
                .utfoertAv(MDC.get(MDC_CONSUMER_ID))
                .melding(melding)
                .build();

        aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, arkivElementEndringTOList);
    }
}
