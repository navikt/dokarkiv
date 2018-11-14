package no.nav.dokarkiv.core.consumer.gsak;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.consumer.gsak.domain.SakInfoTo;
import no.nav.dokarkiv.core.consumer.gsak.hentgsaksaker.GsakConsumer;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class GsakConsumerTest {
	private final String SAKID = "111";
	private final String AKTOERID = "222";

	private RestTemplate restTemplate;
	private GsakConsumer gsakConsumer;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		restTemplate = mock(RestTemplate.class);
		gsakConsumer = new GsakConsumer(restTemplate, "test");
	}

	@Test
	public void shouldRunOK() {
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SakInfoTo.class))).thenReturn(new ResponseEntity<>(SakInfoTo.builder().aktoerId(AKTOERID).build(), HttpStatus.OK));
		SakInfoTo sakInfoTo = gsakConsumer.hentSakInfo(SAKID);
		assertThat(sakInfoTo.getAktoerId(), is(AKTOERID));
	}

	@Test
	public void shouldThrowRestClientException() {
		exception.expect(DokarkivFunctionalException.class);
		exception.expectMessage("getGsaksaker feilet funksjonelt med statusKode=400");
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SakInfoTo.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "message"));
		gsakConsumer.hentSakInfo(SAKID);
	}

	@Test
	public void shouldThrowRestServerException() {
		exception.expect(DokarkivTechnicalException.class);
		exception.expectMessage("getGsaksaker feilet teknisk med statusKode=500");
		when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(SakInfoTo.class))).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "message"));
		gsakConsumer.hentSakInfo(SAKID);
	}

}
