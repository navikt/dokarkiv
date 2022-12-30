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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

/**
 * Test class for {@link DefaultArkiverUstrukturertKravV3}.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultArkiverUstrukturertKravV3Test {

	private static final long JOURNALPOST_ID = 100L;
	private static final long DOKUMENT_INFO_ID = 1000L;
	private final String validFnr = "02016126007";
	private final String invalidFnr = "99999999999";
	private final String brevkode = "BREVKODE";

	@InjectMocks
	private DefaultArkiverUstrukturertKravV3 service;
	@Mock
	private JournalpostRepositorySkjermet repositoryMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private ArkiverUstrukturertKravV3JournalpostValidator behandleJournalJournalpostValidatorMock;

	private ArkiverUstrukturertKravRequest request;
	private ArkiverUstrukturertKravResponse response;

	private Journalpost journalpost;

	@BeforeEach
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
		journalpost = createJournalpost(validFnr, FagomradeCode.UFO);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostInRequestIsNull() {
		request = new ArkiverUstrukturertKravRequest(null);

		assertThrows(ApplicationException.class,
				() -> service.arkiverUstrukturertKrav(request));
	}

	@Test
	public void shouldNotValidateRequestWithInvalidPartId() {
		Journalpost journalpost = createJournalpost(invalidFnr, FagomradeCode.UFO);
		request = new ArkiverUstrukturertKravRequest(journalpost);

		assertThrows(InvalidBrukerException.class,
				() -> service.arkiverUstrukturertKrav(request));
	}

	@Test
	public void shouldThrowExceptionForMissingDokumentInfoRelasjon() {
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
	public void shouldBehandleJournalJournalpostValidator() {
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
	public void shouldCallRepositoryToSaveJournalpost() {
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

		assertThrows(ApplicationException.class,
				() -> service.arkiverUstrukturertKrav(request),
				errormessagePart);
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
