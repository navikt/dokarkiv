package no.nav.dokarkiv.core.domain.service;

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
import java.util.Objects;
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

	public boolean isJournalpostSkjermet(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost != null) {
			return skjermingTypeCode.equals(journalpost.getSkjermingType());
		}
		return false;
	}

	public boolean isJournalpostSkjermet(Long journalpostId) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost != null) {
			return Objects.nonNull(journalpost.getSkjermingType());
		}

		return false;
	}

	public boolean isJournalpostSkjermet(Journalpost journalpost) {
		return Objects.nonNull(journalpost.getSkjermingType());
	}

	public boolean isJournalpostDokumentInfoRelasjonSkjermet(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		Optional<JournalpostDokumentInfoRelasjon> rel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpostId, dokumentInfoId);
		return rel.filter(journalpostDokumentInfoRelasjon -> skjermingTypeCode.equals(journalpostDokumentInfoRelasjon.getSkjermingType()))
				.isPresent();
	}

	public boolean isAlleFildetaljerSkjermet(Long dokumentInfoId) {
		Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId);
		return dokumentInfo.map(dokumentInfo1 -> dokumentInfo1.getFildetaljerListeAdmin()
				.stream()
				.allMatch(filDetaljer -> Objects.nonNull(filDetaljer.getSkjermingType())))
				.orElse(false);
	}

	public boolean isVariantSkjermet(Long dokumentInfoId, VariantFormatCode variant) {
		Optional<DokumentInfo> dokumentInfo = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoId);
		if (dokumentInfo.isPresent()) {
			FilDetaljer filDetaljer = dokumentInfo.get().findFilDetaljerByVariantFormatAdmin(variant);
			if (Objects.nonNull(filDetaljer) && Objects.nonNull(filDetaljer.getSkjermingType())) {
				return true;
			}
		}
		return false;
	}

	public void skjermAllFildetaljer(DokumentInfo dokumentInfo, SkjermingTypeCode skjermingTypeCode) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListeAdmin()) {
			setFildetaljerSkjerming(filDetaljer, skjermingTypeCode);
		}
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
		setJournalpostSkjerming(journalpost, skjermingTypeCode);
	}

	public void opphevSkjermJournalpostByJournalpostId(Long journalpostId) {
		Journalpost journalpost = hentJournalpost(journalpostId);
		setJournalpostSkjerming(journalpost, null);
	}

	public void skjermJpDokInfoRelByJournalpostIdAndDokumentInfoIdAndSkjermingType(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		JournalpostDokumentInfoRelasjon rel = hentJpDokInfoRel(journalpostId, dokumentInfoId);
		setJpDokInfoRelSkjerming(rel, skjermingTypeCode);
	}

	public void opphevSkjermJpDokInfoRelByJournalpostIdAndDokumentInfoId(Long journalpostId, Long dokumentInfoId) {
		JournalpostDokumentInfoRelasjon rel = hentJpDokInfoRel(journalpostId, dokumentInfoId);
		setJpDokInfoRelSkjerming(rel, null);
	}

	private Journalpost hentJournalpost(Long journalpostId) {
		return joarkRepository.findById(journalpostId).orElseThrow(() ->
				new JournalpostIkkeFunnetException("Kan ikke finne journalpost med journalpostId=" + journalpostId));
	}

	public void setVariantSkjermet(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, SkjermingTypeCode skjermingTypeCode) {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormatAdmin(variantFormatCode);
		setFildetaljerSkjerming(filDetaljer, skjermingTypeCode);
	}

	public void setFildetaljerSkjerming(FilDetaljer filDetaljer, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update FilDetaljer set skjermingType = :skjermingTypeCode where fildetaljerId = :filDetaljerId").setParameter("filDetaljerId", filDetaljer.getFildetaljerId()).setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}

	public void setJpDokInfoRelSkjerming(JournalpostDokumentInfoRelasjon rel, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update JournalpostDokumentInfoRelasjon set skjermingType = :skjermingTypeCode where journalpostDokumentInfoRelasjonId = :relId").setParameter("relId", rel.getJournalpostDokumentInfoRelasjonId()).setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}

	public void setJournalpostSkjerming(Journalpost journalpost, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update Journalpost set skjermingType = :SkjermingTypeCode where journalpostId = :journalpostId").setParameter("journalpostId", journalpost.getJournalpostId()).setParameter("SkjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
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

	public static Long convertBigToLong(Object value) {
		if (value instanceof BigDecimal) {
			return ((BigDecimal) value).longValue();
		} else if (value instanceof BigInteger) {
			return ((BigInteger) value).longValue();
		}
		return (Long) value;
	}

}
