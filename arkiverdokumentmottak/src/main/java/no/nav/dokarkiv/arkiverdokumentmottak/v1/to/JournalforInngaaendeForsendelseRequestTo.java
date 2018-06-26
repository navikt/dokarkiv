package no.nav.dokarkiv.arkiverdokumentmottak.v1.to;


import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.util.Assert;

/**
 * Request object for operation OpprettogFerdigstillJournalpost.
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 16.02.2017
 */
public class JournalforInngaaendeForsendelseRequestTo {

	private static final String ERROR_DESC = "Missing required field in request: ";
	private static final String ERROR_DESC_COLLECTION = "Missing or empty list of required field in request: ";

	private Journalpost journalpost;

	public JournalforInngaaendeForsendelseRequestTo(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

	public void validate() {
		Assert.notNull(journalpost, ERROR_DESC + "Journalpost");
		Assert.notNull(journalpost.getSaksrelasjon(), ERROR_DESC + "Saksrelasjon");
		Assert.notEmpty(journalpost.getBrukere(), ERROR_DESC_COLLECTION + "Brukere");
		validateJournalpostDokumentInfoRelasjoner();
	}

	private void validateJournalpostDokumentInfoRelasjoner() {
		Assert.notEmpty(this.journalpost.getJournalpostDokumentInfoRelasjoner(), ERROR_DESC_COLLECTION + "JournalpostDokumentInfoRelasjoner");
		for (JournalpostDokumentInfoRelasjon relasjon : this.journalpost.getJournalpostDokumentInfoRelasjoner()) {
			Assert.notNull(relasjon.getDokumentInfo(), ERROR_DESC + "JournalpostDokumentInfoRelasjoner.DokumentInfo");
			Assert.notEmpty(relasjon.getDokumentInfo()
					.getFildetaljerListe(), ERROR_DESC_COLLECTION + "JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer");
		}
	}

	public Journalpost getJournalpost() {
		return journalpost;
	}

	public void setJournalpost(Journalpost journalpost) {
		this.journalpost = journalpost;
	}

}
