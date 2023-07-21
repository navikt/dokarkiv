package no.nav.dokarkiv.safintern.hentdokument;

import java.io.InputStream;

record HentDokumentResponse(String filtype, InputStream dokument, int dokumentLength) {

}
