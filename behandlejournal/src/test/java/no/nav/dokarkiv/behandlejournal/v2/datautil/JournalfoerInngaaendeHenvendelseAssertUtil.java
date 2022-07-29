package no.nav.dokarkiv.behandlejournal.v2.datautil;

import com.google.common.collect.Iterables;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.Journalpost;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Assert util specific for JournalfoerInngaaendeHenvendelse Journalpost
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerInngaaendeHenvendelseAssertUtil extends BehandleJournalCommonAssertUtil {

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
		assertThat(domainJournalpost.getFagomrade(), is(JournalfoerInngaaendeHenvendelseDataUtil.ARKIVTEMA));
		assertThat(domainJournalpost.getMottattDato(), is(DateProvider.getToday()));
		assertThat(domainJournalpost.getDokumentDato(), is(DateProvider.getToday()));
		assertThat(domainJournalpost.getMottakskanal(), is(JournalfoerInngaaendeHenvendelseDataUtil.KANAL));
		assertThat(domainJournalpost.getSignatur(), is(BehandleJournalCommonDataUtil.SIGNATUR));
		assertThat(domainJournalpost.getJournalForendeEnhetId(), is(JournalfoerInngaaendeHenvendelseDataUtil.JOURNALFOERENDE_ENHET_REF));
		assertThat(domainJournalpost.getJournalfortAvNavn(), is(JournalfoerInngaaendeHenvendelseDataUtil.OPPRETTET_AV_NAVN));
		assertThat(domainJournalpost.getInnhold(), is(JournalfoerInngaaendeHenvendelseDataUtil.INNHOLD));
		assertThat(domainJournalpost.getAvsenderMottaker(), is(JournalfoerInngaaendeHenvendelseDataUtil.EKSTERNPART_NAVN));
		assertThat(domainJournalpost.getAvsenderMottakerId(), is(JournalfoerInngaaendeHenvendelseDataUtil.PERSONIDENT));
	}

	private static void assertDokumentinfoRelasjon(JournalpostDokumentInfoRelasjon domainDokumentInfoRelasjon) {
		assertThat(domainDokumentInfoRelasjon.getTilknyttetJournalpostSom().name(), is(JournalfoerInngaaendeHenvendelseDataUtil.HOVEDDOKUMENT));
	}

	private static void assertDokumentInfo(DokumentInfo domainDokumentInfo, JournalfoertDokumentInfo wsDokumentInfo) {
		assertThat(domainDokumentInfo.getDokumenttypeId(), nullValue());
		assertThat(domainDokumentInfo.getInnskrenketPartsinnsyn(), is(JournalfoerInngaaendeHenvendelseDataUtil.BEGRENSET_PARTS_INNSYN));
		assertThat(domainDokumentInfo.getSensitivt(), is(JournalfoerInngaaendeHenvendelseDataUtil.SENSITIVITET));
		assertThat(domainDokumentInfo.getTittel(), is(JournalfoerInngaaendeHenvendelseDataUtil.TITTEL));
		assertThat(domainDokumentInfo.getKategori(), is(JournalfoerInngaaendeHenvendelseDataUtil.KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), is(JournalfoerInngaaendeHenvendelseDataUtil.DOKUMENT_TYPE_ID));
		assertTilleggsopplysninger(domainDokumentInfo.getTilleggsopplysninger(), wsDokumentInfo.getTilleggsopplysninger());
	}

	private static void assertKryssreferanse(Kryssreferanse kryssreferanse) {
		assertThat(kryssreferanse.getReferanseId(), is(JournalfoerInngaaendeHenvendelseDataUtil.REFERANSEID));
		assertThat(kryssreferanse.getReferanseType().name(), is(JournalfoerInngaaendeHenvendelseDataUtil.REFERANSEKODE));
	}

	private static void assertSak(Saksrelasjon saksrelasjon) {
		assertThat(saksrelasjon.getSakId(), is(JournalfoerInngaaendeHenvendelseDataUtil.SAKSID));
		assertThat(saksrelasjon.getFagsystem().name(), is(JournalfoerInngaaendeHenvendelseDataUtil.FAGSYSTEMKODE));
	}
}
