package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056;

import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.inngaaendejournal.v1.common.DokumentInformasjonManglerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalfoeringsbehovTo;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalpostManglerTo;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournalpostManglerToMapper {
	public JournalpostManglerTo map(Journalpost journalpost) {
		try {
			return doMap(journalpost);
		} catch(Exception e) {
			throw new DokarkivFunctionalException("Kunne ikke mappe Journalpost. journalpostId=" + journalpost.getJournalpostId(), e);
		}
	}

	private JournalpostManglerTo doMap(Journalpost journalpost) {
		return JournalpostManglerTo.builder()
				.avsenderId(isNull(journalpost.getAvsenderMottakerId()))
				.avsenderNavn(isNull(journalpost.getAvsenderMottaker()))
				.arkivSak(journalpost.getSaksrelasjon() == null ? JournalfoeringsbehovTo.MANGLER : isNull(journalpost.getSaksrelasjon().getSakId()))
				.innhold(isNull(journalpost.getInnhold()))
				.tema(isNull(journalpost.getFagomrade()))
				.bruker(isNull(journalpost.getBrukere()))
				.hoveddokument(mapHoveddokument(journalpost.findHoveddokumentDokumentInfoRelasjon()))
				.vedlegg(mapVedlegg(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)))
				.build();
	}

	private DokumentInformasjonManglerTo mapHoveddokument(JournalpostDokumentInfoRelasjon hoveddokumentDokumentInfoRelasjon) {
		return mapToDokumentInformasjonMangler(hoveddokumentDokumentInfoRelasjon);
	}

	private List<DokumentInformasjonManglerTo> mapVedlegg(Set<JournalpostDokumentInfoRelasjon> vedlegg) {
		if(vedlegg.isEmpty()) {
			return Collections.emptyList();
		} else {
			return vedlegg.stream().map(this::mapToDokumentInformasjonMangler).collect(Collectors.toList());
		}
	}

	private DokumentInformasjonManglerTo mapToDokumentInformasjonMangler(JournalpostDokumentInfoRelasjon relasjon) {
		DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
		return DokumentInformasjonManglerTo.builder()
				.dokumentId(dokumentInfo.getDokumentInfoId())
				.tittel(isNull(dokumentInfo.getTittel()))
				.dokumentKategori(isNull(dokumentInfo.getKategori()))
				.build();
	}

	private JournalfoeringsbehovTo isNull(Object object) {
		if(object == null) {
			return JournalfoeringsbehovTo.MANGLER;
		} else {
			return JournalfoeringsbehovTo.MANGLER_IKKE;
		}
	}

	private JournalfoeringsbehovTo isNull(Collection<?> collection) {
		if(collection == null || collection.isEmpty()) {
			return JournalfoeringsbehovTo.MANGLER;
		} else {
			return JournalfoeringsbehovTo.MANGLER_IKKE;
		}
	}
}
