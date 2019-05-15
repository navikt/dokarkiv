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
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */

@Entity
@Table(name = "T_K_AVSEND_MOTTAK_ID_T")
@AttributeOverrides({@AttributeOverride(name = "code", column = @Column(name = "k_avsend_mottak_id_t")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom"))})
public class AvsenderMottakerTypeCti extends CodesTablePeriodicItem<AvsenderMottakerCode, String> {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = -***gammelt_fnr***89214456L;

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
