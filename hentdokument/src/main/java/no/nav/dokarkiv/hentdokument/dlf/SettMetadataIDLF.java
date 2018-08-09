package no.nav.dokarkiv.hentdokument.dlf;

import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataIDLFRequest;
import no.nav.dokarkiv.hentdokument.dlf.to.SettMetadataIDLFResponse;

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
