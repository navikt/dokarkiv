package no.nav.dokarkiv.core.consumer.gsak;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.consumer.gsak.domain.SakInfoTo;
import no.nav.dokarkiv.core.consumer.gsak.hentgsaksaker.GsakConsumer;
import org.junit.Before;
import org.junit.Test;

public class GsakConsumerServiceTest {
	private final String SAKID = "111";
	private final String AKTOERID = "222";

	private GsakConsumer gsakConsumer;
	private SakConsumerService service;

	@Before
	public void setUp() {
		gsakConsumer = mock(GsakConsumer.class);
		service = new SakConsumerService(gsakConsumer);
	}

	@Test
	public void shouldRunOK() {
//		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SakInfoTo.class))).thenReturn(new ResponseEntity<SakInfoTo>(SakInfoTo.builder().aktoerId(AKTOERID).build(), HttpStatus.OK));
		when(gsakConsumer.hentSakInfo(anyString())).thenReturn(SakInfoTo.builder().aktoerId(AKTOERID).build());
		String aktoerId = service.hentAktoerForSak(SAKID);
		assertThat(aktoerId, is(AKTOERID));
	}
}
