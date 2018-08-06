package no.nav.dokarkiv.core.journalbehandling;

import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataIDLFRequest;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataIDLFResponse;

/**
 * Definition of SettMetadataIDLF operation. The operations adds metadata to a DLF document.
 * This operation is Joark internal
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public interface SettMetadataIDLF {

	/**
	 * Sett metadata i DLF
	 *
	 * @param settMetadataIDLFRequest The request with the dlfDokument to update
	 * @return The response with the updated dlfDokument
	 */
	SettMetadataIDLFResponse settMetadataIDLF(SettMetadataIDLFRequest settMetadataIDLFRequest);

}
