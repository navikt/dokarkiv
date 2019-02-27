package no.nav.dokarkiv.rjoark102;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_AV;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_DATO;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.repository.DefaultDokumentFilRepository.FIL_UUID_DUMMY_DOKUMENT;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import no.nav.dokarkiv.exception.ArkivVariantkkeFunnetException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		slettFildetaljerIkkeArkivVariant(request.getDokumentInfoId(), dokumentInfoTilTidligKassering.getFildetaljerListeAdmin());

		FilDetaljer arkiv = dokumentInfoTilTidligKassering.findFilDetaljerByVariantFormatAdmin(ARKIV);
		slettArkivVariantDokumentFilOgErstattMedDummy(request.getDokumentInfoId(), arkiv.getFilUuid());

		return arkivElementEndringTOList;
	}

	private void slettFildetaljerIkkeArkivVariant(Long dokumentInfoId, Set<FilDetaljer> filDetaljerList) {
		filDetaljerList
				.stream()
				.filter(filDetaljer -> filDetaljer.getVariantFormat() != ARKIV)
				.forEach(filDetaljer -> {
					slettDokumentFil(dokumentInfoId, filDetaljer.getVariantFormat());
					slettFildetaljer(dokumentInfoId, filDetaljer.getVariantFormat());
				});

	}

	private void slettArkivVariantDokumentFilOgErstattMedDummy(Long dokumentInfoId, String oldFilUuid) {
		slettDokumentFil(dokumentInfoId, ARKIV);
		fjernSkjermingFraFildetaljer(dokumentInfoId, oldFilUuid);
		oppdaterFildetaljerFilUuid(dokumentInfoId, oldFilUuid, FIL_UUID_DUMMY_DOKUMENT);
	}

	private void oppdaterFildetaljerFilUuid(Long dokumentInfoId, String oldFilUuid, String newFilUuid) {
		entityManager.createQuery("update FilDetaljer set filUuid=:dummy_fil_uuid where filUuid=:oldFilUuid and dokumentInfo.dokumentInfoId=:dokumentInfoId")
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("oldFilUuid", oldFilUuid)
				.setParameter("dummy_fil_uuid", newFilUuid)
				.executeUpdate();
		entityManager.flush();
		entityManager.clear();
	}

	private void fjernSkjermingFraFildetaljer(Long dokumentInfoId, String filUuid) {
		entityManager.createQuery("update FilDetaljer set skjermingType=null where filUuid=:filUuid and dokumentInfo.dokumentInfoId=:dokumentInfoId")
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("filUuid", filUuid)
				.executeUpdate();
		entityManager.flush();
		entityManager.clear();
	}


	private void slettFildetaljer(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		deleteRepository.deleteFilDetaljerByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode.name());
	}

	private void slettDokumentFil(Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		deleteRepository.deleteDokumentFilByDokumentInfoIdAndVariantFormat(dokumentInfoId, variantFormatCode.name());
	}

	private void settKassasjonInfo(DokumentInfo dokumentInfo, String kassertAvNavn) {
		dokumentInfo.setDatoKassert(LocalDateTime.now());
		dokumentInfo.setKassertAvNavn(kassertAvNavn);
		dokumentInfoRepository.save(dokumentInfo);
	}

	private List<ArkivElementEndringTO> opprettArkivElementEndring(DokumentInfo dokumentInfoTilTidligKassering) {
		List<ArkivElementEndringTO> arkivElementEndringTOList = dokumentInfoTilTidligKassering.getFildetaljerListeAdmin()
				.stream()
				.map(filDetaljer -> ArkivElementEndringTO.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(filDetaljer.getVariantFormat().name())
						.tilVerdi(null)
						.build()
				)
				.collect(Collectors.toList());

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
