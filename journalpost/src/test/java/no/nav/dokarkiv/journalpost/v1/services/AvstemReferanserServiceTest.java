package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.repository.AvstemReferanseRepository;
import no.nav.dokarkiv.journalpost.v1.api.AvstemmingReferanser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("itest")
class AvstemReferanserServiceTest {
	public static final String EKSISTERENDE_EKSTERN_REFERANSE = "en_ekstern_referanse_id";
	public static final String MANGLENDE_EKSTERN_REFERANSE = "enikkeeksisterendeid";

	@MockBean
	private AvstemReferanseRepository avstemReferanseRepository;

	private AvstemReferanserService avstemReferanserService;

	@BeforeEach
	void setup() {
		when(avstemReferanseRepository.findKanalReferanseIdsMatchedInDB(any())).thenReturn(Set.of(EKSISTERENDE_EKSTERN_REFERANSE));
		avstemReferanserService = new AvstemReferanserService(avstemReferanseRepository);
	}

	@Test
	void shouldSubtractEksternReferanseThatExistsFromSubmittedList() {
		var resultat = avstemReferanserService.avstemReferanser(new AvstemmingReferanser(Set.of(EKSISTERENDE_EKSTERN_REFERANSE, MANGLENDE_EKSTERN_REFERANSE)));
		assertThat(resultat, hasSize(1));
		assertThat(resultat, containsInAnyOrder(MANGLENDE_EKSTERN_REFERANSE));
	}
}