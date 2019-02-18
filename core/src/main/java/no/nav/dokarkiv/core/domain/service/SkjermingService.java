package no.nav.dokarkiv.core.domain.service;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
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

	@Inject
	private EntityManager entityManager;

	@Inject
	public SkjermingService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository, DokumentinfoRepository dokumentinfoRepository) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
		this.dokumentinfoRepository = dokumentinfoRepository;
	}

	public boolean isJournalpostSkjermet(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost != null) {
			return skjermingTypeCode.equals(journalpost.getSkjermingType());
		}
		return false;
	}

	public boolean isJournalpostDokumentInfoRelasjonOrJournalpostBegrenset(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		return isJournalpostDokumentInfoRelasjonSkjermet(journalpostId, dokumentInfoId, skjermingTypeCode) || isJournalpostSkjermet(journalpostId, skjermingTypeCode);
	}

	public boolean isJournalpostDokumentInfoRelasjonSkjermet(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		Optional<JournalpostDokumentInfoRelasjon> rel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpostId, dokumentInfoId);
		if (rel.isPresent()) {
			return skjermingTypeCode.equals(rel.get().getSkjermingType());
		}
		return false;
	}

	public boolean isDokumentInfoIdKassert(Long dokumentInfoId) {
		Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId);
		return dokumentInfo.map(dokumentInfo1 -> dokumentInfo1.getFildetaljerListe()
				.stream()
				.allMatch(filDetaljer -> isFalse(filDetaljer.getSkjermingType() == null)))
				.orElse(false);
	}

	public boolean isVariantSkjermet(Long dokumentInfoId, VariantFormatCode variant) {
		Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId);
		if (dokumentInfo.isPresent()) {
			FilDetaljer filDetaljer = dokumentInfo.get().findFilDetaljerByVariantFormat(variant);
			if (filDetaljer != null && filDetaljer.getSkjermingType() != null) {
				return true;
			}
		}
		return false;
	}

	public void setVariantSkjermet(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, SkjermingTypeCode SkjermingTypeCode) {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(variantFormatCode);
		setFildetaljerSkjerming(filDetaljer, SkjermingTypeCode);
	}

	public void setJournalpostBegrensning(Journalpost journalpost, SkjermingTypeCode SkjermingTypeCode) {
		Query q = entityManager.createQuery("update Journalpost set skjermingType = :begrenset where journalpostId = :journalpostId").setParameter("journalpostId", journalpost.getJournalpostId()).setParameter("begrenset", SkjermingTypeCode);
		q.executeUpdate();
	}

	private Journalpost hentJournalpost(Long journalpostId) {
		return joarkRepository.findById(journalpostId).orElseThrow(() ->
				new JournalpostIkkeFunnetException("Kan ikke finne journalpost med journalpostId=" + journalpostId));
	}

	private DokumentInfo hentDokumentInfo(Long dokumentInfoId) {
		return dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId).orElseThrow(() ->
				new DokumentInfoIkkeFunnetException(String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s", dokumentInfoId)));
	}

	private JournalpostDokumentInfoRelasjon hentJpDokInfoRel(Long journalpostId, Long dokumentInfoId) {
		return journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpostId, dokumentInfoId)
				.orElseThrow(() -> new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.format(
						"Kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s", journalpostId, dokumentInfoId)));
	}

	public void skjermVariantByDokumentInfoIdAndVariantFormatAndSkjermingType(Long dokumentInfoId, VariantFormatCode variantFormat, SkjermingTypeCode skjermingType) {
		DokumentInfo dokumentInfo = hentDokumentInfo(dokumentInfoId);
		setVariantSkjermet(dokumentInfo, variantFormat, skjermingType);
	}

	public void opphevSkjermVariantByDokumentInfoIdAndVariantFormat(Long dokumentInfoId, VariantFormatCode variantFormat) {
		DokumentInfo dokumentInfo = hentDokumentInfo(dokumentInfoId);
		setVariantSkjermet(dokumentInfo, variantFormat, null);
	}

	public void skjermJournalpostByJournalpostIdAndSkjermingType(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		Journalpost journalpost = hentJournalpost(journalpostId);
		setJournalpostBegrensning(journalpost, skjermingTypeCode);
	}

	public void opphevSkjermJournalpostByJournalpostId(Long journalpostId) {
		Journalpost journalpost = hentJournalpost(journalpostId);
		setJournalpostBegrensning(journalpost, null);
	}

	public void skjermJpDokInfoRelByJournalpostIdAndDokumentInfoIdAndSkjermingType(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		JournalpostDokumentInfoRelasjon rel = hentJpDokInfoRel(journalpostId, dokumentInfoId);
		setJpDokInfoRelBegrensning(rel, skjermingTypeCode);
	}

	public void opphevSkjermJpDokInfoRelByJournalpostIdAndDokumentInfoId(Long journalpostId, Long dokumentInfoId) {
		JournalpostDokumentInfoRelasjon rel = hentJpDokInfoRel(journalpostId, dokumentInfoId);
		setJpDokInfoRelBegrensning(rel, null);
	}

	public void setDokumentKassert(DokumentInfo dokumentInfo, SkjermingTypeCode SkjermingTypeCode) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			setFildetaljerSkjerming(filDetaljer, SkjermingTypeCode);
		}
	}

	public void setFildetaljerSkjerming(FilDetaljer filDetaljer, SkjermingTypeCode SkjermingTypeCode) {
		Query q = entityManager.createQuery("update FilDetaljer set skjermingType = :skjermingTypeCode where fildetaljerId = :filDetaljerId").setParameter("filDetaljerId", filDetaljer.getFildetaljerId()).setParameter("skjermingTypeCode", SkjermingTypeCode);
		q.executeUpdate();
	}

	public void setJpDokInfoRelBegrensning(JournalpostDokumentInfoRelasjon rel, SkjermingTypeCode SkjermingTypeCode) {
		Query q = entityManager.createQuery("update JournalpostDokumentInfoRelasjon set skjermingType = :skjermingTypeCode where journalpostDokumentInfoRelasjonId = :relId").setParameter("relId", rel.getJournalpostDokumentInfoRelasjonId()).setParameter("skjermingTypeCode", SkjermingTypeCode);
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
