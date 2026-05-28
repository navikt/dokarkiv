package no.nav.dokarkiv.internal.dokvaktmester;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.core.stelvio.RequestContextUtil.createAndSetUsername;
import static no.nav.dokarkiv.internal.dokvaktmester.DokVaktmesterController.INTERN_ROLE;

@Slf4j
@RestController
@RequestMapping("/rest/internal/journalpostapi/v1")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + INTERN_ROLE})
public class DokVaktmesterController {
	public static final String INTERN_ROLE = "api_intern";

	private final SettAvbruttJournalpostRedigerbarService settAvbruttJournalpostRedigerbarService;
	private final EndreFerdigstiltJournalpostValidator endreFerdigstiltJournalpostValidator;
	private final EndreFerdigstiltJournalpostService endreFerdigstiltJournalpostService;

	public DokVaktmesterController(SettAvbruttJournalpostRedigerbarService settAvbruttJournalpostRedigerbarService,
								   EndreFerdigstiltJournalpostValidator endreFerdigstiltJournalpostValidator,
								   EndreFerdigstiltJournalpostService endreFerdigstiltJournalpostService) {
		this.settAvbruttJournalpostRedigerbarService = settAvbruttJournalpostRedigerbarService;
		this.endreFerdigstiltJournalpostValidator = endreFerdigstiltJournalpostValidator;
		this.endreFerdigstiltJournalpostService = endreFerdigstiltJournalpostService;
	}

	@PutMapping("/journalpost/{journalpostId}/settAvbruttJournalpostRedigerbar")
	public ResponseEntity<String> settAvbruttJournalpostRedigerbar(@PathVariable Long journalpostId) {
		MDC.put(MDC_REQUEST_ID, "settAvbruttJournalpostRedigerbar");
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		log.info("{} har mottatt kall for å sette journalpost med journalpostId={} redigerbar", MDC.get(MDC_REQUEST_ID), journalpostId);
		settAvbruttJournalpostRedigerbarService.settAvbruttJournalpostRedigerbar(journalpostId);
		log.info("{} har satt journalpost med journalpostId={} redigerbar", MDC.get(MDC_REQUEST_ID), journalpostId);

		return ResponseEntity.ok().body("Journalpost med journalpostId=%s tilbakestilt til redigerbar tilstand".formatted(journalpostId));
	}

	@PatchMapping("/journalpost/{journalpostId}/endreFerdigstiltJournalpost")
	public ResponseEntity<String> endreFerdigstiltJournalpost(@PathVariable long journalpostId, @RequestBody EndreFerdigstiltJournalpostRequest request) {
		MDC.put(MDC_REQUEST_ID, "settAvbruttJournalpostRedigerbar");
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));

		log.info("{} skal endre ferdigstilt journalpostId={}", MDC.get(MDC_REQUEST_ID), journalpostId);
		endreFerdigstiltJournalpostValidator.validate(request);
		endreFerdigstiltJournalpostService.endreFerdigstiltJournalpost(journalpostId, request);
		log.info("{} har endret ferdigstilt journalpostId={}", MDC.get(MDC_REQUEST_ID), journalpostId);

		return ResponseEntity.ok().body("Endret journalpost for journalpostId=%d og begrunnelseNokkel=%s".formatted(journalpostId, request.begrunnelseNokkel()));
	}
}
