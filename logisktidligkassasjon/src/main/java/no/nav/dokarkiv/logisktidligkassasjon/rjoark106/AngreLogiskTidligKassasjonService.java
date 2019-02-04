package no.nav.dokarkiv.logisktidligkassasjon.rjoark106;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.logisktidligkassasjon.rjoark105.LogiskTidligKassasjonResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AngreLogiskTidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final SkjermingService skjermingService;

	@Inject
	public AngreLogiskTidligKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			SkjermingService skjermingService) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.skjermingService = skjermingService;
	}

	public LogiskTidligKassasjonResponse angreLogiskTidligKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoDerKasseringSkalAngres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(
						String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
								dokumentInfoId)));

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);
		skjermingService.setDokumentKassert(dokumentInfoDerKasseringSkalAngres, null);

		return LogiskTidligKassasjonResponse.builder()
				.dokumentInfoId(dokumentInfoDerKasseringSkalAngres.getDokumentInfoId())
				.tittel(dokumentInfoDerKasseringSkalAngres.getTittel())
				.build();
	}

	private void sjekkAtDokumentErLogiskKassert(Long dokumentInfoId) {
		if (!skjermingService.isDokumentInfoIdKassert(dokumentInfoId)) {
			throw new SkjermingIkkeFunnetException(
					String.format("Fant ikke forventet skjerming for dokument med dokumentInfoId=%s og skjermingType=%s",
							dokumentInfoId,
							SkjermingTypeCode.POL));
		}
	}
}
