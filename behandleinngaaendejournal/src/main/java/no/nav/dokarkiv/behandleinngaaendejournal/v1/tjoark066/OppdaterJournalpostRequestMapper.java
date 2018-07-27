package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066;

import com.google.common.base.Strings;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.UgyldigInputException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.AktoerTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.ArkivSakTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.AvsenderTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.DokumentInformasjonTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostRequestTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostTo;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.ArkivSak;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Avsender;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper from {@link OppdaterJournalpostRequest} to {@link OppdaterJournalpostRequestTo}
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 29.05.2017.
 */
@Component
public class OppdaterJournalpostRequestMapper {
	
	public OppdaterJournalpostRequestTo map(OppdaterJournalpostRequest request) {
		if (request.getInngaaendeJournalpost() == null) {
			throw new UgyldigInputException("OppdaterJournalpostRequest.InngaaendeJournal kan ikke være null");
		}

		OppdaterJournalpostTo oppdaterTo = new OppdaterJournalpostTo();
		
		InngaaendeJournalpost inngaaendeJournalpost = request.getInngaaendeJournalpost();
		
		oppdaterTo.setJournalpostId(inngaaendeJournalpost.getJournalpostId());
		oppdaterTo.setInnhold(inngaaendeJournalpost.getInnhold());
		
		if(inngaaendeJournalpost.getTema() != null) {
			oppdaterTo.setTema(FagomradeCode.valueOf(inngaaendeJournalpost.getTema().getValue()));
		}
		
		String journalpostId = oppdaterTo.getJournalpostId();
		oppdaterTo.setAvsenderTo(mapAvsender(inngaaendeJournalpost.getAvsender()));
		oppdaterTo.setArkivSak(mapArkivsak(inngaaendeJournalpost.getArkivSak(), journalpostId));
		oppdaterTo.setAktoerTo(mapAktoer(inngaaendeJournalpost.getBruker(), journalpostId));
		oppdaterTo.setHoveddokument(mapDokumentInformasjon(inngaaendeJournalpost.getHoveddokument(), journalpostId));
		oppdaterTo.setVedlegg(mapVedlegg(inngaaendeJournalpost.getVedleggListe(), journalpostId));

		OppdaterJournalpostRequestTo requestTo = new OppdaterJournalpostRequestTo();
		requestTo.setOppdaterJournalpostTo(oppdaterTo);
		return requestTo;
	}
	
	private List<DokumentInformasjonTo> mapVedlegg(List<Dokumentinformasjon> dokumentinformasjonList, String journalpostId) {
		List<DokumentInformasjonTo> vedlegg = new ArrayList<>();
		if(!dokumentinformasjonList.isEmpty()) {
			for (Dokumentinformasjon dokumentinformasjon : dokumentinformasjonList) {
				vedlegg.add(mapDokumentInformasjon(dokumentinformasjon, journalpostId));
			}
		}
		
		return vedlegg;
	}
	
	private DokumentInformasjonTo mapDokumentInformasjon(Dokumentinformasjon dokumentinformasjon, String journalpostId) {
		DokumentInformasjonTo dokumentTo = null;
		if (dokumentinformasjon != null) {
			if(Strings.isNullOrEmpty(dokumentinformasjon.getDokumentId())) {
				throw new UgyldigInputException("Mangler DokumentId på Dokument i request for å oppdatere journalpost. journalpostId=" + journalpostId);
			}
			dokumentTo = new DokumentInformasjonTo();
			dokumentTo.setDokumentId(Long.valueOf(dokumentinformasjon.getDokumentId()));
			dokumentTo.setTittel(dokumentinformasjon.getTittel());
			if(dokumentinformasjon.getDokumentkategori() != null) {
				dokumentTo.setDokumentkategori(DokumentKategoriCode.valueOf(dokumentinformasjon.getDokumentkategori().getValue()));
			}
		}
		return dokumentTo;
	}
	
	private AktoerTo mapAktoer(Aktoer bruker, String journalpostId) {
		AktoerTo aktoerTo = null;
		if(bruker != null) {
			aktoerTo = new AktoerTo();
			if (bruker instanceof Organisasjon) {
				if(Strings.isNullOrEmpty(((Organisasjon) bruker).getOrganisasjonsnummer())) {
					throw new UgyldigInputException("Mangler Organisasjonsnummer på Aktoer i request for å oppdatere journalpost. journalpostId=" + journalpostId);
				}
				aktoerTo.setBrukerTypeCode(BrukerTypeCode.ORGANISASJON);
				aktoerTo.setAktoerId(((Organisasjon) bruker).getOrganisasjonsnummer());
			} else if (bruker instanceof Person) {
				if(Strings.isNullOrEmpty(((Person) bruker).getIdent())) {
					throw new UgyldigInputException("Mangler Ident på Aktoer i request for å oppdatere journalpost. journalpostId=" + journalpostId);
				}
				aktoerTo.setBrukerTypeCode(BrukerTypeCode.PERSON);
				aktoerTo.setAktoerId(((Person) bruker).getIdent());
			}
		}
		return aktoerTo;
	}
	
	private ArkivSakTo mapArkivsak(ArkivSak arkivSak, String journalpostId) {
		ArkivSakTo arkivSakTo = null;
		if(arkivSak != null) {
			if(Strings.isNullOrEmpty(arkivSak.getArkivSakId())) {
				throw new UgyldigInputException("Mangler id på ArkivSak i request for å oppdatere journalpost. journalpostId=" + journalpostId);
			}
			if(Strings.isNullOrEmpty(arkivSak.getArkivSakSystem())) {
				throw new UgyldigInputException("Mangler fagsystem på ArkivSak i request for å oppdatere journalpost. journalpostId=" + journalpostId);
			}
			arkivSakTo = new ArkivSakTo(arkivSak.getArkivSakId(),
					FagsystemCode.valueOf(arkivSak.getArkivSakSystem()));
		}
		return arkivSakTo;
	}
	
	private AvsenderTo mapAvsender(Avsender avsender) {
		AvsenderTo avsenderTo = null;
		if(avsender != null) {
			avsenderTo = new AvsenderTo(avsender.getAvsenderId(),
					avsender.getAvsenderNavn());
		}
		return avsenderTo;
	}
}