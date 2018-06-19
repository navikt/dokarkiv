package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;

/**
 * Builder for Bruker.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Deprecated // bruk lombok builder istedet
public class BrukerBuilder extends Builder<Bruker> {

	private BrukerBuilder() {
	}
	
	public static BrukerBuilder getBrukerBuilder() {
		return new BrukerBuilder();
	}

	private Long brukerInfoId;
	private String brukerId;
	private BrukerTypeCode brukerType;
	private String opprettetKildeNavn;
	private String endretKildeNavn;
	private ChangeStamp changeStamp;
	
	public BrukerBuilder brukerInfoId(Long value) { this.brukerInfoId = value; return this; }
	public BrukerBuilder brukerId(String value) { this.brukerId = value; return this; }
	public BrukerBuilder brukerType(BrukerTypeCode value) { this.brukerType = value; return this; }
	public BrukerBuilder opprettetKildeNavn(String value) { this.opprettetKildeNavn = value; return this; }
	public BrukerBuilder endretKildeNavn(String value) { this.endretKildeNavn = value; return this; }
	public BrukerBuilder changeStamp(ChangeStamp value) { this.changeStamp = value; return this; }

	
	@Override
	public Bruker build() {
		Bruker bruker = new Bruker(brukerInfoId, 1);
		bruker.setBrukerId(brukerId);
		bruker.setBrukerType(brukerType);
		bruker.setOpprettetKildeNavn(opprettetKildeNavn);
		bruker.setEndretKildeNavn(endretKildeNavn);
		bruker.setChangeStamp(changeStamp);
		return bruker;
	}

}
