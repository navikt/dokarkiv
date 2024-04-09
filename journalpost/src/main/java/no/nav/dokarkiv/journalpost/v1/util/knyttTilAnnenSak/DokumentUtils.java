package no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumers.saf.exceptions.saf.SafJournalpostUnauthorizedException;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo.DokumentInfo;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo.Dokumentvariant;

import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;

@Slf4j
public class DokumentUtils {

	/*
	 * For hvert dokument
	 * 		Sjekk at det finnes et dokument hvor (arkivvariant == ARKIV || SLADDET) && isSaksbehandlerHarTilgang == true
	 */
	public static void sjekkOmAlleDokumentvarianterErGyldige(SafJournalpostTo safJournalpost, long journalpostId) {
		List<SafJournalpostTo.DokumentInfo> dokumenter = safJournalpost.getDokumenter();
		if(dokumenter.isEmpty()){
			return;
		}
		for(DokumentInfo dokument : dokumenter){
			if(!harSaksbehandlerTilgangTilDokumentet(dokument)){
				throw new SafJournalpostUnauthorizedException(String.format("Dokumentvariant har ikke variantformat 'ARKIV' eller 'SLADDET' der saksbehandlerHarTilgang = TRUE for journalpostId=%s", journalpostId));
			}
		}
	}

	private static boolean harSaksbehandlerTilgangTilDokumentet(DokumentInfo dokument){
		for(Dokumentvariant variant : dokument.getDokumentvarianter()){
			if(isDokumentVariantArkivOrSladdet(variant) && variant.isSaksbehandlerHarTilgang()){
				return true;
			}
		}
		return false;
	}

	private static boolean isDokumentVariantArkivOrSladdet(Dokumentvariant dokumentvariant) {
		return (ARKIV.name().equals(dokumentvariant.getVariantformat())
				|| (SLADDET.name()).equals(dokumentvariant.getVariantformat()));
	}
}
