package no.nav.dokarkiv.journal.v3.tjoark050;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.dokumenturl.DefaultHentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * Unit tests for DefaultHentDokumentUrlService.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultHentDokumentUrlServiceTest {

	private static final long JOURNALPOST_ID = 1L;
	private static final long DOKUMENT_INFO_ID = 1L;
	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private static final String FIL_UUID = "456b166e-5f9f-430f-8e35-09a732156562";
	private static final VariantFormatCode VARIANT_FORMAT_SLADDET = VariantFormatCode.SLADDET;
	private static final String FIL_UUID_SLADDET = "456b166e-5f9f-430f-8e35-09a732156563";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	HentDokumentUrlRequestTo hentDokumentUrlRequest = new HentDokumentUrlRequestTo(
			JOURNALPOST_ID, DOKUMENT_INFO_ID, VARIANT_FORMAT);
	@Mock
	private DefaultHentDokumentUrl hentDokumentUrlMock;
	@Mock
	private JoarkRepositorySkjermet joarkRepositoryMock;

	@Captor
	private ArgumentCaptor<HentDokumentUrlRequest> delegateRequestCaptor;

	@InjectMocks
	private DefaultHentDokumentUrlService hentDokumentUrlService;

	@Test
	public void shouldFailValidationWhenRequestIsNull() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("HentDokumentUrlRequest is null");

		hentDokumentUrlService.hentDokumentUrl(null);
	}

	@Test
	public void shouldFailValidationWhenJournalpostIdIsNull() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Missing parameter journalpostId");

		hentDokumentUrlService.hentDokumentUrl(new HentDokumentUrlRequestTo(null, DOKUMENT_INFO_ID, VARIANT_FORMAT));
	}

	@Test
	public void shouldFailValidationWhenDokumentInfoIdIsNull() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Missing parameter dokumentInfoId");

		hentDokumentUrlService.hentDokumentUrl(new HentDokumentUrlRequestTo(JOURNALPOST_ID, null, VARIANT_FORMAT));
	}

	@Test
	public void shouldFailValidationWhenVariantFormatIsNull() throws Exception {
		expectedException.expect(InvalidArgumentException.class);
		expectedException.expectMessage("Missing parameter variantFormat");

		hentDokumentUrlService.hentDokumentUrl(new HentDokumentUrlRequestTo(JOURNALPOST_ID, DOKUMENT_INFO_ID, null));
	}

	@Test
	public void shouldThrowExceptionForJournalpostNotFound() throws Exception {
		try {
			hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest);
			fail("Expected exception");
		} catch (DocumentNotFoundException e) {
			assertThat(e.getCause(), is(instanceOf(NoJournalpostFoundException.class)));
		}
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() throws Exception {
		Journalpost journalpost = createJournalPost();
		DokumentInfo dokumentInfo = new DokumentInfo(10L, 0);
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(dokumentInfo);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		try {
			hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest);
			fail("Expected exception");
		} catch (DocumentNotFoundException e) {
			assertThat(e.getCause(), is(instanceOf(NoDokumentInfoFoundException.class)));
		}
	}

	@Test
	public void shouldThrowExceptionWhenFilDetaljerWithGivenVariantNotFound() throws Exception {
		Journalpost journalpost = createJournalPost();
		journalpost.findFilDetaljerByFilUuid(FIL_UUID).setVariantFormat(VariantFormatCode.PRODUKSJON);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		try {
			hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest);
			fail("Expected exception");
		} catch (DocumentNotFoundException e) {
			assertThat(e.getCause(), is(instanceOf(InvalidArgumentException.class)));
		}
	}

	@Test
	public void shouldThrowExceptionWhenDelegateThrowsNoJournalpostFound() throws Exception {
		expectedException.expect(DocumentNotFoundException.class);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));

		hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest);
	}

	@Test
	public void shouldThrowExceptionWhenDelegateThrowsInvalidFilUuid() throws Exception {
		expectedException.expect(DocumentNotFoundException.class);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));

		hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest);
	}

	@Test
	public void shouldCallDelegateWithCorrectValues() throws Exception {
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));
		when(hentDokumentUrlMock.hentDokumentUrl(isA(HentDokumentUrlRequest.class))).thenReturn(
				new HentDokumentUrlResponse("Test"));
		hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest);

		verify(hentDokumentUrlMock).hentDokumentUrl(delegateRequestCaptor.capture());

		HentDokumentUrlRequest delegateRequest = delegateRequestCaptor.getValue();
		assertThat(delegateRequest.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(delegateRequest.getFilUuid(), is(FIL_UUID));
	}

	@Test
	public void shouldCallDelegateWithCorrectValuesSkjermet() throws Exception {
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalPost()));
		when(hentDokumentUrlMock.hentDokumentUrl(isA(HentDokumentUrlRequest.class))).thenReturn(
				new HentDokumentUrlResponse("Test"));
		hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest);

		verify(hentDokumentUrlMock).hentDokumentUrl(delegateRequestCaptor.capture());

		HentDokumentUrlRequest delegateRequest = delegateRequestCaptor.getValue();
		assertThat(delegateRequest.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(delegateRequest.getFilUuid(), is(FIL_UUID_SLADDET));
	}


	@Test
	public void shouldReturnDokumentUrl() throws Exception {
		String dokumentUrl = "nav.no/joark/dokument123";
		Journalpost journalpost = createJournalPost();
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(hentDokumentUrlMock.hentDokumentUrl(isA(HentDokumentUrlRequest.class))).thenReturn(
				new HentDokumentUrlResponse(dokumentUrl));

		HentDokumentUrlResponseTo response = hentDokumentUrlService.hentDokumentUrl(hentDokumentUrlRequest);

		assertThat(response.getDokumentUrl(), is(dokumentUrl));
	}

	private Journalpost createJournalPost() {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder
						.getJournalpostDokumentInfoRelasjonBuilder()
						.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
								.dokumentInfoId(DOKUMENT_INFO_ID)
								.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
												.filUuid(FIL_UUID)
												.variantFormat(VARIANT_FORMAT).build(),
										FilDetaljerBuilder.getFilDetaljerBuilder()
												.filUuid(FIL_UUID_SLADDET)
												.variantFormat(VARIANT_FORMAT_SLADDET)
												.build())
								.build())
						.build())
				.build();
	}
}
