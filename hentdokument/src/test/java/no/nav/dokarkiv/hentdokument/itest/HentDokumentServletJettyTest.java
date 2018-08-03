//package no.nav.dokarkiv.hentdokument.itest;
//
//import static org.hamcrest.CoreMatchers.nullValue;
//import static org.hamcrest.Matchers.containsString;
//import static org.hamcrest.Matchers.is;
//import static org.hamcrest.Matchers.not;
//import static org.hamcrest.Matchers.notNullValue;
//import static org.hamcrest.Matchers.nullValue;
//import static org.hamcrest.core.Is.is;
//import static org.hamcrest.core.IsNot.not;
//import static org.hamcrest.core.IsNull.notNullValue;
//import static org.hamcrest.core.StringContains.containsString;
//import static org.junit.Assert.assertThat;
//import static org.mockito.Matchers.isA;
//import static org.mockito.Mockito.mock;
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
//import no.nav.dokarkiv.hentdokument.HentDokumentServletMock;
//import no.nav.domain.dok.joark.DokumentUrlInfo;
//import no.nav.domain.dok.joark.Journalpost;
//import no.nav.domain.dok.joark.builder.DokumentInfoBuilder;
//import no.nav.domain.dok.joark.builder.FilDetaljerBuilder;
//import no.nav.domain.dok.joark.builder.JournalpostBuilder;
//import no.nav.domain.dok.joark.builder.JournalpostDokumentInfoRelasjonBuilder;
//import no.nav.domain.dok.joark.codestable.FilTypeCode;
//import no.nav.repository.dok.joark.DokumentUrlNotFoundException;
//import no.nav.repository.dok.joark.MultipleDokumentUrlFoundException;
//import no.nav.service.dok.joark.journal.JournalServiceBi;
//import no.nav.service.dok.joark.journal.UrlNotValidException;
//import no.nav.service.dok.joark.journal.to.HentDokumentRequest;
//import no.nav.service.dok.joark.journal.to.HentDokumentResponse;
//import no.nav.service.dok.joark.journal.to.HentDokumentUrlInfoRequest;
//import no.nav.service.dok.joark.journal.to.HentDokumentUrlInfoResponse;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.mortbay.jetty.testing.ServletTester;
//
//import java.util.UUID;
//
///**
// * Integrationtest of the HentDokumentServlet. Uses Jetty to run the servlet in
// * a container.
// *
// * @author Magnus Skuland, Sirius IT
// */
//public class HentDokumentServletJettyTest {
//
//	private static ServletTester tester;
//	private static String baseUrl;
//	private String filUuid;
//	private final static String PATH = "/hentDokument";
//
//	public static JournalServiceBi journalServiceMock;
//
//	/**
//	 * Mocks out necessary attributes in the servlet. Kicks off an instance of
//	 * the Jetty servlet container so that it is possible to GET against it.
//	 * Registers the HentDokumentServletMock servlet.
//	 */
//	@BeforeClass
//	public static void beforeClass() throws Exception {
//		journalServiceMock = mock(JournalServiceBi.class);
//		tester = new ServletTester();
//		tester.setContextPath("/");
//		tester.addServlet(HentDokumentServletMock.class, PATH);
//		baseUrl = tester.createSocketConnector(true);
//		tester.start();
//	}
//
//	@Before
//	public void before() throws Exception {
//		filUuid = UUID.randomUUID().toString();
//	}
//
//	/**
//	 * Stops the Jetty container.
//	 */
//	@AfterClass
//	public static void afterClass() throws Exception {
//		tester.stop();
//	}
//
//	/**
//	 * Testing happy scenario. The servlet invokes underlying services and
//	 * writes a byte array of length 2 to the response.
//	 */
//	@Test
//	public void executeServlet() throws Exception {
//		String docToken = "78564";
//
//		DokumentUrlInfo dokUrlInfo = new DokumentUrlInfo();
//		dokUrlInfo.setDocToken(docToken);
//		Journalpost journalpost = Journalpost.builder()
//		.dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjon.builder()
//				.dokumentInfo(DokumentInfo.builder()
//						.fildetaljerListe(Sets.newHashSet(FilDetaljer.builder()
//								.filtype(FilTypeCode.PDF)
//								.filUuid(filUuid)
//								.build()))
//						.build())
//				.build())
//		.build();
//
//		dokUrlInfo.setJournalpost(journalpost);
//		dokUrlInfo.setFilUuid(filUuid);
//		HentDokumentUrlInfoResponse response = new HentDokumentUrlInfoResponse(dokUrlInfo);
//
//		when(journalServiceMock.hentDokumentUrlInfo(isA(HentDokumentUrlInfoRequest.class))).thenReturn(response);
//
//		byte[] dokument = new byte[] { new Byte("1"), new Byte("2") };
//		HentDokumentResponse hentDokResponse = new HentDokumentResponse(dokument);
//		when(journalServiceMock.hentDokument(isA(HentDokumentRequest.class))).thenReturn(hentDokResponse);
//
//		String[] servletResponse = sendHttpGet(baseUrl + PATH, "docToken=" + docToken);
//		assertThat(servletResponse, notNullValue());
//		assertThat(servletResponse[0].length(), is(2));
//		assertThat(servletResponse[1], is("application/pdf"));
//	}
//
//	/**
//	 * Testing a bad weather scenario where the service invocation
//	 * HentDokumentUrl fails with an UrlNotValidException.
//	 */
//	@Test
//	public void servletFailsWithUrlNotValidException() throws Exception {
//		String docToken = "78564";
//		Long dokumentUrlId = 33L;
//		DokumentUrlInfo dokUrl = new DokumentUrlInfo();
//		dokUrl.setDokumentUrlInfoId(dokumentUrlId);
//		testExceptionInHentDokumentUrl(dokumentUrlId, docToken, new UrlNotValidException(dokUrl),
//				"The time to live is exceeded for the DokumentUrlInfo with identifier: " + dokumentUrlId);
//	}
//
//	@Test
//	public void servletFailsWithDokumentUrlNotFoundException() throws Exception {
//		Long dokumentUrlId = 33L;
//		String docToken = "78678";
//		DokumentUrlInfo dokUrl = new DokumentUrlInfo();
//		dokUrl.setDokumentUrlInfoId(dokumentUrlId);
//		testExceptionInHentDokumentUrl(dokumentUrlId, docToken, new DokumentUrlNotFoundException(docToken),
//				"No DokumentUrl entry could be found for docToken: " + docToken);
//	}
//
//	@Test
//	public void servletFailsWithMultipleDokumentUrlFoundException() throws Exception {
//		Long dokumentUrlId = 31L;
//		String docToken = "78678";
//		DokumentUrlInfo dokUrl = new DokumentUrlInfo();
//		dokUrl.setDokumentUrlInfoId(dokumentUrlId);
//		testExceptionInHentDokumentUrl(dokumentUrlId, docToken, new MultipleDokumentUrlFoundException(2, docToken),
//				"2 DokumentUrl entries were found for docToken: " + docToken);
//	}
//
//	@Test
//	public void servletFailsWithNullPointerException() throws Exception {
//		Long dokumentUrlId = 32L;
//		String docToken = "78897";
//		DokumentUrlInfo dokUrl = new DokumentUrlInfo();
//		dokUrl.setDokumentUrlInfoId(dokumentUrlId);
//		testExceptionInHentDokumentUrl(dokumentUrlId, docToken, new NullPointerException(), "500");
//	}
//
//	private void testExceptionInHentDokumentUrl(Long dokumentUrlId, String docToken, Throwable t, String expectedErrMsg)
//			throws Exception {
//		when(journalServiceMock.hentDokumentUrlInfo(isA(HentDokumentUrlInfoRequest.class))).thenThrow(t);
//		String[] servletResponse = sendHttpGet(baseUrl + PATH, "docToken=" + docToken);
//		assertThat(servletResponse, not(nullValue()));
//		assertThat(servletResponse[0], containsString(expectedErrMsg));
//	}
//
//	/**
//	 * Testing servlet behaviour when doPost is invoked.
//	 */
//	@Test
//	public void invokeDoPost() throws Exception {
//		HttpClient client = new HttpClient();
//		PostMethod post = new PostMethod(baseUrl + PATH);
//
//		client.executeMethod(post);
//		String result = post.getResponseBodyAsString();
//		String expectedErrMsg = "doPost() is not implemented. Use doGet() instead.";
//		String expectedErrorStatus = "501";
//		assertThat(result, containsString(expectedErrMsg));
//		assertThat(result, containsString(expectedErrorStatus));
//
//		post.releaseConnection();
//	}
//
//	/**
//	 * Util method that invokes a GET request on the specified url.
//	 */
//	private String[] sendHttpGet(String url, String queryString) throws Exception {
//		HttpClient client = new HttpClient();
//		GetMethod get = new GetMethod(url);
//		get.setQueryString(queryString);
//		client.executeMethod(get);
//
//		String[] result = new String[2];
//		result[0] =	get.getResponseBodyAsString();
//		result[1] =	get.getResponseHeader("Content-Type").getValue();
//		get.releaseConnection();
//		return result;
//	}
//
//}
