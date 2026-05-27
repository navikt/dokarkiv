package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

/**
 * Builder for Saksrelasjon.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
@Deprecated // bruk lombok builder istedet
public class SaksrelasjonBuilder extends Builder<Saksrelasjon> {

	private SaksrelasjonBuilder() {
	}

	public static SaksrelasjonBuilder getSaksrelasjonBuilder() {
		return new SaksrelasjonBuilder();
	}

	private Long saksrelasjonId;
	private Long sakId;
	private Boolean feilregistrert;
	private String endretAvNavn;
	private FagsystemCode fagsystem;
	private String opprettetKildeNavn;
	private String endretKildeNavn;

	public SaksrelasjonBuilder saksrelasjonId(Long value) {
		this.saksrelasjonId = value;
		return this;
	}

	public SaksrelasjonBuilder sakId(Long value) {
		this.sakId = value;
		return this;
	}

	public SaksrelasjonBuilder feilregistrert(Boolean value) {
		this.feilregistrert = value;
		return this;
	}

	public SaksrelasjonBuilder endretAvNavn(String value) {
		this.endretAvNavn = value;
		return this;
	}

	public SaksrelasjonBuilder fagsystem(FagsystemCode value) {
		this.fagsystem = value;
		return this;
	}

	public SaksrelasjonBuilder opprettetKildeNavn(String value) {
		this.opprettetKildeNavn = value;
		return this;
	}

	public SaksrelasjonBuilder endretKildeNavn(String value) {
		this.endretKildeNavn = value;
		return this;
	}

	@Override
	public Saksrelasjon build() {
		Saksrelasjon saksrelasjon = new Saksrelasjon(saksrelasjonId, 1);
		saksrelasjon.setSakId(sakId);
		saksrelasjon.setFeilregistrert(feilregistrert);
		saksrelasjon.setEndretAvNavn(endretAvNavn);
		saksrelasjon.setFagsystem(fagsystem);
		saksrelasjon.setOpprettetKildeNavn(opprettetKildeNavn);
		saksrelasjon.setEndretKildeNavn(endretKildeNavn);
		return saksrelasjon;
	}

}
