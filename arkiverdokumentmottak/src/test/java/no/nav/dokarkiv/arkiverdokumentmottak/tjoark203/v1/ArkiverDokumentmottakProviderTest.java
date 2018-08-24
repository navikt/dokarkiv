package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import static no.nav.dokarkiv.arkiverdokumentmottak.ArkiverDokumentmottakConstants.FORSENDELSE_MOTTAK_ID_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.JournalpostDokumentInfoRelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;


@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class ArkiverDokumentmottakProviderTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private JournalforInngaaendeForsendelseRequestMapper journalforInngaaendeForsendelseRequestMapperMock;

	@Mock
	private JournalforInngaaendeForsendelseResponseMapper journalforInngaaendeForsendelseResponseMapperMock;

	@Mock
	private JournalforInngaaendeForsendelseService journalforInngaaendeForsendelseServiceMock;

	@Mock
	private ArkiverDokumentmottakFaultInfoPopulator faultInfoPopulator;

	@InjectMocks
	private ArkiverDokumentmottakProvider provider;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		when(journalforInngaaendeForsendelseServiceMock.journalforInngaaendeForsendelse(any()))
				.thenReturn(JournalforInngaaendeForsendelseResponseTo.builder()
						.journalpostId(1L)
						.dokumentInfoIdHoveddokument(1L)
						.build());
		when(journalforInngaaendeForsendelseRequestMapperMock.map(any()))
				.thenReturn(new JournalforInngaaendeForsendelseRequestTo(new Journalpost()));

	}

	@Test
	public void shouldJournalforInngaaendeForsendelse() throws Exception {
		JournalforInngaaendeForsendelseResponse wsResponse =
				new JournalforInngaaendeForsendelseResponse();

		when(journalforInngaaendeForsendelseResponseMapperMock
				.map(any(JournalforInngaaendeForsendelseResponseTo.class)))
				.thenReturn(wsResponse);

		JournalforInngaaendeForsendelseResponse response = provider
				.journalforInngaaendeForsendelse(new JournalforInngaaendeForsendelseRequest());

		assertThat(response, is(wsResponse));
	}

	@Test
	public void journalforInngaaendeForsendelseThrowsKanIkkeJournalfores() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);

		when(journalforInngaaendeForsendelseServiceMock
				.journalforInngaaendeForsendelse(any(JournalforInngaaendeForsendelseRequestTo.class)))
				.thenThrow(new InvalidArgumentException("test"));

		provider.journalforInngaaendeForsendelse(new JournalforInngaaendeForsendelseRequest());
	}

	@Test
	public void journalforInngaaendeForsendelseThrowsKanIkkeJournalforesLogTilleggsopplysning() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);

		when(journalforInngaaendeForsendelseServiceMock
				.journalforInngaaendeForsendelse(any(JournalforInngaaendeForsendelseRequestTo.class)))
				.thenThrow(new InvalidArgumentException("test"));

		provider.journalforInngaaendeForsendelse(createRequest());
	}

	@Test
	public void shouldPing() throws Exception {
		provider.ping();
	}

	private JournalforInngaaendeForsendelseRequest createRequest() {
		return new JournalforInngaaendeForsendelseRequest()
				.withJournalpost(new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost()
						.withJournalpostTilleggsopplysninger(new Tilleggsopplysning()
								.withOpplysningsnoekkel(FORSENDELSE_MOTTAK_ID_KEY)
								.withOpplysningsverdi("VERDI"))
						.withJournalpostDokumentInfoRelasjon(
								new JournalpostDokumentInfoRelasjon()
										.withTilknyttetJournalpostSom(TilknyttetJournalpostEnum.HOVEDDOKUMENT)
										.withDokumentInfo(new DokumentInfo())));
	}
}