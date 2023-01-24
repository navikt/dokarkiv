package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelse;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


/**
 * Test class for DefaultBehandleJournalService with mocked services.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultBehandleJournalV3ServiceTest {

	@Mock
	private JournalfoerNotatHenvendelse journalfoerNotatHenvendelseMock;
	@InjectMocks
	private DefaultBehandleJournalV3Service service;

	@Test
	public void shouldDelegateCallToJournalfoerNotatHenvendelseService() throws Exception {
		JournalfoerNotatHenvendelseRequest request = new JournalfoerNotatHenvendelseRequest(new Journalpost());
		service.journalfoerNotatHenvendelse(request);
		verify(journalfoerNotatHenvendelseMock).journalfoerNotatHenvendelse(eq(request));
	}

}
