package no.nav.dokarkiv.safintern.hentdokument;

import java.sql.Blob;

record HentDokumentDto(String filtype, Blob dokument) {
	public boolean harDokument() {
		return dokument != null;
	}
}
