package no.nav.dokarkiv.rjoark102;

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
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;


	@Inject
	public KasserDokumentOrchestrator(KasserDokumentService kasserDokumentService, KasserSkjermDokumentService kasserSkjermDokumentService, AksjonsLoggService aksjonsLoggService) {
		this.kasserDokumentService = kasserDokumentService;
		this.kasserSkjermDokumentService = kasserSkjermDokumentService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	public void opphevKasserSkjermDokument(Long dokumentInfoId, String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserSkjermDokumentService.opphevSkjermDokument(dokumentInfoId));
		lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, dokumentInfoId, aksjonsLoggHeaderString, arkivElementEndringTOList);
	}

	public void kasserSkjermDokument(Long dokumentInfoId, String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserSkjermDokumentService.skjermDokument(dokumentInfoId));
		lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, dokumentInfoId, aksjonsLoggHeaderString, arkivElementEndringTOList);
	}


	public void kasserDokument(Long dokumentInfoId, String kassertAvNavn, String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserDokumentService.kasserDokument(dokumentInfoId, kassertAvNavn));
		lagreAksjonsLogg(AksjonsTypeCode.KASSASJON, dokumentInfoId, aksjonsLoggHeaderString, arkivElementEndringTOList);
	}

	private void lagreAksjonsLogg(AksjonsTypeCode aksjonsTypeCode, Long dokumentInfoId, String aksjonsLoggHeaderString, List<ArkivElementEndringTO> arkivElementEndringTOList) throws
			UgyldigAksjonsLoggException {

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, aksjonsTypeCode, null, dokumentInfoId);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
	}

}
