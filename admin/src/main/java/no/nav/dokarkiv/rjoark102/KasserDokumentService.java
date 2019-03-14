package no.nav.dokarkiv.rjoark102;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_FIL_FIL_UUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_AV;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_DATO;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_FIL_UUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_SKJERMING_TYPE_VARIANT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

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
import java.util.Objects;
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
		DokumentInfo dokumentInfoTilTidligKassering = dokumentInfoRepository.findByDokumentInfoId(request.getDokumentInfoId())
				.orElseThrow(
						() -> new DokumentInfoIkkeFunnetException(String.format(
								"Kan ikke finne dokument med dokumentInfoId=%s", request.getDokumentInfoId())));

		settKassasjonInfo(dokumentInfoTilTidligKassering, request.getKassertAvNavn());

		List<ArkivElementEndringTO> arkivElementEndringTOList = opprettArkivElementEndring(dokumentInfoTilTidligKassering);

		//Slett alle Fildetaljer som ikke er ARKIV variant
		arkivElementEndringTOList.addAll(slettFildetaljerIkkeArkivVariant(request.getDokumentInfoId(), dokumentInfoTilTidligKassering
				.getFildetaljerListeAdmin()));

		FilDetaljer arkiv = dokumentInfoTilTidligKassering.findFilDetaljerByVariantFormatAdmin(ARKIV);
		arkivElementEndringTOList.addAll(slettArkivVariantDokumentFilOgErstattMedDummy(request.getDokumentInfoId(), arkiv.getFilUuid()));

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

	private List<ArkivElementEndringTO> slettArkivVariantDokumentFilOgErstattMedDummy(Long dokumentInfoId, String oldFilUuid) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.add(slettDokumentFil(dokumentInfoId, ARKIV, oldFilUuid));
		arkivElementEndringTOList.add(fjernSkjermingFraFildetaljer(dokumentInfoId, oldFilUuid, ARKIV));
//		arkivElementEndringTOList.add(oppdaterFildetaljerFilUuid(dokumentInfoId, oldFilUuid, FIL_UUID_DUMMY_DOKUMENT_KASSERT));
		return arkivElementEndringTOList;
	}

	//Ikke fjern dette. Midlertidlig løsning. Tenker å legge dette tilbake senere. Filluuid på Fildetaljer er ikke unik som gjør det umulig å bytte filluuid til dummmy_dokument
	private ArkivElementEndringTO oppdaterFildetaljerFilUuid(Long dokumentInfoId, String oldFilUuid, String newFilUuid) {
		entityManager.createQuery("update FilDetaljer set filUuid=:dummy_fil_uuid where filUuid=:oldFilUuid and dokumentInfo.dokumentInfoId=:dokumentInfoId")
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("oldFilUuid", oldFilUuid)
				.setParameter("dummy_fil_uuid", newFilUuid)
				.executeUpdate();
		entityManager.flush();
		entityManager.clear();
		return ArkivElementEndringTO.builder()
				.arkivElement(FILDETALJER_FIL_UUID)
				.fraVerdi(oldFilUuid)
				.tilVerdi(newFilUuid)
				.build();
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
				.arkivElement(String.format(FILDETALJER_SKJERMING_TYPE_VARIANT, variantFormatCode))
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
