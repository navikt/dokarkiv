package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.Behandlingsrelasjon;

/**
 * Builder for Bruker.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Deprecated // bruk lombok builder istedet
public class BehandlingsrelasjonBuilder extends Builder<Behandlingsrelasjon> {

	private BehandlingsrelasjonBuilder() {
	}
	
	public static BehandlingsrelasjonBuilder getBehandlingsrelasjonBuilder() {
		return new BehandlingsrelasjonBuilder();
	}

	private Long behandlingsrelasjonId;
	private String behandlingsId;
	private String behandlingsType;
	private String opprettetKildeNavn;
	private String endretKildeNavn;
	
	public BehandlingsrelasjonBuilder behandlingsrelasjonId(Long value) { this.behandlingsrelasjonId = value; return this; }
	public BehandlingsrelasjonBuilder behandlingsId(String value) { this.behandlingsId = value; return this; }
	public BehandlingsrelasjonBuilder behandlingsType(String value) { this.behandlingsType = value; return this; }
	public BehandlingsrelasjonBuilder opprettetKildeNavn(String value) { this.opprettetKildeNavn = value; return this; }
	public BehandlingsrelasjonBuilder endretKildeNavn(String value) { this.endretKildeNavn = value; return this; }

	@Override
	public Behandlingsrelasjon build() {
		Behandlingsrelasjon behandlingsrelasjon = new Behandlingsrelasjon(behandlingsrelasjonId);
		behandlingsrelasjon.setBehandlingsId(behandlingsId);
		behandlingsrelasjon.setBehandlingsType(behandlingsType);
		behandlingsrelasjon.setOpprettetKildeNavn(opprettetKildeNavn);
		behandlingsrelasjon.setEndretKildeNavn(endretKildeNavn);
		return behandlingsrelasjon;
	}

}
