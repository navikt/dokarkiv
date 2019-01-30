package no.nav.dokarkiv.journal.v3.tjoark058;

import static no.nav.dokarkiv.core.util.DateConverterUtil.convertDateToXMLGregorianCalendar;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.validator.FoedselsnummerValidator;
import no.nav.dokarkiv.core.domain.validator.OrgnrValidator;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Dokumentkategorier;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Dokumenttilstand;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.DokumenttypeIder;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journalposttyper;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Mottakskanaler;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Referanser;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Tema;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Utsendingskanaler;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.Variantformater;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.ArkivSak;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.DetaljertDokumentinformasjon;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.DokumentInnhold;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Journalpost;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.KorrespendansePart;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.Kryssreferanse;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.SkannetInnhold;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class HentKjerneJournalpostListeResponseMapper {
	public static final String KORRESPODANSE_TYPE_MOTTAKER = "Mottaker";
	public static final String KORRESPODANSE_TYPE_AVSENDER = "Avsender";


	public HentKjerneJournalpostListeResponse map(HentKjerneJournalpostListeResponseTo responseTo) {
		HentKjerneJournalpostListeResponse response = new HentKjerneJournalpostListeResponse();
		response.withJournalpostListe(mapJournalpostListe(responseTo))
			.withSisteIntervall(responseTo.isSisteIntervall());		
		return response;
	}

	private List<Journalpost> mapJournalpostListe(HentKjerneJournalpostListeResponseTo responseTo) {
		List<Journalpost> response = new ArrayList<>();
		if (responseTo.getJournalpostListe() != null) {
			for (no.nav.dokarkiv.core.domain.entities.Journalpost toJournalpost : responseTo.getJournalpostListe()) {
				Journalpost wsJournalpost = new Journalpost();
				wsJournalpost.setJournalpostId(String.valueOf(toJournalpost.getJournalpostId()));
				wsJournalpost.setGjelderArkivSak(mapArkivSak(toJournalpost.getSaksrelasjon()));
				wsJournalpost.getKryssreferanseListe().addAll(mapKryssreferanse(toJournalpost.getKryssreferanser()));
				wsJournalpost.setKorrespondansePart(mapKorrespondansePart(toJournalpost));

				if(toJournalpost.findHoveddokumentDokumentInfoRelasjon() != null &&
				   toJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo() != null) {
					DetaljertDokumentinformasjon detaljertDokumentinformasjon = mapDetaljerDokumentinformasjon(
							toJournalpost
									.findHoveddokumentDokumentInfoRelasjon()
									.getDokumentInfo());
					if(detaljertDokumentinformasjon != null) {
						wsJournalpost
								.setHoveddokument(detaljertDokumentinformasjon);
					}
					else {
						log.warn("Detaljert dokumentInformasjon er null for {}",
								toJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId());
					}
				}
				else {
					log.warn("Journalpost {} mangler HOVEDDUKUMENT med DokumentInfo", toJournalpost.getJournalpostId());
				}

				wsJournalpost.getVedleggListe().addAll(mapVedleggsListe(toJournalpost
						.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)));
				wsJournalpost.getBrukerListe().addAll(mapBrukere(toJournalpost.getBrukere()));
				wsJournalpost.setJournaltilstand(mapJournaltilstand(toJournalpost));
				wsJournalpost.setTema(new Tema().withValue(getEnumName(toJournalpost.getFagomrade())));
				wsJournalpost
						.setMottakskanal(new Mottakskanaler().withValue(getEnumName(toJournalpost.getMottakskanal())));
				wsJournalpost.setUtsendingskanal(
						new Utsendingskanaler().withValue(getEnumName(toJournalpost.getUtsendingskanal())));
				wsJournalpost.setInnhold(toJournalpost.getInnhold());
				wsJournalpost
						.setForsendelseJournalfoert(convertDateToXMLGregorianCalendar(toJournalpost.getJournalDato()));
				wsJournalpost.setForsendelseMottatt(convertDateToXMLGregorianCalendar(toJournalpost.getMottattDato()));
				wsJournalpost.setJournalposttype(
						new Journalposttyper().withValue(getEnumName(toJournalpost.getJournalposttype())));
				response.add(wsJournalpost);
			}
		}

		return response;
	}

	private Journaltilstand mapJournaltilstand(no.nav.dokarkiv.core.domain.entities.Journalpost toJournalpost) {
		if (toJournalpost.getSaksrelasjon() != null && toJournalpost.getSaksrelasjon().getFeilregistrert() != null && toJournalpost.getSaksrelasjon().getFeilregistrert()) {
			return Journaltilstand.UTGAAR;
		}
		
		switch (toJournalpost.getJournalstatus()) {
		case FL: case FS: case J: case E:
			return Journaltilstand.ENDELIG;
		case UB : case M : case MO : case OD : case R : case D :
			return Journaltilstand.MIDLERTIDIG;
		case U: case A:
			return Journaltilstand.UTGAAR;
		default:
			return null;
		}
	}	

	private List<DetaljertDokumentinformasjon> mapVedleggsListe(
			Set<JournalpostDokumentInfoRelasjon> vedleggRelasjonListe) {
		List<DetaljertDokumentinformasjon> mapVedleggsListe = new ArrayList<>();
		
		List<JournalpostDokumentInfoRelasjon> sortedList = new LinkedList<>(vedleggRelasjonListe);
		Collections.sort(sortedList, new DokumentInfoRelasjonComperator());
		for(JournalpostDokumentInfoRelasjon jpRel : sortedList) {
			mapVedleggsListe.add(mapDetaljerDokumentinformasjon(jpRel.getDokumentInfo()));
		}
		return mapVedleggsListe;
	}	

	private ArkivSak mapArkivSak(Saksrelasjon toSaksrelasjon) {
		return new ArkivSak()
			.withArkivSakId(toSaksrelasjon.getSakId())
			.withArkivSakSystem(getEnumName(toSaksrelasjon.getFagsystem()))
			.withErFeilregistrert(toSaksrelasjon.getFeilregistrert());
	}
	
	private List<Kryssreferanse> mapKryssreferanse(
			Set<no.nav.dokarkiv.core.domain.entities.Kryssreferanse> toKryssreferanser) {
		List<Kryssreferanse> wsKryssreferanse = new ArrayList<>();
		for (no.nav.dokarkiv.core.domain.entities.Kryssreferanse toKryssreferanse : toKryssreferanser) {
			wsKryssreferanse.add(new Kryssreferanse()
					.withReferanse(new Referanser().withValue(getEnumName(toKryssreferanse.getReferanseType())))
					.withReferanseId(toKryssreferanse.getReferanseId()));			
		}
		return wsKryssreferanse;
	}


	private KorrespendansePart mapKorrespondansePart(no.nav.dokarkiv.core.domain.entities.Journalpost toJournalpost) {
		return new KorrespendansePart()
				.withKorrespondansepartId(toJournalpost.getAvsenderMottakerId())
				.withKorrespondansepartNavn(toJournalpost.getAvsenderMottaker())
				.withKorrespondansepartType(mapKorrespondanseparttyper(toJournalpost.getJournalposttype()));
	}

	private String mapKorrespondanseparttyper(JournalpostTypeCode toJournalposttype) {
		if (JournalpostTypeCode.I.equals(toJournalposttype)) {
			return KORRESPODANSE_TYPE_AVSENDER;
		} else {
			return KORRESPODANSE_TYPE_MOTTAKER;
		}
	}
	
	private List<Aktoer> mapBrukere(Set<Bruker> toBrukere) {
		List<Aktoer> aktoer = new ArrayList<>();
		for (Bruker toBruker : toBrukere) {
			if (toBruker.getBrukerType() == BrukerTypeCode.PERSON
					|| (toBruker.getBrukerType() == BrukerTypeCode.SAMHANDLER
							&& FoedselsnummerValidator.isValidPid(toBruker.getBrukerId()))) {
				aktoer.add(new Person().withIdent(toBruker.getBrukerId()));
			} else if (toBruker.getBrukerType() == BrukerTypeCode.ORGANISASJON
					|| (toBruker.getBrukerType() == BrukerTypeCode.SAMHANDLER
							&& OrgnrValidator.isOrgnr(toBruker.getBrukerId()))) {
				aktoer.add(new Organisasjon().withOrgnr(toBruker.getBrukerId()));
			}
		}
		return aktoer;
	}
	
	private DetaljertDokumentinformasjon mapDetaljerDokumentinformasjon(DokumentInfo dokumentInfo) {
		return new DetaljertDokumentinformasjon()
			.withDokumentId(String.valueOf(dokumentInfo.getDokumentInfoId()))
			.withDokumentInnholdListe(mapDokumentInnhold(dokumentInfo.getFildetaljerListe()))
			.withDokumentTypeId(new DokumenttypeIder().withValue(dokumentInfo.getDokumenttypeId()))
			.withTittel(dokumentInfo.getTittel())
			.withDokumentkategori(new Dokumentkategorier().withValue(getEnumName(dokumentInfo.getKategori())))
			.withDokumenttilstand(mapDokumenttilstand(dokumentInfo))
			.withSkannetInnholdListe(mapSkannetInnhold(dokumentInfo.getSkannetInnholdListe()));
	}
	

	private List<DokumentInnhold> mapDokumentInnhold(Set<no.nav.dokarkiv.core.domain.entities.FilDetaljer> filDetaljer) {
		List<DokumentInnhold> dokumentInnhold = new ArrayList<>();
		for(no.nav.dokarkiv.core.domain.entities.FilDetaljer filDetalj : filDetaljer) {
			if (!filDetalj.getVariantFormat().equals(VariantFormatCode.SLADDET)) {
				dokumentInnhold.add(new DokumentInnhold()
						.withArkivfiltype(new Arkivfiltyper().withValue(getEnumName(filDetalj.getFiltype())))
						.withVariantformat(new Variantformater().withValue(getEnumName(filDetalj.getVariantFormat())))
				);
			}
		}
		return dokumentInnhold;
	}
	

	private Dokumenttilstand mapDokumenttilstand(DokumentInfo dokumentInfo) {
		if (dokumentInfo.isAvbrutt()) {
			return Dokumenttilstand.AVBRUTT;
		} else if (dokumentInfo.isFerdigstilt()) {
			return Dokumenttilstand.FERDIGSTILT;
		} else if (dokumentInfo.isUnderRedigering()) {
			return Dokumenttilstand.UNDER_REDIGERING;
		}
		return null;
	}

	private List<SkannetInnhold> mapSkannetInnhold(Set<no.nav.dokarkiv.core.domain.entities.SkannetInnhold> skannetInnholdListe) {
		List<SkannetInnhold> skannetInnhold = new ArrayList<>();
		for(no.nav.dokarkiv.core.domain.entities.SkannetInnhold domainSkannetInnhold : skannetInnholdListe) {
			skannetInnhold.add(new SkannetInnhold()
					.withVedleggInnhold(domainSkannetInnhold.getVedleggInnhold())
					.withDokumenttypeId(new DokumenttypeIder().withValue(domainSkannetInnhold.getDokumenttypeid()))
					);
		}
		return skannetInnhold;
	}
	
	private static String getEnumName(Enum<?> aEnum) {
		return aEnum == null ? null : aEnum.name();
	}

	
	
}
