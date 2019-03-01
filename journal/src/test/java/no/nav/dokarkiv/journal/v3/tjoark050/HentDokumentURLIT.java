package no.nav.dokarkiv.journal.v3.tjoark050;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokarkiv.core.repository.DefaultDokumentFilRepository.FIL_UUID_DUMMY_DOKUMENT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidFilUuidException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.journal.v3.AbstractJournalV3Itest;
import no.nav.modig.core.domain.IdentType;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.transaction.TestTransaction;

/**
 * Integration test for HentDokumentURL in 3rd gen. Journal service.
 *
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class HentDokumentURLIT extends AbstractJournalV3Itest {

	private static final String FIL_UUID = FilDetaljer.generateUuid();
	private static final String FIL_UUID_SLADDET = FilDetaljer.generateUuid();
	private static final String FIL_UUID_DUMMY = FilDetaljer.generateUuid();

	private static final VariantFormatCode VARIANT_FORMAT = VariantFormatCode.ARKIV;
	private String journalpostId;
	private String dokumentInfoId;
	private Journalpost journalpost;
	private HentDokumentURLRequest request = new HentDokumentURLRequest();

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
			journalV3Provider.hentDokumentURL(request);
			fail();
		} catch (HentDokumentURLSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), equalTo("Bruker har ikke tilgang til journalpost"));
		}

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/hentdokumenturl.json"))));
	}

	@Test
	public void shouldAllowAccessWhenAbacPermits() throws Exception {
		abacPermit();

		persistDokumentFil();

		journalV3Provider.hentDokumentURL(request);
	}
	
	@Test
	public void shouldThrowExceptionWhenJournalpostNotFound() throws Exception {
		abacPermit();
		request.setJournalpostId("123");

		expectedException.expect(HentDokumentURLDokumentIkkeFunnet.class);
		expectedException.expectMessage("Journalpost ikke funnet. journalpostId=123");
		expectedException.expect(hasProperty("faultInfo",
				hasProperty("feilaarsak", containsString("Journalpost ikke funnet. journalpostId=123"))));

		journalV3Provider.hentDokumentURL(request);
	}
	
	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() throws Exception {
		abacPermit();
		request.setDokumentId("123");

		setupExpectedException(NoDokumentInfoFoundException.class.getName());

		journalV3Provider.hentDokumentURL(request);
	}

	@Test
	public void shouldThrowExceptionWhenFilDetaljerNotFoundWithGivenVariant() throws Exception {
		abacPermit();
		Variantformater variantFormat = new Variantformater();
		variantFormat.setValue(VariantFormatCode.PRODUKSJON.name());
		request.setVariantformat(variantFormat);

		setupExpectedException(InvalidArgumentException.class.getName());

		journalV3Provider.hentDokumentURL(request);
	}
	
	@Test
	public void shouldThrowExceptionWhenDokumentFilNotFound() throws Exception {
		abacPermit();
		setupExpectedException(InvalidFilUuidException.class.getName());

		journalV3Provider.hentDokumentURL(request);
	}
	
	@Test
	public void shouldGetDokumentUrl() throws Exception {
		abacPermit();
		persistDokumentFil();
		
		HentDokumentURLResponse response = journalV3Provider.hentDokumentURL(request);
			
		assertThat(response.getDokumentURL(), containsString("docToken"));
		assertDokumentUrlInfoIsPersisted(FIL_UUID);
	}

	@Test
	public void shouldThrowWhenJournalpostIsSkjermet() throws Exception {
		expectedException.expect(HentDokumentURLDokumentIkkeFunnet.class);
		expectedException.expectMessage("Journalpost ikke funnet");
		abacPermit();
		persistDokumentFil();

		skjermingService.setJournalpostSkjermet(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		journalV3Provider.hentDokumentURL(request);


	}

	@Test
	public void shouldGetSladdetDokumentUrl() throws Exception {
		abacPermit();
		persistDokumentFil();

		skjermingService.setVariantSkjermet(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HentDokumentURLResponse response = journalV3Provider.hentDokumentURL(request);

		assertThat(response.getDokumentURL(), containsString("docToken"));
		assertDokumentUrlInfoIsPersisted(FIL_UUID_SLADDET);
	}

	@Test
	public void shouldReturnFilUuidForArkivVariantWhenDokumentIsKassert() throws Exception {
		abacPermit();
		persistDokumentFil();

		skjermingService.skjermAllFildetaljer(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HentDokumentURLResponse response = journalV3Provider.hentDokumentURL(request);

		assertThat(response.getDokumentURL(), containsString("docToken"));
		assertDokumentUrlInfoIsPersisted(FIL_UUID);
	}

	private HentDokumentURLRequest createRequest() {
		request.setJournalpostId(journalpostId);
		request.setDokumentId(dokumentInfoId);
		Variantformater variantFormat = new Variantformater();
		variantFormat.setValue(VARIANT_FORMAT.name());
		request.setVariantformat(variantFormat);
		return request;
	}
	
	private void setupExpectedException(String rootCause) {
		expectedException.expect(HentDokumentURLDokumentIkkeFunnet.class);
		expectedException.expectMessage("Could not find document");
		expectedException.expect(hasProperty("faultInfo",
				hasProperty("feilaarsak", containsString(rootCause))));
	}
	
	private void assertDokumentUrlInfoIsPersisted(String filuuid) {
		DokumentUrlInfo dokUrlInfo = dokumentUrlInfoRepository.findByFilUuid(filuuid);
		assertThat(dokUrlInfo.getDoctoken(), notNullValue());
		assertThat(dokUrlInfo.getFilUuid(), is(filuuid));
	}
	
	private void createRequestFromJournalpost(Journalpost journalpost) { // hentet fra HentDokumentTest
		journalpostId = journalpost.getId().toString();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId().toString();
		createRequest();
	}
	
	private Journalpost buildAndPersistJournalpost(String dokumentTittel) { // hentet fra HentDokumentTest
		return joarkRepository.save(createJournalpostBuilder(dokumentTittel)
				.fagomrade(FagomradeCode.FOR).build());
	}
	
	
	private JournalpostBuilder createJournalpostBuilder(String dokumentTittel) { // hentet fra HentDokumentTest
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
																.opprettetKildeNavn("test").build(),
														FilDetaljerBuilder.getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.filUuid(FIL_UUID_SLADDET).variantFormat(VariantFormatCode.SLADDET)
																.opprettetKildeNavn("test").build()).build()).build());
	}

	private void persistDokumentFil() {
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
							.filUuid(FIL_UUID)
							.fil("Test".getBytes())
							.opprettetKildeNavn("test")
							.build());
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID_SLADDET)
				.fil("Test".getBytes())
				.opprettetKildeNavn("test")
				.build());
		dokumentFilRepository.save(DokumentFilBuilder.getDokumentFilBuilder()
				.filUuid(FIL_UUID_DUMMY_DOKUMENT)
				.fil("Dummy".getBytes())
				.opprettetKildeNavn("test")
				.build());
	}

}
