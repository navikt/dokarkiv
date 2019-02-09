package no.nav.dokarkiv.kasserdokument.rjoark103;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class KasserDokumentService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final JoarkDeleteRepository deleteRepository;
	private final SkjermingService skjermingService;

	@Inject
	public KasserDokumentService(
			DokumentinfoRepository dokumentinfoRepository,
			JoarkDeleteRepository deleteRepository,
			SkjermingService skjermingService) {
		this.dokumentInfoRepository = dokumentinfoRepository;
		this.deleteRepository = deleteRepository;
		this.skjermingService = skjermingService;
	}

	public KasserDokumentResponse kasserDokument(KasserDokumentRequest request) {
		DokumentInfo dokumentInfoTilTidligKassering = dokumentInfoRepository.findByDokumentInfoId(request.getDokumentInfoId()).orElseThrow(
				() -> new DokumentInfoIkkeFunnetException(String.format(
						"Kan ikke finne dokument med dokumentInfoId=%s", request.getDokumentInfoId())));

		sjekkAtDokumentErLogiskKassert(dokumentInfoTilTidligKassering);
		settKassasjonInfo(dokumentInfoTilTidligKassering, request.getKassertAvNavn());
		slettFildetaljerOgFil(request.getDokumentInfoId());

		return KasserDokumentResponse.builder()
				.dokumentInfoId(request.getDokumentInfoId())
				.build();
	}

	private void sjekkAtDokumentErLogiskKassert(DokumentInfo dokumentInfo) {
		if (!skjermingService.isDokumentInfoKassert(dokumentInfo)) {
			throw new SkjermingIkkeFunnetException(
					String.format("Fildetaljene for dokumentInfoId=%s er ikke skjermet, kan ikke kassere dokumentet",
							dokumentInfo.getDokumentInfoId(),
							SkjermingTypeCode.POL));
		}
	}

	private void slettFildetaljerOgFil(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}

	private void settKassasjonInfo (DokumentInfo dokumentInfo, String kassertAvNavn) {
		dokumentInfo.setDatoKassert(LocalDateTime.now());
		dokumentInfo.setKassertAvNavn(kassertAvNavn);
		dokumentInfoRepository.save(dokumentInfo);
	}
}
