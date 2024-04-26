package no.nav.dokarkiv.safintern;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.safintern.views.FetchPaths;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.slf4j.Logger;

import java.util.Set;

import static no.nav.dokarkiv.safintern.views.FetchPaths.DOKUMENTER;


public class FetchingFieldsUtil {
	public static EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> fetch(Set<String> fields, Logger callingClassLogger) {
		if (fields == null || fields.isEmpty()) {
			return EntityViewSetting.create(JournalpostView.class);
		}
		EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs = EntityViewSetting.create(JournalpostView.class);
		for (String path : fields) {
			if (FetchPaths.erGyldig(path)) {
				evs.fetch(path);
			} else {
				String feilmelding = "safintern/journalpost forsøker fetch på ugyldig path=" + path;
				callingClassLogger.error(feilmelding);
				throw new IllegalArgumentException(feilmelding);
			}
		}
		return evs;
	}

	public static CriteriaBuilder<Journalpost> dokumenterOrder(EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs, CriteriaBuilder<Journalpost> cb) {
		if (evs.getFetches().isEmpty() || evs.getFetches().stream().anyMatch(f -> f.contains(DOKUMENTER))) {
			return cb.orderByAsc("journalpostDokumentInfoRelasjoner.tilknyttetJournalpostSom")
					.orderByAsc("journalpostDokumentInfoRelasjoner.journalpostDokumentInfoRelasjonId");
		}
		return cb;
	}
}
