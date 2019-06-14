package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

/**
 * Tests for {@link DefaultJournalfoerUtgaaendeHenvendelse}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJournalfoerUtgaaendeHenvendelseTest {
	private static final Long JOURNALPOST_ID = 1L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final String TODAY_DATE = "2013-01-01T12:00:00";
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private JournalfoerUtgaaendeHenvendelseValidator behandleJournalJournalpostValidatorMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
    private JoarkRepositorySkjermet joarkRepositoryMock;
	@InjectMocks
	private DefaultJournalfoerUtgaaendeHenvendelse service;
	private JournalfoerUtgaaendeHenvendelseRequest request;
	private JournalfoerUtgaaendeHenvendelseResponse response;
	private Journalpost journalpost;

	@Before
	public void setUp() {
		DateProvider.configure(true, TODAY_DATE);
		journalpost = createJournalpost();
		request = new JournalfoerUtgaaendeHenvendelseRequest(journalpost);
	}

	@Test
	public void shouldValidateAndPersistJournalpostCorrectly() {
		response = service.journalfoerUtgaaendeHenvendelse(request);
		verify(behandleJournalJournalpostValidatorMock).validate(journalpost);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verify(joarkRepositoryMock).save(journalpost);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowExceptionWhenRequestIsNull() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter: request");
		request = null;
		service.journalfoerUtgaaendeHenvendelse(request);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostMissingInRequest() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: Journalpost");
		request = new JournalfoerUtgaaendeHenvendelseRequest(null);
		service.journalfoerUtgaaendeHenvendelse(request);
	}

	@Test
	public void shouldThrowExceptionWhenHoveddokumentIsMissingInRequest() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: Hoveddokument");
		request.getJournalpost().findHoveddokumentDokumentInfoRelasjon().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		service.journalfoerUtgaaendeHenvendelse(request);

	}

	@Test
	public void shouldSetJournalpostTypeToUtgaaende() {
		service.journalfoerUtgaaendeHenvendelse(request);
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}

	@Test
	public void shouldSetJournalstatusToDokumentEkspedert() {
		service.journalfoerUtgaaendeHenvendelse(request);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FS));
	}

	@Test
	public void shouldSetJournalDatoToToday() {
		service.journalfoerUtgaaendeHenvendelse(request);
		assertThat(journalpost.getJournalDato(), is(DateProvider.getToday()));
	}

	@Test
	public void shouldSetTilknyttetAvNavn() {
		service.journalfoerUtgaaendeHenvendelse(request);
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(1));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getTilknyttetAvNavn(),
				is(OPPRETTET_AV_NAVN));
	}

	@Test
	public void shouldSetDokumentStatusToFerdigstilt() {
		service.journalfoerUtgaaendeHenvendelse(request);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus(),
				is(DokumentStatusCode.FERDIGSTILT));
	}

	@Test
	public void shouldSetEkspedertDatoToToday() {
		service.journalfoerUtgaaendeHenvendelse(request);
		assertThat(journalpost.getEkspedertDato(),
				is(DateProvider.getToday()));
	}

	@Test
	public void shouldSetDokumentFerdigDatoToToday() {
		service.journalfoerUtgaaendeHenvendelse(request);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentFerdigDato(),
				is(DateProvider.getToday()));
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("***gammelt_fnr***").build())
				.saksrelasjon(
						SaksrelasjonBuilder.getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.FS22).build())
				.signatur(true)
				.innhold("innhold")
				.mottattDato(new Date())
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
				.fagomrade(FagomradeCode.AAP)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.ekspedertDato(DateProvider.getToday())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENTINFO_ID)
												.dokumenttypeId("dokumenttypeId")
												.sensitivt(SENSITIVT_REQUEST)
												.filDetaljerList(
														getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.fileContent("file".getBytes())
																.variantFormat(VariantFormatCode.SLADDET).build()).build())
								.build()).build();
	}
}
