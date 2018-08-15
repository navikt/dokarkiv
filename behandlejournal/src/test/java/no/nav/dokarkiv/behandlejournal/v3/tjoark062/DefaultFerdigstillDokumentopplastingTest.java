package no.nav.dokarkiv.behandlejournal.v3.tjoark062;

import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringBuilder.getBidragMellomlagringBuilder;
import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringDokumentBuilder.getBidragMellomlagringDokumentBuilder;
import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.behandlejournal.v2.SporingsMetaData;
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
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;
import java.util.Optional;

/**
 * Test class for {@link DefaultFerdigstillDokumentopplasting}.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultFerdigstillDokumentopplastingTest {
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";
	private static final Long JOURNALPOST_ID = 100L;
	private static final Long BIDRAG_JOURNALPOST_ID = ***gammelt_fnr***1L;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@InjectMocks
	private DefaultFerdigstillDokumentopplasting service;
	@Mock
	private JoarkRepository repositoryMock;
	@Mock
	private BidragMellomlagringRepository bidragMellomlagringRepositoryMock;
	@Mock
	private SporingPopulator sporingPopulatorMock;

	private FerdigstillDokumentopplastingRequest request;

	@Before
	public void setUp() {
		request = new FerdigstillDokumentopplastingRequest(JOURNALPOST_ID, new SporingsMetaData(SPORING_FORNAVN, SPORING_ETTERNAVN,
				null));
	}

	@Test
	public void shouldThrowExceptionIfRequestIsNull() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter: ferdigstillJournalpostRequest");

		request = null;
		service.ferdigstillDokumentOpplasting(request);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdIsNull() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter: journalpostId");

		request = new FerdigstillDokumentopplastingRequest(null, new SporingsMetaData(SPORING_FORNAVN, SPORING_ETTERNAVN, null));
		service.ferdigstillDokumentOpplasting(request);
	}

	@Test
	public void shouldThrowExceptionIfNoJournalpostInRequest() throws Exception {
		expectedException.expect(NoJournalpostFoundException.class);
		expectedException.expectMessage("Journalpost with id: " + JOURNALPOST_ID + " does not exist");

		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.empty());

		service.ferdigstillDokumentOpplasting(request);
	}

	@Test
	public void shouldThrowExceptionIfNoSporingsMetaDataInRequest() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Missing parameter: sporingsMetaData");
		request = new FerdigstillDokumentopplastingRequest(JOURNALPOST_ID, null);

		service.ferdigstillDokumentOpplasting(request);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsNotInngaende() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Journalpost is not of type Inngaaende");

		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(createJournalpost(JournalpostTypeCode.U)));

		service.ferdigstillDokumentOpplasting(request);
	}

	@Test
	public void shouldThrowExceptionIfJournalstatusIsNotOD() throws Exception {
		Journalpost journalpost = createBasicJournalpost().build();
		journalpost.setJournalstatus(JournalStatusCode.MO);
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Journalpost must have status OD");

		service.ferdigstillDokumentOpplasting(request);
	}

	@Test
	public void shouldFerdigstillDokumentopplastingForNonBidragJournalpostId() throws Exception {
		Journalpost journalpost = createJournalpost(JournalpostTypeCode.I);
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));

		service.ferdigstillDokumentOpplasting(request);

		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.MO));
		verify(sporingPopulatorMock).populateSporingInfo(journalpost, SPORING_FORNAVN + " " + SPORING_ETTERNAVN);
	}

	@Test
	public void shouldNeverCallJoarkRepositoryWhenFerdigstillDokumentOpplastingForBidragJournalpostId()
			throws Exception {
		request = new FerdigstillDokumentopplastingRequest(BIDRAG_JOURNALPOST_ID, new SporingsMetaData(SPORING_FORNAVN,
				SPORING_ETTERNAVN, null));
		BidragMellomlagring bidragMellomlagring = createBidragMellomlagring();
		when(bidragMellomlagringRepositoryMock.findById(BidragMellomlagring
				.removePrefixFromId(BIDRAG_JOURNALPOST_ID))).thenReturn(Optional.of(bidragMellomlagring));

		service.ferdigstillDokumentOpplasting(request);

		verify(repositoryMock, never()).findById(BIDRAG_JOURNALPOST_ID);
	}

	@Test
	public void shouldFerdigstillDokumentopplastingForBidragJournalpostId() throws Exception {
		request = new FerdigstillDokumentopplastingRequest(BIDRAG_JOURNALPOST_ID, new SporingsMetaData(SPORING_FORNAVN,
				SPORING_ETTERNAVN, null));
		BidragMellomlagring bidragMellomlagring = createBidragMellomlagring();
		when(bidragMellomlagringRepositoryMock.findById(BidragMellomlagring
				.removePrefixFromId(BIDRAG_JOURNALPOST_ID))).thenReturn(Optional.of(bidragMellomlagring));

		service.ferdigstillDokumentOpplasting(request);

		assertThat(bidragMellomlagring.getStatus(), is(BidragMellomlagringStatus.KLAR_TIL_OVERFORING));
	}

	@Test
	public void shouldThrowExceptionIfBidragMellomlagringCannotBeFound() throws Exception {
		request = new FerdigstillDokumentopplastingRequest(BIDRAG_JOURNALPOST_ID, new SporingsMetaData(SPORING_FORNAVN,
				SPORING_ETTERNAVN, null));
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Could not find BidragMellomlagring for overforing");

		when(bidragMellomlagringRepositoryMock.findById(BidragMellomlagring
				.removePrefixFromId(BIDRAG_JOURNALPOST_ID))).thenReturn(Optional.empty());

		service.ferdigstillDokumentOpplasting(request);
	}

	private BidragMellomlagring createBidragMellomlagring() {
		return getBidragMellomlagringBuilder()
				.avsenderFnr("***gammelt_fnr***")
				.mottattDato(new Date())
				.status(BidragMellomlagringStatus.DOKUMENTOPPLASTING)
				.bidragMellomlagringDokuments(
						getBidragMellomlagringDokumentBuilder()
								.dokumentType(BidragMellomlagringDokumentType.HOVEDDOKUMENT)
								.dokument("Testfil".getBytes()).build()).build();
	}

	private Journalpost createJournalpost(JournalpostTypeCode journalpostTypeCode) {
		Journalpost jp = createBasicJournalpost()
				.avsenderMottakerId("***gammelt_fnr***")
				.journalStatus(JournalStatusCode.OD)
				.journalpostType(journalpostTypeCode)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(createDokumentInfoWithFildetaljer(1L))
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT).build()).build();

		return jp;
	}

	private JournalpostBuilder createBasicJournalpost() {
		return getJournalpostBuilder().journalStatus(JournalStatusCode.OD).journalpostType(JournalpostTypeCode.I)
				.opprettetAvNavn("opprettetAvNavn").fagomrade(FagomradeCode.UFO).journalForendeEnhetId("1234")
				.signatur(true).mottattDato(DateProvider.getToday()).dokumentDato(DateProvider.getToday())
				.mottakskanal(MottaksKanalCode.ALTINN).brukere(getBrukerBuilder().brukerId("***gammelt_fnr***").build());
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
