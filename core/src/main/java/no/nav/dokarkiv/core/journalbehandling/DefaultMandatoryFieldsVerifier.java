package no.nav.dokarkiv.core.journalbehandling;


import no.nav.dokarkiv.core.domain.Bruker;
import no.nav.dokarkiv.core.domain.DokumentInfo;
import no.nav.dokarkiv.core.domain.FilDetaljer;
import no.nav.dokarkiv.core.domain.Journalpost;
import no.nav.dokarkiv.core.domain.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.Kryssreferanse;
import no.nav.dokarkiv.core.domain.ReturInfo;
import no.nav.dokarkiv.core.domain.SkannetInnhold;

/**
 * Implementation of <code>MandatoryFieldsVerifier</code>.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultMandatoryFieldsVerifier implements MandatoryFieldsVerifier {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void verifyFields(Journalpost journalpost) {
		journalpost.verifyMandatoryFields();

		verifySaksrelasjon(journalpost);
		verifyKryssreferanse(journalpost);
		verifyReturInfo(journalpost);
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

	private void verifyReturInfo(Journalpost journalpost) {
		for (ReturInfo returInfo : journalpost.getReturInfos()) {
			returInfo.verifyMandatoryFields();
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
