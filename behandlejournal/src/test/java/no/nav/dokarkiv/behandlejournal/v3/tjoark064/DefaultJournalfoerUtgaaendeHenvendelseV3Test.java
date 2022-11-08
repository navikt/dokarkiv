package no.nav.dokarkiv.behandlejournal.v3.tjoark064;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneId;
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
 * Tests for {@link DefaultJournalfoerUtgaaendeHenvendelseV3}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultJournalfoerUtgaaendeHenvendelseV3Test {
	private static final Long JOURNALPOST_ID = 1L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final String TODAY_DATE = "2013-01-01T12:00:00";
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";

	@Mock
	private JournalfoerUtgaaendeHenvendelseV3Validator behandleJournalJournalpostValidatorMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
    private JoarkRepositorySkjermet joarkRepositoryMock;
	@InjectMocks
	private DefaultJournalfoerUtgaaendeHenvendelseV3 service;
	private JournalfoerUtgaaendeHenvendelseRequest request;
	private JournalfoerUtgaaendeHenvendelseResponse response;
	private Journalpost journalpost;

	@BeforeEach
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
		request = null;
		assertThrows(ApplicationException.class,
				() -> service.journalfoerUtgaaendeHenvendelse(request),
				"Missing parameter: request");
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostMissingInRequest() {
		request = new JournalfoerUtgaaendeHenvendelseRequest(null);

		assertThrows(ApplicationException.class,
				() -> service.journalfoerUtgaaendeHenvendelse(request),
				"Missing parameter in request: Journalpost");
	}

	@Test
	public void shouldThrowExceptionWhenHoveddokumentIsMissingInRequest() {
		request.getJournalpost().findHoveddokumentDokumentInfoRelasjon().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		assertThrows(ApplicationException.class,
				() -> service.journalfoerUtgaaendeHenvendelse(request),
				"Missing parameter in request: Hoveddokument");
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
				.ekspedertDato(OffsetDateTime.from(DateProvider.getToday().toInstant().atZone(ZoneId.of("Europe/Oslo"))))
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
