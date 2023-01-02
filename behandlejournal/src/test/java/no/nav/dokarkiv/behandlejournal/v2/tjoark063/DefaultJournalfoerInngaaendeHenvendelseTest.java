package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

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
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for DefaultJournalfoerInngaaendeHenvendelse.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 * @author Rune Romundstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultJournalfoerInngaaendeHenvendelseTest {

	private static final String DATE_TODAY = "2013-12-24T12:00:00";
	private static final boolean REQUEST_SENSITIVT = false;
	private static final String OPPRETTET_AV_NAVN = "Siri Saksbehandler";
	@Mock
	private JournalfoerInngaaendeHenvendelseValidator behandleJournalJournalpostValidatorMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
    private JournalpostRepositorySkjermet journalpostRepositorySkjermetMock;
	@InjectMocks
	private DefaultJournalfoerInngaaendeHenvendelse service;
	private JournalfoerInngaaendeHenvendelseRequest request;
	private Journalpost journalpost;

	@BeforeEach
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
		assertThrows(ApplicationException.class,
				() -> service.journalfoerInngaaendeHenvendelse(null),
				"Missing parameter: journalfoerInngaaendeHenvendelseRequest");
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostMissingInRequest() {
		request = new JournalfoerInngaaendeHenvendelseRequest(null);

		assertThrows(ApplicationException.class,
				() -> service.journalfoerInngaaendeHenvendelse(request),
				"Missing parameter in request: Journalpost");
	}

	@Test
	public void shouldThrowExceptionWhenHoveddokumentIsMissingInRequest() {
		request.getJournalpost().findHoveddokumentDokumentInfoRelasjon().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		assertThrows(ApplicationException.class,
				() -> service.journalfoerInngaaendeHenvendelse(request),
				"Missing parameter in request: Hoveddokument");
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
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("01054512313").build())
				.saksrelasjon(
						SaksrelasjonBuilder.getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.FS22).build())
				.signatur(true)
				.mottattDato(new Date())
				.journalpostType(JournalpostTypeCode.I)
				.mottakskanal(MottaksKanalCode.NAV_NO)
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
