package no.nav.dokarkiv.core.aksjonslogg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;

@Slf4j
class AksjonsLoggMapper {

	public static AksjonsLogg mapToAksjonsLoggAndSetDefaults(AksjonsLoggTO aksjonsLoggTO, List<ArkivElementEndringTO> arkivElementEndringTOList, Journalpost journalpost) {
		String componentId = RequestContextHolder.currentRequestContext().getComponentId();

		final String utfoertAv = Strings.isEmpty(aksjonsLoggTO.getUtfoertAv()) ? MDC.get(MDC_USER_ID) : aksjonsLoggTO.getUtfoertAv();

		AksjonsLogg aksjonsLogg = AksjonsLogg.builder()
				.tidspunkt(LocalDateTime.now())
				.aksjon(aksjonsLoggTO.getAksjon())
				.applikasjon(componentId)
				.bruker(mapBruker(aksjonsLoggTO.getBruker(), journalpost))
				.arkivsaksnummer(mapArkivsaksnummer(journalpost))
				.arkivsaksystem(mapArkivsaksystem(journalpost))
				.dokumentInfoId(aksjonsLoggTO.getDokumentInfoId())
				.journalpostId(aksjonsLoggTO.getJournalpostId())
				.hjemmel(aksjonsLoggTO.getHjemmel())
				.melding(aksjonsLoggTO.getMelding())
				.utfoertAv(utfoertAv)
				.arkivElementEndringer(mapArkivElementEndring(arkivElementEndringTOList))
				.build();

		aksjonsLogg.getArkivElementEndringer().forEach(arkivElementEndring -> arkivElementEndring.setAksjonsLogg(aksjonsLogg));
		return aksjonsLogg;
	}

	private static String mapArkivsaksnummer(Journalpost journalpost) {
		return journalpost != null && journalpost.getSaksrelasjon() != null ? journalpost.getSaksrelasjon().getSaknrfk() : null;
	}

	private static FagsystemCode mapArkivsaksystem(Journalpost journalpost) {
		return journalpost != null && journalpost.getSaksrelasjon() != null ? journalpost.getSaksrelasjon().getFagsystem() : null;
	}

	private static String mapBruker(String bruker, Journalpost journalpost) {
	    if (Strings.isNotEmpty(bruker)) {
			return bruker;
		} else if (journalpost != null && journalpost.getBrukere() != null && !journalpost.getBrukere().isEmpty()) {
	        if (journalpost.getBrukere().size() == 1) {
                bruker = journalpost.getBrukere().iterator().next().getBrukerId();
			} else {
				log.warn("Journalpost med journalpostId=" + journalpost.getJournalpostId() + " har mer enn én bruker. " +
						"Siste lagrede bruker settes i aksjonslogg.");
				List<Bruker> brukere = new ArrayList<>(journalpost.getBrukere());
				brukere.sort(Comparator.comparing(Bruker::getBrukerInfoId));
                bruker = brukere.get(brukere.size()-1).getBrukerId();
	        }
	        return bruker;
		} else {
			return null;
		}
	}

	private static Set<ArkivElementEndring> mapArkivElementEndring(List<ArkivElementEndringTO> arkivElementEndringTOList) {
		return arkivElementEndringTOList.stream()
				.map(arkivElementEndringTO -> ArkivElementEndring.builder()
						.arkivElement(arkivElementEndringTO.getArkivElement())
						.fraVerdi(arkivElementEndringTO.getFraVerdi())
						.tilVerdi(arkivElementEndringTO.getTilVerdi())
						.tidspunkt(LocalDateTime.now())
						.build()
				).collect(Collectors.toSet());
	}

}
