package no.nav.dokarkiv.rjoark102;

import no.nav.dokarkiv.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class KasserDokumentOrchestrator {

	private final KasserDokumentService kasserDokumentService;
	private final KasserSkjermDokumentService kasserSkjermDokumentService;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;


	@Inject
	public KasserDokumentOrchestrator(KasserDokumentService kasserDokumentService, KasserSkjermDokumentService kasserSkjermDokumentService, LagreAksjonsLoggService lagreAksjonsLoggService) {
		this.kasserDokumentService = kasserDokumentService;
		this.kasserSkjermDokumentService = kasserSkjermDokumentService;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
	}

	public void opphevKasserSkjermDokument(Long dokumentInfoId, String hjemmel, String bruker, String melding, String utfoertAv) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserSkjermDokumentService.opphevSkjermDokument(dokumentInfoId));
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, dokumentInfoId, hjemmel, bruker, melding, utfoertAv, arkivElementEndringTOList);
	}

	public void kasserSkjermDokument(Long dokumentInfoId, String hjemmel, String bruker, String melding, String utfoertAv) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserSkjermDokumentService.skjermDokument(dokumentInfoId));
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, dokumentInfoId, hjemmel, bruker, melding, utfoertAv, arkivElementEndringTOList);
	}


	public void kasserDokument(Long dokumentInfoId, String kassertAvNavn, String hjemmel, String bruker, String melding, String utfoertAv) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserDokumentService.kasserDokument(dokumentInfoId, kassertAvNavn));
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.KASSASJON, dokumentInfoId, hjemmel, bruker, melding, utfoertAv, arkivElementEndringTOList);
	}

}
