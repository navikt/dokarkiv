package no.nav.dokarkiv.core.domain.service;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.modig.core.context.SubjectHandler;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
@Component
public class SkjermingService {

	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private JoarkRepository joarkRepository;

	@Inject
	private EntityManager entityManager;

	@Inject
	public SkjermingService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
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
		List<JournalpostDokumentInfoRelasjon> rel = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		if (!rel.isEmpty()) {
			return isDokumentInfoKassert(rel.get(0).getDokumentInfo());
		}
		return false;
	}

	public FilDetaljer getVariantSkjermet(DokumentInfo dokumentInfo, VariantFormatCode variant) {
		String consumer = hentBrukerSomKaller();

		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(variant);
		if (!"srvjoarkadmin".equals(consumer) && filDetaljer != null) {
			if (SkjermingTypeCode.POL.equals(filDetaljer.getSkjermingType())) {
				filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
				if (filDetaljer != null && SkjermingTypeCode.POL.equals(filDetaljer.getSkjermingType())) {
					filDetaljer = null;
				}
			}
		}
		return filDetaljer;
	}

	private String hentBrukerSomKaller() {
		String consumer;
		if (SubjectHandler.getSubjectHandler().getConsumerId() == null) {
			if (MDC.get(MDCConstants.MDC_CONSUMER_ID) != null) {
				consumer = MDC.get(MDCConstants.MDC_CONSUMER_ID).toString();
			} else if (MDC.get("user") != null) {
				consumer = MDC.get("user").toString();
			} else {
				consumer = MDC.get(MDCConstants.MDC_USER_ID).toString();
			}
		} else {
			consumer = SubjectHandler.getSubjectHandler().getConsumerId();
		}
		return consumer;
	}

	public static Long convertBigToLong(Object value) {
		if (value instanceof BigDecimal) {
			return ((BigDecimal) value).longValue();
		} else if (value instanceof BigInteger) {
			return ((BigInteger) value).longValue();
		}
		return (Long) value;
	}

	public Boolean isDokumentInfoKassert(DokumentInfo dokumentInfo) {
		return dokumentInfo.getFildetaljerListe().stream().allMatch(filDetaljer -> SkjermingTypeCode.POL.equals(filDetaljer.getSkjermingType()));
	}

	public void setVariantSkjermet(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, SkjermingTypeCode SkjermingTypeCode) {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(variantFormatCode);
		setFildetaljerBegrensning(filDetaljer, SkjermingTypeCode);
	}


	public void setJournalpostBegrensning(Journalpost journalpost, SkjermingTypeCode SkjermingTypeCode) {
		Query q = entityManager.createQuery("update Journalpost set skjermingType = :begrenset where journalpostId = :journalpostId").setParameter("journalpostId", journalpost.getJournalpostId()).setParameter("begrenset", SkjermingTypeCode);
		q.executeUpdate();
	}

	public void setJpDokInfoRelBegrensning(JournalpostDokumentInfoRelasjon rel, SkjermingTypeCode SkjermingTypeCode) {
		Query q = entityManager.createQuery("update JournalpostDokumentInfoRelasjon set skjermingType = :begrenset where journalpostDokumentInfoRelasjonId = :relId").setParameter("relId", rel.getJournalpostDokumentInfoRelasjonId()).setParameter("begrenset", SkjermingTypeCode);
		q.executeUpdate();
	}

	public void setDokumentKassert(DokumentInfo dokumentInfo, SkjermingTypeCode SkjermingTypeCode) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			setFildetaljerBegrensning(filDetaljer, SkjermingTypeCode);
		}
	}

	public void setFildetaljerBegrensning(FilDetaljer filDetaljer, SkjermingTypeCode SkjermingTypeCode) {
		Query q = entityManager.createQuery("update FilDetaljer set skjermingType = :begrenset where fildetaljerId = :filDetaljerId").setParameter("filDetaljerId", filDetaljer.getFildetaljerId()).setParameter("begrenset", SkjermingTypeCode);
		q.executeUpdate();
	}
}
