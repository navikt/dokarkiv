package no.nav.dokarkiv.logiskkassasjon.rjoark106;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.logisktidligkassasjon.rjoark105.LogiskTidligKassasjonResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AngreLogiskTidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningService begrensningService;

	@Inject
	public AngreLogiskTidligKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			BegrensningService begrensningService) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.begrensningService = begrensningService;
	}

	public LogiskTidligKassasjonResponse angreLogiskTidligKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoDerKasseringSkalAngres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(
						String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
								dokumentInfoId)));

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);
		begrensningService.setDokumentKassert(dokumentInfoDerKasseringSkalAngres, null);

		return LogiskTidligKassasjonResponse.builder()
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
