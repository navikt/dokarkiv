package no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class DokumentInfoCopier {

	public DokumentInfo copy(DokumentInfo dokumentInfo){
		DokumentInfo kopiertDokumentInfo = dokumentInfo.toBuilder()
				.dokumentInfoId(null)
				.tilleggsopplysninger(copyTilleggsopplysninger(dokumentInfo.getTilleggsopplysninger()))
				.fildetaljerListe(new HashSet<>())
				.journalpostRelasjoner(new HashSet<>())
				.skannetInnholdListe(new HashSet<>())
				.build();
		return kopiertDokumentInfo;
	}

	private Map<String, String> copyTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		HashMap<String, String> kopiertTilleggsopplysninger = new HashMap<>();
		tilleggsopplysninger.forEach(kopiertTilleggsopplysninger::put);
		return kopiertTilleggsopplysninger;
	}
}
