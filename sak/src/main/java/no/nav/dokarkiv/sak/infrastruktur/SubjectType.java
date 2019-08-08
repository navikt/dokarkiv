package no.nav.dokarkiv.sak.infrastruktur;

public enum SubjectType {
	SUBJECT_TYPE_SYSTEMBRUKER("Systemressurs"), SUBJECT_TYPE_INTERNBRUKER("InternBruker"), SUBJECT_TYPE_EKSTERNBRUKER("EksternBruker");

	private String value;

	SubjectType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
