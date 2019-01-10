package no.nav.dokarkiv.tidligkassasjon.rjoark107;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class TidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningRepository begrensningRepository;
	private final JoarkDeleteRepository deleteRepository;

	@Inject
	public TidligKassasjonService(
			DokumentinfoRepository dokumentinfoRepository,
			BegrensningRepository begrensningRepository,
			JoarkDeleteRepository deleteRepository) {
		this.dokumentInfoRepository = dokumentinfoRepository;
		this.begrensningRepository = begrensningRepository;
		this.deleteRepository = deleteRepository;
	}

	public TidligKassasjonResponse tidligKassasjonAvDokument(Long dokumentInfoId, String kassertAvNavn) {
		DokumentInfo dokumentInfoTilTidligKassering = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElseThrow(
				() -> new DokumentInfoIkkeFunnetException(String.format(
						"Kan ikke finne dokument med dokumentInfoId=%s", dokumentInfoId)));

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);

		tidligKassasjonAvEtDokument(dokumentInfoId);

		dokumentInfoTilTidligKassering.setDatoKassert(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
		dokumentInfoTilTidligKassering.setKassertAvNavn(kassertAvNavn);
		dokumentInfoRepository.save(dokumentInfoTilTidligKassering);

		return TidligKassasjonResponse.builder()
				.dokumentInfoId(dokumentInfoId)
				.tittel(dokumentInfoTilTidligKassering.getTittel())
				.build();
	}

	private void sjekkAtDokumentErLogiskKassert(Long dokumentInfoId) {
		if (isFalse(begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfoId, BegrensningTypeCode.KASSERT)
				.isPresent())) {
			throw new BegrensningIkkeFunnetException(
					String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
							dokumentInfoId,
							BegrensningTypeCode.KASSERT));
		}
	}

	private void tidligKassasjonAvEtDokument(Long dokumentInfoId) {
		begrensningRepository.deleteByDokumentInfoIdAndBegrensningType(dokumentInfoId, BegrensningTypeCode.KASSERT);
		slettFilOgBeholdMetadata(dokumentInfoId);
	}

	private void slettFilOgBeholdMetadata(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}
}
