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
	private final KasserDokumentSkjermService kasserDokumentSkjermService;
	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;


	@Inject
	public KasserDokumentOrchestrator(KasserDokumentService kasserDokumentService, KasserDokumentSkjermService kasserDokumentSkjermService, AksjonsLoggService aksjonsLoggService) {
		this.kasserDokumentService = kasserDokumentService;
		this.kasserDokumentSkjermService = kasserDokumentSkjermService;
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	public void opphevKasserSkjermDokument(Long dokumentInfoId, String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserDokumentSkjermService.opphevSkjermDokument(dokumentInfoId));
		lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, dokumentInfoId, aksjonsLoggHeaderString, arkivElementEndringTOList);
	}

	public void kasserSkjermDokument(Long dokumentInfoId, String aksjonsLoggHeaderString) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserDokumentSkjermService.skjermDokument(dokumentInfoId));
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
