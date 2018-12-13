package no.nav.dokarkiv.tidligkassasjon.rjoark107;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.KassasjonAvDokumentKnyttetFlereJPException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class TidligKassasjonService {


	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningRepository begrensningRepository;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final JoarkDeleteRepository deleteRepository;

	@Inject
	public TidligKassasjonService(
			DokumentinfoRepository dokumentinfoRepository,
			BegrensningRepository begrensningRepository,
			JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository,
			JoarkDeleteRepository deleteRepository) {
		this.dokumentInfoRepository = dokumentinfoRepository;
		this.begrensningRepository = begrensningRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.deleteRepository = deleteRepository;
	}


	public TidligKassasjonResponse tidligKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoSomSkalKasseres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId).orElse(null);

		if (dokumentInfoSomSkalKasseres == null) {
			throw new DokumentInfoIkkeFunnetException(
					String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
							dokumentInfoId));
		}

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);

		if (dokumentInfoSomSkalKasseres.isRelatedToMultipleJournalposts()) {
			throw new KassasjonAvDokumentKnyttetFlereJPException(
					String.format("Kan ikke utføre tidlig kassasjon av dokument med dokumentInfoId=%s fordi " +
									"dokumentet er knyttet til flere journalposter og den funksjonaliteten er ikke implementert",
							dokumentInfoId));
		}

		tidligKassasjonAvEtDokument(dokumentInfoId);

		//TODO: Hvis dokumentInfo må være knyttet en journalpost så kan vi skippe og kontrollere mot 0 relasjoner
		/**
		 int antallRelasjoner =
		 journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId).size();

		 if (antallRelasjoner > 1) {
		 throw new KassasjonAvDokumentKnyttetFlereJPException(
		 String.format("Kan ikke utføre tidlig kassasjon av dokument med dokumentInfoId=%s fordi " +
		 "dokumentet er knyttet til flere journalposter og den funksjonaliteten er ikke implementert",
		 dokumentInfoId));
		 }
		 else if (antallRelasjoner == 0){
		 throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
		 String.format("Kan ikke finne relasjon til journalpost for dokument med dokumentInfoId=%s",
		 dokumentInfoId));
		 }
		 else if (antallRelasjoner == 1){
		 tidligKassasjonAvEtDokument(dokumentInfoId);
		 }
		 **/

		return TidligKassasjonResponse.builder()
				.journalpostId(dokumentInfoSomSkalKasseres.getOriginalJournalpost()
						== null ? null : dokumentInfoSomSkalKasseres.getOriginalJournalpost().getJournalpostId())
				.dokumentInfoId(dokumentInfoId)
				.tittel(dokumentInfoSomSkalKasseres.getTittel())
				.build();
	}

	private void sjekkAtDokumentErLogiskKassert(Long dokumentInfoId) {
		if (isFalse(begrensningRepository)) {
			throw new BegrensningIkkeFunnetException(
					String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
							dokumentInfoId,
							//TODO: Endre til begrensningsType for kassasjon
							BegrensningTypeCode.UTILGJENGELIGGJORT));
		}
	}

	private void tidligKassasjonAvEtDokument(Long dokumentInfoId) {
		slettFilOgBeholdMetadata(dokumentInfoId);
	}

	private void slettFilOgBeholdMetadata(Long dokumentInfoId) {
		deleteRepository.deleteDokumentFilByDokumentInfoId(dokumentInfoId);
		deleteRepository.deleteFilDetaljerByDokumentInfoId(dokumentInfoId);
	}

}
