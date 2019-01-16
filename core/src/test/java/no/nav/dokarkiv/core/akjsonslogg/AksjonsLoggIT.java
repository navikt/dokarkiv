package no.nav.dokarkiv.core.akjsonslogg;

import static no.nav.dokarkiv.core.util.ConverterUtils.objectToJsonString;
import static no.nav.dokarkiv.core.util.TestDataUtils.createAksjonsLoggRequestAksjon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggRequest;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggInfoException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.RepositoryConfig;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.dokarkiv.core.util.TestDataUtils;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RepositoryConfig.class, AksjonsLoggService.class, BegrensningService.class, JdbcAbacSecurityRepository.class})
@DataJpaTest
@Transactional
@ActiveProfiles("itest")
public class AksjonsLoggIT {

	@Inject
	private AksjonsLoggService aksjonsLoggService;

	@Inject
	private AksjonsLoggRepository aksjonsLoggRepository;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		RequestContextUtil.createAndSetUsername("username", "appId");

	}

	@Test
	public void shouldSaveAksjonsLogg() throws IOException, UgyldigAksjonsLoggInfoException {


		String aksjonLoggRequest = objectToJsonString(AksjonsLoggRequest.builder()
				.aksjonListe(Arrays.asList(
						createAksjonsLoggRequestAksjon(1L, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name()),
						createAksjonsLoggRequestAksjon(1L, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name())
				))
				.build());
		aksjonsLoggService.validerOgLagreAksjon(aksjonLoggRequest);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(2));
		aksjonsLoggList.forEach(aksjonsLogg -> {
			assertThat(aksjonsLogg.getAksjon(), is(AksjonTypeCode.ENDRE_BEGRENSNING));
			assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
			assertThat(aksjonsLogg.getApplikasjon(), is(TestDataUtils.AKSJON_APPLIKASJON));
			assertThat(aksjonsLogg.getBruker(), is(TestDataUtils.AKSJON_BRUKER));
			assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
			assertThat(aksjonsLogg.getDokumentInfoId(), is(1L));
			assertThat(aksjonsLogg.getJournalpostId(), is(1L));
			assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
			assertThat(aksjonsLogg.getArkivElement(), is(TestDataUtils.AKSJON_ARKIVELEMENT));
			assertThat(aksjonsLogg.getFraVerdi(), is(TestDataUtils.AKSJON_FRA_VERDI));
			assertThat(aksjonsLogg.getTilVerdi(), is(TestDataUtils.AKSJON_TIL_VERDI));
			assertThat(Duration.between(Instant.ofEpochMilli(aksjonsLogg.getChangeStamp().getCreatedDate().getTime())
					.atZone(ZoneId.systemDefault())
					.toLocalDateTime(), LocalDateTime.now()).getSeconds(), lessThan(10L));
			assertThat(aksjonsLogg.getChangeStamp().getCreatedBy(), is("username"));
		});
	}

	@Test
	public void shouldThrowWhenOneOfAksjonIsMissingJournalpostId() throws IOException, UgyldigAksjonsLoggInfoException {
		expectedException.expect(UgyldigAksjonsLoggInfoException.class);
		expectedException.expectMessage("journalpostId");
		String request = objectToJsonString(AksjonsLoggRequest.builder()
				.aksjonListe(Arrays.asList(
						createAksjonsLoggRequestAksjon(1L, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name()),
						createAksjonsLoggRequestAksjon(null, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name())
				))
				.build());
		aksjonsLoggService.validerOgLagreAksjon(request);
	}

	@Test
	public void shouldThrowWhenMissingAksjonHeaderValue() throws UgyldigAksjonsLoggInfoException {
		expectedException.expect(UgyldigAksjonsLoggInfoException.class);
		expectedException.expectMessage("Meldingen mangler påkrevd");

		aksjonsLoggService.validerOgLagreAksjon("");
	}

	@Test
	public void shouldThrowWhenInvalidJsonValue() throws UgyldigAksjonsLoggInfoException {
		expectedException.expect(UgyldigAksjonsLoggInfoException.class);
		expectedException.expectMessage("Sjekk om headeren er i gyldig JSON format");

		aksjonsLoggService.validerOgLagreAksjon("not valid");
	}

	@Test
	public void shouldThrowWhenJournalpostIdIsNotIncluded() throws IOException, UgyldigAksjonsLoggInfoException {
		expectedException.expect(UgyldigAksjonsLoggInfoException.class);
		expectedException.expectMessage("journalpostId");

		AksjonsLoggRequest request = TestDataUtils.createAksjonsLoggRequest(null, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name());
		String aksjonLoggRequest = objectToJsonString(request);
		aksjonsLoggService.validerOgLagreAksjon(aksjonLoggRequest);
	}

	@Test
	public void shouldThrowWhenApplikasjonIsNotIncluded() throws IOException, UgyldigAksjonsLoggInfoException {
		expectedException.expect(UgyldigAksjonsLoggInfoException.class);
		expectedException.expectMessage("applikasjon");

		AksjonsLoggRequest request = TestDataUtils.createAksjonsLoggRequest(1L, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name());
		request.getAksjonListe().get(0).setApplikasjon(null);
		String aksjonLoggRequest = objectToJsonString(request);
		aksjonsLoggService.validerOgLagreAksjon(aksjonLoggRequest);
	}

	@Test
	public void shouldThrowWhenAksjonIsNotIncluded() throws IOException, UgyldigAksjonsLoggInfoException {
		expectedException.expect(UgyldigAksjonsLoggInfoException.class);
		expectedException.expectMessage("aksjon");

		AksjonsLoggRequest request = TestDataUtils.createAksjonsLoggRequest(1L, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name());
		request.getAksjonListe().get(0).setAksjon(null);
		String aksjonLoggRequest = objectToJsonString(request);
		aksjonsLoggService.validerOgLagreAksjon(aksjonLoggRequest);
	}

	@Test
	public void shouldThrowWhenUtfoertAvIsNotIncluded() throws IOException, UgyldigAksjonsLoggInfoException {
		expectedException.expect(UgyldigAksjonsLoggInfoException.class);
		expectedException.expectMessage("utfoertAv");

		AksjonsLoggRequest request = TestDataUtils.createAksjonsLoggRequest(1L, 1L, AksjonTypeCode.ENDRE_BEGRENSNING.name());
		request.getAksjonListe().get(0).setUtfoertAv(null);
		String aksjonLoggRequest = objectToJsonString(request);
		aksjonsLoggService.validerOgLagreAksjon(aksjonLoggRequest);
	}


	@Test
	public void shouldThrowWhenInvalidAksjonValue() throws IOException, UgyldigAksjonsLoggInfoException {
		expectedException.expect(UgyldigAksjonsLoggInfoException.class);
		expectedException.expectMessage("AAA er ikke en gyldig verdi for aksjon");

		AksjonsLoggRequest request = TestDataUtils.createAksjonsLoggRequest(1L, 1L, "AAA");
		String aksjonLoggRequest = objectToJsonString(request);
		aksjonsLoggService.validerOgLagreAksjon(aksjonLoggRequest);
	}
}
