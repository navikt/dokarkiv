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
 * CodesTableItem for the T_K_DOKUMENT_S table.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Entity
@Table(name = "T_K_DOKUMENT_S")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "k_dokument_s")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class DokumentStatusCti extends CodesTablePeriodicItem<DokumentStatusCode, String> {

	/** Serialization UID */
	private static final long serialVersionUID = 7687696421261680004L;

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
