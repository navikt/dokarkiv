package no.nav.dokarkiv.rjoark102;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.util.ConverterUtils.enumToString;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Service
public class KasserSkjermDokumentService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final SkjermingService skjermingService;

	public KasserSkjermDokumentService(DokumentinfoRepository dokumentInfoRepository, SkjermingService skjermingService) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.skjermingService = skjermingService;
	}

	public List<ArkivElementEndringTO> skjermDokument(Long dokumentInfoId) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.addAll(endreSkjermingForAlleFildetaljer(dokumentInfoId, SkjermingTypeCode.POL));

		arkivElementEndringTOList.addAll(oppdaterKassertParameter(dokumentInfoId, true));
		return arkivElementEndringTOList;
	}

	public List<ArkivElementEndringTO> opphevSkjermDokument(Long dokumentInfoId) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.addAll(endreSkjermingForAlleFildetaljer(dokumentInfoId, null));

		arkivElementEndringTOList.addAll(oppdaterKassertParameter(dokumentInfoId, false));
		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> oppdaterKassertParameter(Long dokumentInfoId, boolean kassert) {
		DokumentInfo dokumentInfoForSkjerming = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(
						() -> new DokumentInfoIkkeFunnetException(String.format(
								"Fant ikke dokument med dokumentInfoId=%s", dokumentInfoId)));
		dokumentInfoForSkjerming.setKassert(kassert);
		// FIXME riktig?
		dokumentInfoRepository.persist(dokumentInfoForSkjerming);
		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(DOKUMENT_INFO_KASSERT)
						.fraVerdi(String.valueOf(!kassert))
						.tilVerdi(String.valueOf(kassert))
						.build()
		);

	}

	private List<ArkivElementEndringTO> endreSkjermingForAlleFildetaljer(Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		DokumentInfo dokumentInfoForSkjerming = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(String.format(
						"Fant ikke dokument med dokumentInfoId=%s", dokumentInfoId)));

		dokumentInfoForSkjerming.getFildetaljerListeAdmin()
				.forEach(filDetaljer -> arkivElementEndringTOList.addAll(endreSkjermingFildetaljer(dokumentInfoId, filDetaljer.getVariantFormat(), filDetaljer
						.getSkjermingType(), skjermingTypeCode)));

		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> endreSkjermingFildetaljer(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode forrigeSkjerming, SkjermingTypeCode skjerming) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		if (forrigeSkjerming != skjerming) {
			skjermingService.setFildetaljerSkjerming(dokumentInfoId, variant, skjerming);
			arkivElementEndringTOList.add(
					ArkivElementEndringTO.builder()
							.arkivElement(fildetaljerSkjermingTypeVariant(variant))
							.fraVerdi(enumToString(forrigeSkjerming))
							.tilVerdi(enumToString(skjerming))
							.build()
			);
		}
		return arkivElementEndringTOList;
	}
}
