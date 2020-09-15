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
 * CodesTable item for OnDemandInstans
 * @author Hans Olav Loftum, BEKK
 */
@Entity
@Table(name = "T_K_ONDEMAND_INST")
@AttributeOverrides( { @AttributeOverride(name = "code", column = @Column(name = "k_ondemand_inst")),
		@AttributeOverride(name = "decode", column = @Column(name = "dekode")),
		@AttributeOverride(name = "valid", column = @Column(name = "er_gyldig")),
		@AttributeOverride(name = "fromDate", column = @Column(name = "dato_fom")),
		@AttributeOverride(name = "toDate", column = @Column(name = "dato_tom")) })
public class OnDemandInstansCti extends CodesTablePeriodicItem<OnDemandInstansCode, String> {

	private static final long serialVersionUID = 3047109171239936458L;
	
	@Embedded
	private ChangeStamp changeStamp;
	
	protected OnDemandInstansCti() {
	}
	
	public ChangeStamp getChangeStamp() {
		return changeStamp;
	}
}
