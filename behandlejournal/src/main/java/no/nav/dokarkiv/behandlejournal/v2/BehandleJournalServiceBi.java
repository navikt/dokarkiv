package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.behandlejournal.v2.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKravRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKravResponse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpostRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpostResponse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark062.FerdigstillDokumentopplastingRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelseResponse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelseResponse;
import no.nav.dokarkiv.behandlejournal.v2.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v2.tjoark065.JournalfoerNotatHenvendelseResponse;

/**
 * Defines the Joark MOD information service BehandleJournal.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface BehandleJournalServiceBi {

	/**
	 * Service method for archiving electronical forms and optional attachments.
	 * Forms are archived with a temporary (M) journalstatus.
	 *
	 * @param arkiverUstrukturertKravRequest the request object
	 * @return the response object
	 */
	ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(
			ArkiverUstrukturertKravRequest arkiverUstrukturertKravRequest);

	/**
	 * Adds a dokumentInfo as vedlegg to an existing Journalpost.
	 *
	 * @param lagreVedleggPaaJournalpostRequest The request containg the dokumentInfo object and the
	 *                                          journalpostId.
	 * @return a LagreVedleggPaaJournalpostResponse with dokumentId of the
	 * stored dokumentInfo.
	 */
	LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(
			LagreVedleggPaaJournalpostRequest lagreVedleggPaaJournalpostRequest) throws NoJournalpostFoundException;

	/**
	 * Creates and journalfører an incoming Journalpost with hoveddokument.
	 *
	 * @param journalfoerInngaaendeHenvendelseRequest The request containing the new Journalpost to create.
	 * @return a JournalfoerInngaaendeHenvendelseMedHoveddokumentResponse with the journalpostId of
	 * the new persisted Journalpost.
	 */
	JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(
			JournalfoerInngaaendeHenvendelseRequest journalfoerInngaaendeHenvendelseRequest);

	/**
	 * Finalize the journalpost from a dokumentopplasting.
	 *
	 * @param ferdigstillDokumentOpplastingRequest The request containing the Journalpost to update
	 */
	void ferdigstillDokumentopplasting(FerdigstillDokumentopplastingRequest ferdigstillDokumentOpplastingRequest)
			throws NoJournalpostFoundException;

	/**
	 * Creates and journalfører an outgoing Journalpost with hoveddokument.
	 *
	 * @param journalfoerUtgaaendeHenvendelseRequest The request containing the Journalpost to create.
	 * @return The response containing the journalpostId and dokumentId of the
	 * created Journalpost and document.
	 */
	JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest journalfoerUtgaaendeHenvendelseRequest);

	/**
	 * Creates a notat Journalpost with a main document.
	 *
	 * @param journalfoerNotatHenvendelseRequest The request containing the Journalpost to create.
	 * @return The response containing the journalpostId and dokumentId of the
	 * created Journalpost and document.
	 */
	JournalfoerNotatHenvendelseResponse journalfoerNotatHenvendelse(
			JournalfoerNotatHenvendelseRequest journalfoerNotatHenvendelseRequest);
}
