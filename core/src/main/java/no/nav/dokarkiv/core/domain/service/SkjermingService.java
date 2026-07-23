package no.nav.dokarkiv.core.domain.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Transactional
@Component
public class SkjermingService {

	private final JournalpostRepository journalpostRepository;

	private final EntityManager entityManager;

	public SkjermingService(JournalpostRepository journalpostRepository, EntityManager entityManager) {
		this.journalpostRepository = journalpostRepository;
		this.entityManager = entityManager;
	}

	public boolean isJournalpostSkjermet(Long journalpostId) {
		Journalpost journalpost = journalpostRepository.findById(journalpostId).orElse(null);
		if (journalpost != null) {
			return journalpost.isSkjermet();
		}

		return false;
	}

	public boolean isKassertByFilUuid(String filUuid) {
		return isFalse(entityManager.createQuery("select 'kassert' from FilDetaljer where filUuid=:filUuid and dokumentInfo.kassert is true")
				.setParameter("filUuid", filUuid)
				.getResultList()
				.isEmpty());
	}

	public boolean isAllFildetaljerSkjermet(DokumentInfo dokumentInfo) {
		return dokumentInfo.getFildetaljerListe()
				.stream()
				.allMatch(FilDetaljer::isSkjermet);
	}

	public boolean isFildetaljerSkjermetByFilUuid(String filUuid) {
		return isFalse(entityManager.createQuery("select 'skjermet' from FilDetaljer where filUuid=:filUuid and skjermingType is not null")
				.setParameter("filUuid", filUuid)
				.getResultList()
				.isEmpty());
	}

	public void setFildetaljerSkjerming(Long dokumentInfoId, VariantFormatCode variantFormatCode, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update FilDetaljer set skjermingType = :skjermingTypeCode where dokumentInfo.dokumentInfoId = :dokumentInfoId and variantFormat=:variantFormat")
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("variantFormat", variantFormatCode)
				.setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}

	public void setJournalpostSkjerming(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update Journalpost set skjermingType = :skjermingTypeCode where journalpostId = :journalpostId")
				.setParameter("journalpostId", journalpostId)
				.setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}
}
