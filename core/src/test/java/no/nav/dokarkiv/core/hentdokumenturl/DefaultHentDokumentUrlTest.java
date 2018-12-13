package no.nav.dokarkiv.core.hentdokumenturl;

import static no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode.SYFO;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.dokumenturl.DefaultHentDokumentUrl;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlConstants;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlRequest;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder;
import no.nav.dokarkiv.core.domain.builder.ReturInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentUrlInfoRepositoryBegrenset;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URLDecoder;
import java.util.Optional;

/**
 * Unit tests for HentDokumentUrlServiceTest.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 * @author Magnus Skuland, Sirius IT
 * @author Thao Thanh Nguyen, Visma Sirius
 */
public class DefaultHentDokumentUrlTest {

	private static final String SERVLET_URL = "https://10.33.1.62/joark/HentDokument";
	private static final Long JOURNALPOST_ID = 1L;
	private static final String FIL_UUID = "456b166e-5f9f-430f-8e35-09a732156562";
	private static final String FIL_UUID_SLADDET = "456b166e-5f9f-430f-8e35-09a732156563";

	@Mock
	private JoarkRepositoryBegrenset joarkRepositoryMock;
	@Mock
	private DokumentFilRepository dokumentFilRepositoryMock;
	@Mock
	private DokumentUrlInfoRepositoryBegrenset dokumentUrlInfoRepositoryMock;
	@Mock
	private BegrensningService begrensningService;
	@Captor
	ArgumentCaptor<DokumentUrlInfo> dokumentUrlInfoCaptor;

	private DefaultHentDokumentUrl hentDokumentUrl;
	
