package no.nav.dokarkiv.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.constants.ServiceConstants;
import no.nav.dokarkiv.core.dokument.HentDokumentRequest;
import no.nav.dokarkiv.core.dokument.HentDokumentResponse;
import no.nav.dokarkiv.core.dokumenturl.MimeTypeMapper;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoRequest;
import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoResponse;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.journal.JournalServiceBi;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Servlet implementation class for Servlet: HentDokumentServlet.
 * <p>
 * Servlet that is invoked with a docToken in request. The servlet then
 * redirects to HentDokumentUrlInfo and HentDokument to finally write a document
 * byte array to its response.
 *
 * @author Magnus Skuland, Sirius IT
 * @author Eirik Bergande, Sirius IT
 * @web.servlet name="HentDokumentServlet" display-name="HentDokumentServlet"
 * @web.servlet-mapping url-pattern="/HentDokument"
 */
@Slf4j
public class HentDokumentServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

	/**
	 * Id used for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * Members that are retrieved from Spring context. Default scope for
	 * testability.
	 */
	JournalServiceBi service;
	TransactionTemplate transactionTemplate;

	private MimeTypeMapper mimeTypeMapper = new MimeTypeMapper();

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try {
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						DokumentUrlInfo dokUrlInfo = getDokumentUrlInfo(request);
						Long journalpostId = dokUrlInfo.getJournalpost().getJournalpostId();
						String filUuid = dokUrlInfo.getFilUuid();
						byte[] document = getDocument(journalpostId, filUuid);

						FilTypeCode filtype = getFilTypeFromJournalpost(dokUrlInfo.getJournalpost(), filUuid);
						writeDocumentToResponse(response, document, filtype);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		} catch (Exception exception) {
			handleException(response, exception, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private FilTypeCode getFilTypeFromJournalpost(Journalpost journalpost, String filUuid) {
		FilDetaljer filDetaljer = journalpost.findFilDetaljerByFilUuid(filUuid);
		return filDetaljer != null ? filDetaljer.getFiltype() : null;
	}

	private DokumentUrlInfo getDokumentUrlInfo(HttpServletRequest request) {
		String docToken = request.getParameter(ServiceConstants.HENT_DOKUMENT_SERVLET_PARAM);
		HentDokumentUrlInfoResponse hentDokumentUrlInfoResponse = null;
		hentDokumentUrlInfoResponse = service.hentDokumentUrlInfo(new HentDokumentUrlInfoRequest(docToken));
		return hentDokumentUrlInfoResponse.getDokumentUrl();
	}

	private byte[] getDocument(final Long journalpostId, final String filUuid) throws NoJournalpostFoundException,
			InvalidFilUuidException, DocumentNotFoundException {
		HentDokumentResponse hentDokumentResponse = service.hentDokument(createHentDokumentRequest(journalpostId, filUuid));
		return hentDokumentResponse.getDokument();
	}

	private HentDokumentRequest createHentDokumentRequest(Long journalpostId, String filUuid) {
		HentDokumentRequest hentDokumentRequest = new HentDokumentRequest(journalpostId, filUuid);
		setRequestContextOnRequest(hentDokumentRequest);
		return hentDokumentRequest;
	}

	/**
	 * Must set requestContext via reflection as there is no exposed setter in
	 * <code>ServiceRequest</code>.
	 *
	 * @param hentDokumentRequest The request.
	 */
	private void setRequestContextOnRequest(HentDokumentRequest hentDokumentRequest) {
		Field requestContextField = ReflectionUtils.findField(HentDokumentRequest.class, "requestContext");
		ReflectionUtils.makeAccessible(requestContextField);
		SimpleRequestContext requestContext = createRequestContext();
		ReflectionUtils.setField(requestContextField, hentDokumentRequest, requestContext);
		RequestContextSetter.setRequestContext(requestContext);
	}

	private SimpleRequestContext createRequestContext() {
		return new SimpleRequestContext("", "", "", "", this.getClass().getSimpleName());
	}

	private void writeDocumentToResponse(HttpServletResponse response, byte[] dokument, FilTypeCode filtype)
			throws IOException {
		response.setContentType(getContentType(filtype));
		response.setContentLength(dokument.length);
		response.getOutputStream().write(dokument);
		response.getOutputStream().flush();
	}

	private String getContentType(FilTypeCode filtype) {
		return mimeTypeMapper.getMimeTypeForFileExtension(filtype.name());
	}

	private void handleException(HttpServletResponse response, Throwable rootCause, int errorMessage) throws ServletException,
			IOException {
		response.sendError(errorMessage, rootCause.getMessage());
		log.warn("Henting av dokument fra HentDokumentServlet feilet", rootCause);
		throw new ServletException(rootCause.getMessage(), rootCause);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String errorMsg = "doPost() is not implemented. Use doGet() instead.";
		response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errorMsg);
		throw new ServletException(errorMsg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		final WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(config
				.getServletContext());
		this.service = (JournalServiceBi) context.getBean("srv.joark.journalService");
//		this.exceptionLogger = (ExceptionLogger) context.getBean("cfg.common.exceptionLogger");
		this.transactionTemplate = (TransactionTemplate) context.getBean("rep.joark.transactionTemplate");
	}

}