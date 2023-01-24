package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import com.google.common.collect.Iterables;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

import java.util.Map;
import java.util.Set;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.BREVKODE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.BRUKERTYPE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.EKSTERNPART_NAVN;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.FAGOMRADE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.FAGSYSTEMKODE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.HOVEDDOKUMENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.INNHOLD;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.JOURNALFOERENDE_ENHET_REF;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.KATEGORI;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.LAND;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.METAFORCE_INSTANCE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.OPPRETTET_AV_NAVN;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.PERSONIDENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.SAKSID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.SENSITIVITET;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.TILLEGGSOPPLYSNING_KEY_1;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.TILLEGGSOPPLYSNING_KEY_2;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.TILLEGGSOPPLYSNING_VALUE_1;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.TILLEGGSOPPLYSNING_VALUE_2;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostDataUtil.TITTEL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

/**
 * Assert util specific for OpprettJournalpost in arkiverDokumentproduksjon
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostAssertUtil {

	public static void assertEqualJournalposts(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) throws Exception {
		DokumentInfo domainDokumentInfo = Iterables.getFirst(
				domainJournalpost.getJournalpostDokumentInfoRelasjoner(), null)
				.getDokumentInfo();
		assertJournalpostFields(domainJournalpost);
		assertSak(domainJournalpost.getSaksrelasjon());
		assertBruker(domainJournalpost.getBrukere());
		assertDokumentinfoRelasjon(domainJournalpost.findHoveddokumentDokumentInfoRelasjon());
		assertDokumentInfo(domainDokumentInfo);
	}

	private static void assertJournalpostFields(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) {
		assertThat(domainJournalpost.getFagomrade(), is(FAGOMRADE));
		assertThat(domainJournalpost.getJournalForendeEnhetId(), is(JOURNALFOERENDE_ENHET_REF));
		assertThat(domainJournalpost.getOpprettetAvNavn(), is(OPPRETTET_AV_NAVN));
		assertThat(domainJournalpost.getInnhold(), is(INNHOLD));
		assertThat(domainJournalpost.getAvsenderMottaker(), is(EKSTERNPART_NAVN));
		assertThat(domainJournalpost.getAvsenderMottakerId(), is(PERSONIDENT));
		assertThat(domainJournalpost.getLand(), is(LAND));
	}

	private static void assertDokumentinfoRelasjon(JournalpostDokumentInfoRelasjon domainDokumentInfoRelasjon) {
		assertThat(domainDokumentInfoRelasjon.getTilknyttetJournalpostSom().name(), is(HOVEDDOKUMENT));
	}

	private static void assertDokumentInfo(DokumentInfo domainDokumentInfo) {
		assertThat(domainDokumentInfo.getDokumenttypeId(), is(DOKUMENT_TYPE_ID));
		assertThat(domainDokumentInfo.getSensitivt(), is(SENSITIVITET));
		assertThat(domainDokumentInfo.getTittel(), is(TITTEL));
		assertThat(domainDokumentInfo.getKategori().name(), is(KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), is(BREVKODE));
		assertThat(domainDokumentInfo.getFildetaljerListe().size(), is(1));
		assertFildetaljer(domainDokumentInfo.getFildetaljerListe().iterator().next());
		assertTilleggsopplysninger(domainDokumentInfo.getTilleggsopplysninger());
	}

	private static void assertSak(Saksrelasjon saksrelasjon) {
		assertThat(saksrelasjon, is(notNullValue()));
		assertThat(saksrelasjon.getSaknrfk(), is(SAKSID));
		assertThat(saksrelasjon.getFagsystem().name(), is(FAGSYSTEMKODE));
	}

	private static void assertFildetaljer(FilDetaljer fildetaljer) {
		assertThat(fildetaljer, is(notNullValue()));
		assertThat(fildetaljer.getFiltype(), is(FilTypeCode.XML));
		assertThat(fildetaljer.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(fildetaljer.getMetaforceInstanceId(), is(METAFORCE_INSTANCE_ID));
	}

	private static void assertTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		assertThat(tilleggsopplysninger, hasEntry(TILLEGGSOPPLYSNING_KEY_1, TILLEGGSOPPLYSNING_VALUE_1));
		assertThat(tilleggsopplysninger, hasEntry(TILLEGGSOPPLYSNING_KEY_2, TILLEGGSOPPLYSNING_VALUE_2));
	}

	private static void assertBruker(Set<Bruker> domainBrukere) {
		Bruker bruker = domainBrukere.iterator().next();
		assertThat(bruker.getBrukerId(), is(PERSONIDENT));
		assertThat(bruker.getBrukerType(), is(BRUKERTYPE));
	}
}
