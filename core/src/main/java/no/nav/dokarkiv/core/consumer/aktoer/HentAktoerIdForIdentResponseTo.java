package no.nav.dokarkiv.core.consumer.aktoer;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object for HentAktoerIdForIdent
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class HentAktoerIdForIdentResponseTo {

	private String aktoerId;
	private List<IdentDetaljerTo> historiskeIdenter = new ArrayList<>();

	public HentAktoerIdForIdentResponseTo(String aktoerId, List<IdentDetaljerTo> historiskeIdenter) {
		setAktoerId(aktoerId);
		setHistoriskeIdenter(historiskeIdenter);
	}

	public HentAktoerIdForIdentResponseTo() {
	}

	public String getAktoerId() {
		return aktoerId;
	}

	public void setAktoerId(String aktoerId) {
		this.aktoerId = aktoerId;
	}

	public List<IdentDetaljerTo> getHistoriskeIdenter() {
		return historiskeIdenter;
	}

	public void setHistoriskeIdenter(List<IdentDetaljerTo> historiskeIdenter) {
		if(historiskeIdenter == null) {
			this.historiskeIdenter = new ArrayList<>();
		} else {
			this.historiskeIdenter = historiskeIdenter;
		}
	}

	@Override
	public String toString() {
		return "HentAktoerIdForIdentResponseTo{" +
				"aktoerId='" + aktoerId + '\'' +
				", historiskeIdenter=" + historiskeIdenter +
				'}';
	}
}
