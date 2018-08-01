package no.nav.dokarkiv.journal.v3.tjoark051;

import static no.nav.dokarkiv.core.domain.entities.DokumentInfo.DELETED_DOCUMENT_TITLE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.journal.v3.AbstractJournalV3Itest;
import no.nav.dokarkiv.journal.v3.exceptions.NoDokumentInfoFoundException;
import no.nav.modig.core.domain.IdentType;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.feil.DokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration test for HentDokument(TJOARK051) in 3rd gen. Journal service.
 *
 * @author Stig Strøm (Copied by Roar Bjurstrøm)
 */
@Ignore
public class HentDokumentIT extends AbstractJournalV3Itest {

	private static final String FIL_UUID = FilDetaljer.generateUuid();
	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private static final byte[] FIL_CONTENT = "Test".getBytes();

	private static final OnDemandInstansCode ON_DEMAND_INSTANS = OnDemandInstansCode.PESYS;
	private static final String ON_DEMAND_ID = "onDemandId";

	private String journalpostId;
	private String dokumentInfoId;

	private HentDokumentRequest request = new HentDokumentRequest();
	private Journalpost journalpost;
//
//	@Inject
//	private JoarkOndemandRepositoryStub joarkOndemandRepositoryStub;

	@Before
	public void setUp() {
		journalpost = buildAndPersistJournalpost("Dokumenttittel");
		createRequestFromJournalpost(journalpost);

		SubjectHandlerUtils.setSubject(new SubjectHandlerUtils.SubjectBuilder(INTERN_BRUKER_USER_ID, IdentType.InternBruker).getSubject());
	}

	@Test
	public void shouldFailWhenABACDenies() throws Exception {
		abacDeny();

		persistDokumentFil();

		try {
			journalV3Provider.hentDokument(request);
			fail();
		} catch (HentDokumentSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), equalTo("Bruker har ikke tilgang til journalpost"));
		}

//		assertAbacRequestFromFile("abac/hentdokument.json"); FIXME
	}

	@Test
	public void shouldAllowAccessWhenAbacPermits() throws Exception {
		abacPermit();

		persistDokumentFil();

		journalV3Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostNotFound() throws Exception {
		request.setJournalpostId("123");

		expectedException.expect(HentDokumentDokumentIkkeFunnet.class);
		expectedException.expectMessage("Journalpost ikke funnet. journalpostId=123");
		expectedException.expect(hasProperty("faultInfo", hasProperty("feilaarsak", containsString(JournalpostIkkeFunnetException.class.getName()))));


		journalV3Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() throws Exception {
		request.setDokumentId("123");

		setupExpectedException(NoDokumentInfoFoundException.class.getName());

		journalV3Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenFilDetaljerNotFoundWithGivenVariant() throws Exception {
		Variantformater variantFormat = new Variantformater();
		variantFormat.setValue(VariantFormatCode.PRODUKSJON.name());
		request.setVariantformat(variantFormat);

		setupExpectedException(InvalidArgumentException.class.getName());

		journalV3Provider.hentDokument(request);
	}

	@Test
	public void shouldThrowExceptionWhenDokumentFilNotFound() throws Exception {
		try {
			journalV3Provider.hentDokument(request);
		} catch (HentDokumentDokumentIkkeFunnet e) {
			assertThat(e.getFaultInfo(), isA(DokumentIkkeFunnet.class));
			assertThat(e.getFaultInfo().getFeilaarsak(), containsString("Could not find DokumentFil with filUuid=" + FIL_UUID));
		}
	}

	@Test
	public void shouldReturnDeletedDocument() throws Exception {
		Journalpost journalpost = buildAndPersistJournalpost(DELETED_DOCUMENT_TITLE);
		createRequestFromJournalpost(journalpost);

		persistDokumentFil();

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(FIL_CONTENT));
	}

	@Test
	public void shouldGetDocumentWhenMottakskanalAltinn() throws Exception {
		Journalpost journalpost = buildAndPersistJournalpost(MottaksKanalCode.ALTINN);
		createRequestFromJournalpost(journalpost);

		persistDokumentFil();

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(FIL_CONTENT));
	}

	@Test
	public void shouldGetDokument() throws Exception {
		persistDokumentFil();

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(FIL_CONTENT));
	}

	@Test
	public void shouldGetOnDemandDokument() throws Exception {
		FilDetaljer filDetaljer = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo().findFilDetaljerByFilUuid(FIL_UUID);
		filDetaljer.setOnDemandId(ON_DEMAND_ID);
		filDetaljer.setOnDemandInstans(ON_DEMAND_INSTANS);
//		joarkOndemandRepositoryStub.saveDocument(ON_DEMAND_INSTANS, ON_DEMAND_ID, FIL_CONTENT);

		HentDokumentResponse response = journalV3Provider.hentDokument(request);

		assertThat(response.getDokument(), is(FIL_CONTENT));
	}

	private void createRequestFromJournalpost(Journalpost journalpost) {
		journalpostId = journalpost.getId().toString();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId().toString();
		createRequest();
	}

	private void createRequest() {
		request.setJournalpostId(journalpostId);
		request.setDokumentId(dokumentInfoId);
		Variantformater variantFormat = new Variantformater();
		variantFormat.setValue(VARIANT_FORMAT.name());
		request.setVariantformat(variantFormat);
	}

	private void setupExpectedException(String rootCause) {
		expectedException.expect(HentDokumentDokumentIkkeFunnet.class);
		expectedException.expectMessage("Could not find document");
		expectedException.expect(hasProperty("faultInfo", hasProperty("feilaarsak", containsString(rootCause))));
	}

	private Journalpost buildAndPersistJournalpost(String dokumentTittel) {
		return joarkRepository.save(createJournalpostBuilder(dokumentTittel)
				.fagomrade(FagomradeCode.FOR).build());
	}

	private Journalpost buildAndPersistJournalpost(MottaksKanalCode mottaksKanalCode) {
		return joarkRepository.save(createJournalpostBuilder("tittel")
				.mottakskanal(mottaksKanalCode)
				.build());
	}

	private JournalpostBuilder createJournalpostBuilder(String dokumentTittel) {
		return JournalpostBuilder
				.getJournalpostBuilder()
				.journalStatus(JournalStatusCode.FS)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.brukere(BrukerTestDataProvider.createBruker().build())
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder
								.getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetAvNavn("testuser")
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.opprettetKildeNavn("test")
								.dokumentInfo(
										DokumentInfoBuilder
												.getDokumentInfoBuilder()
												.opprettetKildeNavn("test")
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.tittel(dokumentTittel)
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.filUuid(FIL_UUID).variantFormat(VARIANT_FORMAT)
																.opprettetKildeNavn("test").build()).build()).build());
	}

	private void persistDokumentFil() {
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder().filUuid(FIL_UUID).fil(FIL_CONTENT).opprettetKildeNavn("test").build());
	}

}
