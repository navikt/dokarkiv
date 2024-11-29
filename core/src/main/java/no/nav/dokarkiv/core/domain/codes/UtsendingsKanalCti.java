package no.nav.dokarkiv.core.domain.codes;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * CodesTableItem for the T_K_UTSENDINGS_KANAL table.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Entity
@Table(name = "T_K_UTSENDINGS_KANAL")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "k_utsendings_kanal")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class UtsendingsKanalCti extends CodesTablePeriodicItem<UtsendingsKanalCode, String> {

	/** Serialization UID */
	private static final long serialVersionUID = 3952639029892000821L;
	
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
