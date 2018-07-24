package no.nav.dokarkiv.core.consumer.aktoer;

import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.cache.Cache;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.feil.PersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link DefaultAktoerConsumerService}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAktoerConsumerServiceTest {

	private static final String IDENT = "Ident";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private HentAktoerIdForIdentRequestMapper requestMapper;
	@Mock
	private HentAktoerIdForIdentResponseMapper responseMapper;
	@Mock
	private AktoerV2 aktoerV2;
	@Mock
	private Cache<String, HentAktoerIdForIdentResponse> aktoerResponseCache;

	@InjectMocks
	private DefaultAktoerConsumerService consumerService;

	@Before
	public void setUpMocks() {
		when(requestMapper.map(any(HentAktoerIdForIdentRequestTo.class))).thenReturn(createHentAktoerIdForIdentWsRequest());
	}

	@Test
	public void shouldCallService() throws Exception {
		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());

		verify(aktoerV2).hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class));
	}

	@Test
	public void shouldGetResponseFromCacheIfPresent() throws PersonIkkeFunnetException {
		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
		verify(aktoerResponseCache).getIfPresent(IDENT);
	}

	@Test
	public void shouldCacheAktoerConsumerResponse() throws HentAktoerIdForIdentPersonIkkeFunnet, PersonIkkeFunnetException {
		HentAktoerIdForIdentResponse response = new HentAktoerIdForIdentResponse();
		when(aktoerResponseCache.getIfPresent(IDENT)).thenReturn(null);
		when(aktoerV2.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class))).thenReturn(response);

		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
		verify(aktoerResponseCache).put(IDENT, response);
	}

	@Test
	public void shouldCallHentIdentForAktoerIdWithCorrectIdent() throws Exception {
		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());

		ArgumentCaptor<HentAktoerIdForIdentRequest> captor = ArgumentCaptor.forClass(HentAktoerIdForIdentRequest.class);
		verify(aktoerV2).hentAktoerIdForIdent(captor.capture());
		assertThat(captor.getValue().getIdent(), is(IDENT));
	}

	@Test
	public void shouldCallWithCorrectIdent() throws Exception {
		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());

		ArgumentCaptor<HentAktoerIdForIdentRequestTo> captor = ArgumentCaptor.forClass(HentAktoerIdForIdentRequestTo.class);
		verify(requestMapper).map(captor.capture());
		assertThat(captor.getValue().getIdent(), is(IDENT));
	}

	@Test
	public void shouldThrowPersonIkkeFunnetException() throws Exception {
		when(aktoerV2.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class)))
				.thenThrow(new HentAktoerIdForIdentPersonIkkeFunnet("", new PersonIkkeFunnet()));

		thrown.expect(PersonIkkeFunnetException.class);
		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
	}

	@Test
	public void shouldMapResponse() throws Exception {
		HentAktoerIdForIdentResponse wsResponse = new HentAktoerIdForIdentResponse();
		when(aktoerV2.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class))).thenReturn(wsResponse);

		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
		verify(responseMapper).map(wsResponse);
	}


	@Test
	public void shouldReturnResponseTo() throws Exception {
		HentAktoerIdForIdentResponseTo mappedResponseTo = new HentAktoerIdForIdentResponseTo();
		when(responseMapper.map(any())).thenReturn(mappedResponseTo);

		HentAktoerIdForIdentResponseTo result = consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());

		assertThat(result, is(sameInstance(mappedResponseTo)));
	}

	private HentAktoerIdForIdentRequestTo createHentAktoerIdForIdentRequestTo() {
		return new HentAktoerIdForIdentRequestTo(IDENT);
	}

	private HentAktoerIdForIdentRequest createHentAktoerIdForIdentWsRequest() {
		HentAktoerIdForIdentRequest request = new HentAktoerIdForIdentRequest();
		request.setIdent(IDENT);
		return request;
	}
}