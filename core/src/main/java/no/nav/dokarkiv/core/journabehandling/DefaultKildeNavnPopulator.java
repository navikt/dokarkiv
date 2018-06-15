package no.nav.dokarkiv.core.journabehandling;


import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.Behandlingsrelasjon;
import no.nav.dokarkiv.core.domain.Bruker;
import no.nav.dokarkiv.core.domain.DokumentInfo;
import no.nav.dokarkiv.core.domain.FilDetaljer;
import no.nav.dokarkiv.core.domain.Journalpost;
import no.nav.dokarkiv.core.domain.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.Kryssreferanse;
import no.nav.dokarkiv.core.domain.ReturInfo;
import no.nav.dokarkiv.core.domain.Saksrelasjon;
import no.nav.dokarkiv.core.domain.SkannetInnhold;

import java.util.Set;

/**
 * Implementation of KildeNavnPopulator.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultKildeNavnPopulator implements KildeNavnPopulator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void populateKildeNavnForEntireJournalStructure(Journalpost journalpost, String kildeNavn) {
		populateJournalpost(journalpost, kildeNavn);
		populateSaksrelasjon(journalpost.getSaksrelasjon(), kildeNavn);
		populateBehandlingsrelasjon(journalpost.getBehandlingsrelasjon(), kildeNavn);
		populateBruker(journalpost.getBrukere(), kildeNavn);
		populateReturInfo(journalpost.getReturInfos(), kildeNavn);
		populateKryssreferanse(journalpost.getKryssreferanser(), kildeNavn);
		populateDokumentInfoRelasjon(journalpost.getJournalpostDokumentInfoRelasjoner(), kildeNavn);
	}


	private void populateJournalpost(Journalpost journalpost, String kilde) {
		populateKilde(kilde, journalpost);
	}

	private void populateBehandlingsrelasjon(Behandlingsrelasjon behandlingsrelasjon, String kilde) {
		if (behandlingsrelasjon != null) {
			populateKilde(kilde, behandlingsrelasjon);
		}
	}

	private void populateSaksrelasjon(Saksrelasjon saksrelasjon, String kilde) {
		if (saksrelasjon != null) {
			populateKilde(kilde, saksrelasjon);
		}
	}

	private void populateBruker(Set<Bruker> brukere, String kilde) {
		for (Bruker bruker : brukere) {
			populateKilde(kilde, bruker);
		}
	}

	private void populateReturInfo(Set<ReturInfo> returInfos, String kilde) {
		for (ReturInfo returInfo : returInfos) {
			populateKilde(kilde, returInfo);
		}
	}

	private void populateKryssreferanse(Set<Kryssreferanse> kryssreferanser, String kilde) {
		for (Kryssreferanse kryssreferanse : kryssreferanser) {
			populateKilde(kilde, kryssreferanse);
		}
	}

	private void populateDokumentInfoRelasjon(Set<JournalpostDokumentInfoRelasjon> dokumentInfoRelasjoner, String kilde) {
		for (JournalpostDokumentInfoRelasjon dokumentInfoRelasjon : dokumentInfoRelasjoner) {
			populateKilde(kilde, dokumentInfoRelasjon);
			populateDokumentInfo(kilde, dokumentInfoRelasjon.getDokumentInfo());
		}
	}

	private void populateDokumentInfo(String kilde, DokumentInfo dokumentInfo) {
		if (dokumentInfo != null) {
			populateKilde(kilde, dokumentInfo);
			populateSkannetInnhold(kilde, dokumentInfo.getSkannetInnholdListe());
			populateFilDetaljer(kilde, dokumentInfo.getFildetaljerListe());
		}
	}

	private void populateSkannetInnhold(String kilde, Set<SkannetInnhold> skannetInnholdListe) {
		for (SkannetInnhold skannetInnhold : skannetInnholdListe) {
			populateKilde(kilde, skannetInnhold);
		}
	}

	private void populateFilDetaljer(String kilde, Set<FilDetaljer> filDetaljerListe) {
		for (FilDetaljer filDetaljer : filDetaljerListe) {
			populateKilde(kilde, filDetaljer);
		}
	}

	private <T extends AbstractPersistentVersionedDomainObjectWithKilde> void populateKilde(String kilde, T domainObject) {
		if (domainObject.hasId()) {
			domainObject.setEndretKildeNavn(kilde);
		} else {
			domainObject.setOpprettetKildeNavn(kilde);
		}
	}

}
