package no.nav.dokarkiv.journalpost.v1.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.LogiskVedleggKanIkkeBulkOppdateresException;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.journalpost.v1.api.BulkOppdaterLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.EndreLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggResponse;
import no.nav.dokarkiv.journalpost.v1.services.LogiskVedleggService;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerBulkOppdaterLogiskVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerEndreLogiskVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerLeggTilLogiskVedlegg;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerSlettLogiskVedlegg;
import no.nav.security.token.support.core.api.Protected;
import org.hibernate.StaleObjectStateException;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.entities.SkannetInnhold.VEDLEGG_INNHOLD_LENGTH;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.hasText;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateIdAndParse;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateNotNull;

@Tag(name = "journalpostapi - logiske vedlegg", description = "Tjenester for å oppdatere, slette, endre og legge til logiske vedlegg")
@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/dokumentInfo")
public class LogiskVedleggRestController {

	private final LogiskVedleggService logiskVedleggService;

	private static final String DOKUMENT_INFO_ID_STRING = "dokumentInfoId";
	private static final String LOGISK_VEDLEGG_ID_STRING = "logiskVedleggId";
	private static final String TITTEL_STRING = "tittel";

	public LogiskVedleggRestController(final LogiskVedleggService logiskVedleggService) {
		this.logiskVedleggService = logiskVedleggService;
	}

	@SwaggerEndreLogiskVedlegg
	@PostMapping(value = "/{dokumentInfoId}/logiskVedlegg/{logiskVedleggId}")
		public ResponseEntity<String> endreLogiskVedlegg(
			@PathVariable String dokumentInfoId,
			@PathVariable String logiskVedleggId,
			@RequestBody EndreLogiskVedleggRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		long dokumentInfoIdParsed = validateIdAndParse(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
		long logiskVedleggIdParsed = validateIdAndParse(logiskVedleggId, LOGISK_VEDLEGG_ID_STRING);
		log.info("endrelogiskvedlegg har mottatt kall om å endre logisk vedlegg med logiskVedleggId={} på dokument med dokumentInfoId={}",
				logiskVedleggIdParsed, dokumentInfoIdParsed);

		hasText(request.getTittel(), TITTEL_STRING);

		logiskVedleggService.endreLogiskVedlegg(logiskVedleggIdParsed, request);

		log.info("endrelogiskvedlegg har endret logisk vedlegg med logiskVedleggId={}.", logiskVedleggIdParsed);
		return ResponseEntity.ok("Logisk vedlegg endret");
	}

	@SwaggerLeggTilLogiskVedlegg
	@PostMapping(value = "/{dokumentInfoId}/logiskVedlegg")
		public ResponseEntity<LeggTilLogiskVedleggResponse> leggTilLogiskVedlegg(
			@PathVariable String dokumentInfoId,
			@RequestBody LeggTilLogiskVedleggRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		long dokumentInfoIdParsed = validateIdAndParse(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
		log.info("leggtillogiskvedlegg har mottatt kall om å legge til logisk vedlegg på dokument med dokumentInfoId={}", dokumentInfoIdParsed);

		hasText(request.getTittel(), TITTEL_STRING);

		long logiskVedleggId = logiskVedleggService.leggTilLogiskVedlegg(dokumentInfoIdParsed, request);
		LeggTilLogiskVedleggResponse response = LeggTilLogiskVedleggResponse.builder().logiskVedleggId(String.valueOf(logiskVedleggId)).build();

		log.info("leggtillogiskvedlegg har lagt til logisk vedlegg med logiskVedleggId={}, dokumentInfoId={}.", logiskVedleggId, dokumentInfoIdParsed);
		return ResponseEntity.ok(response);
	}

	@SwaggerSlettLogiskVedlegg
	@DeleteMapping(value = "/{dokumentInfoId}/logiskVedlegg/{logiskVedleggId}")
		public ResponseEntity<String> slettLogiskVedlegg(
			@PathVariable String dokumentInfoId,
			@PathVariable String logiskVedleggId) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		long dokumentInfoIdParsed = validateIdAndParse(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
		long logiskVedleggIdParsed = validateIdAndParse(logiskVedleggId, LOGISK_VEDLEGG_ID_STRING);
		log.info("slettlogiskvedlegg har mottatt kall om å har mottatt kall om å slette logisk vedlegg med logiskVedleggId={}, dokumentInfoId={}", logiskVedleggIdParsed, dokumentInfoIdParsed);

		logiskVedleggService.slettLogiskVedlegg(logiskVedleggIdParsed);

		log.info("slettlogiskvedlegg har slettet logisk vedlegg med logiskVedleggId={}, dokumentInfoId={}.", logiskVedleggIdParsed, dokumentInfoIdParsed);
		return ResponseEntity.ok("Logisk vedlegg slettet");
	}

	@SwaggerBulkOppdaterLogiskVedlegg
	@PutMapping("/{dokumentInfoId}/logiskVedlegg")
		public ResponseEntity<Void> bulkOppdaterLogiskVedlegg(@PathVariable String dokumentInfoId, @RequestBody BulkOppdaterLogiskVedleggRequest request) {
		RequestContextUtil.createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		long dokumentInfoIdParsed = validateIdAndParse(dokumentInfoId, DOKUMENT_INFO_ID_STRING);
		log.info("bulkoppdaterlogiskvedlegg har mottatt kall. dokumentInfoId={}", dokumentInfoIdParsed);

		validateNotNull(request.getTitler(), "titler");
		validateTitlerLength(request.getTitler());

		try {
			logiskVedleggService.bulkOppdaterLogiskVedlegg(dokumentInfoIdParsed, request);
		} catch (ObjectOptimisticLockingFailureException | StaleObjectStateException e) {
			throw new LogiskVedleggKanIkkeBulkOppdateresException("Kan ikke bulkOppdaterLogiskVedlegg for dokumentInfoId=" + dokumentInfoIdParsed + ". Ressursen er sannsynligvis nylig oppdatert av en annen prosess. Forsøk på nytt.", e);
		}

		log.info("bulkoppdaterlogiskvedlegg oppdaterte dokumentInfoId={} til antall_titler={}", dokumentInfoIdParsed, request.getTitler().size());
		return ResponseEntity.noContent().build();
	}

	private void validateTitlerLength(List<String> titler) {
		titler.forEach(t -> {
			if (t.length() > VEDLEGG_INNHOLD_LENGTH) {
				throw new InputValideringFeiletException("Hver tittel i titler kan ikke være lengre enn " + VEDLEGG_INNHOLD_LENGTH + " tegn. Lengde på tittel=" + t.length());
			}
		});
	}
}
