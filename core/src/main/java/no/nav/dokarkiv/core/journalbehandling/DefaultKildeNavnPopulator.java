package no.nav.dokarkiv.core.journalbehandling;


import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.entities.Behandlingsrelasjon;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.ReturInfo;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;


import java.util.Set;

/**
 * Implementation of KildeNavnPopulator.
 *
 * @author Thomas Eugen Bj�rge, Visma Sirius
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
