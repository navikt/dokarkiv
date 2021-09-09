package no.nav.dokarkiv.journalpost.v1.bidrag;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.DokumentInfoId;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BidragService {

	private final BidragMellomlagringRepository bidragMellomlagringRepository;
	private final OpprettJournalpostBidragRequestValidator opprettJournalpostBidragRequestValidator;
	private static final String BIDRAG_BREVKODE = "458212";

	@Inject
	public BidragService(BidragMellomlagringRepository bidragMellomlagringRepository) {
		this.bidragMellomlagringRepository = bidragMellomlagringRepository;
		this.opprettJournalpostBidragRequestValidator = new OpprettJournalpostBidragRequestValidator();
	}

	public ResponseEntity<OpprettJournalpostResponse> opprettBidrag(OpprettJournalpostRequest request) {
		try {
			opprettJournalpostBidragRequestValidator.validateRequest(request);
		} catch (InputValideringFeiletException e) {
			log.warn("rjoark202 feilet under validering for bidrag. " + e.getMessage(), e);
			throw e;
		}
		log.info("Bidrag har validert OK.");

		BidragMellomlagring bidragMellomlagring = mapOpprettJournalPostRequestToBidragMellomlagring(request);
		addDokumentTilBidragMellomlagring(request, bidragMellomlagring);

		BidragMellomlagring lagretBidragMellomlagring = bidragMellomlagringRepository.save(bidragMellomlagring);
		String bidragMellomlagringId = lagretBidragMellomlagring.getIdWithPrefix().toString();
		log.info("Opprettet ny BidragMellomlagring. bidragMellomlagringId={}", bidragMellomlagringId);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(OpprettJournalpostResponse.builder()
						.journalpostId(bidragMellomlagringId)
						.melding("Forsendelsen er lagret i den midlertidige verdikjeden for arkivering av innsendinger til Bisys.")
						.journalpostferdigstilt(false)
						.dokumenter(getDokumentInfoIdListe(lagretBidragMellomlagring))
						.build());
	}

	private void addDokumentTilBidragMellomlagring(OpprettJournalpostRequest request, BidragMellomlagring bidragMellomlagring) {
		for (int i = 0; i < request.getDokumenter().size(); i++) {
			Dokument dokument = request.getDokumenter().get(i);
			BidragMellomlagringDokument bidragMellomlagringDokument = new BidragMellomlagringDokument();
			if (i == 0) {
				bidragMellomlagringDokument.setDokumentType(BidragMellomlagringDokumentType.HOVEDDOKUMENT);
			} else {
				if (dokument.getBrevkode() != null && dokument.getBrevkode().equals(BIDRAG_BREVKODE)) {
					bidragMellomlagringDokument.setDokumentType(BidragMellomlagringDokumentType.VEDLEGG_KVITTERING);
				} else {
					bidragMellomlagringDokument.setDokumentType(BidragMellomlagringDokumentType.VEDLEGG);
				}
			}
			bidragMellomlagringDokument.setDokument(dokument.getDokumentvarianter().get(0).getFysiskDokument());
			bidragMellomlagring.addBidragMellomlagringDokument(bidragMellomlagringDokument);
		}
	}

	private List<DokumentInfoId> getDokumentInfoIdListe(BidragMellomlagring lagretBidragMellomlagring) {
		List<DokumentInfoId> dokumentInfoIdListe = new ArrayList<>();
		lagretBidragMellomlagring.getBidragMellomlagringDokuments().iterator()
				.forEachRemaining(el -> dokumentInfoIdListe.add(new DokumentInfoId(el.getBidragMellomlagringDokumentId().toString())));
		return dokumentInfoIdListe;
	}

	private BidragMellomlagring mapOpprettJournalPostRequestToBidragMellomlagring(OpprettJournalpostRequest request) {
		BidragMellomlagring bidragMellomlagring = new BidragMellomlagring();
		bidragMellomlagring.setAvsenderFnr(request.getAvsenderMottaker().getId());
		bidragMellomlagring.setStatus(BidragMellomlagringStatus.KLAR_TIL_OVERFORING);
		bidragMellomlagring.setMottattDato(request.getDatoMottatt());
		return bidragMellomlagring;
	}
}
