package no.nav.dokarkiv.innsynjournal.v2.hentdokument;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

/**
 * Domain request for HentDokument(TJOARK051 and TJOARK054)
 *
 * @author Stig Strøm
 */
public class HentDokumentRequestTo {
	private Long dokumentInfoId;
	private Long journalpostId;
	private VariantFormatCode variantFormat;

	public HentDokumentRequestTo(Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {
		super();
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
		this.variantFormat = variantFormat;
	}

	public Long getDokumentInfoId() {
		return dokumentInfoId;
	}

	public Long getJournalpostId() {
		return journalpostId;
	}

	public VariantFormatCode getVariantFormat() {
		return variantFormat;
	}

	@Override
	public String toString() {
		return "HentDokumentRequestTo [dokumentInfoId=" + dokumentInfoId + ", journalpostId=" + journalpostId
				+ ", variantFormat=" + variantFormat + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		HentDokumentRequestTo that = (HentDokumentRequestTo) o;

		if (dokumentInfoId != null ? !dokumentInfoId.equals(that.dokumentInfoId) : that.dokumentInfoId != null) {
			return false;
		}
		if (journalpostId != null ? !journalpostId.equals(that.journalpostId) : that.journalpostId != null) {
			return false;
		}
		return variantFormat == that.variantFormat;
	}

	@Override
	public int hashCode() {
		int result = dokumentInfoId != null ? dokumentInfoId.hashCode() : 0;
		result = 31 * result + (journalpostId != null ? journalpostId.hashCode() : 0);
		result = 31 * result + (variantFormat != null ? variantFormat.hashCode() : 0);
		return result;
	}
}
