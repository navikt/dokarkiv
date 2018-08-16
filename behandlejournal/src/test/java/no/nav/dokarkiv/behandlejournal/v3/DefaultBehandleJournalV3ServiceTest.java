package no.nav.dokarkiv.behandlejournal.v3;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKrav;
import no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpost;
import no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplasting;
import no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelse;
import no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelse;
import no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelse;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * Test class for DefaultBehandleJournalService with mocked services.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultBehandleJournalV3ServiceTest {

	@Mock
	private ArkiverUstrukturertKrav arkiverUstrukturertKravMock;
	@Mock
	private JournalfoerInngaaendeHenvendelse journalfoerInngaaendeHenvendelseMock;
	@Mock
	private JournalfoerUtgaaendeHenvendelse journalfoerUtgaaendeHenvendelseMock;
	@Mock
	private FerdigstillDokumentopplasting ferdigstillDokumentOpplastingMock;
	@Mock
	private LagreVedleggPaaJournalpost lagreVedleggPaaJournalpostMock;
	@Mock
	private JournalfoerNotatHenvendelse journalfoerNotatHenvendelseMock;
	@InjectMocks
	private DefaultBehandleJournalV3Service service;

	@Test
	public void shouldDelegateCallToArkiverUstrukturertKravService() throws Exception {
		ArkiverUstrukturertKravRequest request = new ArkiverUstrukturertKravRequest(new Journalpost());
		service.arkiverUstrukturertKrav(request);
		verify(arkiverUstrukturertKravMock).arkiverUstrukturertKrav(eq(request));
	}

	@Test
	public void shouldDelegateCallToLagreVedleggPaaJournalpostService() throws Exception {
		LagreVedleggPaaJournalpostRequest request = new LagreVedleggPaaJournalpostRequest(1L, new DokumentInfo(),
				createSporingsMetaData());
		service.lagreVedleggPaaJournalpost(request);
		verify(lagreVedleggPaaJournalpostMock).lagreVedleggPaaJournalpost(eq(request));
	}

	@Test
	public void shouldDelegateCallToJournalfoerInngaaendeHenvendelseMedHoveddokumentService() throws Exception {
		JournalfoerInngaaendeHenvendelseRequest request = new JournalfoerInngaaendeHenvendelseRequest(new Journalpost());
		service.journalfoerInngaaendeHenvendelse(request);
		verify(journalfoerInngaaendeHenvendelseMock).journalfoerInngaaendeHenvendelse(
				eq(request));
	}

	@Test
	public void shouldDelegateCallToJournalfoerUtgaaendeHenvendelseMedHoveddokumentService() throws Exception {
		JournalfoerUtgaaendeHenvendelseRequest request = new JournalfoerUtgaaendeHenvendelseRequest(new Journalpost());
		service.journalfoerUtgaaendeHenvendelse(request);
		verify(journalfoerUtgaaendeHenvendelseMock).journalfoerUtgaaendeHenvendelse(eq(request));
	}

	@Test
	public void shouldDelegateCallToFerdigstillDokumentOpplastingService() throws Exception {
		FerdigstillDokumentopplastingRequest request = new FerdigstillDokumentopplastingRequest(1L, createSporingsMetaData());
		service.ferdigstillDokumentopplasting(request);
		verify(ferdigstillDokumentOpplastingMock).ferdigstillDokumentOpplasting(eq(request));
	}

	@Test
	public void shouldDelegateCallToJournalfoerNotatHenvendelseService() throws Exception {
		JournalfoerNotatHenvendelseRequest request = new JournalfoerNotatHenvendelseRequest(new Journalpost());
		service.journalfoerNotatHenvendelse(request);
		verify(journalfoerNotatHenvendelseMock).journalfoerNotatHenvendelse(eq(request));
	}

	private SporingsMetaData createSporingsMetaData() {
		return new SporingsMetaData(null, null, null);
	}
}
