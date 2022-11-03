package no.nav.dokarkiv.core.domain.service;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

@Transactional
@Component
public class SkjermingService {

	private final JoarkRepository joarkRepository;

	private final EntityManager entityManager;

	public SkjermingService(JoarkRepository joarkRepository, EntityManager entityManager) {
		this.joarkRepository = joarkRepository;
		this.entityManager = entityManager;
	}

	public boolean isJournalpostSkjermet(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost != null) {
			return nonNull(journalpost.getSkjermingType());
		}

		return false;
	}

	public boolean isJournalpostSkjermet(Journalpost journalpost) {
		return nonNull(journalpost.getSkjermingType());
	}

	public boolean isKassertByFilUuid(String filUuid) {
		return isFalse(entityManager.createQuery("select 'kassert' from FilDetaljer where filUuid=:filUuid and dokumentInfo.kassert is true")
				.setParameter("filUuid", filUuid)
				.getResultList()
				.isEmpty());
	}


	public boolean isAllFildetaljerSkjermet(DokumentInfo dokumentInfo) {
		return dokumentInfo.getFildetaljerListeAdmin()
				.stream()
				.allMatch(filDetaljer -> nonNull(filDetaljer.getSkjermingType()));
	}

	public boolean isFildetaljerSkjermetByFilUuid(String filUuid) {
		return isFalse(entityManager.createQuery("select 'skjermet' from FilDetaljer where filUuid=:filUuid and skjermingType is not null")
				.setParameter("filUuid", filUuid)
				.getResultList()
				.isEmpty());
	}

	public void setFildetaljerSkjerming(Long dokumentInfoId, VariantFormatCode variantFormatCode, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update FilDetaljer set skjermingType = :skjermingTypeCode where dokument_info_id = :dokumentInfoId and variantFormat=:variantFormat")
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("variantFormat", variantFormatCode)
				.setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}

	public void setJpDokInfoRelSkjerming(Long jpId, Long dokInfoId, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update JournalpostDokumentInfoRelasjon set skjermingType = :skjermingTypeCode where dokument_info_id = :dokInfoId and journalpost_id=:jpId")
				.setParameter("jpId", jpId)
				.setParameter("dokInfoId", dokInfoId)
				.setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}

	public void setJournalpostSkjerming(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update Journalpost set skjermingType = :skjermingTypeCode where journalpostId = :journalpostId")
				.setParameter("journalpostId", journalpostId)
				.setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}


	public static Long convertBigToLong(Object value) {
		if (value instanceof BigDecimal) {
			return ((BigDecimal) value).longValue();
		} else if (value instanceof BigInteger) {
			return ((BigInteger) value).longValue();
		}
		return (Long) value;
	}


}
