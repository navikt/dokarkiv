package no.nav.dokarkiv.core.domain.service;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.modig.core.context.SubjectHandler;
import org.apache.commons.lang3.BooleanUtils;
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
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
@Component
public class BegrensningService {

	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private JoarkRepository joarkRepository;

	@Inject
	private EntityManager entityManager;

	@Inject
	public BegrensningService(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository) {
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
	}

	public boolean isJournalpostBegrenset(Long journalpostId, BegrensningTypeCode begrensningTypeCode) {
		Journalpost journalpost = joarkRepository.findById(journalpostId).orElse(null);
		if (journalpost != null) {
			return begrensningTypeCode.equals(journalpost.getBegrensning());
		} else return false;
	}

	public boolean isJournalpostDokumentInfoRelasjonOrJournalpostBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		return isJournalpostDokumentInfoRelasjonBegrenset(journalpostId, dokumentInfoId, begrensningTypeCode) || isJournalpostBegrenset(journalpostId, begrensningTypeCode);
	}

	public boolean isJournalpostDokumentInfoRelasjonBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		Optional<JournalpostDokumentInfoRelasjon> rel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpostId, dokumentInfoId);
		if (rel.isPresent()) {
			return begrensningTypeCode.equals(rel.get().getBegrensning());
		} else
			return false;
	}

	public boolean isDokumentInfoIdKassert(Long dokumentInfoId) {
		List<JournalpostDokumentInfoRelasjon> rel = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
		if (rel.isEmpty()) {
			return false;
		} else {
			return isDokumentInfoKassert(rel.get(0).getDokumentInfo());
		}
	}

	public FilDetaljer getVariantSkjermet(DokumentInfo dokumentInfo, VariantFormatCode variant) {
		String consumer = hentBrukerSomKaller();

		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(variant);
		if (!consumer.equals("srvjoarkadmin") && filDetaljer != null) {
			if (BegrensningTypeCode.POL.equals(filDetaljer.getBegrensning())) {
				filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
				if (BegrensningTypeCode.POL.equals(filDetaljer.getBegrensning())) {
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

	public Journalpost addBegrensetDokumentInfoIdsToJournalpost(Journalpost journalpost) {
		List<Long> begrensetDokumentInfoIdList = journalpostDokumentInfoRelasjonRepository.findBegrensetRelasjonDokumentInfoIdByJournalpostId(journalpost
				.getJournalpostId()).stream().map(BegrensningService::convertBigToLong).collect(Collectors.toList());
		journalpost.addAllbegrensetRelasjonerDokumentInfoIds(begrensetDokumentInfoIdList);
		return journalpost;
	}

	public List<Journalpost> addBegrensetDokumentInfoIdsToJournalpostList(List<Journalpost> journalpostList) {
		if (BooleanUtils.isFalse((journalpostList == null || journalpostList.isEmpty()))) {
			for (Journalpost journalpost : journalpostList) {
				List<Long> begrensetDokumentInfoIdList = journalpost.getJournalpostDokumentInfoRelasjoner().stream()
						.filter(rel -> BegrensningTypeCode.POL.equals(rel.getBegrensning()))
						.map(rel -> rel.getDokumentInfo().getDokumentInfoId())
						.collect(Collectors.toList());
				journalpost.addAllbegrensetRelasjonerDokumentInfoIds(begrensetDokumentInfoIdList);
			}
		}
		return journalpostList;
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
		return dokumentInfo.getFildetaljerListe().stream().allMatch(filDetaljer -> BegrensningTypeCode.POL.equals(filDetaljer.getBegrensning()));
	}

	public void setVariantSkjermet(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, BegrensningTypeCode begrensningTypeCode) {
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(variantFormatCode);
		setFildetaljerBegrensning(filDetaljer, begrensningTypeCode);
	}


	public void setJournalpostBegrensning(Journalpost journalpost, BegrensningTypeCode begrensningTypeCode) {
		Query q = entityManager.createQuery("update Journalpost set begrensning = :begrenset where journalpostId = :journalpostId").setParameter("journalpostId", journalpost.getJournalpostId()).setParameter("begrenset", begrensningTypeCode);
		q.executeUpdate();
		entityManager.flush();
	}

	public void setJpDokInfoRelBegrensning(JournalpostDokumentInfoRelasjon rel, BegrensningTypeCode begrensningTypeCode) {
		Query q = entityManager.createQuery("update JournalpostDokumentInfoRelasjon set begrensning = :begrenset where journalpostDokumentInfoRelasjonId = :relId").setParameter("relId", rel.getJournalpostDokumentInfoRelasjonId()).setParameter("begrenset", begrensningTypeCode);
		q.executeUpdate();
		entityManager.flush();
	}

	public void setDokumentKassert(DokumentInfo dokumentInfo, BegrensningTypeCode begrensningTypeCode) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			setFildetaljerBegrensning(filDetaljer, begrensningTypeCode);
		}
	}


	public void setFildetaljerBegrensning(FilDetaljer filDetaljer, BegrensningTypeCode begrensningTypeCode) {
		Query q = entityManager.createQuery("update FilDetaljer set begrensning = :begrenset where fildetaljerId = :filDetaljerId").setParameter("filDetaljerId", filDetaljer.getFildetaljerId()).setParameter("begrenset", begrensningTypeCode);
		q.executeUpdate();
	}
}
