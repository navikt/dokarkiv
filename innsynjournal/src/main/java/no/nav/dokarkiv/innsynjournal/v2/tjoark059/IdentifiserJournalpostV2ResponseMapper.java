package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.innsynjournal.v2.InnsynJournalpostTo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Dokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;

import java.util.Map;

/**
 * Mapper for domain response to ws-reponse.
 *
 * @author Ketill Fenne, Visma Consulting.
 */
public class IdentifiserJournalpostV2ResponseMapper {

	/**
	 * Mapping of domain journalposts to ws-response
	 *
	 * @param innsynJournalpostTo
	 * @return {@link HentTilgjengeligJournalpostListeResponse}
	 */
	public IdentifiserJournalpostResponse map(InnsynJournalpostTo innsynJournalpostTo) {
		IdentifiserJournalpostResponse response = new IdentifiserJournalpostResponse();
		if ((innsynJournalpostTo != null) && (innsynJournalpostTo.getJournalpost() != null) && (innsynJournalpostTo.getJournalpost().getJournalpostId() != null)){
			Journalpost journalpost = innsynJournalpostTo.getJournalpost();
			response.setJournalpostId(journalpost.getJournalpostId().toString());
			mapHoveddokument(journalpost, response, innsynJournalpostTo.getDokumentInnsyn());
			mapVedlegg(journalpost, response, innsynJournalpostTo.getDokumentInnsyn());
		}
		return response;
	}

	private void mapHoveddokument(Journalpost journalpost, IdentifiserJournalpostResponse response, Map<Long, InnsynJournalpostTo.DokumentInnsyn> dokumentInnsynMap) {
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon  : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (TilknyttetJournalpostSomCode.HOVEDDOKUMENT.equals(dokumentInfoRelasjon.getTilknyttetJournalpostSom())) {
				Dokument dokument =  new Dokument();
				Long dokumentId = Long.valueOf(dokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId());
				dokument.setDokumentId(dokumentId.toString());
				dokument.setTittel(dokumentInfoRelasjon.getDokumentInfo().getTittel());
				if (dokumentInnsynMap.containsKey(dokumentId)) {
					InnsynJournalpostTo.DokumentInnsyn innsyn = dokumentInnsynMap.get(dokumentId);
					dokument.setInnsynDokument(InnsynDokument.valueOf(innsyn.name()));
				}
				response.setHoveddokument(dokument);
				break;
			}
		}
	}

	private void mapVedlegg(Journalpost journalpost, IdentifiserJournalpostResponse response, Map<Long, InnsynJournalpostTo.DokumentInnsyn> dokumentInnsynMap) {
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon  : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (TilknyttetJournalpostSomCode.VEDLEGG.equals(dokumentInfoRelasjon.getTilknyttetJournalpostSom())) {
				Dokument dokument =  new Dokument();
				Long dokumentId = Long.valueOf(dokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId());
				dokument.setDokumentId(dokumentId.toString());
				dokument.setTittel(dokumentInfoRelasjon.getDokumentInfo().getTittel());
				if (dokumentInnsynMap.containsKey(dokumentId)) {
					InnsynJournalpostTo.DokumentInnsyn innsyn = dokumentInnsynMap.get(dokumentId);
					dokument.setInnsynDokument(InnsynDokument.valueOf(innsyn.name()));
				}
				response.getVedleggListe().add(dokument);
			}
		}
	}
}
