package no.nav.dokarkiv.core.domain.service;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.modig.core.context.SubjectHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
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
public class SkjermingService {

	private final BegrensningRepository begrensningRepository;
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	public SkjermingService(BegrensningRepository begrensningRepository, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.begrensningRepository = begrensningRepository;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	public boolean isJournalpostBegrenset(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		Optional<Begrensning> begrensning = begrensningRepository.findByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(
				journalpostId, skjermingTypeCode);
		return begrensning.isPresent();
	}

	public boolean isJournalpostDokumentInfoRelasjonOrJournalpostBegrenset(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		return isJournalpostDokumentInfoRelasjonBegrenset(journalpostId, dokumentInfoId, skjermingTypeCode) || isJournalpostBegrenset(journalpostId, skjermingTypeCode);
	}

	public boolean isJournalpostDokumentInfoRelasjonBegrenset(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		Optional<Begrensning> begrensning = begrensningRepository.findByJournalpostIdAndDokumentInfoIdAndBegrensningType(
				journalpostId, dokumentInfoId, skjermingTypeCode);
		return begrensning.isPresent();
	}

	public boolean isDokumentKassert(Long dokumentInfoId) {
		return begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfoId, SkjermingTypeCode.POL)
				.isPresent();
	}

	public boolean isVariantSkjermet(Long dokumentInfoId, VariantFormatCode variant) {
		Optional<Begrensning> variantSkjermet = begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(dokumentInfoId, variant, SkjermingTypeCode.POL);
		String consumer = hentBrukerSomKaller();

		//Midlertidig løsning i påvente av SAF
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
	}

	public void deleteValidertJournalpostBegrensning(
			Long journalpostId,
			SkjermingTypeCode skjermingTypeCode) {
		begrensningRepository.deleteByJournalpostIdAndBegrensningTypeAndDokumentInfoIdIsNull(journalpostId, skjermingTypeCode);
	}

	public void deleteValidertJournalpostDokumentInfoRelasjonBegrensning(
			Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		begrensningRepository.deleteByJournalpostIdAndDokumentInfoIdAndBegrensningType(journalpostId, dokumentInfoId, skjermingTypeCode);
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
				.getJournalpostId()).stream().map(SkjermingService::convertBigToLong).collect(Collectors.toList());
		journalpost.addAllbegrensetRelasjonerDokumentInfoIds(begrensetDokumentInfoIdList);
		return journalpost;
	}


	public List<Journalpost> addBegrensetDokumentInfoIdsToJournalpostList(List<Journalpost> journalpostList) {
		if (BooleanUtils.isFalse((journalpostList == null || journalpostList.isEmpty()))) {
			for (Journalpost journalpost : journalpostList) {
				List<Long> begrensetDokumentInfoIdList = journalpostDokumentInfoRelasjonRepository.findBegrensetRelasjonDokumentInfoIdByJournalpostId(journalpost
						.getJournalpostId()).stream().map(SkjermingService::convertBigToLong).collect(Collectors.toList());
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
}
