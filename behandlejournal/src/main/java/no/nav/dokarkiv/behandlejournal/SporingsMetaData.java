package no.nav.dokarkiv.behandlejournal;

import lombok.Builder;

/**
 * Type that holds metadata about request tracking.
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@Builder
public class SporingsMetaData {
	private String personFornavn;
	private String personEtternavn;
	private String applikasjonsID;

	@SuppressWarnings("unused")
	private SporingsMetaData() {
		
	}
	
	public SporingsMetaData(String personFornavn, String personEtternavn, String applikasjonsID) {
		this.personFornavn = personFornavn;
		this.personEtternavn = personEtternavn;
		this.applikasjonsID = applikasjonsID;
	}

	/**
	 * Getter for the personFornavn property.
	 * 
	 * @return the personFornavn
	 */
	public String getPersonFornavn() {
		return personFornavn;
	}

	/**
	 * Getter for the personEtternavn property.
	 * 
	 * @return the personEtternavn
	 */
	public String getPersonEtternavn() {
		return personEtternavn;
	}

	/**
	 * Getter for the applikasjonsID property.
	 * 
	 * @return the applikasjonsID
	 */
	public String getApplikasjonsID() {
		return applikasjonsID;
	}
}
