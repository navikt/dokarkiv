package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056;

import static no.nav.dokarkiv.core.util.DateConverterUtil.convertDateToXMLGregorianCalendar;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.AktoerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.ArkivSakTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumentInnholdTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumentinformasjonTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumenttilstandTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.InngaaendeJournalpostTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.JournaltilstandTo;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.ArkivSak;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentinnhold;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumentkategorier;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumenttilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.DokumenttypeIder;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Kodeverdi;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Mottakskanaler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentInngaaendeJournalpostResponseMapper {

	public HentJournalpostResponse map(InngaaendeJournalpostTo to) {
		HentJournalpostResponse response = new HentJournalpostResponse();
		response.setInngaaendeJournalpost(mapInngaendeJournalpost(to));
		return response;
	}

	private InngaaendeJournalpost mapInngaendeJournalpost(InngaaendeJournalpostTo to) {
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		inngaaendeJournalpost.setAvsenderId(to.getAvsenderId());
		if (to.getForsendelseMottatt() == null) {
			inngaaendeJournalpost.setForsendelseMottatt(null);
		} else {
			inngaaendeJournalpost.setForsendelseMottatt(convertDateToXMLGregorianCalendar(Date.from(to.getForsendelseMottatt().atZone(ZoneId.systemDefault()).toInstant())));
		}
		inngaaendeJournalpost.setMottakskanal(nullsafeEnumToKodeverdiMapper(to.getMottakskanal(), Mottakskanaler.class));
		inngaaendeJournalpost.setTema(nullsafeEnumToKodeverdiMapper(to.getTema(), Tema.class));
		inngaaendeJournalpost.setJournaltilstand(mapJournaltilstand(to.getJournaltilstand()));
		inngaaendeJournalpost.setJournalfEnhet(to.getJournalfEnhet());
		inngaaendeJournalpost.setArkivSak(mapArkivSak(to.getArkivSak()));
		inngaaendeJournalpost.getBrukerListe().addAll(mapBrukere(to.getBrukere()));
		inngaaendeJournalpost.setHoveddokument(mapDokumentinformasjon(to.getHoveddokument()));
		inngaaendeJournalpost.getVedleggListe().addAll(mapDokumentinformasjon(to.getVedlegg()));
		return inngaaendeJournalpost;
	}

	private List<Dokumentinformasjon> mapDokumentinformasjon(List<DokumentinformasjonTo> vedleggTo) {
		if (vedleggTo == null) {
			return new ArrayList<>();
		} else {
			return vedleggTo.stream().map(this::mapDokumentinformasjon).collect(Collectors.toList());
		}
	}

	private Dokumentinformasjon mapDokumentinformasjon(DokumentinformasjonTo to) {
		if (to == null) {
			return null;
		} else {
			Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
			dokumentinformasjon.setDokumentkategori(nullsafeEnumToKodeverdiMapper(to.getDokumentkategori(), Dokumentkategorier.class));
			DokumenttypeIder dokumenttypeIder = new DokumenttypeIder();
			dokumenttypeIder.setValue(to.getDokumenttypeId());
			dokumentinformasjon.setDokumenttypeId(dokumenttypeIder);
			dokumentinformasjon.setDokumentId(to.getDokumentId().toString());
			dokumentinformasjon.setDokumenttilstand(mapDokumenttilstand(to.getDokumenttilstand()));
			dokumentinformasjon.getDokumentInnholdListe().addAll(mapDokumentinnhold(to.getDokumentInnhold()));
			return dokumentinformasjon;
		}
	}

	private Journaltilstand mapJournaltilstand(JournaltilstandTo journaltilstand) {
		return Journaltilstand.fromValue(journaltilstand.name());
	}

	private Dokumenttilstand mapDokumenttilstand(DokumenttilstandTo dokumenttilstand) {
		if (dokumenttilstand == null) {
			return null;
		} else {
			return Dokumenttilstand.fromValue(dokumenttilstand.name());
		}
	}

	private ArkivSak mapArkivSak(ArkivSakTo arkivSakTo) {
		if (arkivSakTo == null) {
			return null;
		} else {
			ArkivSak arkivSak = new ArkivSak();
			arkivSak.setArkivSakId(arkivSakTo.getArkivSakId());
			arkivSak.setArkivSakSystem(arkivSakTo.getFagsystem().toString());
			return arkivSak;
		}
	}

	private List<? extends Aktoer> mapBrukere(List<AktoerTo> brukere) {
		if (brukere == null) {
			return new ArrayList<>();
		} else {
			return brukere.stream().map(aktoerTo -> {
				Aktoer mappedAktor = null;
				if (aktoerTo.getAktoerType() == BrukerTypeCode.ORGANISASJON) {
					mappedAktor = new Organisasjon();
					((Organisasjon) mappedAktor).setOrganisasjonsnummer(aktoerTo.getAktoerId());
				} else if (aktoerTo.getAktoerType() == BrukerTypeCode.PERSON) {
					mappedAktor = new Person();
					((Person) mappedAktor).setIdent(aktoerTo.getAktoerId());
				}
				return mappedAktor;
			}).collect(Collectors.toList());
		}
	}

	private List<? extends Dokumentinnhold> mapDokumentinnhold(List<DokumentInnholdTo> dokumentInnholds) {
		if (dokumentInnholds == null) {
			return new ArrayList<>();
		} else {
			return dokumentInnholds.stream().map(dokumentInnholdTo -> {
				Dokumentinnhold dokumentinnhold = new Dokumentinnhold();
				dokumentinnhold.setArkivfiltype(nullsafeEnumToKodeverdiMapper(dokumentInnholdTo.getArkivFiltype(), Arkivfiltyper.class));
				dokumentinnhold.setVariantformat(nullsafeEnumToKodeverdiMapper(dokumentInnholdTo.getVariantFormat(), Variantformater.class));
				return dokumentinnhold;
			}).collect(Collectors.toList());
		}
	}

	private static <T extends Kodeverdi> T nullsafeEnumToKodeverdiMapper(Enum enumz, Class<T> clazz) {
		try {
			if (enumz == null) {
				return null;
			} else {
				T kodeverdi = clazz.newInstance();
				kodeverdi.setValue(enumz.name());
				return kodeverdi;
			}
		} catch (IllegalAccessException e) {
			throw new DokarkivFunctionalException("Unable to access Kodeverdi", e);
		} catch (InstantiationException e) {
			throw new DokarkivFunctionalException("Unable to instantiate Kodeverdi", e);
		}
	}
}
