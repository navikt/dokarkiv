package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link DefaultFerdigstillDokumentopplasting}.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultFerdigstillDokumentopplastingTest {
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";
	private static final Long JOURNALPOST_ID = 100L;

	@InjectMocks
	private DefaultFerdigstillDokumentopplasting service;
	@Mock
	private JournalpostRepositorySkjermet repositoryMock;
	@Mock
	private SporingPopulator sporingPopulatorMock;

	private FerdigstillDokumentopplastingRequest request;

	@BeforeEach
	public void setUp() {
		request = new FerdigstillDokumentopplastingRequest(JOURNALPOST_ID, new SporingsMetaData(SPORING_FORNAVN, SPORING_ETTERNAVN,
				null));
	}

	@Test
	public void shouldThrowExceptionIfRequestIsNull() {
		request = null;

		assertThrows(ApplicationException.class,
				() -> service.ferdigstillDokumentOpplasting(request),
				"Missing parameter: ferdigstillJournalpostRequest");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdIsNull() {
		request = new FerdigstillDokumentopplastingRequest(null, new SporingsMetaData(SPORING_FORNAVN, SPORING_ETTERNAVN, null));
		assertThrows(ApplicationException.class,
				() -> service.ferdigstillDokumentOpplasting(request),
				"Missing parameter: journalpostId");
	}

	@Test
	public void shouldThrowExceptionIfNoJournalpostInRequest() {
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.empty());

		assertThrows(NoJournalpostFoundException.class,
				() -> service.ferdigstillDokumentOpplasting(request),
				"Journalpost with id: " + JOURNALPOST_ID + " does not exist");
	}

	@Test
	public void shouldThrowExceptionIfNoSporingsMetaDataInRequest() {
		request = new FerdigstillDokumentopplastingRequest(JOURNALPOST_ID, null);

		assertThrows(ApplicationException.class,
				() -> service.ferdigstillDokumentOpplasting(request),
				"Missing parameter: sporingsMetaData");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsNotInngaende() {
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalpost(JournalpostTypeCode.U)));

		assertThrows(ApplicationException.class,
				() -> service.ferdigstillDokumentOpplasting(request),
				"Journalpost is not of type Inngaaende");
	}

	@Test
	public void shouldThrowExceptionIfJournalstatusIsNotOD() {
		Journalpost journalpost = createBasicJournalpost().build();
		journalpost.setJournalstatus(JournalStatusCode.MO);
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		assertThrows(ApplicationException.class,
				() -> service.ferdigstillDokumentOpplasting(request),
				"Journalpost must have status OD");
	}

	@Test
	public void shouldFerdigstillDokumentopplastingForNonBidragJournalpostId() {
		Journalpost journalpost = createJournalpost(JournalpostTypeCode.I);
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		service.ferdigstillDokumentOpplasting(request);

		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.MO));
		verify(sporingPopulatorMock).populateSporingInfo(journalpost, SPORING_FORNAVN + " " + SPORING_ETTERNAVN);
	}

	private Journalpost createJournalpost(JournalpostTypeCode journalpostTypeCode) {
		return createBasicJournalpost()
				.avsenderMottakerId("12312312312")
				.journalStatus(JournalStatusCode.OD)
				.journalpostType(journalpostTypeCode)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(createDokumentInfoWithFildetaljer(1L))
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT).build()).build();
	}

	private JournalpostBuilder createBasicJournalpost() {
		return getJournalpostBuilder().journalStatus(JournalStatusCode.OD).journalpostType(JournalpostTypeCode.I)
				.opprettetAvNavn("opprettetAvNavn").fagomrade(FagomradeCode.UFO).journalForendeEnhetId("1234")
				.signatur(true).mottattDato(DateProvider.getToday()).dokumentDato(DateProvider.getToday())
				.mottakskanal(MottaksKanalCode.ALTINN).brukere(getBrukerBuilder().brukerId("02016126007").build());
	}

	private DokumentInfo createDokumentInfoWithFildetaljer(Long dokumentInfoId) {
		return getDokumentInfoBuilder().dokumentInfoId(dokumentInfoId).dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.brevkode("NAV-01-02-03").filDetaljerList(createFildetaljer(VariantFormatCode.ARKIV)).build();
	}

	private FilDetaljer createFildetaljer(VariantFormatCode variantFormat) {
		return getFilDetaljerBuilder().filtype(FilTypeCode.PDF).variantFormat(variantFormat)
				.fileContent("Test pdf".getBytes()).build();
	}
}
