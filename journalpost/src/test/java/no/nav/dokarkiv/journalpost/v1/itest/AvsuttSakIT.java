package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.SakStatusCode;
import no.nav.dokarkiv.core.domain.entities.Sak;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.avsluttSak.AvsluttSakRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createSakForAktoerId;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.util.AvvikstypeConstants.FEILREGISTRER_SAKSTILKNYTNING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpMethod.PATCH;

public class AvsuttSakIT extends AbstractJournalpostIT {

	private static final String SAKSTYPE_FAGSAK = "FAGSAK";
	private static final String SAKSTYPE_GENERELL = "GENERELL_SAK";
	private static final String FAGSAK_ID = "0123A21";
	private static final String FAGSAK_SYSTEM = "IT01";
	private static final String TEMA = "SYK";
	private static final String BRUKER_ID = "12345612345";
	private static final String AKTOER_ID = "1234567890123";
	private static final String ORG_NR = "123456789";
	private static final long KILDE_JOURNALPOST_ID = 111111111;
	private static final String JOURNALFOERENDE_ENHET = "9999";
	private static final String URL_AVSLUTT_SAK = "/rest/journalpostapi/v1/sak/avsluttSak";

	@Test
	public void happyPath() {
		stubAzure();
		happyAktoerIdStub();
		Sak sak = createSakForAktoerId(TEMA, AKTOER_ID, FAGSAK_SYSTEM, FAGSAK_ID);
		Long sakId = sakTestRepository.persist(sak).getSakId();
		commitAndStartNewTransaction();

		AvsluttSakRequest avsluttSakRequest = AvsluttSakRequest.builder()
				.avsluttetDato(LocalDateTime.of(2023, 1, 1, 1, 1))
				.sakAnsvarlig("Frankly Frank")
				.tema(TEMA)
				.opprettetDato(LocalDateTime.of(2010, 1, 1, 1, 1))
				.fagsakId(FAGSAK_ID)
				.bruker(new Bruker(AKTOERID, AKTOER_ID))
				.administrativEnhet("9999")
				.fagsaksystem(FAGSAK_SYSTEM)
				.build();
		var requestEntity = new HttpEntity<>(avsluttSakRequest, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(URL_AVSLUTT_SAK, PATCH, requestEntity, String.class);


		commitAndStartNewTransaction();
		Sak updatedSak = sakTestRepository.findById(sakId).get();
		System.out.println("Sakstatus: " + updatedSak.getSakStatus());
		assertThat(updatedSak.getSakStatus(), is(SakStatusCode.AVSLUTTET));
	}

}
