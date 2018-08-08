package no.nav.dokarkiv.core.hentdokument;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.dokument.DefaultHentDokument;
import no.nav.dokarkiv.core.dokument.HentDokumentRequest;
import no.nav.dokarkiv.core.dokument.HentDokumentResponse;
import no.nav.dokarkiv.core.dokumenturl.HentDokumentUrlResponse;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.exceptions.SettMetadataIDlfFailedException;
import no.nav.dokarkiv.core.journalbehandling.SettMetadataIDLF;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataIDLFRequest;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataIDLFResponse;
import no.nav.dokarkiv.core.ondemand.HentOndemandDokument;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.util.Optional;

/**
 * Unittests for DefaultHentDokument.
 *
 * @author Carl-Henrik Wolf Lund, Bekk Consulting
 * @author Lamisi Gurah Blackman, Accenture
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultHentDokumentTest {

	private static final Long JOURNALPOST_ID = 1L;
	private static final String JOARK_URL = "http://hentdokument";
	private static final byte[] BYTES = "fil".getBytes();
	public static final String DOCTOKEN = "doctoken";

	private DefaultHentDokument hentDokument;

	@Mock
	private JoarkRepository joarkRepositoryMock;
	@Mock
	private HentOndemandDokument hentOndemandDokument;
	@Mock
	private DokumentFilRepository dokumentFilRepositoryMock;
	@Mock
	private SettMetadataIDLF settMetadataIDLFMock;

	@Captor
	ArgumentCaptor<SettMetadataIDLFRequest> settMetadataIDLFRequestCaptor;

	private HentDokumentRequest request;
	private static final byte[] dokument = "dokument".getBytes();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		hentDokument = new DefaultHentDokument();
		hentDokument.setJoarkRepository(joarkRepositoryMock);
		hentDokument.setHentOndemandDokument(hentOndemandDokument);
		hentDokument.setDokumentFilRepository(dokumentFilRepositoryMock);
		hentDokument.setSettMetadataIDLF(settMetadataIDLFMock);

		ReflectionTestUtils.setField(hentDokument, "joarkUrl", JOARK_URL);
	}

	@Test
	public void shouldGetDokumentFromOnDemand() throws Exception {
		String filUuid = "355b166e-5f9f-430f-8e35-09a732156775";
		Journalpost journalpost = createJournalpost(JournalStatusCode.J);

		FilDetaljer fildetaljer = journalpost.findFilDetaljerByFilUuid(filUuid);

		fildetaljer.setOnDemandId("10");
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(hentOndemandDokument.hentOndemandDokumentFromJoark(anyString())).thenReturn(BYTES);

		HentDokumentRequest request = new HentDokumentRequest(JOURNALPOST_ID, filUuid, DOCTOKEN);

		assertServiceReturnsDocument(request, BYTES);
	}

	@Test
	public void shouldGetDokumentFromDatabase() throws Exception {
		String filUuid = "355b166e-5f9f-430f-8e35-09a732156775"; // same as in createFildetaljer
		Journalpost journalpost = createJournalpost(JournalStatusCode.J);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(dokumentFilRepositoryMock.findByFilUuid(filUuid)).thenReturn(
				getDokumentFilBuilder().fil(dokument).build());

		HentDokumentRequest request = new HentDokumentRequest(JOURNALPOST_ID, filUuid);

		assertServiceReturnsDocument(request, dokument);
	}

	/**
	 * Testbetingelse 9 Mangler JournalpostID
	 */
	@Test
	public void validationShouldFailForMissingJournalpostId() throws Exception {
		request = new HentDokumentRequest(null, FilDetaljer.generateUuid());
		assertValidationFailsForParameter("JournalpostId");
	}

	/**
	 * Testbetingelse 10 JournalpostID ikke finnes
	 */
	@Test
	public void validationShouldFailIfJournalpostIdDoesNotExist() throws Exception {
		try {
			request = new HentDokumentRequest(0123L, FilDetaljer.generateUuid());
			assertValidationFailsForParameter("JournalpostId");
		} catch (NoJournalpostFoundException e) {
			assertThat(e.getMessage(), containsString("eksisterer ikke"));
		}
	}

	/**
	 * Testbetingelse 16 Mangler FilUuId
	 */
	@Test
	public void validationShouldFailForMissingFilUuId() throws Exception {
		request = new HentDokumentRequest(1L, null);
		assertValidationFailsForParameter("filUuid");
	}

	@Test
	public void validationShouldFailForBlankFilUuId() throws Exception {
		request = new HentDokumentRequest(1L, "");
		assertValidationFailsForParameter("filUuid");
	}

	/**
	 * Testbetingelse 17 filUuid ikke finnes på angitt journalpost
	 */
	@Test
	public void shouldThrowExceptionIfFilrelasjonDoesNotExist() throws Exception {
		Journalpost journalpost = createJournalpost(JournalStatusCode.J);
		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		assertFilUuIDNotFoundInJournalpost(journalpost.getJournalpostId());
	}

	/**
	 * Testbetingelse 18 ikke finnes noe dokument på angitt filUuid
	 */
	@Test
	public void shouldThrowExceptionIfDokumentDoesNotExist() throws Exception {
		String filUuid = FilDetaljer.generateUuid();

		Journalpost journalpost = createJournalpost(JournalStatusCode.J);

		when(joarkRepositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(dokumentFilRepositoryMock.findByFilUuid(filUuid)).thenReturn(null);
		assertDocumentVersionNotFound(journalpost.getJournalpostId());
	}


	@Test
	public void shouldUpdateDlfByCallingUpdateOperation() throws Exception {
		Long journalpostId = 345L;
		String filUuid = FilDetaljer.generateUuid();
		Long versjon = 2L;

		Journalpost journalpost = createJournalpostWithDlfFilDetaljer(journalpostId, filUuid);
		DokumentFil dokumentFil = createDokumentFil(filUuid, versjon);

		when(joarkRepositoryMock.findById(journalpostId)).thenReturn(Optional.of(journalpost));
		when(dokumentFilRepositoryMock.findByFilUuid(filUuid)).thenReturn(dokumentFil);

		when(settMetadataIDLFMock.settMetadataIDLF(isA(SettMetadataIDLFRequest.class))).thenReturn(
				new SettMetadataIDLFResponse("Test".getBytes()));

		hentDokument.hentDokument(new HentDokumentRequest(journalpostId, filUuid));

		verifySettMetadataIDlfCalledWithCorrectValues(journalpostId, filUuid, versjon);
	}

	@Test
	public void shouldReturnUpdatedDlf() throws Exception {
		byte[] dlfDocument = "DLF".getBytes();
		Long journalpostId = 345L;
		String filUuid = FilDetaljer.generateUuid();

		Journalpost journalpost = createJournalpostWithDlfFilDetaljer(journalpostId, filUuid);
		DokumentFil dokumentFil = createDokumentFil(filUuid, 1L);

		when(joarkRepositoryMock.findById(journalpostId)).thenReturn(Optional.of(journalpost));
		when(dokumentFilRepositoryMock.findByFilUuid(filUuid)).thenReturn(dokumentFil);

		when(settMetadataIDLFMock.settMetadataIDLF(isA(SettMetadataIDLFRequest.class))).thenReturn(
				new SettMetadataIDLFResponse(dlfDocument));

		HentDokumentResponse response = hentDokument.hentDokument(new HentDokumentRequest(journalpostId, filUuid));

		assertThat(response.getDokument(), is(dlfDocument));
	}

	@Test
	public void shouldWrapExceptionsFromSettMetadataIDlf() throws Exception {
		Long journalpostId = 345L;
		String filUuid = FilDetaljer.generateUuid();

		Journalpost journalpost = createJournalpostWithDlfFilDetaljer(journalpostId, filUuid);
		DokumentFil dokumentFil = createDokumentFil(filUuid, 1L);

		when(joarkRepositoryMock.findById(journalpostId)).thenReturn(Optional.of(journalpost));
		when(dokumentFilRepositoryMock.findByFilUuid(filUuid)).thenReturn(dokumentFil);
		Throwable dlfException = new RuntimeException();
		when(settMetadataIDLFMock.settMetadataIDLF(isA(SettMetadataIDLFRequest.class))).thenThrow(dlfException);

		try {
			hentDokument.hentDokument(new HentDokumentRequest(journalpostId, filUuid));
			fail("Expected exception");
		} catch (SettMetadataIDlfFailedException e) {
			assertThat(e.getCause(), is(dlfException));
		}
	}

	private Journalpost createJournalpostWithDlfFilDetaljer(Long journalpostId, String filUuid) {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalpostId(journalpostId)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(
								getDokumentInfoBuilder().filDetaljerList(
										getFilDetaljerBuilder().filUuid(filUuid)
												.variantFormat(VariantFormatCode.PRODUKSJON_DLF).build()).build()).build())
				.build();
	}

	private DokumentFil createDokumentFil(String filUuid, Long versjon) {
		return getDokumentFilBuilder().fil(dokument).filUuid(filUuid).versjon(versjon).build();
	}

	private void verifySettMetadataIDlfCalledWithCorrectValues(Long journalpostId, String filUuid, Long versjon) {
		verify(settMetadataIDLFMock).settMetadataIDLF(settMetadataIDLFRequestCaptor.capture());
		SettMetadataIDLFRequest settMetadataIDLFRequest = settMetadataIDLFRequestCaptor.getValue();
		assertThat(settMetadataIDLFRequest.getSettMetadataForUthenting().getJournalpostId(), is(journalpostId));
		assertThat(settMetadataIDLFRequest.getSettMetadataForUthenting().getFilUuid(), is(filUuid));
		assertThat(settMetadataIDLFRequest.getSettMetadataForUthenting().getVersjon(), is(versjon));
		assertThat(settMetadataIDLFRequest.getDlfDokument(), is(dokument));
	}

	private void assertServiceReturnsDocument(HentDokumentRequest request, byte[] doc) throws Exception {
		HentDokumentResponse response = hentDokument.hentDokument(request);
		assertThat(doc, is(equalTo(response.getDokument())));
	}

	private void assertValidationFailsForParameter(String parameterName) throws Exception {
		try {
			hentDokument.hentDokument(request);
			fail("Validation should fail for parameter " + parameterName);
		} catch (InvalidArgumentException e) {
			assertEquals(parameterName, e.getArgumentName());
		}
	}

	private void assertFilUuIDNotFoundInJournalpost(Long journalpostId) throws Exception {
		String filUuid = FilDetaljer.generateUuid();

		request = new HentDokumentRequest(journalpostId, filUuid);
		try {
			hentDokument.hentDokument(request);
			fail("Should throw InvalidFilUuidException");
		} catch (InvalidFilUuidException e) {
			assertThat(e.getFilUuid(), is(filUuid));
		}
	}

	private void assertDocumentVersionNotFound(Long journalpostId) throws Exception {
		String filUuid = FilDetaljer.generateUuid();

		request = new HentDokumentRequest(journalpostId, filUuid);
		try {
			hentDokument.hentDokument(request);
			fail("Should throw InvalidFilUuidException");
		} catch (InvalidFilUuidException e) {
			assertThat(e.getFilUuid(), is(filUuid));
		}
	}

	private Journalpost createJournalpost(JournalStatusCode statusCode) {
		return JournalpostBuilder
				.getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.fagomrade(FagomradeCode.PEN)
				.journalStatus(statusCode)
				.journalpostType(JournalpostTypeCode.I)
				.opprettetKildeNavn("Kildenavn")
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
								.journalpostDokumentInfoRelasjonId(JOURNALPOST_ID).dokumentInfo(createDokumentInfoWithSkannetInnhold())
								.build()).build();
	}

	private DokumentInfo createDokumentInfoWithSkannetInnhold() {
		return DokumentInfoBuilder
				.getDokumentInfoBuilder()
				.dokumentInfoId(1L)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.endretAvNavn("PEN")
				.brevgruppe("Brevgruppe")
				.brevkode("Brevkode")
				.filDetaljerList(createFildetaljer(), createFildetaljer("355b166e-5f9f-430f-8e35-09a732156775"),
						createFildetaljer())
				.skannetInnhold(SkannetInnholdBuilder.getSkannetInnholdBuilder().opprettetKildeNavn("NAV").build())
				.opprettetKildeNavn("NAV").build();
	}

	private FilDetaljer createFildetaljer() {
		return createFildetaljer(FilDetaljer.generateUuid());
	}

	private FilDetaljer createFildetaljer(String filUuid) {
		return FilDetaljerBuilder.getFilDetaljerBuilder()
				.filUuid(filUuid)
				.filnavn("filNavn")
				.filtype(FilTypeCode.AFP)
				.onDemandInstans(OnDemandInstansCode.PESYS)
				.variantFormat(VariantFormatCode.ARKIV)
				.opprettetKildeNavn("NAV")
				.build();
	}

}
