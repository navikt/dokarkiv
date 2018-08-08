package no.nav.dokarkiv.core.dokument;

import static no.nav.dokarkiv.core.constants.ServiceConstants.HENT_DOKUMENT_SERVLET_PARAM;
import static org.apache.logging.log4j.util.Strings.isNotEmpty;

import no.nav.dokarkiv.core.dokumenturl.AbstractDocumentOperation;
import no.nav.dokarkiv.core.dokumenturl.MimeTypeMapper;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.exceptions.SettMetadataIDlfFailedException;
import no.nav.dokarkiv.core.journalbehandling.SettMetadataIDLF;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataForUthenting;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataIDLFRequest;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataIDLFResponse;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import no.nav.dokarkiv.core.repository.DokumentUrlInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Implementation of <code>Hentdokumentservice</code>.
 *
 * @author Carl-Henrik Wolf Lund, Bekk Consulting
 * @author Lamisi Gurah Blackman, Accenture
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Component
public class DefaultHentDokument extends AbstractDocumentOperation implements HentDokument {

	@Value("${joark.hentdokument.baseurl}")
	private String joarkUrl;

	@Inject
	private HentOndemandDokument hentOndemandDokument;

	@Inject
	private SettMetadataIDLF settMetadataIDLF;

	@Inject
	private DokumentUrlInfoRepository dokumentUrlInfoRepository;

	private MimeTypeMapper mimeTypeMapper = new MimeTypeMapper();

	/**
	 * {@inheritDoc}
	 */
	public HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException, DocumentNotFoundException {
		hentDokumentRequest.validate();
		return getDokument(hentDokumentRequest);
	}

	private HentDokumentResponse getDokument(HentDokumentRequest hentDokumentRequest) throws NoJournalpostFoundException,
			InvalidFilUuidException {

		Long journalpostId = hentDokumentRequest.getJournalpostId();
		String filUuid = hentDokumentRequest.getFilUuid();
		String docToken = hentDokumentRequest.getDocToken();
		Journalpost journalpost = getJournalpost(journalpostId);

		FilDetaljer filDetaljer = getFilDetaljer(filUuid, journalpost);

		generateAuditLogIfDokumentIsSensitivt(journalpost, filDetaljer, "HentDokument");

		byte[] document = getDocumentFromRepository(journalpost, filDetaljer, docToken);

		return new HentDokumentResponse(document);
	}

	private byte[] getDocumentFromRepository(Journalpost journalpost, FilDetaljer filDetaljer, String docToken) throws InvalidFilUuidException {
		if (filDetaljer.getOnDemandId() != null && isNotEmpty(docToken)) {
			return getDocumentFromOnDemandRepository(filDetaljer, docToken);
		} else {
			DokumentFil dokumentFil = getDocumentFromDBRepository(filDetaljer.getFilUuid());
			return updateDocumentIfDlf(dokumentFil, journalpost.getId(), filDetaljer);
		}
	}

	private byte[] getDocumentFromOnDemandRepository(FilDetaljer filDetaljer, String docToken) {
		if (isNotEmpty(filDetaljer.getOnDemandId())) {
			String dokumentUrl = getUrl(filDetaljer, docToken);
			return hentOndemandDokument.hentOndemandDokumentFromJoark(dokumentUrl);
		}
		return null;
	}

	private String getUrl(FilDetaljer filDetaljer, String docToken) {
		String url = new StringBuilder(joarkUrl)
				.append("?")
				.append(HENT_DOKUMENT_SERVLET_PARAM)
				.append("=")
				.append(docToken)
				.toString();

		return addMimetypeToUrl(url, mimeTypeMapper.getMimeTypeForFileExtension(filDetaljer.getFiltype().name()));
	}

	private String addMimetypeToUrl(String url, String mimetype) {
		String mimetypeParam;
		try {
			mimetypeParam = "&mimetype=" + URLEncoder.encode(mimetype, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new DokarkivTechnicalException("Could not generate URL", e);
		}
		return url.concat(mimetypeParam);
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
	 * @param hentOndemandDokument the hentOndemandDokument to set
	 */
	public void setHentOndemandDokument(HentOndemandDokument hentOndemandDokument) {
		this.hentOndemandDokument = hentOndemandDokument;
	}

	/**
	 * Setter for the settMetadataIDLF property.
	 *
	 * @param settMetadataIDLF the settMetadataIDLF to set
	 */
	public void setSettMetadataIDLF(SettMetadataIDLF settMetadataIDLF) {
		this.settMetadataIDLF = settMetadataIDLF;
	}

}
