package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeKassertException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class LogiskKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningService begrensningService;

	@Inject
	public LogiskKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			BegrensningService begrensningService) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.begrensningService = begrensningService;
	}

	public LogiskKassasjonResponse logiskKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoSomSkalKasseres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElseThrow(
				() -> new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
						dokumentInfoId)));

		sjekkAtDokumentIkkeErBegrensetSomKassert(dokumentInfoId);

		logiskKassasjonAvEtDokument(dokumentInfoSomSkalKasseres);

		return LogiskKassasjonResponse.builder()
				.dokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId())
				.tittel(dokumentInfoSomSkalKasseres.getTittel())
				.build();
	}

	private void sjekkAtDokumentIkkeErBegrensetSomKassert(Long dokumentInfoId) {
		sjekkAtDokumentIkkeErKassert(dokumentInfoId);
	}

	private void sjekkAtDokumentIkkeErKassert(Long dokumentInfoId) {
		if (begrensningService.isDokumentInfoIdKassert(dokumentInfoId)) {
			throw new DokumentAlleredeKassertException(String.format(
					"Kan ikke utføre logisk kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk kassert",
					dokumentInfoId));
		}
	}

	private void logiskKassasjonAvEtDokument(DokumentInfo dokumentInfoSomSkalKasseres) {
		begrensningService.setDokumentKassert(dokumentInfoSomSkalKasseres, BegrensningTypeCode.POL);
	}
}
