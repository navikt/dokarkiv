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
	private static final long serialVersionUID = 5520374847498514180L;

	@Embedded
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
