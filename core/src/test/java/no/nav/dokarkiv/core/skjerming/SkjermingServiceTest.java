package no.nav.dokarkiv.core.skjerming;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Optional;

import static java.util.Objects.nonNull;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Transactional
public class SkjermingServiceTest {

	private final SkjermingService skjermingService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	private final DokumentInfoRepository dokumentInfoRepository;
	private final EntityManager entityManager;

	public SkjermingServiceTest(SkjermingService skjermingService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, DokumentInfoRepository dokumentInfoRepository, EntityManager entityManager) {
		this.skjermingService = skjermingService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.entityManager = entityManager;
	}

	public void setJournalpostSkjerming(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		skjermingService.setJournalpostSkjerming(journalpostId, skjermingTypeCode);
	}


	/**
	 * Brukes bare i test
	 */
	public void skjermAllFildetaljer(DokumentInfo dokumentInfo, SkjermingTypeCode skjermingTypeCode) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListeAdmin()) {
			skjermingService.setFildetaljerSkjerming(dokumentInfo.getDokumentInfoId(), filDetaljer.getVariantFormat(), skjermingTypeCode);
		}
	}

	public void setDokumentKassert(DokumentInfo dokumentInfo, SkjermingTypeCode skjermingTypeCode) {
		skjermAllFildetaljer(dokumentInfo, skjermingTypeCode);
		Query q = entityManager.createQuery("update DokumentInfo set kassert=true where dokument_info_id = :dokumentInfoId")
				.setParameter("dokumentInfoId", dokumentInfo.getDokumentInfoId());
		q.executeUpdate();
	}


	public void setVariantSkjermet(Long dokumentInfoId, VariantFormatCode variantFormatCode, SkjermingTypeCode skjermingTypeCode) {
		skjermingService.setFildetaljerSkjerming(dokumentInfoId, variantFormatCode, skjermingTypeCode);
	}


	public void skjermJournalpost(Long journalpostId, SkjermingTypeCode skjermingTypeCode) {
		skjermingService.setJournalpostSkjerming(journalpostId, skjermingTypeCode);
	}


	public void skjermJournalpostDokumentInfoRelasjon(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {
		JournalpostDokumentInfoRelasjon rel = hentJpDokInfoRel(journalpostId, dokumentInfoId);
		setJpDokInfoRelSkjerming(rel.getJournalpostDokumentInfoRelasjonId(), skjermingTypeCode);
	}

	public void setJpDokInfoRelSkjerming(Long journalpostDokumentInfoRelasjonId, SkjermingTypeCode skjermingTypeCode) {
		Query q = entityManager.createQuery("update JournalpostDokumentInfoRelasjon set skjermingType = :skjermingTypeCode where journalpostDokumentInfoRelasjonId = :relId")
				.setParameter("relId", journalpostDokumentInfoRelasjonId)
				.setParameter("skjermingTypeCode", skjermingTypeCode);
		q.executeUpdate();
	}


	public boolean isVariantSkjermet(Long dokumentInfoId, VariantFormatCode variant, SkjermingTypeCode skjermingTypeCode) {
		Optional<DokumentInfo> dokumentInfo = dokumentInfoRepository.findById(dokumentInfoId);
		if (dokumentInfo.isPresent()) {
			FilDetaljer filDetaljer = dokumentInfo.get().findFilDetaljerByVariantFormatAdmin(variant);
			if (nonNull(filDetaljer) && skjermingTypeCode.equals(filDetaljer.getSkjermingType())) {
				return true;
			}
		}
		return false;
	}

	private JournalpostDokumentInfoRelasjon hentJpDokInfoRel(Long journalpostId, Long dokumentInfoId) {
		return journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpostId, dokumentInfoId)
				.orElseThrow(() -> new JournalpostDokumentInfoRelasjonIkkeFunnetException(String.format(
						"Kan ikke finne journalpostDokumentInfoRelasjon med journalpostId=%s og dokumentInfoId=%s", journalpostId, dokumentInfoId)));
	}


}
