package no.nav.dokarkiv.core.domain.service;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
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

	private final BegrensningRepository begrensningRepository;
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private JoarkRepository joarkRepository;

	@Inject
	private EntityManager entityManager;

	@Inject
	public BegrensningService(BegrensningRepository begrensningRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, JoarkRepository joarkRepository) {
		this.begrensningRepository = begrensningRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.joarkRepository = joarkRepository;
	}

	public boolean isJournalpostBegrenset(Long journalpostId, BegrensningTypeCode begrensningTypeCode) {
		Optional<Begrensning> begrensning = begrensningRepository.findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(
				journalpostId, begrensningTypeCode);
		return begrensning.isPresent();
	}

	public boolean isJournalpostDokumentInfoRelasjonOrJournalpostBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		return isJournalpostDokumentInfoRelasjonBegrenset(journalpostId, dokumentInfoId, begrensningTypeCode) || isJournalpostBegrenset(journalpostId, begrensningTypeCode);
	}

	public boolean isJournalpostDokumentInfoRelasjonBegrenset(Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		Optional<Begrensning> begrensning = begrensningRepository.findByJournalpostIdAndDokumentInfoIdAndBegrensningType(
				journalpostId, dokumentInfoId, begrensningTypeCode);
		return begrensning.isPresent();
	}

	public boolean isDokumentKassert(Long dokumentInfoId) {
		return begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfoId, BegrensningTypeCode.KASSERT)
				.isPresent();
	}

	public boolean isVariantSkjermet(Long dokumentInfoId, VariantFormatCode variant) {
		Optional<Begrensning> variantSkjermet = begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(dokumentInfoId, variant, BegrensningTypeCode.SKJERMET);
		String consumer = hentBrukerSomKaller();

		//TODO Midlertidig løsning i påvente av SAF
		if ("srvjoarkadmin".equalsIgnoreCase(consumer)) {
			//Har rettighet til å se originalen uansett
			return false;
		} else {
			//Dersom den er skjermet, returneres TRUE
			return variantSkjermet.isPresent();
		}
	}

	public void saveBegrensning(Begrensning begrensning) {
		begrensningRepository.save(begrensning);
		setJoarkBegrensning(begrensning);
	}

	public void deleteValidertJournalpostBegrensning(
			Long journalpostId,
			BegrensningTypeCode begrensningTypeCode) {
		begrensningRepository.deleteByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(journalpostId, begrensningTypeCode);
	}

	public void deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
			Long journalpostId, Long dokumentInfoId, BegrensningTypeCode begrensningTypeCode) {
		begrensningRepository.deleteByJournalpostIdAndDokumentInfoIdAndBegrensningType(journalpostId, dokumentInfoId, begrensningTypeCode);
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
				List<Long> begrensetDokumentInfoIdList = journalpostDokumentInfoRelasjonRepository.findBegrensetRelasjonDokumentInfoIdByJournalpostId(journalpost
						.getJournalpostId()).stream().map(BegrensningService::convertBigToLong).collect(Collectors.toList());
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

	private void setJoarkBegrensning(Begrensning begrensning) {
		Journalpost journalpost = joarkRepository.findById(begrensning.getJournalpostId()).orElse(null);
		if (journalpost != null && begrensning.getDokumentInfoId() == null && isFalse(journalpost.getBegrensning())) {
			setJournalpostBegrensning(journalpost, true);
		}
		//TODO Kassasjon - har bare kobling til dokumentinfo, må vurdere å begrense alle varianter
		else if (journalpost != null && begrensning.getDokumentInfoId() != null) {
			JournalpostDokumentInfoRelasjon rel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), begrensning.getDokumentInfoId()).orElse(null);
			if (begrensning.getVariantFormat() == null) {
				if (isFalse(rel.getBegrensning())) {
					setJpDokInfoRelBegrensning(rel,true);
				}
			} else {
				FilDetaljer filDetaljer = rel.getDokumentInfo().findFilDetaljerByVariantFormat(begrensning.getVariantFormat());
				if (filDetaljer != null && isFalse(filDetaljer.getBegrensning())) {
					setFildetaljerBegrensning(filDetaljer,true);
				}
			}
		} else if (journalpost == null && begrensning.getDokumentInfoId() != null) {
			DokumentInfo dokumentInfo = null;
			List<JournalpostDokumentInfoRelasjon> rel = journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(begrensning.getDokumentInfoId());
			if (!rel.isEmpty()) {
				dokumentInfo = rel.get(0).getDokumentInfo();
			}
			if (dokumentInfo != null) {
				for (FilDetaljer filDetaljer: dokumentInfo.getFildetaljerListe()) {
					if (isFalse(filDetaljer.getBegrensning())) {
						setFildetaljerBegrensning(filDetaljer,true);
					}
				}
			}
		}
	}

	private void setJournalpostBegrensning(Journalpost journalpost, Boolean begrensning) {
		String begrensningString = begrensning?"T":"F";
		Query q = entityManager.createQuery("update t_journalpost set begrensning = :begrenset where journalpost_id = :journalpostId").setParameter("journalpostId", journalpost.getJournalpostId()).setParameter("begrenset", begrensningString);
		q.executeUpdate();
	}

	private void setJpDokInfoRelBegrensning(JournalpostDokumentInfoRelasjon rel, Boolean begrensning) {
		String begrensningString = begrensning?"T":"F";
		Query q = entityManager.createQuery("update t_jp_dok_info_rel set begrensning = :begrenset where jp_dok_info_rel_id = :relId").setParameter("relId", rel.getJournalpostDokumentInfoRelasjonId()).setParameter("begrenset", begrensningString);
		q.executeUpdate();
	}

	private void setFildetaljerBegrensning(FilDetaljer filDetaljer, Boolean begrensning) {
		String begrensningString = begrensning?"T":"F";
		Query q = entityManager.createQuery("update t_fil_detaljer set begrensning = :begrenset where fil_detaljer_id = :filDetaljerId").setParameter("filDetaljerId", filDetaljer.getFildetaljerId()).setParameter("begrenset", begrensningString);
		q.executeUpdate();
	}
}
