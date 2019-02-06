package no.nav.dokarkiv.fysisktidligkassasjon.rjoark107;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

	public List<ArkivElementEndringTO> fysiskTidligKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoTilTidligKassering = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElseThrow(
				() -> new DokumentInfoIkkeFunnetException(String.format(
						"Kan ikke finne dokument med dokumentInfoId=%s", dokumentInfoId)));

		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.addAll(Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement("DokumentInfo.kassertDato")
						.fraVerdi(null)
						.tilVerdi(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
						.build(),
				ArkivElementEndringTO.builder()
						.arkivElement("DokumentInfo.kassertAv")
						.fraVerdi(null)
						.tilVerdi(RequestContextHolder.currentRequestContext().getUserId())
						.build()
		));

		arkivElementEndringTOList.addAll(dokumentInfoTilTidligKassering.getFildetaljerListe()
				.stream()
				.map(filDetaljer -> ArkivElementEndringTO.builder()
						.arkivElement("FilDetaljer.variantFormat")
						.fraVerdi(null)
						.tilVerdi(filDetaljer.getVariantFormat().name())
						.build())
				.collect(Collectors.toList()));

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);

		fysiskTidligKassasjonAvEtDokument(dokumentInfoId);

		return arkivElementEndringTOList;
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
