package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKrav;
import no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKravRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKravResponse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpost;
import no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpostRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpostResponse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark062.FerdigstillDokumentopplasting;
import no.nav.dokarkiv.behandlejournal.v2.tjoark062.FerdigstillDokumentopplastingRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelseResponse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelseResponse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark065.JournalfoerNotatHenvendelse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark065.JournalfoerNotatHenvendelseResponse;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementation of BehandleJournalServiceBi
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultBehandleJournalService implements BehandleJournalServiceBi {

	@Inject
	private ArkiverUstrukturertKrav arkiverUstrukturertKrav;
	@Inject
	private LagreVedleggPaaJournalpost lagreVedleggPaaJournalpost;
	@Inject
	private JournalfoerInngaaendeHenvendelse journalfoerInngaaendeHenvendelse;
	@Inject
	private FerdigstillDokumentopplasting ferdigstillDokumentopplasting;
	@Inject
	private JournalfoerUtgaaendeHenvendelse journalfoerUtgaaendeHenvendelse;
	@Inject
	private JournalfoerNotatHenvendelse journalfoerNotatHenvendelse;

	@Override
	public ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(
			ArkiverUstrukturertKravRequest arkiverUstrukturertKravRequest) {
		return arkiverUstrukturertKrav.arkiverUstrukturertKrav(arkiverUstrukturertKravRequest);
	}

	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(
			LagreVedleggPaaJournalpostRequest lagreVedleggPaaJournalpostRequest) throws NoJournalpostFoundException {
		return lagreVedleggPaaJournalpost.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Override
	public JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(
			JournalfoerInngaaendeHenvendelseRequest journalfoerInngaaendeHenvendelseRequest) {
		return journalfoerInngaaendeHenvendelse
				.journalfoerInngaaendeHenvendelse(journalfoerInngaaendeHenvendelseRequest);
	}

	@Override
	public void ferdigstillDokumentopplasting(FerdigstillDokumentopplastingRequest ferdigstillDokumentOpplastingRequest)
			throws NoJournalpostFoundException {
		ferdigstillDokumentopplasting.ferdigstillDokumentOpplasting(ferdigstillDokumentOpplastingRequest);
	}

	@Override
	public JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest journalfoerUtgaaendeHenvendelseRequest) {
		return journalfoerUtgaaendeHenvendelse
				.journalfoerUtgaaendeHenvendelse(journalfoerUtgaaendeHenvendelseRequest);
	}

	@Override
	public JournalfoerNotatHenvendelseResponse journalfoerNotatHenvendelse(
			JournalfoerNotatHenvendelseRequest journalfoerNotatHenvendelseRequest) {
		return journalfoerNotatHenvendelse
				.journalfoerNotatHenvendelse(journalfoerNotatHenvendelseRequest);
	}
}
