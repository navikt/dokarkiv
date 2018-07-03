package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Default implementation of ArkiverVedleggService
 *
 * @author Magnar Brandsdal, Visma Consulting
 */
@Component
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

		Journalpost journalpost = joarkRepository.findById(arkiverVedleggRequest.getJournalpostId())
				.orElseThrow(() -> new NoJournalpostFoundException("journalpostId=" + arkiverVedleggRequest.getJournalpostId() + " does not exist", arkiverVedleggRequest
						.getJournalpostId()));

		arkiverVedleggValidator.validate(journalpost, arkiverVedleggRequest.getJournalpostId());

		oppdaterJournalpostMedDokumentInfo(journalpost, arkiverVedleggRequest);

		dokumentFilerDelegate.saveUpdateDokumentFiler(journalpost);
		Journalpost mergedJournalpost = joarkRepository.save(journalpost);
		String attachedFilUuid = arkiverVedleggRequest.getDokumentInfo().getFildetaljerListe().iterator().next().getFilUuid();
		Long dokumentInfoId = mergedJournalpost.findFilDetaljerByFilUuid(attachedFilUuid).getDokumentInfo().getDokumentInfoId();

		return ArkiverVedleggResponseTo.create(
				mergedJournalpost.getJournalpostId(),
				dokumentInfoId);
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
