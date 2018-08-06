package no.nav.dokarkiv.core.journalbehandling;

import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataForKopiering;
import no.nav.dokarkiv.core.journalbehandling.to.SettMetadataForUthenting;

/**
 * Used to manipulate metadata xml files from dlf files.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public interface SettMetadataIDlfXmlUpdater {

	/**
	 * Update the xml with uthenting values.
	 *
	 * @param metadataXml          The xml to update.
	 * @param metadataForUthenting The uthenting values.
	 * @return The updated xml.
	 */
	String updateMetadataXmlForUthenting(String metadataXml, SettMetadataForUthenting metadataForUthenting);

	/**
	 * Update the xml with kopiering values.
	 *
	 * @param metadataXml              The xml to update.
	 * @param metadataForKopiering     The kopiering values.
	 * @param hoveddokumentMetadataXml The hoveddokument xml, used to copy values from.
	 * @return The updated xml.
	 */
	String updateMetadataXmlForKopiering(String metadataXml, SettMetadataForKopiering metadataForKopiering,
										 String hoveddokumentMetadataXml);

}