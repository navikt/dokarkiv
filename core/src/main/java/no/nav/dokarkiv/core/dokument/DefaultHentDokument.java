package no.nav.dokarkiv.core.dokument;

import no.nav.dokarkiv.core.dokumenturl.AbstractDocumentOperation;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.exceptions.SettMetadataIDlfFailedException;
import no.nav.dokarkiv.core.journalbehandling.SettMetadataIDLF;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataForUthenting;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataIDLFRequest;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataIDLFResponse;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Implementation of <code>Hentdokumentservice</code>.
 *
 * @author Carl-Henrik Wolf Lund, Bekk Consulting
 * @author Lamisi Gurah Blackman, Accenture
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Component
public class DefaultHentDokument extends AbstractDocumentOperation implements HentDokument {

	private HentOndemandDokument hentOndemandDokument;

	private SettMetadataIDLF settMetadataIDLF;

	/** {@inheritDoc} */
	public HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException, DocumentNotFoundException {
		hentDokumentRequest.validate();
		return getDokument(hentDokumentRequest);
	}

	private HentDokumentResponse getDokument(HentDokumentRequest hentDokumentRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException, DocumentNotFoundException {

		Long journalpostId = hentDokumentRequest.getJournalpostId();
		String filUuid = hentDokumentRequest.getFilUuid();
		Journalpost journalpost = getJournalpost(journalpostId);

		FilDetaljer filDetaljer = getFilDetaljer(filUuid, journalpost);

		generateAuditLogIfDokumentIsSensitivt(journalpost, filDetaljer, "HentDokument");

		byte[] document = getDocumentFromRepository(journalpost, filDetaljer);

		return new HentDokumentResponse(document);
	}

	private byte[] getDocumentFromRepository(Journalpost journalpost, FilDetaljer filDetaljer) throws InvalidFilUuidException, DocumentNotFoundException {
		if (filDetaljer.getOnDemandId() != null) {
			return getDocumentFromOnDemandRepository(journalpost, filDetaljer);
		} else {
			DokumentFil dokumentFil = getDocumentFromDBRepository(filDetaljer.getFilUuid());
			return updateDocumentIfDlf(dokumentFil, journalpost.getId(), filDetaljer);
		}
	}

	private byte[] getDocumentFromOnDemandRepository(Journalpost journalpost, FilDetaljer filDetaljer) throws DocumentNotFoundException {
//		String onDemandId = filDetaljer.getOnDemandId();
//		OnDemandInstansCode onDemandInstansCode = filDetaljer.getOnDemandInstans();

		if (StringUtils.isNotEmpty(filDetaljer.getOnDemandId())){
			try {
				String dokumentUrl = hentOndemandDokument.createDokumentUrl(journalpost.getJournalpostId(), filDetaljer.getFilUuid()).getDokumentUrl();
				return hentOndemandDokument.hentOndemandDokumentFromJoark(dokumentUrl);
			} catch (InvalidFilUuidException | NoJournalpostFoundException e) {
				throw new DocumentNotFoundException("Dokument med journalpostId=" + journalpost.getJournalpostId() + ", filUuid=" + filDetaljer.getFilUuid() + " ikke funnet i OnDemand.", e);
			}
		}
		return hentOndemandDokument.hentOndemandDokumentFromJoark("");
//		return onDemandRepository.getDocument(onDemandId, onDemandInstansCode);
	}

	private byte[] updateDocumentIfDlf(DokumentFil dokumentFil, Long journalpostId, FilDetaljer filDetaljer) {
		if (filDetaljer.getVariantFormat() == VariantFormatCode.PRODUKSJON_DLF) {
			return updateDlfWitMetadata(dokumentFil, journalpostId, filDetaljer);
		} else {
			return dokumentFil.getFil();
		}
	}

	private byte[] updateDlfWitMetadata(DokumentFil dokumentFil, Long journalpostId, FilDetaljer filDetaljer) {
		SettMetadataForUthenting settMetadataForLagringAvDok = new SettMetadataForUthenting(journalpostId,
				filDetaljer.getFilUuid(), dokumentFil.getVersion());
		SettMetadataIDLFRequest settMetadataIDLFRequest = new SettMetadataIDLFRequest(settMetadataForLagringAvDok,
				dokumentFil.getFil());
		SettMetadataIDLFResponse response = null;
		try {
			response = settMetadataIDLF.settMetadataIDLF(settMetadataIDLFRequest);
		} catch (Exception e) {
			throw new SettMetadataIDlfFailedException(e);
		}
		return response.getDlfDokument();
	}

	/**
	 * Setter for the hentOndemandDokument
	 *
	 * @param hentOndemandDokument
	 *            the hentOndemandDokument to set
	 */
	public void setHentOndemandDokument(HentOndemandDokument hentOndemandDokument) {
		this.hentOndemandDokument = hentOndemandDokument;
	}

	/**
	 * Setter for the settMetadataIDLF property.
	 *
	 * @param settMetadataIDLF
	 *            the settMetadataIDLF to set
	 */
	public void setSettMetadataIDLF(SettMetadataIDLF settMetadataIDLF) {
		this.settMetadataIDLF = settMetadataIDLF;
	}

}
