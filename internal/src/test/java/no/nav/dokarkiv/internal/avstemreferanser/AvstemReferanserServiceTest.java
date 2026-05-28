package no.nav.dokarkiv.internal.avstemreferanser;

import no.nav.dokarkiv.core.repository.AvstemReferanseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AvstemReferanserServiceTest {
	public static final String EKSISTERENDE_EKSTERN_REFERANSE = "en_ekstern_referanse_id";
	public static final String MANGLENDE_EKSTERN_REFERANSE = "enikkeeksisterendeid";

	private final AvstemReferanseRepository avstemReferanseRepository = mock(AvstemReferanseRepository.class);

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
