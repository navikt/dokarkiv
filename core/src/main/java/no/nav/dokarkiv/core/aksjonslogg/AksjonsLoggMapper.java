package no.nav.dokarkiv.core.aksjonslogg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@Slf4j
class AksjonsLoggMapper {

	public AksjonsLogg mapToAksjonsLogg(AksjonsLoggTO aksjonsLoggTO, List<ArkivElementEndringTO> arkivElementEndringTOList, Journalpost journalpost) {
		String componentId = RequestContextHolder.currentRequestContext().getComponentId();
		String userId = RequestContextHolder.currentRequestContext().getUserId();

		String utfoertAv = Strings.isEmpty(aksjonsLoggTO.getUtfoertAv()) ? userId : aksjonsLoggTO.getUtfoertAv();

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

	private String mapArkivsaksnummer(Journalpost journalpost) {
		return journalpost != null && journalpost.getSaksrelasjon() != null ? journalpost.getSaksrelasjon().getSakId() : null;
	}

	private FagsystemCode mapArkivsaksystem(Journalpost journalpost) {
		return journalpost != null && journalpost.getSaksrelasjon() != null ? journalpost.getSaksrelasjon().getFagsystem() : null;
	}

	private String mapBruker(String bruker, Journalpost journalpost) {
	    if (bruker != null) {
			return bruker;
		} else if (journalpost != null && journalpost.getBrukere() != null) {
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

	private Set<ArkivElementEndring> mapArkivElementEndring(List<ArkivElementEndringTO> arkivElementEndringTOList) {
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
