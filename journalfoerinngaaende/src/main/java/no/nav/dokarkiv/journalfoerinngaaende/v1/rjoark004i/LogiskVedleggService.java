package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark004i;

import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils.DOKUMENT_ID;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils.JOURNALPOST_ID;

import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggRequest;
import no.nav.dok.tjenester.journalfoerinngaaende.PostLogiskVedleggResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.LogiskVedleggIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class LogiskVedleggService {

	private final JoarkRepositorySkjermet joarkRepository;
	private final SkannetInnholdRepository skannetInnholdRepository;
	private static final String JOURNALPOST_IKKE_FUNNET = "Kunne ikke finne journalpost med journalpostId=%s i joark";

	@Inject
	public LogiskVedleggService(JoarkRepositorySkjermet joarkRepository,
								SkannetInnholdRepository skannetInnholdRepository) {
		this.joarkRepository = joarkRepository;
		this.skannetInnholdRepository = skannetInnholdRepository;
	}

	public void deleteLogiskVedlegg(String journalpostIdString, String dokumentIdString, String logiskVedleggIdString) {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, JOURNALPOST_ID);
		Long dokumentId = Utils.convertStringToLong(dokumentIdString, DOKUMENT_ID);

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format(JOURNALPOST_IKKE_FUNNET, journalpostIdString)));

		Utils.assertJournalpostIsInngaaende(journalpost);
		Utils.assertDokumentInfoNotNull(journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentId), journalpostIdString, dokumentIdString);

		skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggIdString, dokumentIdString)
				.orElseThrow(() -> new LogiskVedleggIkkeFunnetException(String.format("Kunne ikke finne logisk vedlegg med logiskVedleggId=%s og dokumentId=%s i Joark", logiskVedleggIdString, dokumentIdString)));

		skannetInnholdRepository.deleteSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggIdString, dokumentIdString);
	}

	public void updateLogiskVedlegg(String journalpostIdString, String dokumentIdString, String logiskVedleggIdString, PutLogiskVedleggRequest request) {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, JOURNALPOST_ID);
		Long dokumentId = Utils.convertStringToLong(dokumentIdString, DOKUMENT_ID);

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format(JOURNALPOST_IKKE_FUNNET, journalpostIdString)));

		Utils.assertJournalpostIsInngaaende(journalpost);
		Utils.assertDokumentInfoNotNull(journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentId), journalpostIdString, dokumentIdString);

		SkannetInnhold skannetInnhold = skannetInnholdRepository.findSkannetInnholdBySkannetInnholdIdAndDokumentinfoId(logiskVedleggIdString, dokumentIdString)
				.orElseThrow(() -> new LogiskVedleggIkkeFunnetException(String.format("Kunne ikke finne logisk vedlegg med logiskVedleggId=%s og dokumentId=%s i Joark", logiskVedleggIdString, dokumentIdString)));

		skannetInnhold.setVedleggInnhold(request.getTittel());
		skannetInnhold.setEndretKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
		skannetInnholdRepository.save(skannetInnhold);
	}

	public PostLogiskVedleggResponse persistLogiskVedlegg(String journalpostIdString, String dokumentIdString, PostLogiskVedleggRequest request) {
		Long journalpostId = Utils.convertStringToLong(journalpostIdString, JOURNALPOST_ID);
		Long dokumentId = Utils.convertStringToLong(dokumentIdString, DOKUMENT_ID);

		Journalpost journalpost = joarkRepository.findById(journalpostId)
				.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format(JOURNALPOST_IKKE_FUNNET, journalpostIdString)));

		Utils.assertJournalpostIsInngaaende(journalpost);

		DokumentInfo dokumentInfo = journalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(dokumentId);
		Utils.assertDokumentInfoNotNull(dokumentInfo, journalpostIdString, dokumentIdString);

		SkannetInnhold skannetInnhold = SkannetInnhold.builder().vedleggInnhold(request.getTittel()).build();
		skannetInnhold.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		dokumentInfo.addSkannetInnhold(skannetInnhold);
		skannetInnhold = skannetInnholdRepository.save(skannetInnhold);
		return new PostLogiskVedleggResponse().withLogiskVedleggId(skannetInnhold.getSkannetInnholdId().toString());
	}
}


