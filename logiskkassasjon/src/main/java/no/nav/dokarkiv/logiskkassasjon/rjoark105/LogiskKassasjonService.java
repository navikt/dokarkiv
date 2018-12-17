package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.ErBegrensetException;
import no.nav.dokarkiv.core.exceptions.KassasjonAvDokumentKnyttetFlereJPException;
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
		DokumentInfo dokumentInfoSomSkalKasseres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElse(null);

		if (dokumentInfoSomSkalKasseres == null) {
			throw new DokumentInfoIkkeFunnetException(
					String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
							dokumentInfoId));
		}

		sjekkAtDokumentIkkeErBegrensetSomKassert(dokumentInfoId);

		logiskKassasjonAvEtDokument(dokumentInfoSomSkalKasseres);

		return LogiskKassasjonResponse.builder()
				.journalpostId(dokumentInfoSomSkalKasseres.getOriginalJournalpost()
						== null ? null : dokumentInfoSomSkalKasseres.getOriginalJournalpost().getJournalpostId())
				.dokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId())
				.tittel(dokumentInfoSomSkalKasseres.getTittel())
				.build();
	}

	private void sjekkAtDokumentIkkeErBegrensetSomKassert(Long dokumentInfoId) {
		//TODO: Implementere kassering på relasjoner?
//		sjekkAtJournalpostDokumentInfoRelasjonIkkeErKassert(dokumentInfoId);
		sjekkAtDokumentIkkeErKassert(dokumentInfoId);
	}

	//TODO: Husk å endre til begrensningsType for kassasjon
	private void sjekkAtDokumentIkkeErKassert(Long dokumentInfoId) {
		if (begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfoId, BegrensningTypeCode.UTILGJENGELIGGJORT)
				.isPresent()) {
			throw new ErBegrensetException(String.format(
					"Kan ikke utføre logisk kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk kassert",
					dokumentInfoId));
		}
	}

	private void logiskKassasjonAvEtDokument(DokumentInfo dokumentInfoSomSkalKasseres) {
		//TODO: Se over denne kontrollen etter avklaring av kassasjon
		if (dokumentInfoSomSkalKasseres.isRelatedToMultipleJournalposts()) {
			throw new KassasjonAvDokumentKnyttetFlereJPException(
					String.format("Kan ikke utføre tidlig kassasjon av dokument med dokumentInfoId=%s fordi " +
									"dokumentet er knyttet til flere journalposter og den funksjonaliteten er ikke implementert",
							dokumentInfoSomSkalKasseres.getDokumentInfoId()));
		}

		Begrensning begrensning = Begrensning.builder()
				.journalpostId(dokumentInfoSomSkalKasseres.getOriginalJournalpost()
						== null ? null : dokumentInfoSomSkalKasseres.getOriginalJournalpost().getJournalpostId())
				.dokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId())
				//TODO: Sett riktig begrensningsType for kassasjon
				.begrensningType(BegrensningTypeCode.UTILGJENGELIGGJORT)
				.build();
		begrensning.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));

		begrensningRepository.save(begrensning);

	}
}
