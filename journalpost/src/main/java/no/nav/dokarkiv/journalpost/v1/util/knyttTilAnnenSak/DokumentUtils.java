package no.nav.dokarkiv.journalpost.v1.util.knyttTilAnnenSak;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.core.exceptions.saf.SafJournalpostUnauthorizedException;

import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;

@Slf4j
public class DokumentUtils {
	public static void sjekkOmAlleDokumentvarianterErGyldige(SafJournalpostTo safJournalpost, String journalpostId) {
		List<SafJournalpostTo.DokumentInfo> dokumenter = safJournalpost.getDokumenter();
		if (!dokumenter.isEmpty()) {
			boolean gyldigVariantMangler = true;
			for (SafJournalpostTo.DokumentInfo dokument : dokumenter) {
				List<SafJournalpostTo.Dokumentvariant> dokumentvarianter = dokument.getDokumentvarianter();
				for (SafJournalpostTo.Dokumentvariant variant : dokumentvarianter) {
					gyldigVariantMangler = true;
					if (isDokumentVariantArkivOrSladdet(variant) && variant.isSaksbehandlerHarTilgang()) {
						gyldigVariantMangler = false;
						break;
					}
				}

				if (gyldigVariantMangler) {
					log.error(String.format("Dokument med InfoId=%s oppfyller ikke kravet \"variantformat 'ARKIV' eller 'SLADDET' der saksbehandlerHarTilgang = TRUE\" for journalpostId=%s", dokument.getDokumentInfoId(), journalpostId));
					break;
				}
			}
			if (gyldigVariantMangler) {
				throw new SafJournalpostUnauthorizedException(String.format("Dokumentvariant har ikke variantformat 'ARKIV' eller 'SLADDET' der saksbehandlerHarTilgang = TRUE for journalpostId=%s", journalpostId));
			}
		}
	}

	private static boolean isDokumentVariantArkivOrSladdet(SafJournalpostTo.Dokumentvariant dokumentvariant) {
		return (ARKIV.name().equals(dokumentvariant.getVariantformat())
				|| (SLADDET.name()).equals(dokumentvariant.getVariantformat()));
	}
}
