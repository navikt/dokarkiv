package no.nav.dokarkiv.fysisktidligkassasjon.rjoark107;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class FysiskTidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningRepository begrensningRepository;
	private final JoarkDeleteRepository deleteRepository;

	@Inject
	public FysiskTidligKassasjonService(
			DokumentinfoRepository dokumentinfoRepository,
			BegrensningRepository begrensningRepository,
			JoarkDeleteRepository deleteRepository) {
		this.dokumentInfoRepository = dokumentinfoRepository;
		this.begrensningRepository = begrensningRepository;
		this.deleteRepository = deleteRepository;
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
		if (isFalse(begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfoId, SkjermingTypeCode.POL)
				.isPresent())) {
			throw new SkjermingIkkeFunnetException(
					String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
							dokumentInfoId,
							SkjermingTypeCode.POL));
		}
	}

	private void fysiskTidligKassasjonAvEtDokument(Long dokumentInfoId) {
		begrensningRepository.deleteByDokumentInfoIdAndBegrensningType(dokumentInfoId, SkjermingTypeCode.POL);
		slettFilOgBeholdMetadata(dokumentInfoId);
	}

	private void slettFilOgBeholdMetadata(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
