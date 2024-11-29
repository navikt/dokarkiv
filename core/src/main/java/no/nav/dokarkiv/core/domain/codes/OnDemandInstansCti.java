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
