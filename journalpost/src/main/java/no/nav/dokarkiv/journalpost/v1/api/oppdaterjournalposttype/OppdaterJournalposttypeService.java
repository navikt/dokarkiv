package no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_NAME;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALPOSTTYPE;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_JOURNALPOSTTYPE;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.L;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype.OppdaterJournalpostTypeUtils.determineJournalpostTypeCode;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype.OppdaterJournalpostTypeUtils.determineNewJournalstatusCode;
import static no.nav.dokarkiv.journalpost.v1.api.oppdaterjournalposttype.OppdaterJournalposttypeValidator.validateJournalpostKanEndres;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@Service
public class OppdaterJournalposttypeService {

	private final JournalpostRepository journalpostRepository;
	private final AksjonsLoggService aksjonsLoggService;

	public OppdaterJournalposttypeService(JournalpostRepository journalpostRepository,
										  AksjonsLoggService aksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public void oppdaterJournalpostType(long journalpostId, OppdaterJournalposttypeRequest oppdaterJournalpostTypeRequest) {

		Journalpost journalpostToUpdate = journalpostRepository.findById(journalpostId).orElseThrow(() ->
				new JournalpostIkkeFunnetException(format("Fant ingen journalpost med journalpostId=%s i arkivet", journalpostId)));

		validateJournalpostKanEndres(journalpostToUpdate);
		oppdaterJournalpost(journalpostToUpdate, oppdaterJournalpostTypeRequest);

		populerAksjonslogg(journalpostId, journalpostToUpdate.getJournalposttype());
	}

	private void populerAksjonslogg(Long journalpostId, JournalpostTypeCode nyJournalpostTypeCode) {
		AksjonsLoggTO aksjonsLoggTo = AksjonsLoggTO.builder()
				.journalpostId(journalpostId)
				.aksjon(ENDRE_JOURNALPOSTTYPE).build();
		List<ArkivElementEndringTO> endring = Collections.singletonList(
				ArkivElementEndringTO.builder()
						.arkivElement(JOURNALPOST_JOURNALPOSTTYPE)
						.fraVerdi(I.name())
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
		JournalpostType nyTypeEndresTil = oppdaterJournalpostTypeRequest.typeEndresTil();

		journalpostToUpdate.setJournalposttype(determineJournalpostTypeCode(nyTypeEndresTil));
		journalpostToUpdate.setJournalstatus(determineNewJournalstatusCode(journalpostToUpdate.getJournalstatus()));

		if (UTGAAENDE == nyTypeEndresTil) {
			journalpostToUpdate.setUtsendingskanal(L);
			journalpostToUpdate.setSendtPrintDato(journalpostToUpdate.getChangeStamp().getCreatedDate());
		}

		if (isNotEmpty(nyJournalforendeEnhet)) {
			journalpostToUpdate.setJournalForendeEnhetId(nyJournalforendeEnhet);
		}

		if (NOTAT == nyTypeEndresTil) {
			journalpostToUpdate.setAvsenderMottakerId(null);
			journalpostToUpdate.setAvsenderMottaker(null);
			journalpostToUpdate.setAvsenderMottakerIdType(null);
		}

		journalpostToUpdate.setEndretAvNavn(MDC.get(MDC_USER_NAME));
	}
}
