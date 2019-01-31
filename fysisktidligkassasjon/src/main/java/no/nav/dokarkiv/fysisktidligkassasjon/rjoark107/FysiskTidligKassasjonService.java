package no.nav.dokarkiv.fysisktidligkassasjon.rjoark107;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class FysiskTidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final JoarkDeleteRepository deleteRepository;
	private final SkjermingService skjermingService;

	@Inject
	public FysiskTidligKassasjonService(
			DokumentinfoRepository dokumentinfoRepository,
			JoarkDeleteRepository deleteRepository,
			SkjermingService skjermingService) {
		this.dokumentInfoRepository = dokumentinfoRepository;
		this.deleteRepository = deleteRepository;
		this.skjermingService = skjermingService;
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
		if (!skjermingService.isDokumentInfoKassert(dokumentInfo)) {
			throw new SkjermingIkkeFunnetException(
					String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
							dokumentInfoId,
							SkjermingTypeCode.POL));
		}
	}

	private void fysiskTidligKassasjonAvEtDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfo = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElse(null);
		if (dokumentInfo != null) {
			for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
				skjermingService.setFildetaljerBegrensning(filDetaljer, SkjermingTypeCode.POL);
			}
			slettFilOgBeholdMetadata(dokumentInfoId);
		}
	}

	private void slettFilOgBeholdMetadata(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
