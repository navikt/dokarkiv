package no.nav.dokarkiv.safintern.journalpost;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static no.nav.dokarkiv.safintern.FetchingFieldsUtil.fetch;

@Slf4j
@Component
@Transactional(readOnly = true)
public class SafinternJournalpostService {

	private final SafinternJournalpostRepository repository;

	public SafinternJournalpostService(SafinternJournalpostRepository repository) {
		this.repository = repository;
	}

	public JournalpostView hentJournalpostById(final Long journalpostId, Set<String> fields) {
		EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs = fetch(fields, log);
		return repository.hentJournalpostById(journalpostId, evs)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Journalpost med journalpostId=" + journalpostId + " ikke funnet"));
	}

	public JournalpostView hentJournalpostByIdAndDokumentInfoId(final Long journalpostId, final Long dokumentInfoId, Set<String> fields) {
		EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs = fetch(fields, log);
		return repository.hentJournalpostByIdDokumentInfoId(journalpostId, dokumentInfoId, evs)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Journalpost med journalpostId=" + journalpostId + ", dokumentInfoId" + dokumentInfoId + " ikke funnet"));
	}

	public JournalpostView hentJournalpostByEksternReferanseId(final String eksternReferanseId, Set<String> fields) {
		EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs = fetch(fields, log);
		return repository.hentJournalpostByEksternReferanseId(eksternReferanseId, evs)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Journalpost med eksternReferanseId=" + eksternReferanseId + " ikke funnet"));
	}
}
