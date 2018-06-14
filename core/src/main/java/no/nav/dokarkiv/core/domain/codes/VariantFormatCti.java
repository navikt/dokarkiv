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
 * CodesTableItem for the T_K_VARIANT_FORMAT table.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Entity
@Table(name = "T_K_VARIANT_FORMAT")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "k_variant_format")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class VariantFormatCti extends CodesTablePeriodicItem<VariantFormatCode, String> {

	/** Serialization UID */
	private static final long serialVersionUID = -***gammelt_fnr***68090864L;
	
	/**
	 * Audit information.
	 */
	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "createdBy", column = @Column(name = "opprettet_av")),
			@AttributeOverride(name = "createdDate", column = @Column(name = "dato_opprettet")),
			@AttributeOverride(name = "updatedBy", column = @Column(name = "endret_av")),
			@AttributeOverride(name = "updatedDate", column = @Column(name = "dato_endret")) })
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
