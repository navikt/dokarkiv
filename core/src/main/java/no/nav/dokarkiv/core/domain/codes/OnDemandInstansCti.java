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

	private static final long serialVersionUID = ***gammelt_fnr***39936458L;
	
	@Embedded
	@AttributeOverrides( { @AttributeOverride(name = "createdBy", column = @Column(name = "opprettet_av")),
			@AttributeOverride(name = "createdDate", column = @Column(name = "dato_opprettet")),
			@AttributeOverride(name = "updatedBy", column = @Column(name = "endret_av")),
			@AttributeOverride(name = "updatedDate", column = @Column(name = "dato_endret")) })
	private ChangeStamp changeStamp;
	
	protected OnDemandInstansCti() {
	}
	
	public ChangeStamp getChangeStamp() {
		return changeStamp;
	}
}
