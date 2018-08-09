package no.nav.dokarkiv.hentdokument.dlf;

/**
 * Used by SettMetadataIDlf to obtain a URL which can be used to retrieve a vedlegg from Joark.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public interface VedleggUrlRetriever {

	/**
	 * Retrieves an URL to a vedlegg.
	 * 
	 * @param journalpostIdVedlegg The journalpost Id.
	 * @param filUuidVedlegg The fil Uuid.
	 * @return The URL.
	 */
	String retrieveVedleggUrl(String journalpostIdVedlegg, String filUuidVedlegg);

}