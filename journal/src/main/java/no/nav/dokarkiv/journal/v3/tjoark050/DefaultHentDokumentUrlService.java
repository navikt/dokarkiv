package no.nav.dokarkiv.journal.v3.tjoark050;

import no.nav.dokarkiv.core.dokumenturl.DefaultHentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.journal.v3.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.journal.v3.tjoark051.AbstractJournalOperations;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementation of HentDokumentUrlService. Retrieves the filUuid for the
 * document and delegates to existing HentDokumentUrl.
 * 
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultHentDokumentUrlService extends AbstractJournalOperations implements HentDokumentUrlService {
	
	@Inject
	private DefaultHentDokumentUrl hentDokumentUrl;
	
	@Override
	public HentDokumentUrlResponseTo hentDokumentUrl(HentDokumentUrlRequestTo hentDokumentUrlRequest)
			throws DocumentNotFoundException {
		
		validateRequest(hentDokumentUrlRequest);
		
		String filUuid = getFilUuidForDocument(hentDokumentUrlRequest);
		String dokumentUrl = getDokumentUrlFromDelegateOperation(hentDokumentUrlRequest.getJournalpostId(), filUuid);
		return new HentDokumentUrlResponseTo(dokumentUrl);
	}

	private String getFilUuidForDocument(HentDokumentUrlRequestTo hentDokumentUrlRequest) throws DocumentNotFoundException  {
		Journalpost journalpost = lookupJournalpost(hentDokumentUrlRequest.getJournalpostId());
		
		DokumentInfo dokumentInfo = getDokumentInfo(journalpost, hentDokumentUrlRequest.getDokumentInfoId());
		FilDetaljer filDetaljer = getFilDetaljer(dokumentInfo, hentDokumentUrlRequest.getVariantFormat());
		
		return filDetaljer.getFilUuid();
	}
	
	private String getDokumentUrlFromDelegateOperation(Long journalpostId, String filUuid) throws DocumentNotFoundException {
		HentDokumentUrlRequest delegateRequest = new HentDokumentUrlRequest(journalpostId, filUuid);
		try {
			HentDokumentUrlResponse delegateResponse = hentDokumentUrl.hentDokumentUrl(delegateRequest);
			return delegateResponse.getDokumentUrl();
		} catch (NoJournalpostFoundException | InvalidFilUuidException e) {
			throw new DocumentNotFoundException(e);
		}
	}
	
	private void validateRequest(HentDokumentUrlRequestTo hentDokumentUrlRequest) {
		if (hentDokumentUrlRequest == null) {
			throw new InvalidArgumentException("HentDokumentUrlRequest is null");
		}		
		hentDokumentUrlRequest.validate();
	}

}
