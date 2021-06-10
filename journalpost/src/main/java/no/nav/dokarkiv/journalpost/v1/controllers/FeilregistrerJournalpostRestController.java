package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.metrics.RestMetrics;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.journalpost.v1.services.AvbrytService;
import no.nav.dokarkiv.journalpost.v1.services.FeilregistrerSakstilknytningService;
import no.nav.dokarkiv.journalpost.v1.services.UkjentBrukerService;
import no.nav.dokarkiv.journalpost.v1.services.UtgaarService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerAvbryt;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFeilregistrerSakstilknytning;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerOpphevFeilregistrertSakstilknytning;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerSettStatusUtgår;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerSettUkjentBruker;
import no.nav.freg.abac.core.annotation.Abac;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.ADMIN_UPDATE_ACTION;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.ARKIV_V2;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.SETT_STATUS_AVBRYT;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.FEILREGISTRER_SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.SETT_UKJENT_BRUKER;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.SETT_STATUS_UTGAAR;

@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/journalpost")
@Api(value = "Feilregistrer", description = "Tjenester for å feilregistrere journalpost")
public class FeilregistrerJournalpostRestController {

	private static final String FIKK_UKJENT_BRUKER = "Journalposten fikk status Ukjent Bruker";
	private static final String FEILREGISTRERING_OPPHEVET = "Feilregistreringen ble opphevet";

	private final FeilregistrerSakstilknytningService feilregistrerSakstilknytningService;
	private final UkjentBrukerService ukjentBrukerService;
	private final AvbrytService avbrytService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AbacSecurityService abacSecurityServiceV2;
	private final UtgaarService utgaarService;

	@Inject
	public FeilregistrerJournalpostRestController(
			final FeilregistrerSakstilknytningService feilregistrerSakstilknytningService,
			final UkjentBrukerService ukjentBrukerService,
			final AvbrytService avbrytService,
			final AksjonsLoggService aksjonsLoggService,
			@Qualifier("abacArkivV2SecurityService") AbacSecurityService abacSecurityServiceV2,
			UtgaarService statusUtgaarService
	) {
		this.feilregistrerSakstilknytningService = feilregistrerSakstilknytningService;
		this.ukjentBrukerService = ukjentBrukerService;
		this.avbrytService = avbrytService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.abacSecurityServiceV2 = abacSecurityServiceV2;
		this.utgaarService = statusUtgaarService;
	}

	@Transactional
	@SwaggerFeilregistrerSakstilknytning
	@PatchMapping("/{journalpostId}/feilregistrer/" + FEILREGISTRER_SAKSTILKNYTNING)
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark401"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> feilregistrerSakstilkytning(
			@PathVariable @ApiParam(value = "IDen til journalposten som skal feilregistreres", required = true, example = "77778888") String journalpostId) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = feilregistrerSakstilknytningService.feilregistrerSakstilknytning(journalpostId);
		populerAksjonslogg(journalpostId, AksjonsTypeCode.FEILREGISTRER_SAKSTILKNYTNING, arkivElementEndringTOList, "Saksrelasjonen ble feilregistrert");
		log.info(MDC.get(MDC_REQUEST_ID) + " har feilregistrert journalpost med journalpostId={}", journalpostId);
		return ResponseEntity.ok().body("Saksrelasjonen ble feilregistrert");
	}

	@Transactional
	@SwaggerOpphevFeilregistrertSakstilknytning
	@PatchMapping("/{journalpostId}/feilregistrer/" + OPPHEV_FEILREGISTRERT_SAKSTILKNYTNING)
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark402"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> opphevFeilregistrertSakstilknytning(
			@PathVariable @ApiParam(value = "IDen til journalposten som skal feilregistreres", required = true, example = "77778888") String journalpostId) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = feilregistrerSakstilknytningService.opphevFeilregistrertSakstilknytning(journalpostId);
		populerAksjonslogg(journalpostId, AksjonsTypeCode.OPPHEV_FEILREGISTRERING, arkivElementEndringTOList, FEILREGISTRERING_OPPHEVET);
		log.info(MDC.get(MDC_REQUEST_ID) + " har opphevet feilregistrering av journalpost med journalpostId={}", journalpostId);
		return ResponseEntity.ok().body(FEILREGISTRERING_OPPHEVET);
	}

	@Transactional
	@SwaggerSettUkjentBruker
	@PatchMapping("/{journalpostId}/feilregistrer/" + SETT_UKJENT_BRUKER)
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST),
			@Abac.Attr(key = RESOURCE_FELLES_DOMENE, value = ARKIV_V2)},
			actions = @Abac.Attr(key = ACTION_ID, value = ADMIN_UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark403"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> settUkjentBruker(
			@PathVariable @ApiParam(value = "IDen til journalposten som skal feilregistreres", required = true, example = "77778888") String journalpostId) {
		abacSecurityServiceV2.assertAccessToJournalpost(journalpostId);
		List<ArkivElementEndringTO> arkivElementEndringTOList = ukjentBrukerService.settUkjentBruker(journalpostId);
		populerAksjonslogg(journalpostId, AksjonsTypeCode.UKJENT_BRUKER, arkivElementEndringTOList, FIKK_UKJENT_BRUKER);
		log.info(MDC.get(MDC_REQUEST_ID) + " har satt status til Ukjent Bruker for journalpost med journalpostId={}", journalpostId);
		return ResponseEntity.ok().body(FIKK_UKJENT_BRUKER);
	}

	@Transactional
	@SwaggerAvbryt
	@PatchMapping("/{journalpostId}/feilregistrer/" + SETT_STATUS_AVBRYT)
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark404"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> avbryt(
			@PathVariable @ApiParam(value = "IDen til journalposten som skal settes til avbryt", required = true, example = "77778888") String journalpostId) {
		String response = avbrytService.avbryt(journalpostId);
		return ResponseEntity.ok().body(response);
	}

	@Transactional
	@SwaggerSettStatusUtgår
	@PatchMapping("/{journalpostId}/feilregistrer/" + SETT_STATUS_UTGAAR)
	@Abac(resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST),
			@Abac.Attr(key = RESOURCE_FELLES_DOMENE, value = ARKIV_V2)},
			actions = @Abac.Attr(key = ACTION_ID, value = ADMIN_UPDATE_ACTION))
	@RestMetrics(value = "dok_request", extraTags = {"process_code", "rjoark405"}, percentiles = {0.5, 0.95})
	public ResponseEntity<String> utgaar(
			@PathVariable @ApiParam(value = "IDen til journalposten som skal settes til utgått", required = true, example = "77778888") String journalpostId
	) {
		abacSecurityServiceV2.assertAccessToJournalpost(journalpostId);
		String response = utgaarService.settStatusUtgaar(journalpostId);
		return ResponseEntity.ok().body(response);
	}

	private void populerAksjonslogg(String journalpostId, AksjonsTypeCode aksjon, List<ArkivElementEndringTO> arkivElementEndringTOList, String melding) {
		AksjonsLoggTO aksjonsLoggTo;
		aksjonsLoggTo = AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(Long.parseLong(journalpostId))
				.utfoertAv(MDC.get(MDC_CONSUMER_ID))
				.hjemmel("ARKL")
				.melding(melding)
				.build();
		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, arkivElementEndringTOList);
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: " + e.getMessage());
		}
	}
}
