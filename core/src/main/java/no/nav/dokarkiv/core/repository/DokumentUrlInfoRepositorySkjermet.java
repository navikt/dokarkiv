package no.nav.dokarkiv.core.repository;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class DokumentUrlInfoRepositorySkjermet {

	private final DokumentUrlInfoRepository dokumentUrlInfoRepository;
	private final SkjermingService skjermingService;

	public DokumentUrlInfoRepositorySkjermet(DokumentUrlInfoRepository dokumentUrlInfoRepository, SkjermingService skjermingService) {
		this.dokumentUrlInfoRepository = dokumentUrlInfoRepository;
		this.skjermingService = skjermingService;
	}

	public DokumentUrlInfo save(DokumentUrlInfo dokumentUrlInfo) {
		return dokumentUrlInfoRepository.save(dokumentUrlInfo);
	}

	public DokumentUrlInfo findByFilUuid(String filUuid) {
		DokumentUrlInfo dokumentUrlInfo = dokumentUrlInfoRepository.findByFilUuid(filUuid);
		return Objects.nonNull(dokumentUrlInfo) && isFalse(skjermingService.isJournalpostSkjermet(dokumentUrlInfo.getJournalpost()
				.getJournalpostId())) ? dokumentUrlInfo : null;
	}

	public Optional<DokumentUrlInfo> findByDoctoken(String doctoken) {
		Optional<DokumentUrlInfo> dokumentUrlInfo = dokumentUrlInfoRepository.findByDoctoken(doctoken);
		return dokumentUrlInfo.isPresent() && isFalse(skjermingService.isJournalpostSkjermet(dokumentUrlInfo.get()
				.getJournalpost()
				.getJournalpostId())) ? dokumentUrlInfo : Optional.empty();
	}


}
