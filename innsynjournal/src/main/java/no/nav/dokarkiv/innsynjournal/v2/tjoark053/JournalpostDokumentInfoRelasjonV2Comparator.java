package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;

import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentinfoRelasjon;

import java.util.Comparator;

/**
 * Comparator that orders DokumentInfoRelasjons as follows:
 * <ol>
 * <li>A hoveddokument is always first</li>
 * <li>Vedleggs are ordered by dokumentinfoRelasjonId, lowest first</li>
 * </ol>
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class JournalpostDokumentInfoRelasjonV2Comparator implements Comparator<DokumentinfoRelasjon> {

	@Override
	public int compare(DokumentinfoRelasjon relasjon1, DokumentinfoRelasjon relasjon2) {
		if (HOVEDDOKUMENT.name().equals(relasjon1.getDokumentTilknyttetJournalpost().getValue())) {
			return -1;
		}
		if (HOVEDDOKUMENT.name().equals(relasjon2.getDokumentTilknyttetJournalpost().getValue())) {
			return 1;
		}

		return relasjon1.getDokumentinfoRelasjonId().compareTo(relasjon2.getDokumentinfoRelasjonId());
	}
}
