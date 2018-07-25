package no.nav.dokarkiv.inngaaendejournal.v1.tjoark057;

import no.nav.dokarkiv.inngaaendejournal.v1.common.DokumentInformasjonManglerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalfoeringsbehovTo;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalpostManglerTo;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.DokumentInformasjonMangler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journalfoeringsbehov;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.JournalpostMangler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class UtledJournalfoeringsbehovResponseMapper {
	public UtledJournalfoeringsbehovResponse map(JournalpostManglerTo to) {
		UtledJournalfoeringsbehovResponse response = new UtledJournalfoeringsbehovResponse();
		response.setJournalfoeringsbehov(mapJournalpostMangler(to));
		return response;
	}

	private JournalpostMangler mapJournalpostMangler(JournalpostManglerTo to) {
		JournalpostMangler journalpostMangler = new JournalpostMangler();
		journalpostMangler.setAvsenderId(mapJournalfoeringsbehov(to.getAvsenderId()));
		journalpostMangler.setAvsenderNavn(mapJournalfoeringsbehov(to.getAvsenderNavn()));
		journalpostMangler.setArkivSak(mapJournalfoeringsbehov(to.getArkivSak()));
		journalpostMangler.setInnhold(mapJournalfoeringsbehov(to.getInnhold()));
		journalpostMangler.setTema(mapJournalfoeringsbehov(to.getTema()));
		journalpostMangler.setBruker(mapJournalfoeringsbehov(to.getBruker()));
		journalpostMangler.setHoveddokument(mapDokumentInformasjonMangler(to.getHoveddokument()));
		journalpostMangler.getVedleggListe().addAll(mapVedlegg(to.getVedlegg()));
		return journalpostMangler;
	}

	private DokumentInformasjonMangler mapDokumentInformasjonMangler(DokumentInformasjonManglerTo to) {
		DokumentInformasjonMangler dokumentInformasjonMangler = new DokumentInformasjonMangler();
		dokumentInformasjonMangler.setDokumentId(to.getDokumentId().toString());
		dokumentInformasjonMangler.setDokumentkategori(mapJournalfoeringsbehov(to.getDokumentKategori()));
		dokumentInformasjonMangler.setTittel(mapJournalfoeringsbehov(to.getTittel()));
		return dokumentInformasjonMangler;
	}

	private Journalfoeringsbehov mapJournalfoeringsbehov(JournalfoeringsbehovTo to) {
		return Journalfoeringsbehov.fromValue(to.name());
	}

	private List<? extends DokumentInformasjonMangler> mapVedlegg(List<DokumentInformasjonManglerTo> vedlegg) {
		if(vedlegg == null) {
			return new ArrayList<>();
		} else {
			return vedlegg.stream().map(this::mapDokumentInformasjonMangler).collect(Collectors.toList());
		}
	}
}
