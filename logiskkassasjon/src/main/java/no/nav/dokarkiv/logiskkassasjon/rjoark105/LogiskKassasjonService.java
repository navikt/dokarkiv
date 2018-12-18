package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.ErBegrensetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class LogiskKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningRepository begrensningRepository;

	@Inject
	public LogiskKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			BegrensningRepository begrensningRepository) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.begrensningRepository = begrensningRepository;
	}

	public LogiskKassasjonResponse logiskKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoSomSkalKasseres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).get();

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
		if (begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfoId, BegrensningTypeCode.KASSERT)
				.isPresent()) {
			throw new ErBegrensetException(String.format(
					"Kan ikke utføre logisk kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk kassert",
					dokumentInfoId));
		}
	}

	private void logiskKassasjonAvEtDokument(DokumentInfo dokumentInfoSomSkalKasseres) {
		Begrensning begrensning = Begrensning.builder()
				.dokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId())
				.begrensningType(BegrensningTypeCode.KASSERT)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		begrensningRepository.save(begrensning);
	}
}
