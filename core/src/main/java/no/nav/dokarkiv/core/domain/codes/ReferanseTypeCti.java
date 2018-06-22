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
 * CodesTableItem for the T_K_REFERANSE_T table.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Entity
@Table(name = "T_K_REFERANSE_T")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "k_referanse_t")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class ReferanseTypeCti extends CodesTablePeriodicItem<ReferanseTypeCode, String> {

	/** Serialization UID */
	private static final long serialVersionUID = -***gammelt_fnr***02233269L;

	/**
	 * Audit information.
	 */
	@Embedded
	private ChangeStamp changeStamp;

	/**
	 * Getter for the changeStamp property.
	 *
	 * @return the changeStamp
	 */
	public ChangeStamp getChangeStamp() {
		return changeStamp;
	}
}
