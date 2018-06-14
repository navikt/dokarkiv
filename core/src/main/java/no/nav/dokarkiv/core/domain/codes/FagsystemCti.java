package no.nav.dokarkiv.core.domain.codes;


import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Domain object that represents codes table T_K_FAGSYSTEM.
 * 
 * @author Hans Olav Loftum, BEKK
 */
@Entity
@Table(name = "T_K_FAGSYSTEM")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "k_fagsystem")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class FagsystemCti extends CodesTablePeriodicItem<FagsystemCode, String> {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = ***gammelt_fnr***98514180L;

	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "createdBy", column = @Column(name = "opprettet_av")),
			@AttributeOverride(name = "createdDate", column = @Column(name = "dato_opprettet")),
			@AttributeOverride(name = "updatedBy", column = @Column(name = "endret_av")),
			@AttributeOverride(name = "updatedDate", column = @Column(name = "dato_endret")) })
	private ChangeStamp changeStamp;
	
	/**
	 * Default no-arg constructor. Should only be called by persistence
	 * provider.
	 */
	protected FagsystemCti() {
	}
	
	/**
	 * Getter for the changeStamp property.
	 * 
	 * @return the changeStamp
	 */
	public ChangeStamp getChangeStamp() {
		return changeStamp;
	}

}
