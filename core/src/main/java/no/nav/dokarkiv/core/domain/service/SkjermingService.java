package no.nav.dokarkiv.core.domain.service;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
@Component
public class SkjermingService {

	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private JoarkRepository joarkRepository;
	private DokumentinfoRepository dokumentinfoRepository;

	private final EntityManager entityManager;

	@Inject
	public SkjermingService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository, DokumentinfoRepository dokumentinfoRepository, EntityManager entityManager) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
		this.entityManager = entityManager;
	}

	public boolean isJournalpostSkjermet(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost != null) {
			return nonNull(journalpost.getSkjermingType());
		}

		return false;
	}

	public boolean isKassertByFilUuid(String filUuid) {
		return isFalse(entityManager.createQuery("select 'kassert' from FilDetaljer where filUuid=:filUuid and dokumentInfo.kassert is true")
				.setParameter("filUuid", filUuid)
				.getResultList()
				.isEmpty());
	}

	public boolean isJournalpostSkjermet(Journalpost journalpost) {
		return nonNull(journalpost.getSkjermingType());
	}

	public boolean isVariantSkjermet(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjermingTypeCode) {
		Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId);
		if (dokumentInfo.isPresent()) {
			FilDetaljer filDetaljer = dokumentInfo.get().findFilDetaljerByVariantFormatAdmin(variant);
			if (nonNull(filDetaljer) && skjermingTypeCode.equals(filDetaljer.getSkjermingType())) {
				return true;
			}
		}
		return false;
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

	/**
	 * Brukes bare i test
	 */
	public void skjermAllFildetaljer(DokumentInfo dokumentInfo, SkjermingTypeCode skjermingTypeCode) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListeAdmin()) {
			setFildetaljerSkjerming(dokumentInfo.getDokumentInfoId(), filDetaljer.getVariantFormat(), skjermingTypeCode);
		}
	}

	public void setDokumentKassert(DokumentInfo dokumentInfo, SkjermingTypeCode skjermingTypeCode) {
		skjermAllFildetaljer(dokumentInfo, skjermingTypeCode);
		Query q = entityManager.createQuery("update DokumentInfo set kassert=true where dokument_info_id = :dokumentInfoId")
				.setParameter("dokumentInfoId", dokumentInfo.getDokumentInfoId());
		q.executeUpdate();
	}

	public void skjermJournalpost(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		setJournalpostSkjerming(journalpostId, skjermingTypeCode);
	}


	public void skjermJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		JournalpostDokumentInfoRelasjon rel = hentJpDokInfoRel(journalpostId, dokumentInfoId);
		setJpDokInfoRelSkjerming(rel.getJournalpostDokumentInfoRelasjonId(), skjermingTypeCode);
	}

	public void setVariantSkjermet(Long dokumentInfoId, VariantFormatCode variantFormatCode, SkjermingTypeCode skjermingTypeCode) {
		setFildetaljerSkjerming(dokumentInfoId, variantFormatCode, skjermingTypeCode);
	}

	public void setFildetaljerSkjerming(Long dokumentInfoId, VariantFormatCode variantFormatCode, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update FilDetaljer set skjermingType = :skjermingTypeCode where dokument_info_id = :dokumentInfoId and variantFormat=:variantFormat")
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("variantFormat", variantFormatCode)
				.setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}

	public void setJpDokInfoRelSkjerming(Long journalpostDokumentInfoRelasjonId, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update JournalpostDokumentInfoRelasjon set skjermingType = :skjermingTypeCode where journalpostDokumentInfoRelasjonId = :relId")
				.setParameter("relId", journalpostDokumentInfoRelasjonId)
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

	private JournalpostDokumentInfoRelasjon hentJpDokInfoRel(Long journalpostId, Long dokumentInfoId) {
		return journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpostId, dokumentInfoId)
				.orElseThrow(() -> new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.format(
						"Kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s", journalpostId, dokumentInfoId)));
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
