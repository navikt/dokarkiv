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
 * CodesTableItem representing the T_K_BEGRENSNING_TYPE DB table.
 * 
 * @author Ketill Fenne, Visma Consulting
 */
@Entity
@Table(name = "T_K_BEGRENSNING_TYPE")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "begrensning_type")),
		@AttributeOverride(name = "decode", column = @Column(name = "begrensning_beskrivelse")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class BegrensningTypeCti extends CodesTablePeriodicItem<BegrensningTypeCode, String> {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = ***gammelt_fnr***63616145L;
	/**
	 * Audit information.
	 */
	@Embedded
	private ChangeStamp changeStamp;

	/**
	 * Default no-arg constructor. Should only be called by persistence
	 * provider.
	 */
	protected BegrensningTypeCti() {
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
