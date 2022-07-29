package no.nav.dokarkiv.behandlejournal.v2.datautil;

import com.google.common.collect.Iterables;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerutgaaendehenvendelse.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerutgaaendehenvendelse.Journalpost;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Assert util specific for JournalfoerUtgaaendeHenvendelse Journalpost
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerUtgaaendeHenvendelseAssertUtil extends BehandleJournalCommonAssertUtil {

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
		assertThat(domainJournalpost.getFagomrade(), is(JournalfoerUtgaaendeHenvendelseDataUtil.ARKIVTEMA));
		assertThat(domainJournalpost.getDokumentDato(), is(DateProvider.getToday()));
		assertThat(domainJournalpost.getSignatur(), is(BehandleJournalCommonDataUtil.SIGNATUR));
		assertThat(domainJournalpost.getJournalForendeEnhetId(), is(JournalfoerUtgaaendeHenvendelseDataUtil.JOURNALFOERENDE_ENHET_REF));
		assertThat(domainJournalpost.getJournalfortAvNavn(), is(JournalfoerUtgaaendeHenvendelseDataUtil.OPPRETTET_AV_NAVN));
		assertThat(domainJournalpost.getInnhold(), is(JournalfoerUtgaaendeHenvendelseDataUtil.INNHOLD));
		assertThat(domainJournalpost.getUtsendingskanal(), is(JournalfoerUtgaaendeHenvendelseDataUtil.KANAL));
		assertThat(domainJournalpost.getSendtPrintDato(), is(DateProvider.getToday()));
		assertThat(domainJournalpost.getAvsenderMottaker(), is(JournalfoerInngaaendeHenvendelseDataUtil.EKSTERNPART_NAVN));
		assertThat(domainJournalpost.getAvsenderMottakerId(), is(JournalfoerInngaaendeHenvendelseDataUtil.PERSONIDENT));
		assertThat(domainJournalpost.getEkspedertDato(), is(DateProvider.getToday()));
	}

	private static void assertDokumentinfoRelasjon(JournalpostDokumentInfoRelasjon domainDokumentInfoRelasjon) {
		assertThat(domainDokumentInfoRelasjon.getTilknyttetJournalpostSom().name(), is(JournalfoerUtgaaendeHenvendelseDataUtil.HOVEDDOKUMENT));
	}

	private static void assertDokumentInfo(DokumentInfo domainDokumentInfo, JournalfoertDokumentInfo wsDokumentInfo) {
		assertThat(domainDokumentInfo.getSensitivt(), is(JournalfoerUtgaaendeHenvendelseDataUtil.SENSITIVITET));
		assertThat(domainDokumentInfo.getInnskrenketPartsinnsyn(), is(JournalfoerUtgaaendeHenvendelseDataUtil.BEGRENSET_PARTS_INNSYN));
		assertThat(domainDokumentInfo.getTittel(), is(JournalfoerUtgaaendeHenvendelseDataUtil.TITTEL));
		assertThat(domainDokumentInfo.getKategori(), is(JournalfoerUtgaaendeHenvendelseDataUtil.KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), is(JournalfoerUtgaaendeHenvendelseDataUtil.DOKUMENT_TYPE_ID));
		assertTilleggsopplysninger(domainDokumentInfo.getTilleggsopplysninger(), wsDokumentInfo.getTilleggsopplysninger());
	}

	private static void assertKryssreferanse(Kryssreferanse kryssreferanse) {
		assertThat(kryssreferanse.getReferanseId(), is(JournalfoerUtgaaendeHenvendelseDataUtil.REFERANSEID));
		assertThat(kryssreferanse.getReferanseType().name(), is(JournalfoerUtgaaendeHenvendelseDataUtil.REFERANSEKODE));
	}

	private static void assertSak(Saksrelasjon saksrelasjon) {
		assertThat(saksrelasjon.getSakId(), is(JournalfoerUtgaaendeHenvendelseDataUtil.SAKSID));
		assertThat(saksrelasjon.getFagsystem().name(), is(JournalfoerUtgaaendeHenvendelseDataUtil.FAGSYSTEMKODE));
	}
}
