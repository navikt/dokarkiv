package no.nav.dokarkiv.core.storage;

/// Resultat av en vellykket opplasting til dokarkivmellomlager.
///
/// @param crc32cBase64 crc32c-sjekksummen Google Cloud Storage har beregnet for det opplastede objektet, base64-kodet.
/// @param antallBytes  antall bytes som ble lastet opp.
public record OpplastetDokumentFil(String crc32cBase64, long antallBytes) {
}
