//package no.nav.dokarkiv.hentdokument;
//
//
//import no.nav.dokarkiv.hentdokument.itest.HentDokumentServletJettyTest;
//
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
//
///**
// * Subclass of the servlet used for testing purposes. Mocks out underlying
// * service invocations with Mockito.
// *
// * @author Magnus Skuland, Sirius IT
// */
//public class HentDokumentServletMock extends HentDokumentServlet {
//
//	/** Default serialization ID. */
//	private static final long serialVersionUID = 1L;
//
//	/** {@inheritDoc} */
//	@Override
//	public void init(ServletConfig config) throws ServletException {
//		service = HentDokumentServletJettyTest.journalServiceMock;
////		exceptionLogger = new DefaultExceptionLogger();
//		transactionTemplate = new TransactionTemplateStub();
//	}
//
//}
