package no.nav.dokarkiv.tidligkassasjon.rjoark107;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class FysiskTidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final JoarkDeleteRepository deleteRepository;
	private final BegrensningService begrensningService;

	@Inject
	public FysiskTidligKassasjonService(
			DokumentinfoRepository dokumentinfoRepository,
			JoarkDeleteRepository deleteRepository,
			BegrensningService begrensningService) {
		this.dokumentInfoRepository = dokumentinfoRepository;
		this.deleteRepository = deleteRepository;
		this.begrensningService = begrensningService;
	}

	public FysiskTidligKassasjonResponse fysiskTidligKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoTilTidligKassering = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElseThrow(
				() -> new DokumentInfoIkkeFunnetException(String.format(
						"Kan ikke finne dokument med dokumentInfoId=%s", dokumentInfoId)));

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);

		fysiskTidligKassasjonAvEtDokument(dokumentInfoId);

		return FysiskTidligKassasjonResponse.builder()
				.dokumentInfoId(dokumentInfoId)
				.tittel(dokumentInfoTilTidligKassering.getTittel())
				.build();
	}

	private void sjekkAtDokumentErLogiskKassert(Long dokumentInfoId) {
		DokumentInfo dokumentInfo = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElse(null);
		if (!begrensningService.isDokumentInfoKassert(dokumentInfo)) {
			throw new BegrensningIkkeFunnetException(
					String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
							dokumentInfoId,
							SkjermingTypeCode.POL));
		}
	}

	private void tidligKassasjonAvEtDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfo = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElse(null);
		for (FilDetaljer filDetaljer:dokumentInfo.getFildetaljerListe()) {
			begrensningService.setFildetaljerBegrensning(filDetaljer, BegrensningTypeCode.POL);
		}
		slettFilOgBeholdMetadata(dokumentInfoId);
	}

	private void slettFilOgBeholdMetadata(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
