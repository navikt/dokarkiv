package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.Ordering;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.AbstractBehandleInngaaendeJournalV1Itest;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.datautil.BrukerTestDataProvider;
import no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider;
import no.nav.dokarkiv.core.datautil.JournalpostDokumentInfoRelasjonTestDataProvider;
import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostOppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.ArkivSak;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Avsender;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Dokumentkategori;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Integration test for OppdaterJournalpost(TJOARK066).
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Ignore
public class OppdaterJournalpostIT extends AbstractBehandleInngaaendeJournalV1Itest {
	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String INTERN_USER_NAME = "Dølle duck";
	private static final String TITTEL = "Foreldrepenger!";
	private static final String FNR = "***gammelt_fnr***";
	private static final String ORGNR = "999999999";
	private static final String FNR_2 = "***gammelt_fnr***";
	private static final String AVSENDER_MOTTAKER = "Test Testesen";
	private static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	private static final Date DOKUMENT_DATO = new Date();
	private static final String INNHOLD = "innhold";
	private static final String ENDRET_KILDE_NAVN = "Endret Kildenavn";
	private static final String UGYLDIG_HOVEDOKUMENT = "42";

	@Before
	public void setUp() throws Exception {
		MDC.put(MDCConstants.MDC_CONSUMER_ID, ENDRET_KILDE_NAVN);
		MDC.put(MDCConstants.MDC_USER_ID, INTERN_USER_NAME);
		RequestContextSetter.setRequestContextForUnitTest();
//		when(ldapTemplate.search(argThat(new IsInternBruker()), any(NameMapper.class))).thenReturn(Lists.newArrayList(INTERN_USER_NAME));
//		when(ldapTemplate.search(argThat(new IsLdapNAUser()), any(NameMapper.class))).thenThrow(new InvalidNameException(null));
		SubjectHandlerUtils.setInternBruker(INTERN_BRUKER_USER_ID);
	}

	@After
	public void tearDown() throws Exception {
		SubjectHandlerUtils.reset();
	}

	@Test
	public void shouldFailWhenPDPDenies() throws Exception {
		abacDeny();

		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost());

