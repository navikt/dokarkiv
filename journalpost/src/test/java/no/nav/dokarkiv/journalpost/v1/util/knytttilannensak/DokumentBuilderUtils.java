package no.nav.dokarkiv.journalpost.v1.util.knytttilannensak;

import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;

import java.util.ArrayList;
import java.util.List;

public class DokumentBuilderUtils {
	public static final boolean SAKSBEHANDLER_HAR_TILGANG = true;
	public static final boolean SAKSBEHANDLER_HAR_IKKE_TILGANG = false;
	public static final String INFO_ID = "123456789";

	public static SafJournalpostTo opprettJournalpost(List<SafJournalpostTo.DokumentInfo> dokumentinfoListe){
		return SafJournalpostTo.builder().dokumenter(dokumentinfoListe).build();
	}

	public static SafJournalpostTo opprettJournalpostMedAngittDokumentvariant(String variantformat, boolean hartilgang) {
		List<SafJournalpostTo.Dokumentvariant> dokumentvarianter = new ArrayList<>();
		dokumentvarianter.add(opprettDokumentvariant(variantformat, hartilgang));
		List<SafJournalpostTo.DokumentInfo> dokumentinfoliste = opprettTomDokumentinfoListe();
		dokumentinfoliste.add(opprettDokumentinfo(INFO_ID, dokumentvarianter));
		return opprettJournalpost(dokumentinfoliste);
	}

	public static List<SafJournalpostTo.DokumentInfo> opprettTomDokumentinfoListe(){
		List<SafJournalpostTo.DokumentInfo> liste = new ArrayList<>();
		return liste;
	}

	public static SafJournalpostTo.DokumentInfo opprettDokumentinfo(String infoId, List<SafJournalpostTo.Dokumentvariant> varianter){
		return SafJournalpostTo.DokumentInfo.builder().
				dokumentInfoId(infoId).
				dokumentvarianter(varianter).
				build();
	}

	public static SafJournalpostTo.Dokumentvariant opprettDokumentvariant(String variantformat, boolean saksbehandlerHarTilgang){
		return SafJournalpostTo.Dokumentvariant.
				builder().
				variantformat(variantformat).
				saksbehandlerHarTilgang(saksbehandlerHarTilgang).
				build();
	}
}
