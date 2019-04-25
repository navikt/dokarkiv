package no.nav.dokarkiv.rjoark102;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_FIL_FIL_UUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_AV;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_DATO;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class KasserDokumentService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final JoarkDeleteRepository deleteRepository;
	private final EntityManager entityManager;

	@Inject
	public KasserDokumentService(
			DokumentinfoRepository dokumentinfoRepository,
			JoarkDeleteRepository deleteRepository, EntityManager entityManager) {
		this.dokumentInfoRepository = dokumentinfoRepository;
		this.deleteRepository = deleteRepository;
		this.entityManager = entityManager;
	}

	public List<ArkivElementEndringTO> kasserDokument(KasserDokumentRequest request) {
		DokumentInfo dokumentInfoForKassering = dokumentInfoRepository.findByDokumentInfoId(request.getDokumentInfoId())
				.orElseThrow(
						() -> new DokumentInfoIkkeFunnetException(String.format(
								"Kan ikke finne dokument med dokumentInfoId=%s", request.getDokumentInfoId())));

		settKassasjonInfo(dokumentInfoForKassering, request.getKassertAvNavn());

		List<ArkivElementEndringTO> arkivElementEndringTOList = opprettArkivElementEndring(dokumentInfoForKassering);

		//Slett alle Fildetaljer som ikke er ARKIV variant.
		//Fildetaljer for ARKIV variant beholdes fordi noen tjenester i Joark forventer at DokumentInfo har minst en fildetaljer objekt.
		//DokumentFil for ARKIV variant slettes
		arkivElementEndringTOList.addAll(slettFildetaljerIkkeArkivVariant(request.getDokumentInfoId(), dokumentInfoForKassering
				.getFildetaljerListeAdmin()));

		FilDetaljer arkiv = dokumentInfoForKassering.findFilDetaljerByVariantFormatAdmin(ARKIV);
		arkivElementEndringTOList.addAll(slettArkivVariantDokumentFil(request.getDokumentInfoId(), arkiv.getFilUuid()));

		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> slettFildetaljerIkkeArkivVariant(Long dokumentInfoId, Set<FilDetaljer> filDetaljerList) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		filDetaljerList
				.stream()
				.filter(filDetaljer -> filDetaljer.getVariantFormat() != ARKIV)
				.forEach(filDetaljer -> {
					slettDokumentFil(dokumentInfoId, filDetaljer.getVariantFormat(), filDetaljer.getFilUuid());
					arkivElementEndringTOList.add(slettFildetaljer(dokumentInfoId, filDetaljer.getVariantFormat()));
				});

		return arkivElementEndringTOList;
	}

	private List<ArkivElementEndringTO> slettArkivVariantDokumentFil(Long dokumentInfoId, String oldFilUuid) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.add(slettDokumentFil(dokumentInfoId, ARKIV, oldFilUuid));
		arkivElementEndringTOList.add(fjernSkjermingFraFildetaljer(dokumentInfoId, oldFilUuid, ARKIV));
		return arkivElementEndringTOList;
	}

	private ArkivElementEndringTO fjernSkjermingFraFildetaljer(Long dokumentInfoId, String filUuid, VariantFormatCode variantFormatCode) {
		entityManager.createQuery("update FilDetaljer set skjermingType=null where filUuid=:filUuid and dokumentInfo.dokumentInfoId=:dokumentInfoId and variantFormat=:variantFormat")
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("filUuid", filUuid)
				.setParameter("variantFormat", variantFormatCode)
				.executeUpdate();
		entityManager.flush();
		entityManager.clear();
		return ArkivElementEndringTO.builder()
				.arkivElement(fildetaljerSkjermingTypeVariant(variantFormatCode))
				.fraVerdi(POL.name())
				.tilVerdi(null)
				.build();
	}


	private ArkivElementEndringTO slettFildetaljer(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		deleteRepository.deleteFilDetaljerByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode.name());
		return ArkivElementEndringTO.builder()
				.arkivElement(FILDETALJER_VARIANTFORMAT)
				.fraVerdi(variantFormatCode.name())
				.tilVerdi(null)
				.build();
	}

	private ArkivElementEndringTO slettDokumentFil(Long dokumentInfoId, VariantFormatCode variantFormatCode, String filUuid) {
		deleteRepository.deleteDokumentFilByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode.name());
		return ArkivElementEndringTO.builder()
				.arkivElement(DOKUMENT_FIL_FIL_UUID)
				.fraVerdi(filUuid)
				.tilVerdi(null)
				.build();
	}

	private void settKassasjonInfo(DokumentInfo dokumentInfo, String kassertAvNavn) {
		dokumentInfo.setDatoKassert(LocalDateTime.now());
		dokumentInfo.setKassertAvNavn(kassertAvNavn);
		dokumentInfoRepository.save(dokumentInfo);
	}

	private List<ArkivElementEndringTO> opprettArkivElementEndring(DokumentInfo dokumentInfoTilTidligKassering) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();

		arkivElementEndringTOList.add(
				ArkivElementEndringTO.builder()
						.arkivElement(DOKUMENT_INFO_KASSERT_DATO)
						.fraVerdi(null)
						.tilVerdi(dokumentInfoTilTidligKassering.getDatoKassert().format(DateTimeFormatter.ISO_DATE_TIME))
						.build()
		);

		arkivElementEndringTOList.add(
				ArkivElementEndringTO.builder()
						.arkivElement(DOKUMENT_INFO_KASSERT_AV)
						.fraVerdi(null)
						.tilVerdi(dokumentInfoTilTidligKassering.getKassertAvNavn())
						.build()
		);

		return arkivElementEndringTOList;
	}
}