		try {
			behandleInngaaendeJournalProvider.oppdaterJournalpost(defaultRequest(persistedJournalpost.getJournalpostId()));
			fail();
		} catch (OppdaterJournalpostSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), equalTo("Bruker har ikke tilgang til journalpost"));
		}

		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/oppdaterjournalpost.json"))));
	}

	@Test
	public void shouldAllowAccessWhenAbacPermits() throws Exception {
		abacPermit();

		Journalpost persistedJournalpost = buildAndCommit(buildJournalpost());

		behandleInngaaendeJournalProvider.oppdaterJournalpost(defaultRequest(persistedJournalpost.getJournalpostId()));
	}

	@Test
	public void shouldUpdateInnholdAsInternBruker() throws Exception {
		String innhold = "innhold";
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.innhold(null)
				.build());

		OppdaterJournalpostRequest journalpostRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		inngaaendeJournalpost.setInnhold(innhold);
		journalpostRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(journalpostRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getInnhold(), equalTo(innhold));
		assertThat(resultJournalpost.getEndretAvNavn(), equalTo(INTERN_USER_NAME));
		assertThat(resultJournalpost.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
	}

	@Test
	public void shouldUpdateInnholdAsInternBrukerButLdapNA() throws Exception {
		SubjectHandlerUtils.setInternBruker(INTERN_BRUKER_LDAP_NA_USER_ID);
		MDC.put(MDCConstants.MDC_USER_ID, INTERN_BRUKER_LDAP_NA_USER_ID);
		String innhold = "innhold";
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.innhold(null)
				.build());

		OppdaterJournalpostRequest journalpostRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		inngaaendeJournalpost.setInnhold(innhold);
		journalpostRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(journalpostRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getInnhold(), equalTo(innhold));
		assertThat(resultJournalpost.getEndretAvNavn(), equalTo(INTERN_BRUKER_LDAP_NA_USER_ID));
		assertThat(resultJournalpost.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
	}

	@Test
	public void shouldUpdateInnholdAsSystemUser() throws Exception {
		abacPermit();
		SubjectHandlerUtils.setSystemressurs(SYSTEMERESSURS_USER_ID);
		MDC.put(MDCConstants.MDC_USER_ID, SYSTEMERESSURS_USER_ID);
		String innhold = "innhold";
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.innhold(null)
				.build());

		OppdaterJournalpostRequest journalpostRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		inngaaendeJournalpost.setInnhold(innhold);
		journalpostRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(journalpostRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getInnhold(), equalTo(innhold));
		assertThat(resultJournalpost.getEndretAvNavn(), equalTo(SYSTEMERESSURS_USER_ID));
		assertThat(resultJournalpost.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
	}

	@Test
	public void shouldUpdateInnholdWithoutUserId() throws Exception {
		abacPermit();
		SubjectHandlerUtils.reset();
		MDC.put(MDCConstants.MDC_USER_ID, null);

		String innhold = "innhold";
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.innhold(null)
				.build());

		OppdaterJournalpostRequest journalpostRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		inngaaendeJournalpost.setInnhold(innhold);
		journalpostRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(journalpostRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getInnhold(), equalTo(innhold));
		assertThat(resultJournalpost.getEndretAvNavn(), equalTo("Ukjent"));
		assertThat(resultJournalpost.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
	}

	@Test
	public void shouldUpdateJournalpost() throws Exception {
		abacPermit();
		String innhold = "innhold";
		String avsendMottakId = "avsenderId";
		String avsendMottaker = "avsenderMottaker";
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.innhold(null)
				.dokumentDato(null)
				.fagomrade(null)
				.avsenderMottaker(null)
				.avsenderMottakerId(null)
				.build());

		OppdaterJournalpostRequest journalpostRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		inngaaendeJournalpost.setInnhold(innhold);
		inngaaendeJournalpost.setTema(createTemaFOR());
		Avsender avsender = new Avsender();
		avsender.setAvsenderId(avsendMottakId);
		avsender.setAvsenderNavn(avsendMottaker);
		inngaaendeJournalpost.setAvsender(avsender);
		journalpostRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(journalpostRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getInnhold(), equalTo(innhold));
		assertThat(resultJournalpost.getFagomrade(), equalTo(FagomradeCode.FOR));
		assertThat(resultJournalpost.getAvsenderMottaker(), equalTo(avsendMottaker));
		assertThat(resultJournalpost.getAvsenderMottakerId(), equalTo(avsendMottakId));
		assertThat(resultJournalpost.getEndretAvNavn(), equalTo(INTERN_USER_NAME));
		assertThat(resultJournalpost.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
		assertThat(resultJournalpost.getSaksrelasjon().getEndretAvNavn(), nullValue());
	}

	@Test
	public void shouldNotOverwriteJournalpostValuesWithNull() throws Exception {
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.build());

		OppdaterJournalpostRequest journalpostRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		inngaaendeJournalpost.setInnhold(null);
		inngaaendeJournalpost.setTema(null);
		Avsender avsender = new Avsender();
		avsender.setAvsenderId(null);
		avsender.setAvsenderNavn(null);
		inngaaendeJournalpost.setAvsender(avsender);
		journalpostRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(journalpostRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		assertThat(resultJournalpost.getInnhold(), equalTo(INNHOLD));
		assertThat(resultJournalpost.getFagomrade(), equalTo(FagomradeCode.FOR));
		assertThat(resultJournalpost.getAvsenderMottaker(), equalTo(AVSENDER_MOTTAKER));
		assertThat(resultJournalpost.getAvsenderMottakerId(), equalTo(AVSENDER_MOTTAKER_ID));
		assertThat(resultJournalpost.getEndretAvNavn(), nullValue());
		assertThat(resultJournalpost.getEndretKildeNavn(), nullValue());
		assertThat(resultJournalpost.getSaksrelasjon().getEndretAvNavn(), nullValue());
	}

	@Test
	public void shouldUpdateSaksrelasjon() throws Exception {
		FagsystemCode fagsystem = FagsystemCode.FS22;
		String saksnummer = "00002";
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.saksrelasjon(null)
				.build());

		OppdaterJournalpostRequest journalpostRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		ArkivSak arkivSak = new ArkivSak();
		arkivSak.setArkivSakId(saksnummer);
		arkivSak.setArkivSakSystem(fagsystem.name());
		inngaaendeJournalpost.setArkivSak(arkivSak);
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		journalpostRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(journalpostRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		Saksrelasjon saksrelasjon = resultJournalpost.getSaksrelasjon();
		assertThat(saksrelasjon, notNullValue());
		assertThat(saksrelasjon.getEndretAvNavn(), equalTo(INTERN_USER_NAME));
		assertThat(saksrelasjon.getFagsystem(), equalTo(fagsystem));
		assertThat(saksrelasjon.getSakId(), equalTo(saksnummer));
		assertThat(saksrelasjon.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
		assertThat(resultJournalpost.getEndretAvNavn(), nullValue());
	}

	@Test
	public void shouldUpdateHoveddokumentOnJournalpost() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost().dokumentInfoRelasjoner(createVedleggRelasjon()).build());
		String hovedDokumentInfoId = persistedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId().toString();

		OppdaterJournalpostRequest oppdaterRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
		dokumentinformasjon.setDokumentId(hovedDokumentInfoId);
		dokumentinformasjon.setTittel(TITTEL);
		dokumentinformasjon.setDokumentkategori(createDokumentKategoriSOK());
		inngaaendeJournalpost.setHoveddokument(dokumentinformasjon);
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		oppdaterRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(oppdaterRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		DokumentInfo hovedDokumentInfo = resultJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		assertThat(hovedDokumentInfo.getTittel(), is(TITTEL));
		assertThat(hovedDokumentInfo.getKategori(), is(DokumentKategoriCode.SOK));
		assertThat(hovedDokumentInfo.getEndretAvNavn(), is(INTERN_USER_NAME));
		assertThat(hovedDokumentInfo.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
		assertThat(resultJournalpost.getEndretAvNavn(), nullValue());
		assertThat(resultJournalpost.getSaksrelasjon().getEndretAvNavn(), nullValue());
		Set<JournalpostDokumentInfoRelasjon> vedlegg = resultJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		assertThat(vedlegg, hasSize(1));
		for (JournalpostDokumentInfoRelasjon relasjon : vedlegg) {
			assertThat(relasjon.getDokumentInfo().getEndretAvNavn(), nullValue());
		}
	}

	@Test
	public void shouldUpdateVedleggOnJournalpost() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost().dokumentInfoRelasjoner(createVedleggRelasjon()).build());
		String vedleggDokumentInfoId = persistedJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo().getDokumentInfoId().toString();

		OppdaterJournalpostRequest oppdaterRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
		dokumentinformasjon.setDokumentId(vedleggDokumentInfoId);
		dokumentinformasjon.setTittel(TITTEL);
		Dokumentkategori dokumentkategori = new Dokumentkategori();
		dokumentkategori.setValue("ES");
		dokumentinformasjon.setDokumentkategori(dokumentkategori);
		inngaaendeJournalpost.getVedleggListe().add(dokumentinformasjon);
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		oppdaterRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(oppdaterRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());
		DokumentInfo vedlegg = resultJournalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();
		assertThat(vedlegg.getTittel(), is(TITTEL));
		assertThat(vedlegg.getKategori(), is(DokumentKategoriCode.ES));
		assertThat(vedlegg.getEndretAvNavn(), is(INTERN_USER_NAME));
		assertThat(vedlegg.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
		assertThat(resultJournalpost.getEndretAvNavn(), nullValue());
		JournalpostDokumentInfoRelasjon hovedDokument = resultJournalpost.findHoveddokumentDokumentInfoRelasjon();
		assertThat(hovedDokument.getDokumentInfo().getEndretAvNavn(), nullValue());
	}

	@Test
	public void shouldUpdateBrukerWhenOnlyOneBrukerIsTilknyttet() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpostNoBruker().brukere(BrukerTestDataProvider.createBruker()
				.brukerId(FNR).brukerType(BrukerTypeCode.PERSON).build()).build());

		OppdaterJournalpostRequest oppdaterRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		Organisasjon organisasjon = new Organisasjon();
		organisasjon.setOrganisasjonsnummer(ORGNR);
		inngaaendeJournalpost.setBruker(organisasjon);
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		oppdaterRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(oppdaterRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());

		Bruker bruker = resultJournalpost.getBrukere().iterator().next();
		assertThat(bruker.getBrukerId(), is(ORGNR));
		assertThat(bruker.getBrukerType(), is(BrukerTypeCode.ORGANISASJON));
		assertThat(bruker.getChangeStamp().getUpdatedDate(), nullValue());
		assertThat(bruker.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
		assertThat(resultJournalpost.getEndretAvNavn(), nullValue());
	}

	@Test
	public void shouldUpdateLatestBrukerWhenMultipleBrukereTilknyttet() throws Exception {
		abacPermit();
		LocalDateTime twelveOclock = LocalDateTime.parse("2017-05-17T12:00:00");
		LocalDateTime oneOClock = LocalDateTime.parse("2017-05-17T13:00:00");

		Bruker aPerson = BrukerTestDataProvider.createBruker().brukerId(FNR).brukerType(BrukerTypeCode.PERSON)
				.changeStamp(new ChangeStamp("user", twelveOclock.toDate(), null, null)).build();
		Bruker anOrg = BrukerTestDataProvider.createBruker().brukerId(ORGNR).brukerType(BrukerTypeCode.ORGANISASJON)
				.changeStamp(new ChangeStamp("user", oneOClock.toDate(), null, null)).build();
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpostNoBruker().brukere(aPerson, anOrg).build());

		OppdaterJournalpostRequest oppdaterRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		Person person = new Person();
		person.setIdent(FNR_2);
		inngaaendeJournalpost.setBruker(person);
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		oppdaterRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(oppdaterRequest);

		Journalpost resultJournalpost = getPersistedJournalpostById(persistedJournalpost.getJournalpostId());

		assertThat(resultJournalpost.getBrukere(), hasSize(2));
		Bruker bruker = getLatestBruker(resultJournalpost);
		assertThat(bruker.getBrukerId(), is(FNR_2));
		assertThat(bruker.getBrukerType(), is(BrukerTypeCode.PERSON));
		assertThat(bruker.getEndretKildeNavn(), is(equalTo(ENDRET_KILDE_NAVN)));
		assertThat(bruker.getChangeStamp().getCreatedDate(), equalTo(oneOClock.toDate()));
		assertThat(bruker.getChangeStamp().getUpdatedDate(), notNullValue());
		assertThat(resultJournalpost.getEndretAvNavn(), nullValue());
	}

	@Test
	public void shouldFailIfRequiredInputJournalpostIdIsMissing() throws Exception {
		expectedException.expect(OppdaterJournalpostUgyldigInput.class);

		OppdaterJournalpostRequest request = defaultRequest((String) null);
		behandleInngaaendeJournalProvider.oppdaterJournalpost(request);
	}

	@Test
	public void shouldFailIfRequiredInputJournalpostIdIsInvalid() throws Exception {
		expectedException.expect(OppdaterJournalpostUgyldigInput.class);

		OppdaterJournalpostRequest request = defaultRequest("");
		behandleInngaaendeJournalProvider.oppdaterJournalpost(request);
	}

	@Test
	public void shouldFailIfInvalidTemaKodeverk() throws Exception {
		expectedException.expect(OppdaterJournalpostUgyldigInput.class);

		OppdaterJournalpostRequest request = defaultRequest(1L);
		Tema tema = new Tema();
		tema.setValue("RABARBARA");
		request.getInngaaendeJournalpost().setTema(tema);
		behandleInngaaendeJournalProvider.oppdaterJournalpost(request);
	}

	@Test
	public void shouldFailIfInvalidArkivSystemKodeverk() throws Exception {
		expectedException.expect(OppdaterJournalpostUgyldigInput.class);

		OppdaterJournalpostRequest request = defaultRequest(1L);

		ArkivSak arkivSak = new ArkivSak();
		arkivSak.setArkivSakId("0002");
		arkivSak.setArkivSakSystem("SUPPE");
		request.getInngaaendeJournalpost().setArkivSak(arkivSak);
		behandleInngaaendeJournalProvider.oppdaterJournalpost(request);
	}

	@Test
	public void shouldFailIfInvalidHovedDokumentKategori() throws Exception {
		expectedException.expect(OppdaterJournalpostUgyldigInput.class);

		OppdaterJournalpostRequest request = defaultRequest(1L);

		Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
		dokumentinformasjon.setDokumentId("1");
		dokumentinformasjon.setTittel("tittel");
		Dokumentkategori dokumentkategori = new Dokumentkategori();
		dokumentkategori.setValue("MISTET_I_BAKKEN");
		dokumentinformasjon.setDokumentkategori(dokumentkategori);
		request.getInngaaendeJournalpost().setHoveddokument(dokumentinformasjon);
		behandleInngaaendeJournalProvider.oppdaterJournalpost(request);
	}

	@Test
	public void shouldFailIfInvalidVedleggDokumentKategori() throws Exception {
		expectedException.expect(OppdaterJournalpostUgyldigInput.class);

		OppdaterJournalpostRequest request = defaultRequest(1L);

		Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
		dokumentinformasjon.setDokumentId("1");
		dokumentinformasjon.setTittel("tittel");
		Dokumentkategori dokumentkategori = new Dokumentkategori();
		dokumentkategori.setValue("MISTET_I_BAKKEN");
		dokumentinformasjon.setDokumentkategori(dokumentkategori);
		request.getInngaaendeJournalpost().getVedleggListe().add(dokumentinformasjon);
		behandleInngaaendeJournalProvider.oppdaterJournalpost(request);
	}

	@Test
	public void shouldFailIfJournalpostNotExists() throws Exception {
		expectedException.expect(OppdaterJournalpostObjektIkkeFunnet.class);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(defaultRequest(1L));
	}

	@Test
	public void shouldFailIfJournalpostIsUtgaande() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.journalpostType(JournalpostTypeCode.U)
				.build());

		expectedException.expect(OppdaterJournalpostJournalpostIkkeInngaaende.class);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(defaultRequest(persistedJournalpost.getJournalpostId()));
	}

	@Test
	public void shouldFailIfHoveddokumentDoesNotBelongToJournalpost() throws Exception {
		abacPermit();
		expectedException.expect(OppdaterJournalpostObjektIkkeFunnet.class);

		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost().dokumentInfoRelasjoner(createVedleggRelasjon()).build());

		OppdaterJournalpostRequest oppdaterRequest = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
		dokumentinformasjon.setDokumentId(UGYLDIG_HOVEDOKUMENT);
		inngaaendeJournalpost.setHoveddokument(dokumentinformasjon);
		inngaaendeJournalpost.setJournalpostId(String.valueOf(persistedJournalpost.getJournalpostId()));
		oppdaterRequest.setInngaaendeJournalpost(inngaaendeJournalpost);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(oppdaterRequest);
	}

	@Test
	public void shouldFailIfSaksrelasjonIsFeilregistrert() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon()
						.feilregistrert(true)
						.build())
				.build());

		expectedException.expect(OppdaterJournalpostOppdateringIkkeMulig.class);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(defaultRequest(persistedJournalpost.getJournalpostId()));
	}

	@Test
	public void shouldFailIfJournalfoert() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.journalStatus(JournalStatusCode.J)
				.build());

		expectedException.expect(OppdaterJournalpostOppdateringIkkeMulig.class);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(defaultRequest(persistedJournalpost.getJournalpostId()));
	}

	@Test
	public void shouldFailIfDokumentUnderRedigering() throws Exception {
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonTestDataProvider.createVedleggRelasjon()
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
										.tittel("dokumenttittel")
										.dokumenttypeId("dokumenttype")
										.filDetaljerList(FildetaljerTestDataProvider.createFilDetaljerArkiv().build())
										.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
										.build())
								.build())
				.build());

		expectedException.expect(OppdaterJournalpostOppdateringIkkeMulig.class);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(defaultRequest(persistedJournalpost.getJournalpostId()));
	}

	@Test
	public void shouldFailIfDokumentAvbrutt() throws Exception {
		abacPermit();
		Journalpost persistedJournalpost = joarkRepository.save(buildJournalpost()
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonTestDataProvider.createVedleggRelasjon()
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentstatus(DokumentStatusCode.AVBRUTT)
										.tittel("dokumenttittel")
										.dokumenttypeId("dokumenttype")
										.filDetaljerList(FildetaljerTestDataProvider.createFilDetaljerArkiv().build())
										.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
										.build())
								.build())
				.build());

		expectedException.expect(OppdaterJournalpostOppdateringIkkeMulig.class);

		behandleInngaaendeJournalProvider.oppdaterJournalpost(defaultRequest(persistedJournalpost.getJournalpostId()));
	}

	private Bruker getLatestBruker(Journalpost journalpost) {
		assert(!journalpost.getBrukere().isEmpty());
		List<Bruker> sortedCopy = Ordering.from((Comparator<Bruker>) (o1, o2) -> LocalDateTime.fromDateFields(o2.getChangeStamp().getCreatedDate()).compareTo(LocalDateTime.fromDateFields(o1.getChangeStamp().getCreatedDate()))).sortedCopy(journalpost.getBrukere());
		return sortedCopy.get(0);
	}

	private OppdaterJournalpostRequest defaultRequest(Long journalpostId) {
		return defaultRequest(String.valueOf(journalpostId));
	}

	private OppdaterJournalpostRequest defaultRequest(String journalpostId) {
		OppdaterJournalpostRequest request = new OppdaterJournalpostRequest();
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		inngaaendeJournalpost.setJournalpostId(journalpostId);
		request.setInngaaendeJournalpost(inngaaendeJournalpost);
		return request;
	}


	private Dokumentkategori createDokumentKategoriSOK() {
		Dokumentkategori dokumentkategori = new Dokumentkategori();
		dokumentkategori.setValue(DokumentKategoriCode.SOK.name());
		return dokumentkategori;
	}

	private Tema createTemaFOR() {
		Tema tema = new Tema();
		tema.setValue(FagomradeCode.FOR.name());
		return tema;
	}

	private JournalpostBuilder buildJournalpost() {
		return buildBaseJournalpost()
				.brukere(BrukerTestDataProvider.createBruker().build())
				.dokumentInfoRelasjoner(
						createHoveddokumentRelasjon()
				);
	}

	private JournalpostBuilder buildJournalpostNoBruker() {
		return buildBaseJournalpost()
				.dokumentInfoRelasjoner(createHoveddokumentRelasjon());
	}

	private JournalpostBuilder buildBaseJournalpost() {
		return getJournalpostBuilder()
				.journalStatus(JournalStatusCode.M)
				.journalpostType(JournalpostTypeCode.I)
				.fagomrade(FagomradeCode.FOR)
				.innhold(INNHOLD)
				.dokumentDato(DOKUMENT_DATO)
				.avsenderMottaker(AVSENDER_MOTTAKER)
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon().build());
	}

	private JournalpostDokumentInfoRelasjon createHoveddokumentRelasjon() {
		return JournalpostDokumentInfoRelasjonTestDataProvider.createHoveddokumentRelasjon()
				.dokumentInfo(getDokumentInfoBuilder()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.dokumentFerdigDato(new Date())
						.tittel("dokumenttittel")
						.dokumenttypeId("dokumenttype")
						.filDetaljerList(FildetaljerTestDataProvider.createFilDetaljerArkiv().build())
						.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
						.build())
				.build();
	}

	private JournalpostDokumentInfoRelasjon createVedleggRelasjon() {
		return JournalpostDokumentInfoRelasjonTestDataProvider.createVedleggRelasjon()
				.dokumentInfo(getDokumentInfoBuilder()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.dokumentFerdigDato(new Date())
						.tittel("dokumenttittel")
						.dokumenttypeId("dokumenttype")
						.filDetaljerList(FildetaljerTestDataProvider.createFilDetaljerArkiv().build())
						.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
						.build())
				.build();
	}
}
