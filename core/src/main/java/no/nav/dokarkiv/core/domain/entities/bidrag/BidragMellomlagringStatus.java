package no.nav.dokarkiv.core.domain.entities.bidrag;

/**
 * States that Bidrag documents may have when temporary stored in Joark.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public enum BidragMellomlagringStatus {
	/** New vedleggs may still be added */
	DOKUMENTOPPLASTING,
	/** All documents are uploaded */
	KLAR_TIL_OVERFORING,
	/** Documents are transferred to Bisys */
	OVERFORT,
	/** An error occured in merging the PDF */
	FEIL_I_PDF
}