	private HentDokumentUrlRequest request;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		hentDokumentUrl = new DefaultHentDokumentUrl();
		hentDokumentUrl.setJoarkRepository(joarkRepositoryMock);
		hentDokumentUrl.setDokumentFilRepository(dokumentFilRepositoryMock);
		hentDokumentUrl.setServletUrl(SERVLET_URL);
		hentDokumentUrl.setDokumentUrlInfoRepository(dokumentUrlInfoRepositoryMock);
		hentDokumentUrl.setBegrensningService(begrensningService);
		request = new HentDokumentUrlRequest(JOURNALPOST_ID, FIL_UUID);
	}

	@Test
	public void validationShouldFailWhenRequestIsNull() throws Exception {
		try {
			hentDokumentUrl.hentDokumentUrl(null);
			fail("Validation should fail when request is null");
		}
		catch(InvalidArgumentException e) {
			assertThat(e.getMessage(), is("HentDokumentUrlRequest is null"));
		}
	}

	@Test
	public void validationShouldFailForMissingFilUuid() throws Exception {
		request = new HentDokumentUrlRequest(1L, null);
		assertValidationFailsForArgument("FilUuid");
	}

	/**
	 * Tests the case where the hentDokumentUrl is invoked with a journalpostId that
	 * does not exist.
	 */
	@Test
	public void shouldThrowExceptionForNonExistingJournalpost() throws Exception {
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.empty());
		try {
			hentDokumentUrl.hentDokumentUrl(request);
			fail();
		} catch (NoJournalpostFoundException e) {
			assertThat(e.getMessage(), containsString("Journalpost med id " + request.getJournalpostId() + " eksisterer ikke"));
		}
	}
	
	/**
	 * Happy scenario for a OnDemand document.
	 */
	@Test
	public void shouldGetDokumentUrlForOnDemand() throws Exception {
		Journalpost journalpost = createJournalPost("10", SYFO, FIL_UUID, FIL_UUID_SLADDET);
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		HentDokumentUrlResponse response = hentDokumentUrl.hentDokumentUrl(request);
		String servletUrl = response.getDokumentUrl();

		assertUrl(servletUrl);

		verify(dokumentUrlInfoRepositoryMock).save(isA(DokumentUrlInfo.class));
	}

	/**
	 * Happy scenario for a DB document
	 */
	@Test
	public void shouldGetDokumentUrlForDokumentInDB() throws Exception {
		Journalpost journalpost = createJournalPost(null, null, FIL_UUID, FIL_UUID_SLADDET);
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		when(dokumentFilRepositoryMock.findByFilUuid(FIL_UUID)).thenReturn(new DokumentFil());
		
		HentDokumentUrlResponse response = hentDokumentUrl.hentDokumentUrl(request);
		String servletUrl = response.getDokumentUrl();
		
		assertUrl(servletUrl);
		
		verify(dokumentUrlInfoRepositoryMock).save(isA(DokumentUrlInfo.class));
	}
	
	@Test
	public void shouldCreateDokumentUrlInfoWithCustomTimeToLive() throws Exception {
		long timeToLive = 60;
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(
				createJournalPost(null, null,  FIL_UUID, FIL_UUID_SLADDET)));
		when(dokumentFilRepositoryMock.findByFilUuid(FIL_UUID)).thenReturn(new DokumentFil());
		
		request = new HentDokumentUrlRequest(JOURNALPOST_ID, FIL_UUID, timeToLive);
		hentDokumentUrl.hentDokumentUrl(request);
		
		verify(dokumentUrlInfoRepositoryMock).save(dokumentUrlInfoCaptor.capture());
		
		DokumentUrlInfo dokumentUrlInfo = dokumentUrlInfoCaptor.getValue();
		assertThat(dokumentUrlInfo.getTimeToLiveMinutes(), is(timeToLive ));
	}
	
	@Test
	public void shouldThrowExceptionForMissingFilDetaljer() throws Exception {
		Journalpost journalpost = createJournalPost(null, null, "562b166e-5f9f", FIL_UUID_SLADDET);
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		assertInvalidFilUuidExceptionThrown(FIL_UUID);	
	}
	
	@Test
	public void shouldThrowExceptionForMissingDokumentFil() throws Exception {
		Journalpost journalpost = createJournalPost(null, null, FIL_UUID, FIL_UUID_SLADDET);
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		when(dokumentFilRepositoryMock.findByFilUuid(FIL_UUID)).thenReturn(null);
		
		assertInvalidFilUuidExceptionThrown(FIL_UUID);	
	}
	
	@Test
	public void shouldSetCorrectMimeTypeForDlf() throws Exception {
		Journalpost journalpost = JournalpostBuilder.getJournalpostBuilder()
									.journalpostId(JOURNALPOST_ID)
									.journalStatus(JournalStatusCode.D)
									.fagomrade(FagomradeCode.DAG)
									.dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder
										.getJournalpostDokumentInfoRelasjonBuilder()
											.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
													.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
															.filUuid(FIL_UUID)
															.filtype(FilTypeCode.DLF)
															.build())
													.build())
											.build())
									.build();
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(dokumentFilRepositoryMock.findByFilUuid(FIL_UUID)).thenReturn(new DokumentFil());
		
		HentDokumentUrlResponse response = hentDokumentUrl.hentDokumentUrl(request);
		String servletUrl = response.getDokumentUrl();
		
		assertThat(URLDecoder.decode(servletUrl, "UTF-8"), containsString("application/dlf"));
	}

	private void assertInvalidFilUuidExceptionThrown(String filUuid) throws NoJournalpostFoundException {
		try {
			hentDokumentUrl.hentDokumentUrl(request);
		} catch (InvalidFilUuidException e) {
			assertThat(e.getFilUuid(), is(filUuid));
		}
	}

	private void assertUrl(String servletUrl) {
		assertThat(servletUrl, containsString(SERVLET_URL));
		assertThat(servletUrl, containsString("?" + HentDokumentUrlConstants.HENT_DOKUMENT_SERVLET_PARAM + "="));
		assertThat(servletUrl, containsString("&mimetype="));
	}

	private Journalpost createJournalPost(String onDemandId, OnDemandInstansCode onDemandInstans, String filUuid, String filUuidSladdet) {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostId(1L)
				.journalStatus(JournalStatusCode.J)
				.fagomrade(FagomradeCode.PEN)
				.brukere(BrukerBuilder.getBrukerBuilder().build())
				.kryssReferanser(KryssreferanseBuilder.getKryssreferanseBuilder().build())
				.returInfos(ReturInfoBuilder.getReturInfoBuilder().build())
				.saksrelasjon(SaksrelasjonBuilder.getSaksrelasjonBuilder().build())
				.dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder
					.getJournalpostDokumentInfoRelasjonBuilder()
						.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
								.dokumentInfoId(1L)
								.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
										.filUuid(filUuid)
										.filtype(FilTypeCode.PDF)
										.variantFormat(VariantFormatCode.ARKIV)
										.onDemandId(onDemandId)
										.onDemandInstans(onDemandInstans).build(),
										FilDetaljerBuilder.getFilDetaljerBuilder()
												.filUuid(filUuidSladdet)
												.filtype(FilTypeCode.PDF)
												.variantFormat(VariantFormatCode.SLADDET)
												.onDemandId(onDemandId)
												.onDemandInstans(onDemandInstans).build())
								.build())
						.build())
				.build();
	}
	
	private void assertValidationFailsForArgument(String expectedArgumentName) throws Exception {
		try {
			hentDokumentUrl.hentDokumentUrl(request);
			fail("Validation should fail for argument "+expectedArgumentName);
		}
		catch(InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString("Missing parameter"));
		}
	}
}
