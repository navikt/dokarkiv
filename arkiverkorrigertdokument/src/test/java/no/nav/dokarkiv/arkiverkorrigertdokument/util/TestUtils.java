package no.nav.dokarkiv.arkiverkorrigertdokument.util;

import no.nav.dokarkiv.arkiverkorrigertdokument.rjoark103.ArkiverKorrigertDokumentRequestTo;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TestUtils {

	private static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Endret av navn";
	private static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	private static final String DOKUMENT_TITTEL = "SlettDokumentTittel";
	private static final String BREVGRUPPE = "Brevgruppe";
	private static final String BREVKODE = "Brevkode";
	private static final String FILNAVN = "filNavn";

	public static final Long JOURNALPOST_ID = 42L;
	public static final Long DOKUMENTINFO_ID = 1L;
	public static final byte[] BINAER_FIL = "Test av binærfil".getBytes();


	public static ArkiverKorrigertDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId, byte[] binaerFil) {
		return ArkiverKorrigertDokumentRequestTo.builder()
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.binaerFil(binaerFil)
				.build();
	}

	public static ArkiverKorrigertDokumentRequestTo createRequest() {
		return createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, BINAER_FIL);
	}

	public static DokumentInfo createDokumentInfo() {
		return DokumentInfo.builder()
				.slettet(false)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(DOKUMENT_TITTEL)
				.endretAvNavn(ENDRET_AV_NAVN)
				.brevgruppe(BREVGRUPPE)
				.brevkode(BREVKODE)
				.fildetaljerListe(createSetOfFildetaljer())
				.build();
	}

	public static Set<FilDetaljer> createSetOfFildetaljer() {
		return new HashSet<>(Arrays.asList(createFildetaljer(FilDetaljer.generateUuid())));
	}

	private static FilDetaljer createFildetaljer(String filUuid) {
		return FilDetaljer.builder()
				.filUuid(filUuid)
//				.filnavn(FILNAVN)
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ARKIV)
				.build();
	}

	public static FilDetaljer createFildetaljerVariantFormat(VariantFormatCode variantFormat) {
		return FilDetaljer.builder()
				.filUuid(FilDetaljer.generateUuid())
				.variantFormat(variantFormat)
				.build();
	}
}
