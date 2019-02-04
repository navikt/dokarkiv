package no.nav.dokarkiv.logisktidligkassasjon.rjoark105;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeKassertException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class LogiskTidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final SkjermingService skjermingService;

	@Inject
	public LogiskTidligKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			SkjermingService skjermingService) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.skjermingService = skjermingService;
	}

	public LogiskTidligKassasjonResponse logiskTidligKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoSomSkalKasseres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElseThrow(
				() -> new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
						dokumentInfoId)));

		sjekkAtDokumentIkkeErBegrensetSomKassert(dokumentInfoId);

		logiskTidligKassasjonAvEtDokument(dokumentInfoSomSkalKasseres);

		return LogiskTidligKassasjonResponse.builder()
				.dokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId())
				.tittel(dokumentInfoSomSkalKasseres.getTittel())
				.build();
	}

	private void sjekkAtDokumentIkkeErBegrensetSomKassert(Long dokumentInfoId) {
		sjekkAtDokumentIkkeErKassert(dokumentInfoId);
	}

	private void sjekkAtDokumentIkkeErKassert(Long dokumentInfoId) {
		if (skjermingService.isDokumentInfoIdKassert(dokumentInfoId)) {
			throw new DokumentAlleredeKassertException(String.format(
					"Kan ikke utføre logisk tidlig kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk tidlig kassert",
					dokumentInfoId));
		}
	}

	private void logiskTidligKassasjonAvEtDokument(DokumentInfo dokumentInfoSomSkalKasseres) {
		skjermingService.setDokumentKassert(dokumentInfoSomSkalKasseres, SkjermingTypeCode.POL);
	}
}
