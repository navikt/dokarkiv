package no.nav.dokarkiv.logisktidligkassasjon.rjoark105;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeKassertException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class LogiskTidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningRepository begrensningRepository;

	@Inject
	public LogiskTidligKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			BegrensningRepository begrensningRepository) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.begrensningRepository = begrensningRepository;
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
		if (begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfoId, BegrensningTypeCode.KASSERT)
				.isPresent()) {
			throw new DokumentAlleredeKassertException(String.format(
					"Kan ikke utføre logisk tidlig kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk tidlig kassert",
					dokumentInfoId));
		}
	}

	private void logiskTidligKassasjonAvEtDokument(DokumentInfo dokumentInfoSomSkalKasseres) {
		Begrensning begrensning = Begrensning.builder()
				.dokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId())
				.begrensningType(BegrensningTypeCode.KASSERT)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		begrensningRepository.save(begrensning);
	}
}
