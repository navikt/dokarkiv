package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringBuilder.getBidragMellomlagringBuilder;
import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringDokumentBuilder.getBidragMellomlagringDokumentBuilder;
import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
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
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidBrukerException;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

/**
 * Test class for {@link DefaultArkiverUstrukturertKravV3}.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultArkiverUstrukturertKravV3Test {

	private static final long JOURNALPOST_ID = 100L;
	private static final long DOKUMENT_INFO_ID = 1000L;
	private final String validFnr = "***gammelt_fnr***";
	private final String invalidFnr = "***gammelt_fnr***";
	private final String brevkode = "BREVKODE";

	@InjectMocks
	private DefaultArkiverUstrukturertKravV3 service;
	@Mock
    private JoarkRepositorySkjermet repositoryMock;
	@Mock
	private BidragMellomlagringRepository bidragMellomlagringRepositoryMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private ArkiverUstrukturertKravV3JournalpostValidator behandleJournalJournalpostValidatorMock;

	@Captor
	ArgumentCaptor<BidragMellomlagring> captureBidragMellomlagring;

	private ArkiverUstrukturertKravRequest request;
	private ArkiverUstrukturertKravResponse response;

	private Journalpost journalpost;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		Journalpost persistedJournalpost = Journalpost.builder()
				.journalpostId(JOURNALPOST_ID)
				.build();
		persistedJournalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(DokumentInfo.builder()
						.dokumentInfoId(DOKUMENT_INFO_ID)
						.build())
				.build());
		when(repositoryMock.save(any())).thenReturn(persistedJournalpost);
		journalpost = createJournalpost(validFnr, FagomradeCode.UFO);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostInRequestIsNull() {
		request = new ArkiverUstrukturertKravRequest(null);

		exception.expect(ApplicationException.class);
		service.arkiverUstrukturertKrav(request);
	}

	@Test
	public void shouldNotValidateRequestWithInvalidPartId() {
		Journalpost journalpost = createJournalpost(invalidFnr, FagomradeCode.UFO);
		request = new ArkiverUstrukturertKravRequest(journalpost);

		exception.expect(InvalidBrukerException.class);
		service.arkiverUstrukturertKrav(request);
	}

	@Test
	public void shouldThrowExceptionForMissingDokumentInfoRelasjon() throws Exception {
		journalpost.clearJournalpostDokumentInfoRelasjoner();

		callOperationAndExpectExceptionWithMessageContaining("DokumentInfo");
	}

	@Test
	public void shouldSetInternalValuesOnJournalpost() {
		Journalpost journalpost = createJournalpost(validFnr, FagomradeCode.UFO);
		request = new ArkiverUstrukturertKravRequest(journalpost);

		service.arkiverUstrukturertKrav(request);

		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.I));
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.OD));

		JournalpostDokumentInfoRelasjon relasjon = journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next();
		assertThat(relasjon.getTilknyttetJournalpostSom(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT));
		assertThat(relasjon.getTilknyttetAvNavn(), is(journalpost.getOpprettetAvNavn()));

		assertThat(relasjon.getDokumentInfo().getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT));

		for (FilDetaljer filDetaljer : journalpost.findAllFilDetaljer()) {
			assertThat(filDetaljer.getFilUuid(), is(notNullValue()));
		}
	}

	@Test
	public void shouldBehandleJournalJournalpostValidator() throws Exception {
		Journalpost journalpost = createJournalpost(validFnr, FagomradeCode.UFO);
		request = new ArkiverUstrukturertKravRequest(journalpost);

		service.arkiverUstrukturertKrav(request);

		verify(behandleJournalJournalpostValidatorMock).validate(journalpost);
	}

	@Test
	public void shouldCallDokumentFilerDelegate() {
		Journalpost journalpost = createJournalpost(validFnr, FagomradeCode.UFO);
		request = new ArkiverUstrukturertKravRequest(journalpost);

		service.arkiverUstrukturertKrav(request);

		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
	}

	@Test
	public void shouldCallJoarkRepositoryToSaveJournalpost() {
		Journalpost journalpost = createJournalpost(validFnr, FagomradeCode.UFO);
		request = new ArkiverUstrukturertKravRequest(journalpost);

		service.arkiverUstrukturertKrav(request);

		verify(repositoryMock).save(journalpost);
	}

	@Test
	public void shouldNotCallJoarkRepositoryForBidragsdokument() {
		Journalpost journalpost = createJournalpost(validFnr, FagomradeCode.BID);
		request = new ArkiverUstrukturertKravRequest(journalpost);
		when(bidragMellomlagringRepositoryMock.save(any())).thenReturn(
				createBidragMellomlagring());

		service.arkiverUstrukturertKrav(request);

		verify(repositoryMock, never()).save(journalpost);
	}

	@Test
	public void shouldCallBidragMellomlagringRepositoryToSaveBidragsdokument() {
		Journalpost journalpost = createJournalpost(validFnr, FagomradeCode.BID);
		request = new ArkiverUstrukturertKravRequest(journalpost);
		when(bidragMellomlagringRepositoryMock.save(captureBidragMellomlagring.capture()))
				.thenReturn(createBidragMellomlagring());

		service.arkiverUstrukturertKrav(request);

		assertBidragMellomlagring(captureBidragMellomlagring.getValue(), journalpost);
		assertBidragMellomlagringDokument(captureBidragMellomlagring.getValue().getBidragMellomlagringDokuments()
				.iterator().next(), journalpost.findAllFilDetaljer().iterator().next());

	}

	private void assertBidragMellomlagring(BidragMellomlagring bidragMellomlagring, Journalpost journalpost) {
		assertThat(bidragMellomlagring.getAvsenderFnr(), is(journalpost.getBrukere().iterator().next().getBrukerId()));
		assertThat(bidragMellomlagring.getMottattDato(), is(journalpost.getMottattDato()));
		assertThat(bidragMellomlagring.getStatus(), is(BidragMellomlagringStatus.DOKUMENTOPPLASTING));
	}

	private void assertBidragMellomlagringDokument(BidragMellomlagringDokument bidragMellomlagringDokument,
			FilDetaljer filDetaljer) {
		assertThat(bidragMellomlagringDokument.getDokumentType(), is(BidragMellomlagringDokumentType.HOVEDDOKUMENT));
		assertThat(bidragMellomlagringDokument.getDokument(), is(filDetaljer.getFileContent()));
	}

	@Test
	public void shouldReturnResponseWithBidragMellomlagringIdWithPrefixAsJournalpostIdForBidragsdokument() {
		Journalpost journalpost = createJournalpost(validFnr, FagomradeCode.BID);
		BidragMellomlagring bidragMellomlagring = createBidragMellomlagring();
		request = new ArkiverUstrukturertKravRequest(journalpost);
		when(bidragMellomlagringRepositoryMock.save(any())).thenReturn(
				bidragMellomlagring);

		response = service.arkiverUstrukturertKrav(request);

		assertThat(response.getJournalpostId(), is(bidragMellomlagring.getIdWithPrefix()));
		assertThat(response.getDokumentId(), is(bidragMellomlagring.getBidragMellomlagringDokuments().iterator().next()
				.getBidragMellomlagringDokumentId()));
	}

	@Test
	public void shouldReturnResponseWithJournalpostIdForJoarkdokument() {
		Journalpost journalpost = createJournalpost(validFnr, FagomradeCode.PEN);
		request = new ArkiverUstrukturertKravRequest(journalpost);

		response = service.arkiverUstrukturertKrav(request);

		assertThat(response.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(response.getDokumentId(), is(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo()
				.getDokumentInfoId()));
	}

	private void callOperationAndExpectExceptionWithMessageContaining(String errormessagePart) {
		request = new ArkiverUstrukturertKravRequest(journalpost);

		exception.expect(ApplicationException.class);
		exception.expectMessage(containsString(errormessagePart));

		service.arkiverUstrukturertKrav(request);
	}

	private BidragMellomlagring createBidragMellomlagring() {
		return getBidragMellomlagringBuilder()
				.bidragMellomlagringId(100L)
				.avsenderFnr("***gammelt_fnr***")
				.mottattDato(new Date())
				.status(BidragMellomlagringStatus.DOKUMENTOPPLASTING)
				.bidragMellomlagringDokuments(
						getBidragMellomlagringDokumentBuilder().bidragMellomlagringDokumentId(1000L)
								.dokumentType(BidragMellomlagringDokumentType.HOVEDDOKUMENT)
								.dokument("My little testfile".getBytes()).build()).build();
	}

	private Journalpost createJournalpost(String brukerId, FagomradeCode fagomrade) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.fagomrade(fagomrade)
				.mottattDato(new Date())
				.mottakskanal(MottaksKanalCode.ALTINN)
				.signatur(true)
				.opprettetAvNavn("Bjarne Betjent")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(createDokumentInfoWithFildetaljer())
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT).build())
				.brukere(getBrukerBuilder().brukerId(brukerId).brukerType(BrukerTypeCode.PERSON).build()).build();
	}

	private DokumentInfo createDokumentInfoWithFildetaljer() {
		FilDetaljer fildetaljer = createFildetaljer(VariantFormatCode.ARKIV);

		return getDokumentInfoBuilder().dokumentInfoId(DOKUMENT_INFO_ID).dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.dokumenttypeId("dokumenttypeId").brevkode(brevkode).filDetaljerList(fildetaljer).build();
	}

	private FilDetaljer createFildetaljer(VariantFormatCode variantFormat) {
		return getFilDetaljerBuilder().filtype(FilTypeCode.PDF).variantFormat(variantFormat)
				.fileContent("Test pdf".getBytes()).build();

	}
}
