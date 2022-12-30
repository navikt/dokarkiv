package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

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
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link DefaultJournalfoerNotatHenvendelse}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultJournalfoerNotatHenvendelseTest {
	private static final Long JOURNALPOST_ID = 1L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final String TODAY_DATE = "2013-01-01T12:00:00";
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Kalle Klovn";
	@Mock
	private JournalfoerNotatHenvendelseValidator behandleJournalJournalpostValidatorMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
    private JournalpostRepositorySkjermet journalpostRepositorySkjermetMock;
	@InjectMocks
	private DefaultJournalfoerNotatHenvendelse service;
	private JournalfoerNotatHenvendelseRequest request;
	private JournalfoerNotatHenvendelseResponse response;
	private Journalpost journalpost,journalpostFerdigDato;

	@BeforeEach
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
		verify(journalpostRepositorySkjermetMock).save(journalpost);
		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
	}

	@Test
	public void shouldThrowExceptionWhenRequestIsNull() {
		request = null;

		assertThrows(ApplicationException.class,
				() -> service.journalfoerNotatHenvendelse(request),
				"Missing parameter: request");
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostMissingInRequest() {
		request = new JournalfoerNotatHenvendelseRequest(null);

		assertThrows(ApplicationException.class,
				() -> service.journalfoerNotatHenvendelse(request),
				"Missing parameter in request: Journalpost");
	}

	@Test
	public void shouldThrowExceptionWhenHoveddokumentIsMissingInRequest() {
		request.getJournalpost().findHoveddokumentDokumentInfoRelasjon().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		assertThrows(ApplicationException.class,
				() -> service.journalfoerNotatHenvendelse(request),
				"Missing parameter in request: Hoveddokument");
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
	public void shouldSetDokumentFerdigDatoToFerdigDato() {
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
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("01054512313").build())
				.saksrelasjon(
						SaksrelasjonBuilder.getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.FS22).build())
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
