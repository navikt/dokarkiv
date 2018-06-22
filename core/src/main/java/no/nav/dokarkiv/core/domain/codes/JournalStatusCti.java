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
 * CodesTableItem representing the T_K_JOURNAL_S DB table.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Entity
@Table(name = "T_K_JOURNAL_S")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "k_journal_s")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class JournalStatusCti extends CodesTablePeriodicItem<JournalStatusCode, String> {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = -***gammelt_fnr***61340128L;

	/**
	 * Audit information.
	 */
	@Embedded
	private ChangeStamp changeStamp;

	/**
	 * Default no-arg constructor. Should only be called by persistence
	 * provider.
	 */
	protected JournalStatusCti() {
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
