package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost.JournalpostCopier;
import no.nav.dokarkiv.journalpost.v1.validators.KopierJournalpostValidator;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALPOST_ID;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.KOPIER_JOURNALPOST;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Component
@Slf4j
public class KopierJournalpostService {
	private static final String USERID = "userId";
	private static final String CONSUMERID = "consumerId";
	private static final String UKJENT = "ukjent";
	private final JournalpostRepository journalpostRepository;
	private final LagreAksjonsLoggService aksjonsLoggService;
	private final KopierJournalpostValidator kopierJournalpostValidator;
	private final JournalpostCopier journalpostCopier;

	public KopierJournalpostService(final JournalpostRepository journalpostRepository, final LagreAksjonsLoggService aksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.aksjonsLoggService = aksjonsLoggService;
		this.kopierJournalpostValidator = new KopierJournalpostValidator();
		this.journalpostCopier = new JournalpostCopier();
	}

	public Long kopierJournalpost(Long journalpostId) {
		// finn journalpost
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));
		// verifiser at journalpost er i tilstand som kan kopieres - dvs status = FL, FS eller J, eller har saksrelasjon feilregistrert
		kopierJournalpostValidator.validate(journalpost);

		// kopier journalpost
		Journalpost nyJournalpost = journalpostCopier.copy(journalpost);

		// Nullstill saksrelasjon på den nye journalposten.
		nyJournalpost.setSaksrelasjon(null);

		// låse opp den nye journalpost ved å sette den "tilbake" i status: (eks: FS -> D)
		resetJournalpoststatus(nyJournalpost);

		nyJournalpost = journalpostRepository.persist(nyJournalpost);

		Long nyJournalpostId = nyJournalpost.getJournalpostId();

		ArkivElementEndringTO endring = ArkivElementEndringTO.builder()
				.arkivElement(JOURNALPOST_JOURNALPOST_ID)
				.fraVerdi(Long.toString(journalpost.getJournalpostId()))
				.tilVerdi(Long.toString(nyJournalpostId))
				.build();

		aksjonsLoggService.lagreAksjonsLoggForJournalpost(
				KOPIER_JOURNALPOST, journalpostId, null, "Journalposten ble kopiert. Id til ny journalpost er " + nyJournalpostId,
				getUtfoertAv(),	Collections.singletonList(endring));

		// returnere journalpostId til ny journalpost
		return nyJournalpostId;
	}

	private String getUtfoertAv() {
		String userId = MDC.get(USERID);
		String consumerId = MDC.get(CONSUMERID);
		return isEmpty(userId) ? (isEmpty(consumerId) ? UKJENT : consumerId) : userId;
	}

	private void resetJournalpoststatus(Journalpost journalpost) {
		JournalpostTypeCode type = journalpost.getJournalposttype();
		if (JournalpostTypeCode.I.equals(type)) {
			journalpost.setJournalstatus(JournalStatusCode.M);
		} else if (JournalpostTypeCode.U.equals(type)) {
			journalpost.setJournalstatus(JournalStatusCode.D);
		} else { // Notat
			journalpost.setJournalstatus(JournalStatusCode.D);
		}
	}
}