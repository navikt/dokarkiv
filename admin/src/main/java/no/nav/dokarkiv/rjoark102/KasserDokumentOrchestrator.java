package no.nav.dokarkiv.rjoark102;

import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KasserDokumentOrchestrator {

	private final KasserDokumentService kasserDokumentService;
	private final KasserSkjermDokumentService kasserSkjermDokumentService;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;
	private final JournalpostDokumentInfoRelasjonRepository relasjonRepository;


	@Inject
	public KasserDokumentOrchestrator(KasserDokumentService kasserDokumentService, KasserSkjermDokumentService kasserSkjermDokumentService, LagreAksjonsLoggService lagreAksjonsLoggService, JournalpostDokumentInfoRelasjonRepository relasjonRepository) {
		this.kasserDokumentService = kasserDokumentService;
		this.kasserSkjermDokumentService = kasserSkjermDokumentService;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
		this.relasjonRepository = relasjonRepository;
	}

	public void opphevKasserSkjermDokument(Long dokumentInfoId, String hjemmel, String melding, String utfoertAv) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserSkjermDokumentService.opphevSkjermDokument(dokumentInfoId));
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, dokumentInfoId, hjemmel, melding, utfoertAv, arkivElementEndringTOList);
	}

	public void kasserSkjermDokument(Long dokumentInfoId, String hjemmel, String melding, String utfoertAv) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserSkjermDokumentService.skjermDokument(dokumentInfoId));
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ENDRE_SKJERMING, dokumentInfoId, hjemmel, melding, utfoertAv, arkivElementEndringTOList);
	}


	public void kasserDokument(Long dokumentInfoId, String kassertAvNavn, String hjemmel, String melding, String utfoertAv) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		arkivElementEndringTOList.addAll(kasserDokumentService.kasserDokument(dokumentInfoId, kassertAvNavn));
		String aksjonsLoggMelding = Strings.isBlank(melding) ? fysiskKasserMelding(dokumentInfoId) : melding;
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.KASSERING, dokumentInfoId, hjemmel, aksjonsLoggMelding, utfoertAv, arkivElementEndringTOList);
	}

	private String fysiskKasserMelding(Long dokumentInfoId) {
		List<Long> knyttetTilJournalpostId = relasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId).stream().map(rel -> rel.getJournalpost().getJournalpostId())
				.collect(Collectors.toList());
		return String.format("Dokumentet knyttet til journalpostId(er) %s er kassert i alle steder der det forekom og kan ikke gjenopprettes lenger.",
				knyttetTilJournalpostId
						.stream()
						.map(Object::toString)
						.collect(Collectors.joining(", "))
		);
	}
}
