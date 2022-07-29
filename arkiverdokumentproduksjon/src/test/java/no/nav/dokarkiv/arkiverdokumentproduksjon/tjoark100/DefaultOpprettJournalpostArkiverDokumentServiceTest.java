package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;


/**
 * Tests for {@link DefaultOpprettJournalpostArkiverDokumentService}
 *
 * @author Stig Strøm
 */
@ExtendWith(MockitoExtension.class)
public class DefaultOpprettJournalpostArkiverDokumentServiceTest {
	private static final Long JOURNALPOST_ID = 1L;
	private static final String TODAY_DATE = "2018-06-20T14:31:54.767";
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";
	private static final boolean FERDIGSTILL_JOURNALPOST = true;
	private static final boolean IKKE_FERDIGSTILL_JOURNALPOST = false;
	
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
    private JoarkRepositorySkjermet joarkRepositoryMock;
	@Mock
	private OpprettJournalpostArkiverDokumentValidator opprettJournalpostArkiverDokumentValidator;
	@InjectMocks
	private DefaultOpprettJournalpostArkiverDokumentService service;
	private OpprettJournalpostArkiverDokumentRequestTo requestFerdigstillJournalpost;
	private OpprettJournalpostArkiverDokumentRequestTo requestIkkeFerdigstillJournalpost;
	private OpprettJournalpostArkiverDokumentResponseTo response;
	private Journalpost journalpost;

	@BeforeEach
	public void setUp() {
		DateProvider.configure(true, TODAY_DATE);
		journalpost = createJournalpost().build();
		requestFerdigstillJournalpost = new OpprettJournalpostArkiverDokumentRequestTo(journalpost, FERDIGSTILL_JOURNALPOST);
		requestIkkeFerdigstillJournalpost = new OpprettJournalpostArkiverDokumentRequestTo(journalpost, IKKE_FERDIGSTILL_JOURNALPOST);
		lenient().when(joarkRepositoryMock.save(journalpost)).thenReturn(createdJournalpost());
	}
	
	@Test
	public void shouldValidateAndPersistJournalpostCorrectly() {
		response = service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		verify(opprettJournalpostArkiverDokumentValidator).validate(journalpost, FERDIGSTILL_JOURNALPOST);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verify(joarkRepositoryMock).save(journalpost);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
	}
	
	@Test
	public void shouldThrowExceptionWhenRequestIsNull() {
		assertThrows(ApplicationException.class,
				() -> service.opprettJournalpostArkiverDokument(null),
				"Missing parameter: request");
	}
	
	@Test
	public void shouldSetJournalpostTypeToUtgaaende() {
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}
	
	@Test
	public void shouldSetJournalstatusToDokumentEkspedert() {
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FS));
	}
	
	@Test
	public void shouldSetJournalstatusToDokumentEkspedertLokalprint() {
		requestFerdigstillJournalpost.getJournalpost().setUtsendingskanal(UtsendingsKanalCode.L);
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FL));
	}
	
	@Test
	public void shouldSetJournalstatusToDokumentUnderProduksjon() {
		service.opprettJournalpostArkiverDokument(requestIkkeFerdigstillJournalpost);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.D));
	}
	
	@Test
	public void shouldSetJournalDatoToToday() {
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.getJournalDato(), is(DateProvider.getToday()));
	}
	
	@Test
	public void shouldNotSetJournalDato() {
		service.opprettJournalpostArkiverDokument(requestIkkeFerdigstillJournalpost);
		assertThat(journalpost.getJournalDato(), is(nullValue()));
	}
	
	@Test
	public void shouldSetJournalFortAvNavn() {
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.getJournalfortAvNavn(), is(OPPRETTET_AV_NAVN));
	}
	
	@Test
	public void shouldNotSetJournalFortAvNavn() {
		service.opprettJournalpostArkiverDokument(requestIkkeFerdigstillJournalpost);
		assertThat(journalpost.getJournalfortAvNavn(), is(nullValue()));
	}
	
	@Test
	public void shouldSetUtsendingskanal() {
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.getUtsendingskanal(), is(UtsendingsKanalCode.EESSI));
	}
	
	@Test
	public void shouldNotSetUtsendingskanal() {
		service.opprettJournalpostArkiverDokument(requestIkkeFerdigstillJournalpost);
		assertThat(journalpost.getUtsendingskanal(), is(nullValue()));
	}
	
	@Test
	public void shouldSetTilknyttetAvNavn() {
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), Matchers.is(1));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getTilknyttetAvNavn(),
				Matchers.is(OPPRETTET_AV_NAVN));
	}
	
	@Test
	public void shouldSetDokumentStatusToFerdigstilt() {
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus(),
				is(DokumentStatusCode.FERDIGSTILT));
	}
	
	@Test
	public void shouldSetDokumentFerdigDatoToToday() {
		service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentFerdigDato(),
				is(DateProvider.getToday()));
	}
	
	@Test
	public void shouldRunWithJournalpostTypeI() {
		journalpost.setJournalposttype(JournalpostTypeCode.I);
		
		response = service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		verify(opprettJournalpostArkiverDokumentValidator).validate(journalpost, FERDIGSTILL_JOURNALPOST);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verify(joarkRepositoryMock).save(journalpost);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.I));
	}
	
	@Test
	public void shouldRunWithNullJournalpostType() {
		journalpost.setJournalposttype(null);
		
		response = service.opprettJournalpostArkiverDokument(requestFerdigstillJournalpost);
		verify(opprettJournalpostArkiverDokumentValidator).validate(journalpost, FERDIGSTILL_JOURNALPOST);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verify(joarkRepositoryMock).save(journalpost);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}
	
	private JournalpostBuilder createJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("01054512313").build())
				.saksrelasjon(
						getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.FS22).build())
				.innhold("innhold")
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
				.fagomrade(FagomradeCode.AAP)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.journalForendeEnhetId("309480dfk")
				.land("Norge")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn("Tester")
								.dokumentInfo(
										getDokumentInfoBuilder()
												.kategori(DokumentKategoriCode.SED)
												.tittel("Brev")
												.dokumenttypeId("dokumenttypeId")
												.sensitivt(SENSITIVT_REQUEST)
												.filDetaljerList(
														getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.fileContent("file".getBytes())
																.variantFormat(VariantFormatCode.SLADDET).build()).build())
								.build());
	}
	
	private Journalpost createdJournalpost() {
		return createJournalpost()
				.journalpostId(JOURNALPOST_ID).build();
	}
}
