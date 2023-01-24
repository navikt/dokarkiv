package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;


import no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

import java.util.Map;
import java.util.Set;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentDataUtil.BESTILLINGS_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentDataUtil.TILLEGGSOPPLYSNING_KEY_2;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentDataUtil.TILLEGGSOPPLYSNING_VALUE_2;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;


/**
 * Assert util specific for operation OpprettJournalpostArkiverDokument
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostArkiverDokumentAssertUtil {

	public static void assertEqualJournalposts(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) throws Exception {
		DokumentInfo domainDokumentInfo = domainJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo();
		assertJournalpostFields(domainJournalpost);
		assertSak(domainJournalpost.getSaksrelasjon());
		assertBruker(domainJournalpost.getBrukere());
		assertDokumentinfoRelasjon(domainJournalpost.findHoveddokumentDokumentInfoRelasjon());
		assertDokumentInfo(domainDokumentInfo);
	}

	private static void assertJournalpostFields(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) {
		assertThat(domainJournalpost.getFagomrade(), is(OpprettJournalpostArkiverDokumentDataUtil.FAGOMRADE));
		assertThat(domainJournalpost.getUtsendingskanal(), is(OpprettJournalpostArkiverDokumentDataUtil.UTSENDINGSKANAL));
		assertThat(
				domainJournalpost.getJournalForendeEnhetId(),
				is(OpprettJournalpostArkiverDokumentDataUtil.JOURNALFOERENDE_ENHET_REF));
		assertThat(domainJournalpost.getOpprettetAvNavn(), is(OpprettJournalpostArkiverDokumentDataUtil.OPPRETTET_AV_NAVN));
		assertThat(domainJournalpost.getInnhold(), is(OpprettJournalpostArkiverDokumentDataUtil.INNHOLD));
		assertThat(domainJournalpost.getAvsenderMottaker(), is(OpprettJournalpostArkiverDokumentDataUtil.EKSTERNPART_NAVN));
		assertThat(domainJournalpost.getAvsenderMottakerId(), is(OpprettJournalpostArkiverDokumentDataUtil.PERSONIDENT));
		assertThat(domainJournalpost.getLand(), is(OpprettJournalpostArkiverDokumentDataUtil.LAND));
		assertThat(domainJournalpost.getDokumentDato(), is(OpprettJournalpostArkiverDokumentDataUtil.DATO_DOKUMENT));
		assertThat(domainJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}

	private static void assertDokumentinfoRelasjon(JournalpostDokumentInfoRelasjon domainDokumentInfoRelasjon) {
		assertThat(domainDokumentInfoRelasjon.getTilknyttetJournalpostSom()
						.name(),
				is(OpprettJournalpostArkiverDokumentDataUtil.HOVEDDOKUMENT));
	}

	private static void assertDokumentInfo(DokumentInfo domainDokumentInfo) {
		assertThat(domainDokumentInfo.getDokumenttypeId(), is("dokumentTypeId"));
		assertThat(domainDokumentInfo.getSensitivt(), is(OpprettJournalpostArkiverDokumentDataUtil.SENSITIVITET));
		assertThat(domainDokumentInfo.getTittel(), is(OpprettJournalpostArkiverDokumentDataUtil.TITTEL));
		assertThat(domainDokumentInfo.getKategori().name(), is(OpprettJournalpostArkiverDokumentDataUtil.KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), is("brevkode"));
		assertThat(domainDokumentInfo.getFildetaljerListe().size(), is(1));
		assertFildetaljer(domainDokumentInfo.getFildetaljerListe().iterator().next());
		assertTilleggsopplysninger(domainDokumentInfo.getTilleggsopplysninger());
	}

	private static void assertSak(Saksrelasjon saksrelasjon) {
		assertThat(saksrelasjon, is(notNullValue()));
		assertThat(saksrelasjon.getSaknrfk(), is(OpprettJournalpostArkiverDokumentDataUtil.SAKSID));
		assertThat(saksrelasjon.getFagsystem().name(), is(OpprettJournalpostArkiverDokumentDataUtil.FAGSYSTEMKODE));
	}

	private static void assertFildetaljer(FilDetaljer fildetaljer) {
		assertThat(fildetaljer, is(notNullValue()));
		assertThat(fildetaljer.getFiltype(), is(FilTypeCode.XML));
		assertThat(fildetaljer.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(fildetaljer.getFileContent(), is(OpprettJournalpostArkiverDokumentDataUtil.DOKUMENT_INNHOLD.getBytes()));
	}

	private static void assertTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		assertThat(tilleggsopplysninger, hasEntry(ArkiverDokumentproduksjonConstants.BESTILLINGS_ID_KEY, BESTILLINGS_ID));
		assertThat(tilleggsopplysninger, hasEntry(TILLEGGSOPPLYSNING_KEY_2, TILLEGGSOPPLYSNING_VALUE_2));
	}

	private static void assertBruker(Set<Bruker> domainBrukere) {
		Bruker bruker = domainBrukere.iterator().next();
		assertThat(bruker.getBrukerId(), is(OpprettJournalpostArkiverDokumentDataUtil.PERSONIDENT));
		assertThat(bruker.getBrukerType(), is(OpprettJournalpostArkiverDokumentDataUtil.BRUKERTYPE));
	}
}
