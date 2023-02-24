package no.nav.dokarkiv.core.ondemand;

import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tjenesten henter OnDemand dokumenter fra Joark ved bruk av hentdokumenturl servleten.
 * <p>
 * Hvorfor?
 * Joark bruker et proprietært IBM OnDemand bibliotek (odwek) som kun har støtte for IBM Java 7 som følger med WAS.
 * Vi valgte å ikke portere denne til dokarkiv siden OnDemand skal fases ut.
 */
@Component
public class HentOndemandDokument {
	private final HentDokumentUrl hentDokumentUrl;

	public HentOndemandDokument(HentDokumentUrl hentDokumentUrl) {
		this.hentDokumentUrl = hentDokumentUrl;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public HentDokumentUrlResponse createDokumentUrl(Long journalpostId, String filUuid) throws InvalidFilUuidException, NoJournalpostFoundException {
		return hentDokumentUrl.hentDokumentUrlJoark(mapHentDokumentRequest(journalpostId, filUuid));
	}

	private HentDokumentUrlRequest mapHentDokumentRequest(Long journalpostId, String filUuid) {
		return new HentDokumentUrlRequest(journalpostId, filUuid, 5L);
	}
}
