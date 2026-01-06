package no.nav.dokarkiv.journalpost.v1.api.settbrevdata;

import no.nav.dokarkiv.journalpost.v1.services.SettBrevdata;

public record SettBrevdataResponse(String filUuid, int filstoerrelse) {

	public static SettBrevdataResponse from(SettBrevdata settBrevdata) {
		return new SettBrevdataResponse(settBrevdata.filUuid(), settBrevdata.filstoerrelse());
	}
}
