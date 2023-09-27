package no.nav.dokarkiv.safintern.views;

import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class FetchPaths {
	private static final Set<String> GYLDIGE_PATHS = new HashSet<>();

	public static final String DOKUMENTER = "dokumenter";

	static {
		GYLDIGE_PATHS.add("journalpostId");
		GYLDIGE_PATHS.add("fagomraade");
		GYLDIGE_PATHS.add("fagomraadenavn");
		GYLDIGE_PATHS.add("status");
		GYLDIGE_PATHS.add("type");
		GYLDIGE_PATHS.add("kanalreferanseId");
		GYLDIGE_PATHS.add("mottakskanal");
		GYLDIGE_PATHS.add("utsendingskanal");
		GYLDIGE_PATHS.add("behandlingstema");
		GYLDIGE_PATHS.add("behandlingstemanavn");
		GYLDIGE_PATHS.add("innhold");
		GYLDIGE_PATHS.add("journalfoerendeEnhet");
		GYLDIGE_PATHS.add("journalfoertAvNavn");
		GYLDIGE_PATHS.add("opprettetAvNavn");
		GYLDIGE_PATHS.add("antallRetur");
		GYLDIGE_PATHS.add("innsyn");
		GYLDIGE_PATHS.add("skjerming");
		GYLDIGE_PATHS.add("relevanteDatoer");
		GYLDIGE_PATHS.add("avsenderMottaker");
		GYLDIGE_PATHS.add("saksrelasjon");
		GYLDIGE_PATHS.add("bruker");
		GYLDIGE_PATHS.add("utsendingsInfo");
		GYLDIGE_PATHS.add("tilleggsopplysninger");
		GYLDIGE_PATHS.add(DOKUMENTER);
		GYLDIGE_PATHS.add(DOKUMENTER + ".skjerming");
		GYLDIGE_PATHS.add(DOKUMENTER + ".fildetaljer");
	}

	public static boolean erGyldig(String fetchPath) {
		if (isBlank(fetchPath)) {
			return false;
		}
		return GYLDIGE_PATHS.contains(fetchPath);
	}
}
