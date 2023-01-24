package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelse;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseResponse;
import org.springframework.stereotype.Component;

@Component
public class DefaultBehandleJournalV3Service implements BehandleJournalV3ServiceBi {

	private final JournalfoerNotatHenvendelse journalfoerNotatHenvendelse;

	public DefaultBehandleJournalV3Service(JournalfoerNotatHenvendelse journalfoerNotatHenvendelse) {
		this.journalfoerNotatHenvendelse = journalfoerNotatHenvendelse;
	}

	@Override
	public JournalfoerNotatHenvendelseResponse journalfoerNotatHenvendelse(
			JournalfoerNotatHenvendelseRequest journalfoerNotatHenvendelseRequest) {
		return journalfoerNotatHenvendelse
				.journalfoerNotatHenvendelse(journalfoerNotatHenvendelseRequest);
	}
}
