package no.nav.dokarkiv.journalpost.v1.bidrag;

import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class OpprettJournalpostBidragRequestMapper {
	private static final String VEDLEGG_KVITTERING_BREVKODE = "L7";

	BidragMellomlagring map(OpprettJournalpostRequest request) {
		BidragMellomlagring bidragMellomlagring = new BidragMellomlagring();
		bidragMellomlagring.setAvsenderFnr(request.getAvsenderMottaker().getId());
		bidragMellomlagring.setStatus(BidragMellomlagringStatus.KLAR_TIL_OVERFORING);
		bidragMellomlagring.setMottattDato(request.getDatoMottatt());
		addDokumentTilBidragMellomlagring(request, bidragMellomlagring);
		return bidragMellomlagring;
	}

	private void addDokumentTilBidragMellomlagring(OpprettJournalpostRequest request, BidragMellomlagring bidragMellomlagring) {
		for (int i = 0; i < request.getDokumenter().size(); i++) {
			Dokument dokument = request.getDokumenter().get(i);
			BidragMellomlagringDokument bidragMellomlagringDokument = new BidragMellomlagringDokument();
			if (i == 0) {
				bidragMellomlagringDokument.setDokumentType(BidragMellomlagringDokumentType.HOVEDDOKUMENT);
			} else {
				if (dokument.getBrevkode() != null && dokument.getBrevkode().equals(VEDLEGG_KVITTERING_BREVKODE)) {
					bidragMellomlagringDokument.setDokumentType(BidragMellomlagringDokumentType.VEDLEGG_KVITTERING);
				} else {
					bidragMellomlagringDokument.setDokumentType(BidragMellomlagringDokumentType.VEDLEGG);
				}
			}
			bidragMellomlagringDokument.setDokument(dokument.getDokumentvarianter().get(0).getFysiskDokument());
			bidragMellomlagring.addBidragMellomlagringDokument(bidragMellomlagringDokument);
		}
	}
}
