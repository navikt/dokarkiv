package no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalposttype;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_JOURNALPOSTTYPE;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.L;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalposttype.OppdaterJournalpostTypeUtils.determineJournalpostTypeCode;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalposttype.OppdaterJournalpostTypeUtils.determineNewJournalstatusCode;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalposttype.OppdaterJournalpostTypeUtils.validateJournalpostKanEndres;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterJournalposttype.OppdaterJournalpostTypeUtils.validateOppdaterJournalpostTypeRequest;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@Service
public class OppdaterJournalpostTypeService {

	private final JournalpostRepository journalpostRepository;
	private final AksjonsLoggService aksjonsLoggService;

	public OppdaterJournalpostTypeService(JournalpostRepository journalpostRepository,
										  AksjonsLoggService aksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public ResponseEntity<String> oppdaterJournalpostType(long journalpostId, OppdaterJournalposttypeRequest oppdaterJournalpostTypeRequest) {

		validateOppdaterJournalpostTypeRequest(oppdaterJournalpostTypeRequest);

		Journalpost journalpostToUpdate = journalpostRepository.findById(journalpostId).orElseThrow(() ->
				new JournalpostIkkeFunnetException(format("Fant ingen journalpost med journalpostId=%s i arkivet", journalpostId)));

		validateJournalpostKanEndres(journalpostToUpdate);
		oppdaterJournalpost(journalpostToUpdate, oppdaterJournalpostTypeRequest);

		populerAksjonslogg(journalpostId, journalpostToUpdate.getJournalposttype());
		//TODO: Holder det med OK eller burde det være en response av noe slag her??
		return ResponseEntity.ok("");
	}

	private void populerAksjonslogg(Long journalpostId, JournalpostTypeCode nyJournalpostTypeCode) {
		AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
				.journalpostId(journalpostId)
				.aksjon(ENDRE_JOURNALPOSTTYPE).build();
		List<ArkivElementEndringTO> endring = Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement("Journalpost.journalpost_type")
						.fraVerdi("I")
						.tilVerdi(nyJournalpostTypeCode.name())
						.build());
		try {
			aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTo, endring);
		} catch (UgyldigAksjonsLoggException e) {
			log.warn("Kunne ikke skrive til AksjonsLogg: " + e.getMessage());
		}
	}

	private void oppdaterJournalpost(Journalpost journalpostToUpdate, OppdaterJournalposttypeRequest oppdaterJournalpostTypeRequest) {
		String nyJournalforendeEnhet = oppdaterJournalpostTypeRequest.journalfoerendeEnhet();
		String nyTypeEndresTil = oppdaterJournalpostTypeRequest.typeEndresTil();

		journalpostToUpdate.setJournalposttype(determineJournalpostTypeCode(nyTypeEndresTil));
		journalpostToUpdate.setJournalstatus(determineNewJournalstatusCode(journalpostToUpdate.getJournalstatus()));

		if (UTGAAENDE.name().equals(nyTypeEndresTil)) {
			journalpostToUpdate.setUtsendingskanal(L);
		}

		if (isNotEmpty(nyJournalforendeEnhet)) {
			journalpostToUpdate.setJournalForendeEnhetId(nyJournalforendeEnhet);
		}

		if (NOTAT.name().equals(nyTypeEndresTil)) {
			journalpostToUpdate.setAvsenderMottakerId(null);
			journalpostToUpdate.setAvsenderMottaker(null);
			journalpostToUpdate.setAvsenderMottakerIdType(null);
		}

		journalpostToUpdate.setEndretAvNavn(MDC.get(MDC_USER_NAME));
		journalpostToUpdate.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
	}
}
