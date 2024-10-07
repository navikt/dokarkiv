package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.List;

import static no.nav.dokarkiv.core.util.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class})
@ActiveProfiles("itest")
class AvstemReferanseRepositoryTest {

	@Autowired
	private JournalpostTestRepository journalpostTestRepository;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private AvstemReferanseRepository avstemReferanseRepository;

	@BeforeEach
	void setup() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
		avstemReferanseRepository = new AvstemReferanseRepository(namedParameterJdbcTemplate);
	}

	@Test
	void shouldOnlyMatchEksternReferanseThatExists() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		var resultat = avstemReferanseRepository.findKanalReferanseIdsMatchedInDB(List.of(journalpost.getKanalReferanseId(), "enikkeeksisterendeid"));
		assertThat(resultat, hasSize(1));
		assertThat(resultat, contains(journalpost.getKanalReferanseId()));
	}

}