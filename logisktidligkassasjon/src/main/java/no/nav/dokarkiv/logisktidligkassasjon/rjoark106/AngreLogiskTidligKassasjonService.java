package no.nav.dokarkiv.logisktidligkassasjon.rjoark106;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.logisktidligkassasjon.rjoark105.LogiskTidligKassasjonResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AngreLogiskTidligKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningRepository begrensningRepository;

	@Inject
	public AngreLogiskTidligKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			BegrensningRepository begrensningRepository) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.begrensningRepository = begrensningRepository;
	}

	public LogiskTidligKassasjonResponse angreLogiskTidligKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoDerKasseringSkalAngres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(
						String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
								dokumentInfoId)));

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);
		begrensningRepository.deleteByDokumentInfoIdAndBegrensningType(dokumentInfoId, SkjermingTypeCode.POL);

		return LogiskTidligKassasjonResponse.builder()
				.dokumentInfoId(dokumentInfoDerKasseringSkalAngres.getDokumentInfoId())
				.tittel(dokumentInfoDerKasseringSkalAngres.getTittel())
				.build();
	}

	private void sjekkAtDokumentErLogiskKassert(Long dokumentInfoId) {
		if (isFalse(begrensningRepository.findByDokumentInfoIdAndBegrensningType(dokumentInfoId, SkjermingTypeCode.POL)
				.isPresent())) {
			throw new SkjermingIkkeFunnetException(
					String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
							dokumentInfoId,
							SkjermingTypeCode.POL));
		}
	}
}
