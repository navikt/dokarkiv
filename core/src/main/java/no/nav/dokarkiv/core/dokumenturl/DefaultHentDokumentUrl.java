package no.nav.dokarkiv.core.dokumenturl;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.DokumentUrlInfoRepositorySkjermet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * Implementation of <code>HentDokumentUrl</code>.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Magnus Skuland, Sirius IT
 * @author Thao Thao Nguyen, Visma Sirius
 */
@Component
public class DefaultHentDokumentUrl extends AbstractDocumentOperation implements HentDokumentUrl {

	@Value("${dokarkiv.hentdokument.baseurl}")
	private String servletUrl;
	@Value("${joark.hentdokument.baseurl}")
	private String joarkUrl;

	private final MimeTypeMapper mimeTypeMapper = new MimeTypeMapper();

	@Inject
    private DokumentUrlInfoRepositorySkjermet dokumentUrlInfoRepository;

	public HentDokumentUrlResponse hentDokumentUrl(HentDokumentUrlRequest hentDokumentUrlRequest)
			throws NoJournalpostFoundException, InvalidFilUuidException {
		validateRequest(hentDokumentUrlRequest);

		Long journalpostId = hentDokumentUrlRequest.getJournalpostId();
		String filUuid = hentDokumentUrlRequest.getFilUuid();

		return generateUrlWithBaseUrl(servletUrl, journalpostId, filUuid, hentDokumentUrlRequest.getTimeToLiveMinutes());
	}

	@Override
	public HentDokumentUrlResponse hentDokumentUrlJoark(HentDokumentUrlRequest hentDokumentUrlRequest) throws NoJournalpostFoundException, InvalidFilUuidException {
		validateRequest(hentDokumentUrlRequest);

		Long journalpostId = hentDokumentUrlRequest.getJournalpostId();
		String filUuid = hentDokumentUrlRequest.getFilUuid();

		return generateUrlWithBaseUrl(joarkUrl, journalpostId, filUuid, hentDokumentUrlRequest.getTimeToLiveMinutes());
	}

	private HentDokumentUrlResponse generateUrlWithBaseUrl(String baseUrl, Long journalpostId, String filUuid, Long ttl) throws NoJournalpostFoundException, InvalidFilUuidException {
		Journalpost journalpost = getJournalpost(journalpostId);
		FilDetaljer filDetaljer = getFilDetaljer(filUuid, journalpost);
		generateAuditLogIfDokumentIsSensitivt(journalpost, filDetaljer, "HentDokumentUrl");

		String url = generateUrl(baseUrl, journalpost, filDetaljer, ttl);

		return new HentDokumentUrlResponse(url);
	}

	private void validateRequest(HentDokumentUrlRequest hentDokumentUrlRequest) {
		if (hentDokumentUrlRequest == null) {
			throw new InvalidArgumentException("HentDokumentUrlRequest is null");
		}		
		hentDokumentUrlRequest.validate();
	}

	private String generateUrl(String baseUrl, Journalpost journalpost, FilDetaljer pfildetaljer, Long timeToLiveMinutes)
			throws InvalidFilUuidException {

		FilDetaljer fildetaljer = pfildetaljer.getDokumentInfo().findFilDetaljerByVariantFormat(pfildetaljer.getVariantFormat());
		if (fildetaljer == null) {
			throw new InvalidFilUuidException(String.format("Finner ikke FilDetaljer tilhørende dokumentInfoId: %s og variant %s", pfildetaljer.getDokumentInfo().getDokumentInfoId(), pfildetaljer.getVariantFormat().name()), null);
		}
		String filUuid = fildetaljer.getFilUuid();
		if (fildetaljer.getOnDemandId() != null && fildetaljer.getOnDemandInstans() != null) {
			//dokumentet ligger inntil videre i OnDemand
		} else {
			verifyThatDocumentExistsInDB(filUuid);
		}
		String url = createDokumentUrlInfoAndUrl(baseUrl, journalpost, filUuid, timeToLiveMinutes);
		return addMimetypeToUrl(url, mimeTypeMapper.getMimeTypeForFileExtension(fildetaljer.getFiltype().name()));
	}
	
	private void verifyThatDocumentExistsInDB(String filUuid) throws InvalidFilUuidException {
		getDocumentFromDBRepository(filUuid);
	}

	private String addMimetypeToUrl(String url, String mimetype) {
		String mimetypeParam = null;
		try {
			mimetypeParam = "&mimetype=" + URLEncoder.encode(mimetype, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new DokarkivTechnicalException("Could not generate URL", e);
		}
		return url.concat(mimetypeParam);
	}	

	private String createDokumentUrlInfoAndUrl(String baseUrl, Journalpost journalpost, String filUuid, Long timeToLiveMinutes) {
		String token = saveDokumentUrlInfo(journalpost, filUuid, timeToLiveMinutes);
		return new StringBuilder(baseUrl)
				.append("?")
				.append(HentDokumentUrlConstants.HENT_DOKUMENT_SERVLET_PARAM)
				.append("=")
				.append(token)
				.toString();
	}

	private String saveDokumentUrlInfo(Journalpost journalpost, String filUuid, Long timeToLiveMinutes) {
		String token = UUID.randomUUID().toString();
		DokumentUrlInfo dokUrl = new DokumentUrlInfo();
		dokUrl.setDoctoken(token);
		dokUrl.setJournalpost(journalpost);
		dokUrl.setTidspunkt(DateProvider.getToday());
		dokUrl.setFilUuid(filUuid);
		dokUrl.setTimeToLiveMinutes(timeToLiveMinutes);
		dokumentUrlInfoRepository.save(dokUrl);
		return token;
	}

	/**
	 * Setter for the servletUrl property.
	 * 
	 * @param servletUrl
	 *            the servletUrl to set
	 */
	public void setServletUrl(String servletUrl) {
		this.servletUrl = servletUrl;
	}

    public void setDokumentUrlInfoRepository(DokumentUrlInfoRepositorySkjermet dokumentUrlInfoRepository) {
		this.dokumentUrlInfoRepository = dokumentUrlInfoRepository;
	}
}
