package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.transaction.TestTransaction;

import javax.annotation.Resource;

import java.util.List;

import static no.nav.dokarkiv.core.util.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfig.class, SkjermingService.class})
@ActiveProfiles("itest")
class AvstemReferanseRepositoryTest {

	@Resource
	private JournalpostTestRepository journalpostTestRepository;

	@Resource
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private AvstemReferanseRepository avstemReferanseRepository;

	@BeforeEach
	void setup() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
		avstemReferanseRepository = new AvstemReferanseRepository(namedParameterJdbcTemplate);
	}

	@Test
	void shouldOnlyMatchEksternReferanseThatExists() {
		var journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(null);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		var resultat = avstemReferanseRepository.findKanalReferanseIdsNotMatchedInDB(List.of(journalpost.getKanalReferanseId(), "enikkeeksisterendeid"));
		assertThat(resultat, hasSize(1));
		assertThat(resultat, contains(journalpost.getKanalReferanseId()));
	}

}