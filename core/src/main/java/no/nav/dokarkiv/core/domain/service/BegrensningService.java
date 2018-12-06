package no.nav.dokarkiv.core.domain.service;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.modig.core.context.SubjectHandler;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
@Component
public class BegrensningService {

	private final BegrensningRepository begrensningRepository;

	@Inject
	public BegrensningService(BegrensningRepository begrensningRepository) {
		this.begrensningRepository = begrensningRepository;
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

	public boolean isVariantSkjermet(Long dokumentInfoId, VariantFormatCode variant) {
		Optional<Begrensning> variantSkjermet = begrensningRepository.findByDokumentInfoIdAndVariantFormatAndBegrensningType(dokumentInfoId, variant, BegrensningTypeCode.SKJERMET);
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

}
