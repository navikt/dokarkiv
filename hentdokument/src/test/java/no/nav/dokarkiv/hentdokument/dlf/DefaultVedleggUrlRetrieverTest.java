package no.nav.dokarkiv.hentdokument.dlf;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for DefaultVedleggUrlRetriever.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class DefaultVedleggUrlRetrieverTest {

	private static final String JOURNALPOST_ID_VEDLEGG = "100";
	private static final String FIL_UUID_VEDLEGG = "123-2345-678234-678567";
	
	private static final Long URL_TIME_TO_LIVE = 480L;
	private static final Boolean NON_SSL_URL = true;
	
	@Mock
	private HentDokumentUrl hentDokumentUrlMock;
	@Captor
	private ArgumentCaptor<HentDokumentUrlRequest> requestCaptor;
	
	private DefaultVedleggUrlRetriever vedleggUrlRetriever;
	public static final String DOKUMENT_URL = "http://wasapp.adeo.no/joarkweb/HentDokument?docToken=***gammelt_fnr***";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		vedleggUrlRetriever = new DefaultVedleggUrlRetriever(hentDokumentUrlMock, URL_TIME_TO_LIVE, NON_SSL_URL);
	}
	
	@Test
	public void shouldWrapNoJournalpostFoundException() throws Exception {
		Throwable noJournalpostFoundException = new NoJournalpostFoundException(null, null);
		when(hentDokumentUrlMock.hentDokumentUrlJoark(isA(HentDokumentUrlRequest.class))).thenThrow(noJournalpostFoundException );
		
		try {
			vedleggUrlRetriever.retrieveVedleggUrl(JOURNALPOST_ID_VEDLEGG, FIL_UUID_VEDLEGG);
			fail("Expected exception");
		} catch (InvalidArgumentException e) {
			assertThat(e.getCause(), is(noJournalpostFoundException));
		}
	}
	
	@Test
	public void shouldWrapInvalidFilUuidException() throws Exception {
		Throwable invalidFilUuidException = new InvalidFilUuidException(null, null);
		when(hentDokumentUrlMock.hentDokumentUrlJoark(isA(HentDokumentUrlRequest.class))).thenThrow(invalidFilUuidException );
		
		try {
			vedleggUrlRetriever.retrieveVedleggUrl(JOURNALPOST_ID_VEDLEGG, FIL_UUID_VEDLEGG);
			fail("Expected exception");
		} catch (InvalidArgumentException e) {
			assertThat(e.getCause(), is(invalidFilUuidException));
		}
	}
	
	@Test
	public void shouldCallHentDokumentUrl() throws Exception {
		when(hentDokumentUrlMock.hentDokumentUrlJoark(isA(HentDokumentUrlRequest.class))).thenReturn(
				new HentDokumentUrlResponse(DOKUMENT_URL));
		
		vedleggUrlRetriever.retrieveVedleggUrl(JOURNALPOST_ID_VEDLEGG, FIL_UUID_VEDLEGG);
		
		verify(hentDokumentUrlMock).hentDokumentUrlJoark(requestCaptor.capture());
		HentDokumentUrlRequest request = requestCaptor.getValue();
		assertThat(request.getJournalpostId().toString(), is(JOURNALPOST_ID_VEDLEGG));
		assertThat(request.getFilUuid(), is(FIL_UUID_VEDLEGG));
		assertThat(request.getTimeToLiveMinutes(), is(URL_TIME_TO_LIVE));
	}
	
	@Test
	public void shouldReturnUrl() throws Exception {
		when(hentDokumentUrlMock.hentDokumentUrlJoark(isA(HentDokumentUrlRequest.class))).thenReturn(
				new HentDokumentUrlResponse(DOKUMENT_URL));
		
		String vedleggUrl = vedleggUrlRetriever.retrieveVedleggUrl(JOURNALPOST_ID_VEDLEGG, FIL_UUID_VEDLEGG);
		
		assertThat(vedleggUrl, is(DOKUMENT_URL));
	}
}
