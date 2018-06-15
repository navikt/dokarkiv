package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.domain.dok.joark.DokumentInfo;
import no.nav.domain.dok.joark.Journalpost;
import no.nav.domain.dok.joark.JournalpostDokumentInfoRelasjon;
import no.nav.domain.dok.joark.codestable.DokumentStatusCode;
import no.nav.domain.dok.joark.codestable.TilknyttetJournalpostSomCode;
import no.nav.repository.dok.joark.JoarkRepository;
import no.nav.repository.dok.joark.util.DateProvider;
import no.nav.service.dok.joark.NoJournalpostFoundException;
import no.nav.service.dok.joark.journalbehandling.DokumentFilerDelegate;
import no.nav.service.dok.joark.journalbehandling.SporingPopulator;
import no.nav.service.dok.joark.nsb.ArkiverVedleggService;
import no.nav.service.dok.joark.nsb.ArkiverVedleggValidator;
import no.nav.service.dok.joark.nsb.to.ArkiverVedleggRequestTo;
import no.nav.service.dok.joark.nsb.to.ArkiverVedleggResponseTo;

import javax.inject.Inject;

/**
 * Default implementation of ArkiverVedleggService
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
public class DefaultArkiverVedleggService implements ArkiverVedleggService {

	@Inject
	private JoarkRepository joarkRepository;

	@Inject
	private ArkiverVedleggValidator arkiverVedleggValidator;

	@Inject
	private DokumentFilerDelegate dokumentFilerDelegate;

	@Inject
	private SporingPopulator sporingPopulator;

	@Override
	public ArkiverVedleggResponseTo arkiverVedlegg(ArkiverVedleggRequestTo arkiverVedleggRequest)
			throws NoJournalpostFoundException {
		arkiverVedleggValidator.validate(arkiverVedleggRequest);

		Journalpost journalpost =
				joarkRepository.findJournalpostByJournalpostId(arkiverVedleggRequest.getJournalpostId(), false);

		arkiverVedleggValidator.validate(journalpost, arkiverVedleggRequest.getJournalpostId());

		oppdaterJournalpostMedDokumentInfo(journalpost, arkiverVedleggRequest);

		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		joarkRepository.updateJournalpost(journalpost);

		return ArkiverVedleggResponseTo.create(
				arkiverVedleggRequest.getJournalpostId(),
				arkiverVedleggRequest.getDokumentInfo().getDokumentInfoId());
	}

	private void oppdaterJournalpostMedDokumentInfo(Journalpost journalpost, ArkiverVedleggRequestTo arkiverVedleggRequest) {
		DokumentInfo dokumentInfo = arkiverVedleggRequest.getDokumentInfo();
		updateDokumentInfo(dokumentInfo, arkiverVedleggRequest);
		JournalpostDokumentInfoRelasjon jpDokInfoRel =
				createJournalpostDokumentInfoRelasjon(arkiverVedleggRequest, dokumentInfo);
		journalpost.addJournalpostDokumentInfoRelasjon(jpDokInfoRel);
		sporingPopulator.populateSporingInfo(journalpost, arkiverVedleggRequest.getEndretAvNavn());
	}

	private void updateDokumentInfo(DokumentInfo dokumentInfo, ArkiverVedleggRequestTo arkiverVedleggRequest) {
		boolean ferdigstillDokument = arkiverVedleggRequest.getFerdigstillDokument();
		dokumentInfo.setDokumentstatus(toDokumentStatusCode(ferdigstillDokument));
		dokumentInfo.setDokumentFerdigDato(ferdigstillDokument ? DateProvider.getToday() : null);
}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(ArkiverVedleggRequestTo arkiverVedleggRequest,
																				  DokumentInfo dokumentInfo) {
		JournalpostDokumentInfoRelasjon jpDokInfoRel = new JournalpostDokumentInfoRelasjon();
		jpDokInfoRel.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		jpDokInfoRel.setTilknyttetAvNavn(arkiverVedleggRequest.getEndretAvNavn());
		jpDokInfoRel.setDokumentInfo(dokumentInfo);
		return jpDokInfoRel;
	}

	private DokumentStatusCode toDokumentStatusCode(boolean ferdigstillDokument) {
		return ferdigstillDokument ? DokumentStatusCode.FERDIGSTILT : DokumentStatusCode.UNDER_REDIGERING;
	}

}
