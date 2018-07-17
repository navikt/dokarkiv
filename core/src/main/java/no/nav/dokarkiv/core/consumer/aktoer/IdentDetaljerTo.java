package no.nav.dokarkiv.core.consumer.aktoer;

import java.util.Date;

/**
 * Object representing ident details.
 * Used by {@link HentAktoerIdForIdentResponseTo}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class IdentDetaljerTo {

	private String fnr;
	private Date datoFom;

	public IdentDetaljerTo(String fnr, Date datoFom) {
		this.fnr = fnr;
		this.datoFom = datoFom;
	}

	public String getFnr() {
		return fnr;
	}


	public Date getDatoFom() {
		return datoFom;
	}


	@Override
	public String toString() {
		return "IdentDetaljerTo{" +
				"fnr='" + fnr + '\'' +
				", datoFom=" + datoFom +
				'}';
	}
}
