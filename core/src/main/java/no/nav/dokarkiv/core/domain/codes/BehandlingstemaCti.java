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
 * CodesTableItem for the T_K_BEHANDLINGSTEMA table.
 *
 * @author TPaul Magne Lunde, Visma Consulting
 */

/**
 * Etter å ha endret type på behandlingstema fra Enum til String,
 * er ikke denne klassen lenger i bruk, men brukers fortsatt i noen tester.
 */
@Entity
@Table(name = "T_K_BEHANDLINGSTEMA")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "k_behandlingstema")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class BehandlingstemaCti extends CodesTablePeriodicItem<Behandlingstema, String> {

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
