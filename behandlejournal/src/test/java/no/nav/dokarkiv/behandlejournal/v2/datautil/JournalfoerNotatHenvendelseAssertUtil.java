package no.nav.dokarkiv.behandlejournal.v2.datautil;

import com.google.common.collect.Iterables;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoernotat.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoernotat.Journalpost;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Assert util specific for JournalfoerNotatHenvendelse Journalpost
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerNotatHenvendelseAssertUtil extends BehandleJournalCommonAssertUtil {

	public static void assertEqualJournalposts(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost,
											   Journalpost fimJournalpost) throws Exception {
		DokumentInfo domainDokumentInfo = Iterables.getFirst(domainJournalpost.getJournalpostDokumentInfoRelasjoner(), null).getDokumentInfo();
		JournalfoertDokumentInfo wsDokumentInfo = Iterables.getFirst(fimJournalpost.getDokumentinfoRelasjon(), null).getJournalfoertDokument();
		FilDetaljer domainFildetaljer = Iterables.getFirst(domainDokumentInfo.getFildetaljerListe(), null);

		assertJournalpostFields(domainJournalpost);
		assertSak(domainJournalpost.getSaksrelasjon());
		assertBruker(Iterables.getFirst(domainJournalpost.getBrukere(), null), Iterables.getFirst(fimJournalpost.getForBruker(), null));
		assertKryssreferanse(Iterables.getFirst(domainJournalpost.getKryssreferanser(), null));
		assertDokumentinfoRelasjon(domainJournalpost.findHoveddokumentDokumentInfoRelasjon());
		assertDokumentInfo(domainDokumentInfo, wsDokumentInfo);
		assertFildetaljer(domainFildetaljer);
	}

	private static void assertJournalpostFields(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) {
		assertThat(domainJournalpost.getMottakskanal(), nullValue());
		assertThat(domainJournalpost.getFagomrade().name(), is(JournalfoerNotatHenvendelseDataUtil.ARKIVTEMA));
		assertThat(domainJournalpost.getDokumentDato(), is(DateProvider.getToday()));
		assertThat(domainJournalpost.getSignatur(), is(BehandleJournalCommonDataUtil.SIGNATUR));
		assertThat(domainJournalpost.getJournalForendeEnhetId(), is(JournalfoerNotatHenvendelseDataUtil.JOURNALFOERENDE_ENHET_REF));
		assertThat(domainJournalpost.getJournalfortAvNavn(), is(JournalfoerNotatHenvendelseDataUtil.OPPRETTET_AV_NAVN));
		assertThat(domainJournalpost.getInnhold(), is(JournalfoerNotatHenvendelseDataUtil.INNHOLD));
		assertThat(domainJournalpost.getMottakskanal(), nullValue());
	}

	private static void assertDokumentinfoRelasjon(JournalpostDokumentInfoRelasjon domainDokumentInfoRelasjon) {
		assertThat(domainDokumentInfoRelasjon.getTilknyttetJournalpostSom().name(), is(JournalfoerNotatHenvendelseDataUtil.HOVEDDOKUMENT));
	}

	private static void assertDokumentInfo(DokumentInfo domainDokumentInfo, JournalfoertDokumentInfo wsDokumentInfo) {
		assertThat(domainDokumentInfo.getDokumenttypeId(), nullValue());
		assertThat(domainDokumentInfo.getSensitivt(), is(JournalfoerNotatHenvendelseDataUtil.SENSITIVITET));
		assertThat(domainDokumentInfo.getInnskrenketPartsinnsyn(), is(JournalfoerNotatHenvendelseDataUtil.BEGRENSET_PARTS_INNSYN));
		assertThat(domainDokumentInfo.getTittel(), is(JournalfoerNotatHenvendelseDataUtil.TITTEL));
		assertThat(domainDokumentInfo.getKategori().name(), is(JournalfoerNotatHenvendelseDataUtil.KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), is(JournalfoerNotatHenvendelseDataUtil.DOKUMENT_TYPE_ID));
		assertThat(domainDokumentInfo.getOrganInternt(), is(JournalfoerNotatHenvendelseDataUtil.ORGANINTERNT));
		assertThat(domainDokumentInfo.getDokumentFerdigDato(), is(DateProvider.getToday()));
		assertTilleggsopplysninger(domainDokumentInfo.getTilleggsopplysninger(), wsDokumentInfo.getTilleggsopplysninger());
	}

	private static void assertKryssreferanse(Kryssreferanse kryssreferanse) {
		assertThat(kryssreferanse.getReferanseId(), is(JournalfoerNotatHenvendelseDataUtil.REFERANSEID));
		assertThat(kryssreferanse.getReferanseType().name(), is(JournalfoerNotatHenvendelseDataUtil.REFERANSEKODE));
	}

	private static void assertSak(Saksrelasjon saksrelasjon) {
		assertThat(saksrelasjon.getSakId(), is(JournalfoerNotatHenvendelseDataUtil.SAKSID));
		assertThat(saksrelasjon.getFagsystem().name(), is(JournalfoerNotatHenvendelseDataUtil.FAGSYSTEMKODE));
	}
}
