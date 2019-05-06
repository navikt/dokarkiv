package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

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
 * Tests for {@link DefaultJournalfoerNotatHenvendelseV3}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJournalfoerNotatHenvendelseV3Test {
	private static final Long JOURNALPOST_ID = 1L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final String TODAY_DATE = "2013-01-01T12:00:00";
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Kalle Klovn";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private JournalfoerNotatHenvendelseV3Validator behandleJournalJournalpostValidatorMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
    private JoarkRepositorySkjermet joarkRepositoryMock;
	@InjectMocks
	private DefaultJournalfoerNotatHenvendelseV3 service;
	private JournalfoerNotatHenvendelseRequest request;
	private JournalfoerNotatHenvendelseResponse response;
	private Journalpost journalpost,journalpostFerdigDato;

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, TODAY_DATE);
		journalpost = createJournalpost();
		journalpostFerdigDato = createJournalpost();
		request = new JournalfoerNotatHenvendelseRequest(journalpost);
	}

	@Test
	public void shouldValidateAndPersistJournalpostCorrectly() {
		response = service.journalfoerNotatHenvendelse(request);
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
		service.journalfoerNotatHenvendelse(request);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostMissingInRequest() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: Journalpost");
		request = new JournalfoerNotatHenvendelseRequest(null);
		service.journalfoerNotatHenvendelse(request);
	}

	@Test
	public void shouldThrowExceptionWhenHoveddokumentIsMissingInRequest() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: Hoveddokument");
		request.getJournalpost().findHoveddokumentDokumentInfoRelasjon().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		service.journalfoerNotatHenvendelse(request);

	}

	@Test
	public void shouldSetJournalpostTypeToNotat() {
		service.journalfoerNotatHenvendelse(request);
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.N));
	}

	@Test
	public void shouldSetJournalstatusToFerdigOgLokalPrint() {
		service.journalfoerNotatHenvendelse(request);
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FL));
	}

	@Test
	public void shouldSetJournalDatoToToday() {
		service.journalfoerNotatHenvendelse(request);
		assertThat(journalpost.getJournalDato(), is(DateProvider.getToday()));
	}

	@Test
	public void shouldSetTilknyttetAvNavn() {
		service.journalfoerNotatHenvendelse(request);
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(1));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getTilknyttetAvNavn(),
				is(OPPRETTET_AV_NAVN));
	}

	@Test
	public void shouldSetDokumentStatusToFerdigstilt() {
		service.journalfoerNotatHenvendelse(request);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentstatus(),
				is(DokumentStatusCode.FERDIGSTILT));
	}

	@Test
	public void shouldSetDokumentFerdigDatoToToday() {
		service.journalfoerNotatHenvendelse(request);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentFerdigDato(),
				is(DateProvider.getToday()));
	}

	@Test
	public void shouldSetDokumentFerdigDatoToFerdigDato() throws Exception {
		request = new JournalfoerNotatHenvendelseRequest(journalpostFerdigDato);
		service.journalfoerNotatHenvendelse(request);
		assertThat(journalpostFerdigDato.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentFerdigDato(),
				is(DateProvider.getToday()));
	}

	@Test
	public void shouldSetOriginalJournalpost() {
		service.journalfoerNotatHenvendelse(request);
		assertThat(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getOriginalJournalpost(),
				is(journalpost));
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("***gammelt_fnr***").build())
				.saksrelasjon(
						SaksrelasjonBuilder.getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.PEN).build())
				.signatur(true)
				.innhold("innhold")
				.mottattDato(new Date())
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
				.fagomrade(FagomradeCode.AAP)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENTINFO_ID)
												.dokumenttypeId("dokumenttypeId")
												.sensitivt(SENSITIVT_REQUEST)
												.dokumentFerdigDato(DateProvider.getToday())
												.filDetaljerList(
														getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.fileContent("file".getBytes())
																.variantFormat(VariantFormatCode.SLADDET).build()).build())
								.build()).build();
	}


}
