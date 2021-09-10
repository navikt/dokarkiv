package no.nav.dokarkiv.journalpost.v1.bidrag;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
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
	private final OpprettJournalpostBidragRequestMapper opprettJournalpostBidragRequestMapper;

	@Inject
	public BidragService(BidragMellomlagringRepository bidragMellomlagringRepository) {
		this.bidragMellomlagringRepository = bidragMellomlagringRepository;
		this.opprettJournalpostBidragRequestValidator = new OpprettJournalpostBidragRequestValidator();
		this.opprettJournalpostBidragRequestMapper = new OpprettJournalpostBidragRequestMapper();
	}

	public ResponseEntity<OpprettJournalpostResponse> opprettBidrag(OpprettJournalpostRequest request) {
		final String eksternReferanseId = request.getEksternReferanseId();
		log.info("journalpostapi.opprettJournalpost har mottatt kall for opprettelse av ny bidrag mellomlagring med eksternReferanseId={}", eksternReferanseId);
		try {
			opprettJournalpostBidragRequestValidator.validateRequest(request);
		} catch (InputValideringFeiletException e) {
			log.warn("journalpostapi.opprettJournalpost feilet under validering for bidrag. eksternReferanseId={}.", eksternReferanseId, e);
			throw e;
		}

		BidragMellomlagring bidragMellomlagring = opprettJournalpostBidragRequestMapper.map(request);
		BidragMellomlagring lagretBidragMellomlagring = bidragMellomlagringRepository.save(bidragMellomlagring);
		String bidragMellomlagringId = lagretBidragMellomlagring.getIdWithPrefix().toString();
		log.info("Opprettet ny BidragMellomlagring. eksternReferanseId={}, bidragMellomlagringId={}", eksternReferanseId, bidragMellomlagringId);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(OpprettJournalpostResponse.builder()
						.journalpostId(bidragMellomlagringId)
						.melding("Forsendelsen med eksternReferanseId=" + eksternReferanseId + " er lagret i den midlertidige verdikjeden for arkivering av innsendinger til Bisys.")
						.journalpostferdigstilt(false)
						.dokumenter(getDokumentInfoIdListe(lagretBidragMellomlagring))
						.build());
	}

	private List<DokumentInfoId> getDokumentInfoIdListe(BidragMellomlagring lagretBidragMellomlagring) {
		List<DokumentInfoId> dokumentInfoIdListe = new ArrayList<>();
		lagretBidragMellomlagring.getBidragMellomlagringDokuments().iterator()
				.forEachRemaining(el -> dokumentInfoIdListe.add(new DokumentInfoId(el.getBidragMellomlagringDokumentId().toString())));
		return dokumentInfoIdListe;
	}
}
