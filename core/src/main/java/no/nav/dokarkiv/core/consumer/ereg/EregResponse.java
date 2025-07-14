package no.nav.dokarkiv.core.consumer.ereg;

public record EregResponse(
		String organisasjonsnummer,
		Navn navn) {

	public record Navn(String sammensattnavn) {
	}
}
