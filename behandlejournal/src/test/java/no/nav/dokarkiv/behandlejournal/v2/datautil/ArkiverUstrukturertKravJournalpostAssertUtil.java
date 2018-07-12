package no.nav.dokarkiv.behandlejournal.v2.datautil;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost;

/**
 * Assert util specific for ArkiverUstrukturertKrav Journalpost
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class ArkiverUstrukturertKravJournalpostAssertUtil extends BehandleJournalCommonAssertUtil {

	public static void assertEqualJournalposts(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost,
											   Journalpost fimJournalpost) throws Exception {
		assertJournalpostFields(domainJournalpost);
		assertBruker(domainJournalpost.getBrukere().iterator().next(), fimJournalpost.getForBruker().get(0));

		DokumentInfo domainDokumentInfo = domainJournalpost.getJournalpostDokumentInfoRelasjoner().iterator().next()
				.getDokumentInfo();
		JournalfoertDokumentInfo wsDokumentInfo = fimJournalpost.getJournalfoertDokument();
		assertDokumentInfo(domainDokumentInfo, wsDokumentInfo);
		assertThat(domainDokumentInfo.getDokumenttypeId(), is(nullValue()));
		assertThat(domainDokumentInfo.getBrevkode(), is(ArkiverUstrukturertKravJournalpostDataUtil.DOKUMENT_TYPE_ID));

		FilDetaljer domainFildetaljer = domainDokumentInfo.getFildetaljerListe().iterator().next();
		assertFildetaljer(domainFildetaljer);
	}

	protected static void assertJournalpostFields(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) {
		assertThat(domainJournalpost.getMottakskanal().name(), is(ArkiverUstrukturertKravJournalpostDataUtil.KANAL));
		assertThat(domainJournalpost.getFagomrade().name(), is(ArkiverUstrukturertKravJournalpostDataUtil.ARKIVTEMA));
		assertThat(domainJournalpost.getJournalForendeEnhetId(), is(ArkiverUstrukturertKravJournalpostDataUtil.JOURNALFOERENDE_ENHET_REF));
		assertThat(domainJournalpost.getDokumentDato(), is(DateProvider.getToday()));
		assertThat(domainJournalpost.getMottattDato(), is(DateProvider.getToday()));
		assertThat(domainJournalpost.getSignatur(), is(BehandleJournalCommonDataUtil.SIGNATUR));
	}

	protected static void assertDokumentInfo(DokumentInfo domainDokumentInfo, JournalfoertDokumentInfo wsDokumentInfo) {
		assertThat(domainDokumentInfo.getInnskrenketPartsinnsyn(),
				is(ArkiverUstrukturertKravJournalpostDataUtil.BEGRENSET_PARTS_INNSYN));
		assertTilleggsopplysninger(domainDokumentInfo.getTilleggsopplysninger(),
				wsDokumentInfo.getTilleggsopplysninger());
	}
}
