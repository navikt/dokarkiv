package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;


import static no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants.FILREFERANSE_ID_KEY;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.BESTILLINGS_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterDataUtil.FILREFERANSE_S3;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;

import java.util.Map;
import java.util.Set;


/**
 * Assert util specific for operation OpprettJournalpostArkiverDokument
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostArkiverDokumenterAssertUtil {

	public static void assertEqualJournalposts(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) throws Exception {
		assertJournalpostFields(domainJournalpost);
		assertSak(domainJournalpost.getSaksrelasjon());
		assertBruker(domainJournalpost.getBrukere());
		JournalpostDokumentInfoRelasjon hoveddokument = domainJournalpost.findHoveddokumentDokumentInfoRelasjon();
		assertHovedDokumentInfo(hoveddokument.getDokumentInfo());
		domainJournalpost.getJournalpostDokumentInfoRelasjoner().stream()
				.filter(jpr -> jpr.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.VEDLEGG)
				.forEach(jpr -> assertVedleggDokumentInfo(jpr.getDokumentInfo()));
	}

	private static void assertJournalpostFields(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) {
		assertThat(domainJournalpost.getFagomrade(), is(OpprettJournalpostArkiverDokumenterDataUtil.FAGOMRADE));
		assertThat(
				domainJournalpost.getJournalForendeEnhetId(),
				is(OpprettJournalpostArkiverDokumenterDataUtil.JOURNALFOERENDE_ENHET_REF));
		assertThat(domainJournalpost.getOpprettetAvNavn(), is(OpprettJournalpostArkiverDokumenterDataUtil.OPPRETTET_AV_NAVN));
		assertThat(domainJournalpost.getInnhold(), is(OpprettJournalpostArkiverDokumenterDataUtil.INNHOLD));
		assertThat(domainJournalpost.getAvsenderMottaker(), is(OpprettJournalpostArkiverDokumenterDataUtil.EKSTERNPART_NAVN));
		assertThat(domainJournalpost.getAvsenderMottakerId(), is(OpprettJournalpostArkiverDokumenterDataUtil.PERSONIDENT));
		assertThat(domainJournalpost.getLand(), is(OpprettJournalpostArkiverDokumenterDataUtil.LAND));
		assertThat(domainJournalpost.getDokumentDato(), is(OpprettJournalpostArkiverDokumenterDataUtil.DATO_DOKUMENT));
		assertThat(domainJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
	}

	private static void assertHovedDokumentInfo(DokumentInfo domainDokumentInfo) {
		assertDokumentInfo(domainDokumentInfo);
		assertHoveddokumentTilleggsopplysninger(domainDokumentInfo.getTilleggsopplysninger());
	}

	private static void assertVedleggDokumentInfo(DokumentInfo domainDokumentInfo) {
		assertDokumentInfo(domainDokumentInfo);
		assertTilleggsopplysninger(domainDokumentInfo.getTilleggsopplysninger());
	}

	private static void assertDokumentInfo(DokumentInfo domainDokumentInfo) {
		assertThat(domainDokumentInfo.getDokumenttypeId(), is("dokumentTypeId"));
		assertThat(domainDokumentInfo.getSensitivt(), is(OpprettJournalpostArkiverDokumenterDataUtil.SENSITIVITET));
		assertThat(domainDokumentInfo.getTittel(), is(OpprettJournalpostArkiverDokumenterDataUtil.TITTEL));
		assertThat(domainDokumentInfo.getKategori().name(), is(OpprettJournalpostArkiverDokumenterDataUtil.KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), is("brevkode"));
		assertThat(domainDokumentInfo.getFildetaljerListe().size(), is(2));
		FilDetaljer arkiv = domainDokumentInfo.getFildetaljerListe().stream().filter(fd -> fd.getVariantFormat() == VariantFormatCode.ARKIV).findFirst().get();
		assertFildetaljer(arkiv, FilTypeCode.PDFA, VariantFormatCode.ARKIV);
		FilDetaljer produksjon = domainDokumentInfo.getFildetaljerListe().stream().filter(fd -> fd.getVariantFormat() == VariantFormatCode.PRODUKSJON).findFirst().get();
		assertFildetaljer(produksjon, FilTypeCode.AXML, VariantFormatCode.PRODUKSJON);
	}

	private static void assertSak(Saksrelasjon saksrelasjon) {
		assertThat(saksrelasjon, is(notNullValue()));
		assertThat(saksrelasjon.getSakId(), is(OpprettJournalpostArkiverDokumenterDataUtil.SAKSID));
		assertThat(saksrelasjon.getFagsystem().name(), is(OpprettJournalpostArkiverDokumenterDataUtil.FAGSYSTEMKODE));
	}

	private static void assertFildetaljer(FilDetaljer fildetaljer, FilTypeCode filTypeCode, VariantFormatCode variantFormatCode) {
		assertThat(fildetaljer, is(notNullValue()));
		assertThat(fildetaljer.getFiltype(), is(filTypeCode));
		assertThat(fildetaljer.getVariantFormat(), is(variantFormatCode));
		assertThat(fildetaljer.getFileContent(), is(OpprettJournalpostArkiverDokumenterDataUtil.DOKUMENT_INNHOLD.getBytes()));
	}

	private static void assertHoveddokumentTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		assertThat(tilleggsopplysninger, hasEntry(ArkiverDokumentproduksjonConstants.BESTILLINGS_ID_KEY, BESTILLINGS_ID));
		assertTilleggsopplysninger(tilleggsopplysninger);
	}

	private static void assertTilleggsopplysninger(Map<String, String> tilleggsopplysninger) {
		assertThat(tilleggsopplysninger, hasEntry(FILREFERANSE_ID_KEY, FILREFERANSE_S3));
	}

	private static void assertBruker(Set<Bruker> domainBrukere) {
		Bruker bruker = domainBrukere.iterator().next();
		assertThat(bruker.getBrukerId(), is(OpprettJournalpostArkiverDokumenterDataUtil.PERSONIDENT));
		assertThat(bruker.getBrukerType(), is(OpprettJournalpostArkiverDokumenterDataUtil.BRUKERTYPE));
	}
}
