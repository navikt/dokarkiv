package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

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
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidBrukerException;
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

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link DefaultArkiverUstrukturertKravV3}.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultArkiverUstrukturertKravV3Test {

	private static final long JOURNALPOST_ID = 100L;
	private static final long DOKUMENT_INFO_ID = 1000L;
	private final String validFnr = "02016126007";
	private final String invalidFnr = "99999999999";
	private final String brevkode = "BREVKODE";

	@InjectMocks
	private DefaultArkiverUstrukturertKravV3 service;
	@Mock
	private JoarkRepositorySkjermet repositoryMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private ArkiverUstrukturertKravV3JournalpostValidator behandleJournalJournalpostValidatorMock;

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
