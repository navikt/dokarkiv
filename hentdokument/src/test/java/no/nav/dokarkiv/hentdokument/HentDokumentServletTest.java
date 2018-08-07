//package no.nav.dokarkiv.hentdokument;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.CoreMatchers.notNullValue;
//import static org.junit.Assert.assertThat;
//import static org.mockito.ArgumentMatchers.isA;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import com.google.common.collect.Sets;
//import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
//import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
//import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
//import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
//import no.nav.dokarkiv.core.domain.entities.Journalpost;
//import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
//import no.nav.dokarkiv.core.journal.JournalServiceBi;
//import no.nav.dokarkiv.core.dokument.HentDokumentRequest;
//import no.nav.dokarkiv.core.dokument.HentDokumentResponse;
//import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoRequest;
//import no.nav.dokarkiv.core.dokumenturlinfo.HentDokumentUrlInfoResponse;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//
//import javax.servlet.ServletOutputStream;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.UUID;
//
///**
// * Unit test of the HentDokumentServlet.
// *
// * @author Thomas Eugen Bjørge, Sirius IT
// * @author Lamisi Gurah Blackman, Accenture
// */
//public class HentDokumentServletTest {
//
//	private static final long JOURNALPOST_ID = 100L;
//	private String filUuid;
//	private JournalServiceBi journalServiceMock;
//	private HttpServletRequest httpServletRequestMock;
//	private HttpServletResponse httpServletResponseMock;
//
//	private HentDokumentServlet hentDokumentServlet;
//
//	@Before
//	public void before() throws Exception {
//		setupServlet();
//		setupOutputStreamMock();
//		setupHentDokumentMock();
//		filUuid = UUID.randomUUID().toString();
//	}
//
//	@Ignore
//	@Test
//	public void shouldCallHentDokumentWithCorrectJournapostIdAndFilUuid() throws Exception {
//		setupHentDokumentUrlInfoMock();
//		doGetAndAssert();
//	}
//
//	@SuppressWarnings("deprecation")
//	@Ignore
//	@Test
//	public void shouldSetRequestContextOnHentDokumentRequest() throws Exception {
//		setupHentDokumentUrlInfoMock();
//		hentDokumentServlet.doGet(httpServletRequestMock, httpServletResponseMock);
//
//		ArgumentCaptor<HentDokumentRequest> hentDokumentRequest = ArgumentCaptor.forClass(HentDokumentRequest.class);
//		verify(journalServiceMock).hentDokument(hentDokumentRequest.capture());
//		assertThat(hentDokumentRequest.getValue(), notNullValue());
//	}
//
//	private void doGetAndAssert() throws Exception {
//		hentDokumentServlet.doGet(httpServletRequestMock, httpServletResponseMock);
//
//		ArgumentCaptor<HentDokumentRequest> hentDokumentRequest = ArgumentCaptor.forClass(HentDokumentRequest.class);
//		verify(journalServiceMock).hentDokument(hentDokumentRequest.capture());
//
//		assertThat(hentDokumentRequest.getValue().getJournalpostId(), is(JOURNALPOST_ID));
//		assertThat(hentDokumentRequest.getValue().getFilUuid(), is(filUuid));
//	}
//
//	private void setupHentDokumentMock() throws Exception {
//		when(journalServiceMock.hentDokument(isA(HentDokumentRequest.class))).thenReturn(
//				new HentDokumentResponse("Test".getBytes()));
//	}
//
//	private void setupHentDokumentUrlInfoMock() {
//		Journalpost journalpost = Journalpost.builder()
//										.journalpostId(JOURNALPOST_ID)
//										.build();
//		journalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
//				.dokumentInfo(DokumentInfo.builder()
//						.fildetaljerListe(Sets.newHashSet(FilDetaljer.builder()
//								.filtype(FilTypeCode.PDF)
//								.filUuid(filUuid)
//								.build()))
//						.build())
//				.build());
//
//		DokumentUrlInfo dokumentUrlInfo = DokumentUrlInfo.builder()
//											.journalpost(journalpost)
//											.filUuid(filUuid)
//											.build();
//		HentDokumentUrlInfoResponse hentDokumentUrlInfoResponse = new HentDokumentUrlInfoResponse(dokumentUrlInfo);
//		when(journalServiceMock.hentDokumentUrlInfo(isA(HentDokumentUrlInfoRequest.class))).thenReturn(
//				hentDokumentUrlInfoResponse);
//	}
//
//	private void setupOutputStreamMock() throws IOException {
//		ServletOutputStream servletOutputStreamMock = mock(ServletOutputStream.class);
//		when(httpServletResponseMock.getOutputStream()).thenReturn(servletOutputStreamMock);
//	}
//
//	private void setupServlet() {
//		hentDokumentServlet = new HentDokumentServlet();
//		journalServiceMock = mock(JournalServiceBi.class);
////		hentDokumentServlet.exceptionLogger = new DefaultExceptionLogger();
//		hentDokumentServlet.service = journalServiceMock;
//		hentDokumentServlet.transactionTemplate = new TransactionTemplateStub();
//		httpServletRequestMock = mock(HttpServletRequest.class);
//		httpServletResponseMock = mock(HttpServletResponse.class);
//	}
//
//}
