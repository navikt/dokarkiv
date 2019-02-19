package no.nav.dokarkiv.rjoark102;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.dto.KasserDokumentRequest;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KasserDokumentService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final JoarkDeleteRepository deleteRepository;

	@Inject
	public KasserDokumentService(
			DokumentinfoRepository dokumentinfoRepository,
			JoarkDeleteRepository deleteRepository) {
		this.dokumentInfoRepository = dokumentinfoRepository;
		this.deleteRepository = deleteRepository;
	}

	public List<ArkivElementEndringTO> kasserDokument(KasserDokumentRequest request) {
		DokumentInfo dokumentInfoTilTidligKassering = dokumentInfoRepository.findByDokumentInfoId(request.getDokumentInfoId())
				.orElseThrow(
						() -> new DokumentInfoIkkeFunnetException(String.format(
								"Kan ikke finne dokument med dokumentInfoId=%s", request.getDokumentInfoId())));

		settKassasjonInfo(dokumentInfoTilTidligKassering, request.getKassertAvNavn());

		List<ArkivElementEndringTO> arkivElementEndringTOList = opprettArkivElementEndring(dokumentInfoTilTidligKassering);

		slettFildetaljerOgFil(request.getDokumentInfoId());

		return arkivElementEndringTOList;
	}

	private void slettFildetaljerOgFil(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
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
						.arkivElement("FilDetaljer.variantFormat")
						.fraVerdi(filDetaljer.getVariantFormat().name())
						.tilVerdi(null)
						.build()
				)
				.collect(Collectors.toList());

		arkivElementEndringTOList.add(
				ArkivElementEndringTO.builder()
						.arkivElement("DokumentInfo.kassertDato")
						.fraVerdi(null)
						.tilVerdi(dokumentInfoTilTidligKassering.getDatoKassert().format(DateTimeFormatter.ISO_DATE_TIME))
						.build()
		);

		arkivElementEndringTOList.add(
				ArkivElementEndringTO.builder()
						.arkivElement("DokumentInfo.kassertAv")
						.fraVerdi(null)
						.tilVerdi(dokumentInfoTilTidligKassering.getKassertAvNavn())
						.build()
		);

		return arkivElementEndringTOList;
	}
}
