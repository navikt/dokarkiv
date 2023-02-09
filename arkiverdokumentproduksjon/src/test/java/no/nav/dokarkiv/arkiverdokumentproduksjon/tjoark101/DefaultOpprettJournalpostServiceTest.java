package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;


import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
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
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;


/**
 * Tests for {@link DefaultOpprettJournalpostService}
 *
 * @author Stig Strøm
 */
@ExtendWith(MockitoExtension.class)
public class DefaultOpprettJournalpostServiceTest {
	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 56L;
	private static final String TODAY_DATE = "2018-06-20T14:31:54.767";
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";

	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private JournalpostRepositorySkjermet journalpostRepositorySkjermetMock;
	@Mock
	private OpprettJournalpostValidator opprettJournalpostValidator;
	@InjectMocks
	private DefaultOpprettJournalpostService service;
	private OpprettJournalpostRequestTo request;
	private OpprettJournalpostResponseTo response;
	private Journalpost journalpost;

	@BeforeEach
	public void setUp() {
		DateProvider.configure(true, TODAY_DATE);
		journalpost = createJournalpost(null, null);
		request = new OpprettJournalpostRequestTo(journalpost);
		lenient().when(journalpostRepositorySkjermetMock.save(journalpost)).thenReturn(
				createJournalpost(JOURNALPOST_ID, DOKUMENTINFO_ID));
	}

	@Test
	public void shouldValidateAndPersistJournalpostCorrectly() {

		response = service.opprettJournalpost(request);
		verify(opprettJournalpostValidator).validate(journalpost);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verify(journalpostRepositorySkjermetMock).save(journalpost);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoId(), is(DOKUMENTINFO_ID));
	}

	@Test
	public void shouldThrowExceptionWhenRequestIsNull() {
		assertThrows(ApplicationException.class,
				() -> service.opprettJournalpost(null),
				"Missing parameter: request");
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostMissingInRequest() {
		request = new OpprettJournalpostRequestTo(null);

		assertThrows(ApplicationException.class,
				() -> service.opprettJournalpost(request),
				"Missing parameter in request: Journalpost");
	}

	@Test
	public void shouldThrowExceptionWhenHoveddokumentIsMissingInRequest() {
		request.getJournalpost().findHoveddokumentDokumentInfoRelasjon()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		assertThrows(ApplicationException.class,
				() -> service.opprettJournalpost(request),
				"Missing parameter in request: Hoveddokument");
	}

	@Test
	public void shouldSetJournalpostTypeToUtgaaende() {
		service.opprettJournalpost(request);
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}

	@Test
	public void shouldSetJournalstatusToDokumentUnderProduksjon() {
		service.opprettJournalpost(request);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.D));
	}

	@Test
	public void shouldSetJournalDatoToToday() {
		service.opprettJournalpost(request);
		assertThat(journalpost.getJournalDato(), is(DateProvider.getToday()));
	}

	@Test
	public void shouldSetTilknyttetAvNavn() {
		service.opprettJournalpost(request);
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), Matchers.is(1));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getTilknyttetAvNavn(),
				Matchers.is(OPPRETTET_AV_NAVN));
	}

	@Test
	public void shouldSetDokumentStatusToUnderRedigering() {
		service.opprettJournalpost(request);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus(),
				is(DokumentStatusCode.UNDER_REDIGERING));
	}

	@Test
	public void shouldSetDokumentFerdigDatoToToday() {
		service.opprettJournalpost(request);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentFerdigDato(),
				is(DateProvider.getToday()));
	}

	private Journalpost createJournalpost(Long journalpostId, Long dokumentinfoId) {
		return getJournalpostBuilder()
				.journalpostId(journalpostId)
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(
						getBrukerBuilder()
								.brukerId("01054512313")
								.brukerType(BrukerTypeCode.PERSON).build())
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId(1L)
								.fagsystem(FagsystemCode.FS22).build())
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
												.dokumentInfoId(dokumentinfoId)
												.kategori(
														DokumentKategoriCode.ES)
												.tittel("Brev")
												.dokumenttypeId(
														"dokumenttypeId")
												.sensitivt(SENSITIVT_REQUEST)
												.filDetaljerList(
														getFilDetaljerBuilder()
																.filtype(FilTypeCode.PDF)
																.metaforceInstanceId(123L)
																.variantFormat(
																		VariantFormatCode.ARKIV)
																.build())
												.build()).build()).build();

	}

}
