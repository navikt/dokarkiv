package no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak;

import no.nav.dokarkiv.core.consumers.saf.exceptions.saf.SafJournalpostUnauthorizedException;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.journalpost.v1.api.knyttTilAnnenSak.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.knyttTilAnnenSak.KnyttTilAnnenSakRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak.DokumentBuilderUtils.INFO_ID;
import static no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak.DokumentBuilderUtils.SAKSBEHANDLER_HAR_IKKE_TILGANG;
import static no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak.DokumentBuilderUtils.SAKSBEHANDLER_HAR_TILGANG;
import static no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak.DokumentBuilderUtils.opprettDokumentvariant;
import static no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak.DokumentBuilderUtils.opprettJournalpostMedAngittDokumentvariant;
import static no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak.DokumentUtils.sjekkOmAlleDokumenterEksistererPaaJournalposten;
import static no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak.DokumentUtils.sjekkOmAlleDokumentvarianterErGyldige;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class DokumentUtilsTest {
	public static final long JOURNALPOST_ID = 111111111;
	public static final String VARIANTFORMAT_ARKIV = ARKIV.name();
	public static final String VARIANTFORMAT_SLADDET = SLADDET.name();

	// Positive tester
	@Test
	public void shouldApproveVariantArkiv(){
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant(VARIANTFORMAT_ARKIV, SAKSBEHANDLER_HAR_TILGANG);
		sjekkOmAlleDokumentvarianterErGyldige(journalpost, JOURNALPOST_ID);		// OK hvis exception ikke kastes.
	}

	@Test
	public void shouldApproveVariantSladdet(){
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant(VARIANTFORMAT_SLADDET, SAKSBEHANDLER_HAR_TILGANG);
		sjekkOmAlleDokumentvarianterErGyldige(journalpost, JOURNALPOST_ID);		// OK hvis exception ikke kastes.
	}

	@Test
	public void shouldApproveIfLastVariantIsOk(){
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant("ugyldigvariant", SAKSBEHANDLER_HAR_TILGANG);
		SafJournalpostTo.Dokumentvariant nyvariant = opprettDokumentvariant(VARIANTFORMAT_ARKIV, SAKSBEHANDLER_HAR_TILGANG);
		journalpost.getDokumenter().get(0).getDokumentvarianter().add(nyvariant);
		sjekkOmAlleDokumentvarianterErGyldige(journalpost, JOURNALPOST_ID);		// OK hvis exception ikke kastes.
	}

	@Test
	public void shouldApproveIfFirstVariantIsOk(){
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant(VARIANTFORMAT_ARKIV, SAKSBEHANDLER_HAR_TILGANG);
		SafJournalpostTo.Dokumentvariant nyvariant = opprettDokumentvariant("ugyldigvariant", SAKSBEHANDLER_HAR_TILGANG);
		journalpost.getDokumenter().get(0).getDokumentvarianter().add(nyvariant);
		sjekkOmAlleDokumentvarianterErGyldige(journalpost, JOURNALPOST_ID);		// OK hvis exception ikke kastes.
	}

	@Test
	public void shouldApproveAlleDokumenterEksistererPaaJournalpost() {
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant(VARIANTFORMAT_ARKIV, SAKSBEHANDLER_HAR_TILGANG);

		KnyttTilAnnenSakRequest request = KnyttTilAnnenSakRequest.builder()
				.dokumenter(journalpost.getDokumenter().stream()
						.map(dokument -> new Dokument(dokument.getDokumentInfoId()))
						.toList())
				.build();

		assertDoesNotThrow(() -> sjekkOmAlleDokumenterEksistererPaaJournalposten(request, journalpost, JOURNALPOST_ID));
	}

	@Test
	public void shouldApproveDokumenterIsNull() {
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant(VARIANTFORMAT_ARKIV, SAKSBEHANDLER_HAR_TILGANG);

		KnyttTilAnnenSakRequest request = KnyttTilAnnenSakRequest.builder()
				.dokumenter(null)
				.build();

		assertDoesNotThrow(() -> sjekkOmAlleDokumenterEksistererPaaJournalposten(request, journalpost, JOURNALPOST_ID));
	}

	// Negative tester
	@Test
	public void shouldThrowExeptionOnWrongVariantformat(){
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant("ugyldigvariant", SAKSBEHANDLER_HAR_TILGANG);

		Exception thrownException = assertThrows(SafJournalpostUnauthorizedException.class, () -> sjekkOmAlleDokumentvarianterErGyldige(journalpost, JOURNALPOST_ID));
		assertEquals(String.format("Dokumentvariant har ikke variantformat 'ARKIV' eller 'SLADDET' der saksbehandlerHarTilgang = TRUE for journalpostId=%s", JOURNALPOST_ID),
				thrownException.getMessage());
	}

	@Test
	public void shouldThrowExeptionOnSaksbehandlerHarikkeTilgang(){
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant(VARIANTFORMAT_ARKIV, SAKSBEHANDLER_HAR_IKKE_TILGANG);

		Exception thrownException = assertThrows(SafJournalpostUnauthorizedException.class, () -> sjekkOmAlleDokumentvarianterErGyldige(journalpost, JOURNALPOST_ID));
		assertEquals(String.format("Dokumentvariant har ikke variantformat 'ARKIV' eller 'SLADDET' der saksbehandlerHarTilgang = TRUE for journalpostId=%s", JOURNALPOST_ID),
				thrownException.getMessage());
	}

	@Test
	public void shouldThrowExceptionOnDokumenterEksistererIkkePaaJournalpost() {
		SafJournalpostTo journalpost = opprettJournalpostMedAngittDokumentvariant(VARIANTFORMAT_ARKIV, SAKSBEHANDLER_HAR_TILGANG);

		var dokumentInfoId1 = INFO_ID + "1";
		var dokumentInfoId2 = INFO_ID + "2";

		KnyttTilAnnenSakRequest request = KnyttTilAnnenSakRequest.builder()
				.dokumenter(List.of(new Dokument(dokumentInfoId1), new Dokument(dokumentInfoId2)))
				.build();

		assertThatExceptionOfType(JournalpostDokumentInfoRelasjonIkkeFunnetException.class)
				.isThrownBy(() -> sjekkOmAlleDokumenterEksistererPaaJournalposten(request, journalpost, JOURNALPOST_ID))
				.withMessage("Dokument(er) med id %s finnes ikke paa journalpost med journalpostId=%s".formatted(List.of(dokumentInfoId1, dokumentInfoId2), JOURNALPOST_ID));
	}
}
