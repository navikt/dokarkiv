package no.nav.dokarkiv.internal.settbrevdata;

public record SettBrevdataResponse(String filUuid, int filstoerrelse) {

	public static SettBrevdataResponse from(SettBrevdata settBrevdata) {
		return new SettBrevdataResponse(settBrevdata.filUuid(), settBrevdata.filstoerrelse());
	}
}
