package no.nav.dokarkiv.core.journalbehandling;


import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import org.springframework.stereotype.Component;

/**
 * Implementation of <code>MandatoryFieldsVerifier</code>.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Component
public class DefaultMandatoryFieldsVerifier implements MandatoryFieldsVerifier {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void verifyFields(Journalpost journalpost) {
		journalpost.verifyMandatoryFields();

		verifyRemainingFields(journalpost);
	}

	@Override
	public void verifyFieldsSkipJournalForendeEnhetId(Journalpost journalpost) {
		journalpost.verifyMandatoryFieldsSkipJournalforendeEnhetId();

		verifyRemainingFields(journalpost);
	}

	private void verifyRemainingFields(final Journalpost journalpost) {
		verifySaksrelasjon(journalpost);
		verifyKryssreferanse(journalpost);
		verifyBruker(journalpost);
		verifyDokumentInfoRelasjon(journalpost);
	}


	private void verifySaksrelasjon(Journalpost journalpost) {
		if (journalpost.getSaksrelasjon() != null) {
			journalpost.getSaksrelasjon().verifyMandatoryFields();
		}
	}

	private void verifyKryssreferanse(Journalpost journalpost) {
		for (Kryssreferanse kryssreferanse : journalpost.getKryssreferanser()) {
			kryssreferanse.verifyMandatoryFields();
		}
	}

	private void verifyBruker(Journalpost journalpost) {
		for (Bruker bruker : journalpost.getBrukere()) {
			bruker.verifyMandatoryFields();
		}
	}

	private void verifyDokumentInfoRelasjon(Journalpost journalpost) {
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			dokumentInfoRelasjon.verifyMandatoryFields();

			DokumentInfo dokumentInfo = dokumentInfoRelasjon.getDokumentInfo();
			if (dokumentInfo != null && !dokumentInfoRelasjon.isNewRelasjonToExistingDokumentInfo()) {
				verifyDokumentInfo(journalpost, dokumentInfo);
			}
		}
	}

	private void verifyDokumentInfo(Journalpost journalpost, DokumentInfo dokumentInfo) {
		dokumentInfo.verifyMandatoryFields(journalpost);

		verifySkannetInnhold(dokumentInfo);
		verifyFilDetaljer(dokumentInfo);
	}

	private void verifySkannetInnhold(DokumentInfo dokumentInfo) {
		for (SkannetInnhold skannetInnhold : dokumentInfo.getSkannetInnholdListe()) {
			skannetInnhold.verifyMandatoryFields();
		}
	}

	private void verifyFilDetaljer(DokumentInfo dokumentInfo) {
		for (FilDetaljer filDetaljer : dokumentInfo.getFildetaljerListe()) {
			filDetaljer.verifyMandatoryFields();
		}
	}

}
