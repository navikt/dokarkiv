package no.nav.dokarkiv.journalpost.v1.util.kopierjournalpost;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class ShallowDokumentInfoCopier {

	public DokumentInfo copy(DokumentInfo dokumentInfo){
		return dokumentInfo.toBuilder()
				.dokumentInfoId(null)
				.tilleggsopplysninger(new HashMap<>(dokumentInfo.getTilleggsopplysninger()))
				.fildetaljerListe(new HashSet<>())
				.journalpostRelasjoner(new HashSet<>())
				.skannetInnholdListe(new HashSet<>())
				.build();

	}
}
