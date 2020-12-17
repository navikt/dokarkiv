package no.nav.dokarkiv.core.consumer.pdl;

import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentAktoerIdForIdentPersonIkkeFunnet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link PdlIdentConsumer}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class PdlIdentConsumerTest {

	private static final String IDENT = "Ident";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@InjectMocks
	private PdlIdentConsumer consumerService;

	@Before
	public void setUpMocks() {
//		when(requestMapperIdent.map(any(HentAktoerIdForIdentRequestTo.class))).thenReturn(createHentAktoerIdForIdentWsRequest());
	}

	@Test
	public void shouldCallServiceIdent() throws Exception {
//		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());

//		verify(aktoerV2).hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class));
	}

	@Test
	public void shouldGetResponseFromAktoerCacheIfPresent() throws PersonIkkeFunnetException {
//		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
//		verify(aktoerResponseCache).getIfPresent(IDENT);
	}

	@Test
	public void shouldCacheAktoerConsumerResponse() throws HentAktoerIdForIdentPersonIkkeFunnet, PersonIkkeFunnetException {
//		HentAktoerIdForIdentResponse response = new HentAktoerIdForIdentResponse();
//		when(aktoerResponseCache.getIfPresent(IDENT)).thenReturn(null);
//		when(aktoerV2.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class))).thenReturn(response);
//
//		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
//		verify(aktoerResponseCache).put(IDENT, response);
	}

	@Test
	public void shouldCallHentAktoerIdForIdentWithCorrectIdent() throws Exception {
//		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
//
//		ArgumentCaptor<HentAktoerIdForIdentRequest> captor = ArgumentCaptor.forClass(HentAktoerIdForIdentRequest.class);
//		verify(aktoerV2).hentAktoerIdForIdent(captor.capture());
//		assertThat(captor.getValue().getIdent(), is(IDENT));
	}

	@Test
	public void shouldCallWithCorrectIdent() throws Exception {
//		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
//
//		ArgumentCaptor<HentAktoerIdForIdentRequestTo> captor = ArgumentCaptor.forClass(HentAktoerIdForIdentRequestTo.class);
//		verify(requestMapperIdent).map(captor.capture());
//		assertThat(captor.getValue().getIdent(), is(IDENT));
	}

	@Test
	public void shouldIdentThrowPersonIkkeFunnetException() throws Exception {
//		when(aktoerV2.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class)))
//				.thenThrow(new HentAktoerIdForIdentPersonIkkeFunnet("", new PersonIkkeFunnet()));
//
//		thrown.expect(PersonIkkeFunnetException.class);
//		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
	}

	@Test
	public void shouldMapAktoerIdResponse() throws Exception {
//		HentAktoerIdForIdentResponse wsResponse = new HentAktoerIdForIdentResponse();
//		when(aktoerV2.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequest.class))).thenReturn(wsResponse);
//
//		consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
//		verify(responseMapperIdent).map(wsResponse);
	}

	@Test
	public void shouldReturnIdentResponseTo() throws Exception {
//		HentAktoerIdForIdentResponseTo mappedResponseTo = new HentAktoerIdForIdentResponseTo(null, new ArrayList<>());
//		when(responseMapperIdent.map(any())).thenReturn(mappedResponseTo);
//
//		HentAktoerIdForIdentResponseTo result = consumerService.hentAktoerIdForIdent(createHentAktoerIdForIdentRequestTo());
//
//		assertThat(result, is(sameInstance(mappedResponseTo)));
	}
}