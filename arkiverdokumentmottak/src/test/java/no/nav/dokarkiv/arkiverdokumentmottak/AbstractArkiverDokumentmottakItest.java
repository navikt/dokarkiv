package no.nav.dokarkiv.arkiverdokumentmottak;

import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1.ArkiverDokumentmottakProvider;
import no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2.ArkiverDokumentmottakV2Provider;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, ArkiverDokumentmottakConfig.class})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractArkiverDokumentmottakItest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
    protected JoarkRepositoryBegrenset joarkRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;
	@Inject
	protected ArkiverDokumentmottakProvider arkiverDokumentmottakProviderV1;

	@Inject
	protected ArkiverDokumentmottakV2Provider arkiverDokumentmottakV2Provider;
	protected ArkiverDokumentmottakProvider arkiverDokumentmottakProvider;
	@PersistenceContext
	protected EntityManager entityManager;

	@Before
	public void setUpItest() {
		DateProvider.configure(true, DateProvider.getDate(new Date()));
		joarkRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}
}
