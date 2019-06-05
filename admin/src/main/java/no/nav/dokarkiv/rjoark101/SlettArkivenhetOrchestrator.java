package no.nav.dokarkiv.rjoark101;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.swapCase;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.JournalpostDokumentInfoPair;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.exception.UgyldigSlettArkivenhetInputException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class SlettArkivenhetOrchestrator {

	private final SlettArkivenhetService slettArkivenhetService;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;
	private final JournalpostDokumentInfoRelasjonRepository relasjonRepository;

	public SlettArkivenhetOrchestrator(SlettArkivenhetService slettArkivenhetService, LagreAksjonsLoggService lagreAksjonsLoggService, JournalpostDokumentInfoRelasjonRepository relasjonRepository) {
		this.slettArkivenhetService = slettArkivenhetService;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
		this.relasjonRepository = relasjonRepository;
	}

	public List<ArkivElementEndringTO> slettArkivenhhet(ArkivenhetCode arkivenhet, Long journalpostId, Long dokumentInfoId, VariantFormatCode variant, String hjemmel, String melding, String utfoertAv) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		assertNotNullOrEmpty(arkivenhet, "arkivEnhet");

		String aksjonsLoggMelding = Strings.isBlank(melding) ? generateAksjonsLoggMelding(arkivenhet, journalpostId, dokumentInfoId, variant) : melding;

		switch (arkivenhet) {
			case JOURNALPOST:
				assertNotNullOrEmpty(journalpostId, "journalpostId");
				Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMapJournalpost = slettArkivenhetService.slettJournalpost(journalpostId);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.SLETT, aksjonsLoggMapJournalpost, hjemmel, aksjonsLoggMelding, utfoertAv);
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(dokumentInfoId, "dokumentInfoId");
				Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMapDokumentInfo = slettArkivenhetService.slettDokumentInfo(dokumentInfoId);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.SLETT, aksjonsLoggMapDokumentInfo, hjemmel, aksjonsLoggMelding, utfoertAv);
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(dokumentInfoId, "dokumentInfoId");
				assertNotNullOrEmpty(variant, "variant");
				arkivElementEndringTOList = slettArkivenhetService.slettDokumentFil(dokumentInfoId, variant);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.SLETT, dokumentInfoId, hjemmel, aksjonsLoggMelding, utfoertAv, arkivElementEndringTOList);
				break;
		}

		return arkivElementEndringTOList;
	}

	private String generateAksjonsLoggMelding(ArkivenhetCode arkivenhetCode, Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormatCode) {
		switch (arkivenhetCode) {
			case JOURNALPOST:
				return String.format("Journalpost med journalpostId %s er fysisk slettet og kan ikke gjenopprettes lenger.", journalpostId);
			case DOKUMENT_INFO:
				List<Long> knyttetTilJournalpostId = relasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoId).stream().map(rel -> rel.getJournalpost().getJournalpostId())
						.collect(Collectors.toList());
				return String.format("Dokumentet knyttet til journalpostId(er) %s er fysisk slettet i alle steder der det forekom og kan ikke gjenopprettes lenger.",
						knyttetTilJournalpostId
								.stream()
								.map(Object::toString)
								.collect(Collectors.joining(", "))
				);
			case DOKUMENT_FIL:
				return String.format("Dokumentfil knyttet til dokumentInfoId %s med variant %s er fysisk slettet og kan ikke gjenopprettes lenger.",
						dokumentInfoId, variantFormatCode);
			default:
				throw new InvalidArgumentException(String.format("Det mangler logikk for generering av aksjonslogg melding for arkivEnhetCode=%s.", arkivenhetCode));
		}
	}

	private void assertNotNullOrEmpty(Object value, String parameter) {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSlettArkivenhetInputException(String.format("Validering av input feilet: Input mangler påkrevd parameter \"%s\"", parameter));
		}
	}
}

