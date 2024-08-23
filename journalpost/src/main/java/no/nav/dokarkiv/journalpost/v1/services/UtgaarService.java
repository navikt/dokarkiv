package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALSTATUS;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.UTGAAR;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;

@Component
@Slf4j
public class UtgaarService {

	private final JournalpostRepository journalpostRepository;
	private final LagreAksjonsLoggService aksjonsLoggService;

	static final String FIKK_UTGAAR = "Journalposten ble satt til utgår";
	static final List<JournalStatusCode> JOURNAL_STATUS_AVBRUTT_DOKUMENT_RESERVERT = Arrays.asList(A, D, R);
	static final List<JournalpostTypeCode> JOURNALPOSTTYPE_INNGAAENDE_NOTAT = Arrays.asList(I, N);

	public UtgaarService(final JournalpostRepository journalpostRepository, final LagreAksjonsLoggService aksjonsLoggService) {
		this.journalpostRepository = journalpostRepository;
		this.aksjonsLoggService = aksjonsLoggService;
	}

	public String settStatusUtgaar(long journalpostId) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(format("Kunne ikke finne journalpost med journalpostId=%s i joark", journalpostId)));

		JournalStatusCode oldJournalStatus = journalpost.getJournalstatus();
		JournalpostTypeCode journalposttype = journalpost.getJournalposttype();

		if (JOURNALPOSTTYPE_INNGAAENDE_NOTAT.contains(journalposttype)) {
			journalpost.setJournalstatus(U);
		} else if (JOURNAL_STATUS_AVBRUTT_DOKUMENT_RESERVERT.contains(oldJournalStatus)) {
			throw new UgyldigJournalStatusException("Journalposten er utgående eller notat. Kun inngående journalposter kan settes til Utgår, med journalpostId " + journalpostId);
		} else if (U.equals(oldJournalStatus)) {
			throw new UgyldigJournalStatusException("Journalposten er allerede satt til Utgår, med journalpostId " + journalpostId);
		} else {
			throw new UgyldigJournalStatusException("Journalposten kan ikke settes til utgår, da den er ferdigstilt, med journalpostId " + journalpostId);
		}

		ArkivElementEndringTO endring = ArkivElementEndringTO.builder()
				.arkivElement(JOURNALPOST_JOURNALSTATUS)
				.fraVerdi(oldJournalStatus.name())
				.tilVerdi(journalpost.getJournalstatus().name())
				.build();

		aksjonsLoggService.lagreAksjonsLoggForJournalpost(
				UTGAAR,
				journalpost.getJournalpostId(),
				"ARKL",
				FIKK_UTGAAR,
				null,
				singletonList(endring)
		);

		return FIKK_UTGAAR;
	}
}