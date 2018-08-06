package no.nav.dokarkiv.core.journal;

import no.nav.dokarkiv.core.dokument.HentDokument;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfo;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.dokument.HentDokumentRequest;
import no.nav.dokarkiv.core.dokument.HentDokumentResponse;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoRequest;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoResponse;

/**
 * Implementation of JOARK information service.
 * 
 * @author Magnus Skuland, Sirius IT
 * @author Rune Romundstad, Sirius IT
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultJournalService implements JournalServiceBi {

	private HentDokument hentDokument;
	private HentDokumentUrl hentDokumentUrl;
//	private HentJournalpost hentJournalpost;
//	private FinnJournalpost finnJournalpost;
	private HentDokumentUrlInfo hentDokumentUrlInfo;
//	private IdentifiserBrevgruppe identifiserBrevgruppe;

	/** {@inheritDoc} */
	public HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException, DocumentNotFoundException {
		return hentDokument.hentDokument(hentDokumentRequest);
	}

	/** {@inheritDoc} */
	public HentDokumentUrlResponse hentDokumentUrl(HentDokumentUrlRequest hentDokumentUrlRequest)
			throws NoJournalpostFoundException, InvalidFilUuidException {
		return hentDokumentUrl.hentDokumentUrl(hentDokumentUrlRequest);
	}

//	/** {@inheritDoc} */
//	public HentJournalpostResponse hentJournalpost(HentJournalpostRequest hentJournalpostRequest)
//			throws NoJournalpostFoundException {
//		return hentJournalpost.hentJournalpost(hentJournalpostRequest);
//	}
//
//	/** {@inheritDoc} */
//	public FinnJournalpostResponse finnJournalpost(FinnJournalpostRequest finnJournalpostRequest) {
//		return finnJournalpost.finnJournalpost(finnJournalpostRequest);
//	}

	/** {@inheritDoc} */
	public HentDokumentUrlInfoResponse hentDokumentUrlInfo(HentDokumentUrlInfoRequest hentUrlRequest) {
		return hentDokumentUrlInfo.hentDokumentUrlInfo(hentUrlRequest);
	}

//	/** {@inheritDoc} */
//	@Override
//	public IdentifiserBrevgruppeResponse identifiserBrevgruppe(IdentifiserBrevgruppeRequest identifiserBrevgruppeRequest) {
//		return identifiserBrevgruppe.identifiserBrevgruppe(identifiserBrevgruppeRequest);
//	}
	
	/**
	 * Setter for the hentDokument property.
	 *
	 * @param hentDokument the hentDokument to set
	 */
	public void setHentDokument(HentDokument hentDokument) {
		this.hentDokument = hentDokument;
	}

	/**
	 * Setter for the hentDokumentUrl property.
	 *
	 * @param hentDokumentUrl the hentDokumentUrl to set
	 */
	public void setHentDokumentUrl(HentDokumentUrl hentDokumentUrl) {
		this.hentDokumentUrl = hentDokumentUrl;
	}

//	/**
//	 * Setter for the hentJournalpost property.
//	 *
//	 * @param hentJournalpost the hentJournalpost to set
//	 */
//	public void setHentJournalpost(HentJournalpost hentJournalpost) {
//		this.hentJournalpost = hentJournalpost;
//	}

//	/**
//	 * Setter for the finnJournalpost property.
//	 *
//	 * @param finnJournalpost the finnJournalpost to set
//	 */
//	public void setFinnJournalpost(FinnJournalpost finnJournalpost) {
//		this.finnJournalpost = finnJournalpost;
//	}

	/**
	 * Setter for the hentDokumentUrlInfo property.
	 *
	 * @param hentDokumentUrlInfo the hentDokumentUrlInfo to set
	 */
	public void setHentDokumentUrlInfo(HentDokumentUrlInfo hentDokumentUrlInfo) {
		this.hentDokumentUrlInfo = hentDokumentUrlInfo;
	}

//	/**
//	 * Setter for the identifiserBrevgruppe property.
//	 *
//	 * @param identifiserBrevgruppe the identifiserBrevgruppe to set
//	 */
//	public void setIdentifiserBrevgruppe(IdentifiserBrevgruppe identifiserBrevgruppe) {
//		this.identifiserBrevgruppe = identifiserBrevgruppe;
//	}

}
