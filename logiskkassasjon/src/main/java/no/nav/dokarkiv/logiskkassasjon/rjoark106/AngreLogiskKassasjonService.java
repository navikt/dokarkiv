package no.nav.dokarkiv.logiskkassasjon.rjoark106;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.logiskkassasjon.rjoark105.LogiskKassasjonResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AngreLogiskKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningService begrensningService;

	@Inject
	public AngreLogiskKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			BegrensningService begrensningService) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.begrensningService = begrensningService;
	}

	public LogiskKassasjonResponse angreLogiskKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoDerKasseringSkalAngres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(
						String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
								dokumentInfoId)));

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);
		begrensningService.setDokumentKassert(dokumentInfoDerKasseringSkalAngres, null);

		return LogiskKassasjonResponse.builder()
				.dokumentInfoId(dokumentInfoDerKasseringSkalAngres.getDokumentInfoId())
				.tittel(dokumentInfoDerKasseringSkalAngres.getTittel())
				.build();
	}

	private void sjekkAtDokumentErLogiskKassert(Long dokumentInfoId) {
		if (!begrensningService.isDokumentInfoIdKassert(dokumentInfoId)) {
			throw new BegrensningIkkeFunnetException(
					String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
							dokumentInfoId,
							BegrensningTypeCode.POL));
		}
	}
}
