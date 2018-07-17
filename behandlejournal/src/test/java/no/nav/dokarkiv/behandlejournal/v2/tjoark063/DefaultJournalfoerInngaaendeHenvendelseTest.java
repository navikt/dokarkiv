package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.builder.BehandlingsrelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepository;
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
 * Test class for DefaultJournalfoerInngaaendeHenvendelse.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 * @author Rune Romundstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJournalfoerInngaaendeHenvendelseTest {

	private static final String DATE_TODAY = "2013-12-24T12:00:00";
	private static final boolean REQUEST_SENSITIVT = false;
	private static final String OPPRETTET_AV_NAVN = "Siri Saksbehandler";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private JournalfoerInngaaendeHenvendelseValidator behandleJournalJournalpostValidatorMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private JoarkRepository joarkRepositoryMock;
	@InjectMocks
	private DefaultJournalfoerInngaaendeHenvendelse service;
	private JournalfoerInngaaendeHenvendelseRequest request;
	private Journalpost journalpost;

	@Before
	public void init() {
		DateProvider.configure(true, DATE_TODAY);
		journalpost = createJournalpost();
		request = new JournalfoerInngaaendeHenvendelseRequest(journalpost);
	}

	@Test
	public void shouldValidateAndPersistJournalpostCorrectly() {
		service.journalfoerInngaaendeHenvendelse(request);
	}

	@Test
	public void shouldThrowExceptionWhenRequestIsNull() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter: journalfoerInngaaendeHenvendelseRequest");

		service.journalfoerInngaaendeHenvendelse(null);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostMissingInRequest() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: Journalpost");

		request = new JournalfoerInngaaendeHenvendelseRequest(null);

		service.journalfoerInngaaendeHenvendelse(request);
	}

	@Test
	public void shouldThrowExceptionWhenHoveddokumentIsMissingInRequest() {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter in request: Hoveddokument");
		request.getJournalpost().findHoveddokumentDokumentInfoRelasjon().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		service.journalfoerInngaaendeHenvendelse(request);

	}

	@Test
	public void shouldHaveSetFunctionalJournalpostValues() {
		service.journalfoerInngaaendeHenvendelse(request);

		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.I));
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.J));
		assertThat(journalpost.getJournalDato(), is(DateProvider.getToday()));

		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(1));
		JournalpostDokumentInfoRelasjon relasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		assertThat(relasjon.getTilknyttetAvNavn(), is(OPPRETTET_AV_NAVN));
		assertThat(relasjon.getDokumentInfo().getOriginalJournalpost(), is(journalpost));
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("***gammelt_fnr***").build())
				.saksrelasjon(
						SaksrelasjonBuilder.getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.BID).build())
				.behandlingsrelasjon(
						BehandlingsrelasjonBuilder.getBehandlingsrelasjonBuilder().behandlingsId("1")
								.behandlingsType("TEST").build())
				.signatur(true)
				.mottattDato(new Date())
				.journalpostType(JournalpostTypeCode.I)
				.mottakskanal(MottaksKanalCode.PSELV)
				.fagomrade(FagomradeCode.AAP)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumenttypeId("dokumenttypeId")
												.sensitivt(REQUEST_SENSITIVT)
												.filDetaljerList(
														getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.fileContent("file".getBytes())
																.variantFormat(VariantFormatCode.SLADDET).build()).build())
								.build()).build();
	}

}
