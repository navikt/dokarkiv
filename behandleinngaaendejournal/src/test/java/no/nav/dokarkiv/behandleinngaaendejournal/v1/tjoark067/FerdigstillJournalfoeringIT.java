package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokarkiv.core.datautil.JournalpostDokumentInfoRelasjonTestDataProvider.createVedleggRelasjon;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.behandleinngaaendejournal.v1.AbstractBehandleInngaaendeJournalV1Itest;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider;
import no.nav.dokarkiv.core.datautil.JournalpostDokumentInfoRelasjonTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringFerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Integration test for FerdigstillJournalpost(TJOARK067).
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class FerdigstillJournalfoeringIT extends AbstractBehandleInngaaendeJournalV1Itest {

	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String ENDRET_KILDE_NAVN = "endret_kilde";

	private static final String ENHET_ID = "9999";

	@Before
	public void setUp() throws Exception {
		MDC.put(MDCConstants.MDC_CONSUMER_ID, ENDRET_KILDE_NAVN);
		MDC.put(MDCConstants.MDC_USER_ID, INTERN_BRUKER_USER_ID);
		RequestContextSetter.setRequestContextForUnitTest();
		SubjectHandlerUtils.setInternBruker(INTERN_BRUKER_USER_ID);
	}

	@After
	public void tearDown() throws Exception {
		SubjectHandlerUtils.reset();
	}

	@Test
	public void shouldFerdigstillJournalAsAnInternBruker() throws Exception {
		abacPermit();
		SubjectHandlerUtils.setInternBruker(INTERN_BRUKER_USER_ID);

		MDC.put(MDCConstants.MDC_USER_ID, INTERN_BRUKER_USER_ID);
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost());

		FerdigstillJournalfoeringRequest request = createRequest(persistedJournalpost.getJournalpostId());
		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(request);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getJournalstatus(), equalTo(JournalStatusCode.J));
		assertThat(resultJournalpost.getJournalForendeEnhetId(), equalTo(ENHET_ID));
		assertThat(resultJournalpost.getJournalDato(), notNullValue());
		assertThat(resultJournalpost.getJournalfortAvNavn(), equalTo(INTERN_BRUKER_USER_NAVN));
		assertSporing(resultJournalpost);
	}

	@Test
	public void shouldFailWhenABACDenies() throws Exception {
		abacDeny();

		SubjectHandlerUtils.setInternBruker(INTERN_BRUKER_USER_ID);
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost());

		try {
			behandleInngaaendeJournalProvider.ferdigstillJournalfoering(createRequest(persistedJournalpost.getJournalpostId()));
			fail();
		} catch (FerdigstillJournalfoeringSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), equalTo("Bruker har ikke tilgang til journalpost"));
		}
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/ferdigstilljournalfoering.json"))));
	}

	@Test
	public void shouldAllowAccessWhenAbacPermits() throws Exception {
		abacPermit();
		SubjectHandlerUtils.setInternBruker(INTERN_BRUKER_USER_ID);
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost());

		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(createRequest(persistedJournalpost.getJournalpostId()));
	}

	@Test
	public void shouldFerdigstillJournalAsAnInternBrukerButLdapIsDown() throws Exception {
		MDC.put(MDCConstants.MDC_USER_ID, INTERN_BRUKER_LDAP_NA_USER_ID);
		SubjectHandlerUtils.setInternBruker(INTERN_BRUKER_LDAP_NA_USER_ID);

		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost());

		FerdigstillJournalfoeringRequest request = createRequest(persistedJournalpost.getJournalpostId());
		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(request);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getJournalstatus(), equalTo(JournalStatusCode.J));
		assertThat(resultJournalpost.getJournalForendeEnhetId(), equalTo(ENHET_ID));
		assertThat(resultJournalpost.getJournalDato(), notNullValue());
		assertThat(resultJournalpost.getJournalfortAvNavn(), equalTo(INTERN_BRUKER_LDAP_NA_USER_ID));
		assertSporing(resultJournalpost);
	}

	@Test
	public void shouldFerdigstillJournalAsSystemBruker() throws Exception {
		abacPermit();
		SubjectHandlerUtils.setSystemressurs(SYSTEMERESSURS_USER_ID);
		MDC.put(MDCConstants.MDC_USER_ID, SYSTEMERESSURS_USER_ID);
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost());

		FerdigstillJournalfoeringRequest request = createRequest(persistedJournalpost.getJournalpostId());
		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(request);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getJournalstatus(), equalTo(JournalStatusCode.J));
		assertThat(resultJournalpost.getJournalForendeEnhetId(), equalTo(ENHET_ID));
		assertThat(resultJournalpost.getJournalDato(), notNullValue());
		assertThat(resultJournalpost.getJournalfortAvNavn(), equalTo(SYSTEMERESSURS_USER_ID));
		assertSporing(resultJournalpost);
	}

	@Test
	public void shouldFerdigstillJournalWithoutUserId() throws Exception {
		abacPermit();
		SubjectHandlerUtils.reset();
		MDC.put(MDCConstants.MDC_USER_ID, null);
		MDC.put(MDCConstants.MDC_CONSUMER_ID, ENDRET_KILDE_NAVN);
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost());

		FerdigstillJournalfoeringRequest request = createRequest(persistedJournalpost.getJournalpostId());
		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(request);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getJournalstatus(), equalTo(JournalStatusCode.J));
		assertThat(resultJournalpost.getJournalForendeEnhetId(), equalTo(ENHET_ID));
		assertThat(resultJournalpost.getJournalDato(), notNullValue());
		assertThat(resultJournalpost.getJournalfortAvNavn(), equalTo("Ukjent"));
		assertSporing(resultJournalpost);
	}

	@Test
	public void shouldValidateJournalpostId() throws Exception {
		expectedException.expect(FerdigstillJournalfoeringUgyldigInput.class);

		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(createRequest(null));
	}

	@Test
	public void shouldValidateEnhetsId() throws Exception {
		expectedException.expect(FerdigstillJournalfoeringUgyldigInput.class);

		FerdigstillJournalfoeringRequest request = createRequest(1L);
		request.setEnhetId(null);
		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(request);
	}

	@Test
	public void shouldFailIfJournalpostNotExists() throws Exception {
		expectedException.expect(FerdigstillJournalfoeringObjektIkkeFunnet.class);

		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(createRequest(1L));
	}

	@Test
	public void shouldFailIfJournalpostIsUtgaande() throws Exception {
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost().journalpostType(JournalpostTypeCode.U));

		try {
			behandleInngaaendeJournalProvider.ferdigstillJournalfoering(createRequest(persistedJournalpost.getJournalpostId()));
			fail();
		} catch (FerdigstillJournalfoeringJournalpostIkkeInngaaende e) {
			Journalpost rolledBackJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
			assertChangesRolledBack(rolledBackJournalpost, JournalStatusCode.M);
		}
	}

	@Test
	public void shouldFailIfSaksrelasjonIsFeilregistrert() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost()
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon()
						.feilregistrert(true)
						.build()));

		ferdigstillExpectFerdigstillIkkeMuligAndRollback(persistedJournalpost.getJournalpostId());
	}

	@Test
	public void shouldFailIfJournalfoert() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost().journalStatus(JournalStatusCode.J));

		try {
			behandleInngaaendeJournalProvider.ferdigstillJournalfoering(createRequest(persistedJournalpost.getJournalpostId()));
			fail();
		} catch (FerdigstillJournalfoeringFerdigstillingIkkeMulig e) {
			Journalpost rolledBackJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
			assertChangesRolledBack(rolledBackJournalpost, JournalStatusCode.J);
		}
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldFailIfDokumentUnderRedigering() throws Exception {
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon()
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
										.tittel("dokumenttittel")
										.dokumenttypeId("dokumenttype")
										.kategori(DokumentKategoriCode.SOK)
										.filDetaljerList(FildetaljerTestDataProvider.createFilDetaljerArkiv().build())
										.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
										.build())
								.build()));

		ferdigstillExpectFerdigstillIkkeMuligAndRollback(persistedJournalpost.getJournalpostId());
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldFailIfMultipleHoveddokuments() throws Exception {
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost()
				.dokumentInfoRelasjoner(createHoveddokumentRelasjon()));

		ferdigstillExpectFerdigstillIkkeMuligAndRollback(persistedJournalpost.getJournalpostId());
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldFailIfNoFildetaljerIsArkiv() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = buildAndCommit(buildBaseJournalpost()
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonTestDataProvider.createHoveddokumentRelasjon()
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
										.dokumentFerdigDato(new Date())
										.tittel("dokumenttittel")
										.dokumenttypeId("dokumenttype")
										.filDetaljerList(FildetaljerTestDataProvider.createFilDetaljerProduksjon().build())
										.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
										.build())
								.build()
				));

		ferdigstillExpectFerdigstillIkkeMuligAndRollback(persistedJournalpost.getJournalpostId());
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldFailIfMultipleVariantFormatAreEqual() throws Exception {
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon()
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
										.tittel("dokumenttittel")
										.dokumenttypeId("dokumenttype")
										.filDetaljerList(FildetaljerTestDataProvider.createFilDetaljerArkiv().build(),
												FildetaljerTestDataProvider.createFilDetaljerArkiv().build())
										.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
										.build())
								.build()));

		ferdigstillExpectFerdigstillIkkeMuligAndRollback(persistedJournalpost.getJournalpostId());
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldFailIfJournalpostMissingInnhold() throws Exception {
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost().innhold(null));

		ferdigstillExpectFerdigstillIkkeMuligAndRollback(persistedJournalpost.getJournalpostId());
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void shouldFailIfJournalpostMissingAvsenderMottakerId() throws Exception {
		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost().avsenderMottakerId(null));

		ferdigstillExpectFerdigstillIkkeMuligAndRollback(persistedJournalpost.getJournalpostId());
	}

	public void ferdigstillExpectFerdigstillIkkeMuligAndRollback(Long journalpostId) throws Exception {
		try {
			abacPermit();
			behandleInngaaendeJournalProvider.ferdigstillJournalfoering(createRequest(journalpostId));
			fail();
		} catch (FerdigstillJournalfoeringFerdigstillingIkkeMulig e) {
			Journalpost rolledBackJournalpost = getPersistedJournalpostById(journalpostId);
			assertChangesRolledBack(rolledBackJournalpost, JournalStatusCode.M);
		}
	}

	private FerdigstillJournalfoeringRequest createRequest(Long journalpostId) {
		FerdigstillJournalfoeringRequest request = new FerdigstillJournalfoeringRequest();
		request.setJournalpostId(String.valueOf(journalpostId));
		request.setEnhetId(ENHET_ID);
		return request;
	}

	private void assertSporing(Journalpost resultJournalpost) {
		assertThat(resultJournalpost.getEndretKildeNavn(), equalTo(ENDRET_KILDE_NAVN));
		assertThat(resultJournalpost.getChangeStamp().getUpdatedBy(), notNullValue());

		assertThat(resultJournalpost.getSaksrelasjon().getChangeStamp().getUpdatedBy(), nullValue());
		assertThat(resultJournalpost.getSaksrelasjon().getChangeStamp().getUpdatedDate(), nullValue());
		assertThat(resultJournalpost.getSaksrelasjon().getEndretKildeNavn(), nullValue());
		Bruker bruker = resultJournalpost.getBrukere().iterator().next();
		assertThat(bruker.getChangeStamp().getUpdatedBy(), nullValue());
		assertThat(bruker.getChangeStamp().getUpdatedDate(), nullValue());
		assertThat(bruker.getEndretKildeNavn(), nullValue());
		JournalpostDokumentInfoRelasjon hovedRelasjon = resultJournalpost.getJournalpostDokumentInfoRelasjoner().iterator().next();
		assertThat(hovedRelasjon.getChangeStamp().getUpdatedBy(), nullValue());
		assertThat(hovedRelasjon.getChangeStamp().getUpdatedDate(), nullValue());
		assertThat(hovedRelasjon.getEndretKildeNavn(), nullValue());
		DokumentInfo dokumentInfo = hovedRelasjon.getDokumentInfo();
		assertThat(dokumentInfo.getChangeStamp().getUpdatedBy(), nullValue());
		assertThat(dokumentInfo.getChangeStamp().getUpdatedDate(), nullValue());
		assertThat(dokumentInfo.getEndretKildeNavn(), nullValue());
		FilDetaljer filDetaljer = dokumentInfo.getFildetaljerListe().iterator().next();
		assertThat(filDetaljer.getChangeStamp().getUpdatedBy(), nullValue());
		assertThat(filDetaljer.getChangeStamp().getUpdatedDate(), nullValue());
		assertThat(filDetaljer.getEndretKildeNavn(), nullValue());
	}

	private void assertChangesRolledBack(Journalpost journalpost, JournalStatusCode journalStatus) {
		assertThat(journalpost.getJournalstatus(), is(journalStatus));
		assertThat(journalpost.getJournalDato(), nullValue());
		assertThat(journalpost.getJournalForendeEnhetId(), nullValue());
		assertThat(journalpost.getJournalfortAvNavn(), nullValue());
		assertThat(journalpost.getEndretKildeNavn(), nullValue());
	}

	private JournalpostBuilder buildJournalpost() {
		return buildBaseJournalpost()
				.dokumentInfoRelasjoner(
						createHoveddokumentRelasjon()
				);
	}

	private JournalpostBuilder buildBaseJournalpost() {
		return getJournalpostBuilder()
				.journalStatus(JournalStatusCode.M)
				.journalpostType(JournalpostTypeCode.I)
				.fagomrade(FagomradeCode.FOR)
				.innhold("innhold")
				.dokumentDato(new Date())
				.avsenderMottaker("Test Testesen")
				.avsenderMottakerId("***gammelt_fnr***")
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.changeStamp(new ChangeStamp("userId"))
				.brukere(BrukerTestDataProvider.createBruker().build())
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon().build());
	}

	private JournalpostDokumentInfoRelasjon createHoveddokumentRelasjon() {
		return JournalpostDokumentInfoRelasjonTestDataProvider.createHoveddokumentRelasjon()
				.dokumentInfo(getDokumentInfoBuilder()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.dokumentFerdigDato(new Date())
						.tittel("dokumenttittel")
						.dokumenttypeId("dokumenttype")
						.kategori(DokumentKategoriCode.SOK)
						.sensitivt(false)
						.filDetaljerList(FildetaljerTestDataProvider.createFilDetaljerArkiv().build())
						.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
						.build())
				.build();
	}
}
