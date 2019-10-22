package no.nav.dokarkiv.sak.repository;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.repository.sak.SakSearchCriteria;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {HentSakerRepositoryTest.Config.class, RepositoryConfig.class, SkjermingService.class, HentSakerRepository.class, SakRepository.class, JoarkRepository.class})
@Transactional
@ActiveProfiles("itest")
public class HentSakerRepositoryTest {

	@Configuration
	static class Config {
		@Bean
		MeterRegistry meterRegistry() {
			return new SimpleMeterRegistry();
		}
	}

	@Inject
	private HentSakerRepository hentSakerRepository;

	@Before
	public void setUp() {
		RequestContextUtil.createAndSetUsername("itest", "itest");
	}

	@Test
	public void henter_sak_med_en_gitt_id() {
		Sak opprettet = hentSakerRepository.lagre(new SakTestData().aktoerId("1").build());

		Optional<Sak> hentet = hentSakerRepository.hentSak(opprettet.getSakId());

		assertThat(hentet.orElseThrow(IllegalStateException::new)).isEqualTo(opprettet);
	}

	@Test
	public void oppretter_og_returnerer_opprettet_sak() {
		Sak sak = hentSakerRepository.lagre(new SakTestData().aktoerId("1").build());

		assertThat(sak).isNotNull();
	}

	@Test
	public void finner_saker_for_enkelt_kriterie() {
		String aktoerId = randomNumeric(5);

		hentSakerRepository.lagre(new SakTestData().aktoerId(randomNumeric(5)).build());
		Sak sak1 = hentSakerRepository.lagre(new SakTestData().aktoerId(aktoerId).build());
		Sak sak2 = hentSakerRepository.lagre(new SakTestData().aktoerId(aktoerId).build());

		List<Sak> saker = hentSakerRepository.finnSaker(SakSearchCriteria.builder().aktoerId(sak1.getAktoerId()).build());
		assertThat(saker).containsOnly(sak1, sak2);
	}

	@Test
	public void finner_saker_for_flere_kriterier() {
		String tema = RandomStringUtils.randomAlphabetic(3);
		String orgnr = "974652250";

		Sak sak1 = hentSakerRepository.lagre(new SakTestData().orgnr(orgnr).tema(tema).build());
		hentSakerRepository.lagre(new SakTestData().orgnr(SakTestData.generateValidOrgnr()).build());
		hentSakerRepository.lagre(new SakTestData().aktoerId(randomNumeric(5)).build());
		Sak sak2 = hentSakerRepository.lagre(new SakTestData().orgnr(orgnr).tema(tema).build());

		List<Sak> saker = hentSakerRepository.finnSaker(SakSearchCriteria.builder().tema(Collections.singletonList(tema)).orgnr(orgnr).build());
		assertThat(saker).containsOnly(sak1, sak2);
	}

}
