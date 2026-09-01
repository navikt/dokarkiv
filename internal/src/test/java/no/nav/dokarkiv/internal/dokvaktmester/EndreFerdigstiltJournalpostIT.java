package no.nav.dokarkiv.internal.dokvaktmester;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import no.nav.dokarkiv.core.api.Fagsaksystem;
import no.nav.dokarkiv.core.api.Sakstype;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.internal.AbstractInternalIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.BRUKER_BRUKER_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.SAKSRELASJON_SAKID;
import static no.nav.dokarkiv.core.api.Fagsaksystem.EF;
import static no.nav.dokarkiv.core.api.Fagsaksystem.UFM;
import static no.nav.dokarkiv.core.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.core.api.Sakstype.GENERELL_SAK;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_METADATA;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SAKSTILKNYTNING;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.PERSON;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AAPEN;
import static no.nav.dokarkiv.core.util.TestdataFactory.AKTOER_ID;
import static no.nav.dokarkiv.core.util.TestdataFactory.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestdataFactory.GSAK_APPLIKASJON;
import static no.nav.dokarkiv.core.util.TestdataFactory.GSAK_FAGSAKNR;
import static no.nav.dokarkiv.core.util.TestdataFactory.GSAK_TEMA;
import static no.nav.dokarkiv.core.util.TestdataFactory.createGsak;
import static no.nav.dokarkiv.core.util.TestdataFactory.createJournalpostForSakId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class EndreFerdigstiltJournalpostIT extends AbstractInternalIT {
	private static final String ENDRE_FERDIGSTILT_JOURNALPOST_PATH = "endreFerdigstiltJournalpost";
	private static final String GSAK_OPPRETTET_AV = "itest";
	public static final String BEGRUNNELSE_NOKKEL = "MMA-123";
	public static final String ANNEN_BRUKER_ID = "11111111111";
	public static final String ANNEN_AKTOER_ID = "1234567890123";
	public static final String NY_FAGSAKID = "UFM-123";
	public static final String EKSISTERENDE_FAGSAK = "ABC-123";
	public static final String EKSISTERENDE_FAGSAKSYSTEM = Fagsaksystem.EF.name();

	@Test
	public void skalOppretteNySakAnnetTema() {
		Sak sak = createGsak();
		sakTestRepository.persist(sak);
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(null, null, "FOR", BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo("FOR");
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isEqualTo(GSAK_FAGSAKNR);
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo(GSAK_APPLIKASJON);
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
	}

	@Test
	public void skalOppretteNySakForAnnenBruker() {
		stubAzure();
		pdlStub();

		Sak sak = createGsak();
		sakTestRepository.persist(sak);
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(ANNEN_BRUKER_ID, null, null, BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());
		assertAnnenBruker(oppdatertJournalpost);

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo(GSAK_TEMA);
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(ANNEN_AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isEqualTo(GSAK_FAGSAKNR);
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo(GSAK_APPLIKASJON);
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
		assertAksjonsLoggBruker(journalpostId);
	}

	@Test
	void skalBrukeEksisterendeFagsak() {
		Sak eksisterendeFagsak = createEksisterendeFagsak();
		Sak eksisterendeGenerellSak = createEksisterendeGenerellSak();
		Sak sak = createGsak();
		sakTestRepository.persistAll(List.of(sak, eksisterendeGenerellSak, eksisterendeFagsak));
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(null, new EndreSak(FAGSAK, EKSISTERENDE_FAGSAK, EF), null, BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo(GSAK_TEMA);
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isEqualTo(EKSISTERENDE_FAGSAK);
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo(EKSISTERENDE_FAGSAKSYSTEM);
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(GSAK_OPPRETTET_AV);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
	}

	@Test
	void skalBrukeEksisterendeGenerellSak() {
		Sak eksisterendeFagsak = createEksisterendeFagsak();
		Sak eksisterendeGenerellSak = createEksisterendeGenerellSak();
		Sak sak = createGsak();
		sakTestRepository.persistAll(List.of(sak, eksisterendeFagsak, eksisterendeGenerellSak));
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(null, new EndreSak(Sakstype.GENERELL_SAK, null, null), null, BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo(GSAK_TEMA);
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isNull();
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo("FS22");
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(GSAK_OPPRETTET_AV);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
	}

	@Test
	void skalBrukeNyFagsakSammeBruker() {
		Sak eksisterendeFagsak = createEksisterendeFagsak();
		Sak sak = createGsak();
		sakTestRepository.persistAll(List.of(sak, eksisterendeFagsak));
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(null, new EndreSak(FAGSAK, NY_FAGSAKID, UFM), null, BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo(GSAK_TEMA);
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isEqualTo(NY_FAGSAKID);
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo(UFM.name());
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
	}

	@Test
	void skalBrukeNyFagsakAnnenBruker() {
		stubAzure();
		pdlStub();

		Sak eksisterendeFagsak = createEksisterendeFagsak();
		Sak sak = createGsak();
		sakTestRepository.persistAll(List.of(sak, eksisterendeFagsak));
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(ANNEN_BRUKER_ID, new EndreSak(FAGSAK, NY_FAGSAKID, UFM), null, BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());
		assertAnnenBruker(oppdatertJournalpost);

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo(GSAK_TEMA);
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(ANNEN_AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isEqualTo(NY_FAGSAKID);
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo(UFM.name());
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
	}

	@Test
	void skalBrukeNyFagsakAnnenBrukerOgTema() {
		stubAzure();
		pdlStub();

		Sak eksisterendeFagsak = createEksisterendeFagsak();
		Sak sak = createGsak();
		sakTestRepository.persistAll(List.of(sak, eksisterendeFagsak));
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(ANNEN_BRUKER_ID, new EndreSak(FAGSAK, NY_FAGSAKID, UFM), "MED", BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());
		assertAnnenBruker(oppdatertJournalpost);

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo("MED");
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(ANNEN_AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isEqualTo(NY_FAGSAKID);
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo(UFM.name());
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
	}

	@Test
	void skalBrukeNyGenerellSakAnnenBruker() {
		stubAzure();
		pdlStub();
		Sak eksisterendeGenerellSak = createEksisterendeGenerellSak();
		Sak sak = createGsak();
		sakTestRepository.persistAll(List.of(sak, eksisterendeGenerellSak));
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(ANNEN_BRUKER_ID, new EndreSak(GENERELL_SAK, null, null), null, BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo(GSAK_TEMA);
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(ANNEN_AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isNull();
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo("FS22");
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
		assertAksjonsLoggBruker(journalpostId);
	}

	@Test
	void skalBrukeNyGenerellSakAnnenBrukerOgTema() {
		stubAzure();
		pdlStub();
		Sak eksisterendeGenerellSak = createEksisterendeGenerellSak();
		Sak sak = createGsak();
		sakTestRepository.persistAll(List.of(sak, eksisterendeGenerellSak));
		Journalpost journalpost = createJournalpostForSakId(sak.getSakId());
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(new EndreFerdigstiltJournalpostRequest(ANNEN_BRUKER_ID, new EndreSak(GENERELL_SAK, null, null), "FOR", BEGRUNNELSE_NOKKEL),
				createHeadersWithServiceUserTokenAndRolesClaim("api_intern"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalJournalpostPath(journalpostId.toString(), ENDRE_FERDIGSTILT_JOURNALPOST_PATH), PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).orElseThrow();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isNotEqualTo(sak.getSakId());
		assertSaksrelasjon(oppdatertJournalpost.getSaksrelasjon());

		Sak oppdatertSak = sakTestRepository.findById(oppdatertJournalpost.getSaksrelasjon().getSakId()).get();
		assertThat(oppdatertSak.getTema()).isEqualTo("FOR");
		assertThat(oppdatertSak.getAktoerId()).isEqualTo(ANNEN_AKTOER_ID);
		assertThat(oppdatertSak.getFagsakNr()).isNull();
		assertThat(oppdatertSak.getApplikasjon()).isEqualTo("FS22");
		assertThat(oppdatertSak.getOpprettetAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
		assertAksjonsLoggSaksrelasjon(journalpostId, sak, oppdatertSak);
		assertAksjonsLoggBruker(journalpostId);
	}

	private void assertAksjonsLoggSaksrelasjon(Long journalpostId, Sak sak, Sak oppdatertSak) {
		List<AksjonsLogg> aksjoner = aksjonsLoggTestRepository.getAksjonsLoggByJournalpostId(journalpostId);
		assertThat(aksjoner.stream().filter(aksjonsLogg -> aksjonsLogg.getAksjon() == SAKSTILKNYTNING))
				.singleElement()
				.satisfies(aksjonsLogg -> {
					assertThat(aksjonsLogg.getAksjon()).isEqualTo(SAKSTILKNYTNING);
					assertThat(aksjonsLogg.getJournalpostId()).isEqualTo(journalpostId);
					assertThat(aksjonsLogg.getUtfoertAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
					assertThat(aksjonsLogg.getArkivElementEndringer())
							.singleElement()
							.satisfies(arkivElementEndring -> {
								assertThat(arkivElementEndring.getArkivElement()).isEqualTo(SAKSRELASJON_SAKID);
								assertThat(arkivElementEndring.getFraVerdi()).isEqualTo(sak.getSakId().toString());
								assertThat(arkivElementEndring.getTilVerdi()).isEqualTo(oppdatertSak.getSakId().toString());
							});
				});
	}

	private void assertAksjonsLoggBruker(Long journalpostId) {
		List<AksjonsLogg> aksjoner = aksjonsLoggTestRepository.getAksjonsLoggByJournalpostId(journalpostId);
		assertThat(aksjoner.stream().filter(aksjonsLogg -> aksjonsLogg.getAksjon() == ENDRE_METADATA))
				.singleElement()
				.satisfies(aksjonsLogg -> {
					assertThat(aksjonsLogg.getAksjon()).isEqualTo(ENDRE_METADATA);
					assertThat(aksjonsLogg.getJournalpostId()).isEqualTo(journalpostId);
					assertThat(aksjonsLogg.getUtfoertAv()).isEqualTo(BEGRUNNELSE_NOKKEL);
					assertThat(aksjonsLogg.getArkivElementEndringer())
							.singleElement()
							.satisfies(arkivElementEndring -> {
								assertThat(arkivElementEndring.getArkivElement()).isEqualTo(BRUKER_BRUKER_ID);
								assertThat(arkivElementEndring.getFraVerdi()).isEqualTo(BRUKER_ID);
								assertThat(arkivElementEndring.getTilVerdi()).isEqualTo(ANNEN_BRUKER_ID);
							});
				});
	}

	private static void assertSaksrelasjon(Saksrelasjon saksrelasjon) {
		assertThat(saksrelasjon.getEndretKildeNavn()).isEqualTo(BEGRUNNELSE_NOKKEL);
	}

	private static void assertAnnenBruker(Journalpost oppdatertJournalpost) {
		Bruker bruker = oppdatertJournalpost.getBrukere().stream().max(Comparator.comparing(Bruker::getBrukerInfoId)).orElseThrow();
		assertThat(bruker.getBrukerId()).isEqualTo(ANNEN_BRUKER_ID);
		assertThat(bruker.getBrukerType()).isEqualTo(PERSON);
		assertThat(bruker.getEndretKildeNavn()).isEqualTo(BEGRUNNELSE_NOKKEL);
	}

	private void pdlStub() {
		stubFor(post(urlEqualTo("/pdl"))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/pdl-aktoerid-happy.json")));
	}

	private Sak createEksisterendeFagsak() {
		return Sak.builder()
				.aktoerId(AKTOER_ID)
				.fagsakNr(EKSISTERENDE_FAGSAK)
				.tema(GSAK_TEMA)
				.sakStatus(AAPEN)
				.applikasjon(EKSISTERENDE_FAGSAKSYSTEM)
				.opprettetAv(GSAK_OPPRETTET_AV)
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
	}

	private Sak createEksisterendeGenerellSak() {
		return Sak.builder()
				.aktoerId(AKTOER_ID)
				.fagsakNr(null)
				.tema(GSAK_TEMA)
				.sakStatus(AAPEN)
				.applikasjon("FS22")
				.opprettetAv(GSAK_OPPRETTET_AV)
				.opprettetTidspunkt(LocalDateTime.now())
				.build();
	}
}
