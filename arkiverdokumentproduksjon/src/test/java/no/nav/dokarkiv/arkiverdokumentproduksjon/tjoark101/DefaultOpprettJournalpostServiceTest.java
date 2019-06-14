package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;


import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * Tests for {@link DefaultOpprettJournalpostService}
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultOpprettJournalpostServiceTest {
	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 56L;
	private static final String TODAY_DATE = "2018-06-20T14:31:54.767";
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
    private JoarkRepositorySkjermet joarkRepositoryMock;
	@Mock
	private OpprettJournalpostValidator opprettJournalpostValidator;
	@InjectMocks
	private DefaultOpprettJournalpostService service;
	private OpprettJournalpostRequestTo request;
	private OpprettJournalpostResponseTo response;
	private Journalpost journalpost;

	@Before
	public void setUp() {
		DateProvider.configure(true, TODAY_DATE);
		journalpost = createJournalpost(null, null);
		request = new OpprettJournalpostRequestTo(journalpost);
		when(joarkRepositoryMock.save(journalpost)).thenReturn(
				createJournalpost(JOURNALPOST_ID, DOKUMENTINFO_ID));
	}

	@Test
	public void shouldValidateAndPersistJournalpostCorrectly() {

		response = service.opprettJournalpost(request);
		verify(opprettJournalpostValidator).validate(journalpost);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verify(joarkRepositoryMock).save(journalpost);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoId(), is(DOKUMENTINFO_ID));
	}

	@Test
	public void shouldThrowExceptionWhenRequestIsNull() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter: request");
		service.opprettJournalpost(null);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostMissingInRequest() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: Journalpost");
		request = new OpprettJournalpostRequestTo(null);
		service.opprettJournalpost(request);
	}

	@Test
	public void shouldThrowExceptionWhenHoveddokumentIsMissingInRequest() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: Hoveddokument");
		request.getJournalpost().findHoveddokumentDokumentInfoRelasjon()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		service.opprettJournalpost(request);
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
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(
						getBrukerBuilder()
								.brukerId("***gammelt_fnr***")
								.brukerType(BrukerTypeCode.PERSON).build())
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId("1")
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
