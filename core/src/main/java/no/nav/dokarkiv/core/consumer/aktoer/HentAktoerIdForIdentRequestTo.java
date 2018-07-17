package no.nav.dokarkiv.core.consumer.aktoer;

/**
 * Request object for HentAktoerIdForIdent
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class HentAktoerIdForIdentRequestTo {

	private String ident;

	public HentAktoerIdForIdentRequestTo(String ident) {
		this.ident = ident;
	}

	public String getIdent() {
		return ident;
	}

	@Override
	public String toString() {
		return "HentAktoerIdForIdentRequestTo{" +
				"ident='" + ident + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		HentAktoerIdForIdentRequestTo that = (HentAktoerIdForIdentRequestTo) o;

		return !(ident != null ? !ident.equals(that.ident) : that.ident != null);

	}

	@Override
	public int hashCode() {
		return ident != null ? ident.hashCode() : 0;
	}
}
