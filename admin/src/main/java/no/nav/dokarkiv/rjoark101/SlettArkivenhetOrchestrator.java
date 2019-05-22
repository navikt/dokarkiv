package no.nav.dokarkiv.rjoark101;

import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.exception.UgyldigSlettArkivenhetInputException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class SlettArkivenhetOrchestrator {

	private final SlettArkivenhetService slettArkivenhetService;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;

	public SlettArkivenhetOrchestrator(SlettArkivenhetService slettArkivenhetService, LagreAksjonsLoggService lagreAksjonsLoggService) {
		this.slettArkivenhetService = slettArkivenhetService;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
	}

	public List<ArkivElementEndringTO> slettArkivenhhet(ArkivenhetCode arkivenhet, Long journalpostId, Long dokumentInfoId, VariantFormatCode variant, String hjemmel, String melding, String utfoertAv) throws UgyldigAksjonsLoggException {
		List<ArkivElementEndringTO> arkivElementEndringTOList = new ArrayList<>();
		assertNotNullOrEmpty(arkivenhet, "arkivEnhet");

		switch (arkivenhet) {
			case JOURNALPOST:
				assertNotNullOrEmpty(journalpostId, "journalpostId");
				Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMapJournalpost = slettArkivenhetService.slettJournalpost(journalpostId);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.SLETT, aksjonsLoggMapJournalpost, hjemmel, melding, utfoertAv);
				break;
			case DOKUMENT_INFO:
				assertNotNullOrEmpty(dokumentInfoId, "dokumentInfoId");
				Map<Pair<Long, Long>, List<ArkivElementEndringTO>> aksjonsLoggMapDokumentInfo = slettArkivenhetService.slettDokumentInfo(dokumentInfoId);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.SLETT, aksjonsLoggMapDokumentInfo, hjemmel, melding, utfoertAv);
				break;
			case DOKUMENT_FIL:
				assertNotNullOrEmpty(dokumentInfoId, "dokumentInfoId");
				assertNotNullOrEmpty(variant, "variant");
				arkivElementEndringTOList = slettArkivenhetService.slettDokumentFil(dokumentInfoId, variant);
				lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.SLETT, dokumentInfoId, hjemmel, melding, utfoertAv, arkivElementEndringTOList);
				break;
		}

		return arkivElementEndringTOList;
	}

	private void assertNotNullOrEmpty(Object value, String parameter) {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigSlettArkivenhetInputException(String.format("Validering av input feilet: Input mangler påkrevd parameter \"%s\"", parameter));
		}
	}
}

