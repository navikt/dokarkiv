package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FerdigstillJournalpostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class FerdigstillJournalpostRequestMapperTest {
	private static final Long JOURNALPOST_ID = 200L;
	private static final String ENDRET_AV_NAVN = "Endre Tavnavn";
	private static final String ILLEGAL_UTSENDINGKANAL = "illegal";
	private static final String VALID_UTSENDINGKANAL = "EESSI";

	private FerdigstillJournalpostRequestMapper requestMapper;

	@BeforeEach
	public void setUp() {
		requestMapper = new FerdigstillJournalpostRequestMapper();
	}

	@Test
	public void shouldMap() {
		FerdigstillJournalpostRequestTo domainRequest = requestMapper.map(createWebRequest(VALID_UTSENDINGKANAL));
		assertThat(domainRequest.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(domainRequest.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(domainRequest.getUtsendingskanal(), is(UtsendingsKanalCode.EESSI));
	}

	@Test
	public void shouldMap_utsendingkanalIsNull() {
		FerdigstillJournalpostRequestTo domainRequest = requestMapper.map(createWebRequest(null));
		assertThat(domainRequest.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(domainRequest.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(domainRequest.getUtsendingskanal(), is(nullValue()));
	}

	@Test
	public void shouldThrowException_inputIsNull() {
		assertThrows(IllegalArgumentException.class,
				() -> requestMapper.map(null),
				"Request is null");
	}

	@Test
	public void shouldThrowException_illegalUtsendingskanalValue() {
		assertThrows(IllegalArgumentException.class,
				() -> requestMapper.map(createWebRequest(ILLEGAL_UTSENDINGKANAL)),
				"No enum constant no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.illegal");
	}

	private FerdigstillJournalpostRequest createWebRequest(String utsendingkanal) {
		FerdigstillJournalpostRequest request = new FerdigstillJournalpostRequest();
		request.setJournalpostId(JOURNALPOST_ID);
		request.setEndretAvNavn(ENDRET_AV_NAVN);
		request.setUtsendingskanal(utsendingkanal);
		return request;
	}
}