package no.nav.dokarkiv.logiskkassasjon.rjoark106;

import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.logiskkassasjon.rjoark105.LogiskKassasjonResponse;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service
public class AngreLogiskKassasjonService {

	private final DokumentinfoRepository dokumentInfoRepository;
	private final BegrensningRepository begrensningRepository;

	@Inject
	public AngreLogiskKassasjonService(
			DokumentinfoRepository dokumentInfoRepository,
			BegrensningRepository begrensningRepository) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.begrensningRepository = begrensningRepository;
	}

	public LogiskKassasjonResponse angreLogiskKassasjonAvDokument(Long dokumentInfoId) {
		DokumentInfo dokumentInfoDerKasseringSkalAngres = dokumentInfoRepository.findByDokumentInfoId(dokumentInfoId)
				.orElse(null);

		if (dokumentInfoDerKasseringSkalAngres == null) {
			throw new DokumentInfoIkkeFunnetException(
					String.format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
							dokumentInfoId));
		}

		sjekkAtDokumentErLogiskKassert(dokumentInfoId);
		//TODO: Endre til begrensningsType for kassasjon
		begrensningRepository.deleteByDokumentInfoIdAndBegrensningType(dokumentInfoId, BegrensningTypeCode.UTILGJENGELIGGJORT);

		return LogiskKassasjonResponse.builder()
				.journalpostId(dokumentInfoDerKasseringSkalAngres.getOriginalJournalpost()
						== null ? null : dokumentInfoDerKasseringSkalAngres.getOriginalJournalpost().getJournalpostId())
				.dokumentInfoId(dokumentInfoDerKasseringSkalAngres.getDokumentInfoId())
				.tittel(dokumentInfoDerKasseringSkalAngres.getTittel())
				.build();
	}

	private void sjekkAtDokumentErLogiskKassert(Long dokumentInfoId) {
		if (isFalse(begrensningRepository)) {
			throw new BegrensningIkkeFunnetException(
					String.format("Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
							dokumentInfoId,
							//TODO: Endre til begrensningsType for kassasjon
							BegrensningTypeCode.UTILGJENGELIGGJORT));
		}
	}
}
