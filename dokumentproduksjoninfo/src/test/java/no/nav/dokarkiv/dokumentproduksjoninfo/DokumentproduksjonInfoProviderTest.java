package no.nav.dokarkiv.dokumentproduksjoninfo;

import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatus;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusRequestMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusRequestTo;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusResponseMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusResponseTo;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DokumentproduksjonInfoProvider
 */
@ExtendWith(MockitoExtension.class)
public class DokumentproduksjonInfoProviderTest {

	@Mock
	private HentJournalOgDokumentStatus hentJournalOgDokumentStatusMock;
	@Mock
	private HentJournalOgDokumentStatusRequestMapper hentJournalOgDokumentStatusRequestMapperMock;
	@Mock
	private HentJournalOgDokumentStatusResponseMapper hentJournalOgDokumentStatusResponseMapperMock;

	@InjectMocks
	private DokumentproduksjonInfoProvider dokumentproduksjonInfoProvider;

	@Test
	public void shouldDelegateToHentJournalOgDokumentStatus() throws Exception {
		HentJournalOgDokumentStatusRequest wsRequest = new HentJournalOgDokumentStatusRequest();
		HentJournalOgDokumentStatusResponse wsResponse = new HentJournalOgDokumentStatusResponse();
		HentJournalOgDokumentStatusRequestTo domainRequest = new HentJournalOgDokumentStatusRequestTo();
		HentJournalOgDokumentStatusResponseTo domainResponse = new HentJournalOgDokumentStatusResponseTo(null, null, null);
		when(hentJournalOgDokumentStatusRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);
		when(hentJournalOgDokumentStatusMock.hentJournalOgDokumentStatus(domainRequest)).thenReturn(domainResponse);
		when(hentJournalOgDokumentStatusResponseMapperMock.map(domainResponse)).thenReturn(wsResponse);

		HentJournalOgDokumentStatusResponse response = dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(wsRequest);

		assertThat(response, is(wsResponse));
	}

	@Test
	public void shouldThrowUnsupportedOperationExceptionForHentFerdigstilteDokumenter() throws Exception {
		assertThrows(UnsupportedOperationException.class,
				() -> dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(new HentFerdigstilteDokumenterRequest()),
				"sanert");
	}

	@Test
	public void shouldThrowUnsupportedOperationExceptionForHentJournalpostInfo() throws Exception {
		assertThrows(UnsupportedOperationException.class,
				() -> dokumentproduksjonInfoProvider.hentJournalpostInfo(new HentJournalpostInfoRequest()),
				"sanert");
	}
}
