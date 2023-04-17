package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.LogiskVedleggIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import no.nav.dokarkiv.core.repository.SkannetInnholdRepository;
import no.nav.dokarkiv.journalpost.v1.api.BulkOppdaterLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.EndreLogiskVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.LeggTilLogiskVedleggRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

@Slf4j
@Service
public class LogiskVedleggService {
	private final SkannetInnholdRepository skannetInnholdRepository;
	private final DokumentInfoRepository dokumentInfoRepository;

	public LogiskVedleggService(final SkannetInnholdRepository skannetInnholdRepository, final DokumentInfoRepository dokumentInfoRepository) {
		this.skannetInnholdRepository = skannetInnholdRepository;
		this.dokumentInfoRepository = dokumentInfoRepository;
	}

	@Transactional
	public String leggTilLogiskVedlegg(String dokumentInfoId, LeggTilLogiskVedleggRequest request) {
		try {
			DokumentInfo dokumentInfo = dokumentInfoRepository.getReferenceById(parseLong(dokumentInfoId));

			SkannetInnhold skannetInnhold = SkannetInnhold.builder()
					.vedleggInnhold(request.getTittel())
					.dokumentInfo(dokumentInfo)
					.build();
			skannetInnhold.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
			skannetInnholdRepository.persist(skannetInnhold);
			return skannetInnhold.getSkannetInnholdId().toString();
		} catch (EntityNotFoundException e) {
			throw new DokumentInfoIkkeFunnetException(format("Kunne ikke finne dokumentInfo med dokumentInfoId=%s i joark", dokumentInfoId), e);
		}
	}

	@Transactional
	public void endreLogiskVedlegg(String logiskVedleggId, EndreLogiskVedleggRequest request) {
		SkannetInnhold skannetInnhold = skannetInnholdRepository.findById(parseLong(logiskVedleggId))
				.orElseThrow(() -> new LogiskVedleggIkkeFunnetException(format("Kunne ikke finne logisk vedlegg med logiskVedleggId=%s i joark", logiskVedleggId)));

		skannetInnhold.setVedleggInnhold(request.getTittel());
		skannetInnhold.setEndretKildeNavn(MDC.get(MDC_CONSUMER_ID));
	}

	@Transactional
	public void slettLogiskVedlegg(String logiskVedleggId) {
		skannetInnholdRepository.deleteBySkannetInnholdId(parseLong(logiskVedleggId));
	}

	@Transactional
	public void bulkOppdaterLogiskVedlegg(String dokumentInfoId, BulkOppdaterLogiskVedleggRequest request) {
		var titler = request.getTitler();
		if (titler.isEmpty()) {
			DokumentInfo dokumentInfo = findDokumentInfo(dokumentInfoId);
			dokumentInfo.clearSkannetInnhold();
		} else {
			DokumentInfo dokumentInfo = findDokumentInfo(dokumentInfoId);
			dokumentInfo.clearSkannetInnhold();
			titler.forEach(t -> {
				SkannetInnhold skannetInnhold = SkannetInnhold.builder()
						.vedleggInnhold(t)
						.build();
				skannetInnhold.setOpprettetKildeNavn(MDC.get(MDC_CONSUMER_ID));
				dokumentInfo.addSkannetInnhold(skannetInnhold);
			});
		}
	}

	private DokumentInfo findDokumentInfo(String dokumentInfoId) {
		Optional<DokumentInfo> byId = dokumentInfoRepository.findById(parseLong(dokumentInfoId));
		DokumentInfo dokumentInfo = byId.orElseThrow(() -> new DokumentInfoIkkeFunnetException("Kan ikke bulkOppdaterLogiskVedlegg. Finner ikke dokumentInfoId=" + dokumentInfoId));
		return dokumentInfo;
	}
}
