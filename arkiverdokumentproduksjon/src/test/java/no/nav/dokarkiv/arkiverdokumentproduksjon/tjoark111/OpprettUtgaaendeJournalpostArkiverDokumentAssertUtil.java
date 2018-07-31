package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;


import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.BREVKODE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KANAL_REF_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KRYSSREFERANSE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KRYSSREFERANSE_TYPE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.OPPRETTET_AV_NAVN;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.utils.DateUtil.getDateNow;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.Vedlegg;

import java.util.List;
import java.util.Set;


/**
 * Assert util specific for operation OpprettJournalpostArkiverDokument
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil {

	public static void assertJournalpostFields(no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost) {
		assertThat(domainJournalpost.getFagomrade(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.FAGOMRADE));
		assertThat(domainJournalpost.getUtsendingskanal(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.UTSENDINGSKANAL));
		assertThat(domainJournalpost.getOpprettetAvNavn(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.OPPRETTET_AV_NAVN));
		assertThat(domainJournalpost.getInnhold(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.INNHOLD));
		assertThat(domainJournalpost.getAvsenderMottaker(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.EKSTERNPART_NAVN));
		assertThat(domainJournalpost.getAvsenderMottakerId(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.PERSONIDENT));
		assertThat(domainJournalpost.getDokumentDato(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DATO_DOKUMENT));
		assertThat(domainJournalpost.getJournalposttype(), is(JournalpostTypeCode.U));
		assertThat(domainJournalpost.getKanalReferanseId(), is(KANAL_REF_ID));
		assertThat(domainJournalpost.getOpprettetKildeNavn(), is("itestuser"));
	}

	public static void assertEqualDokumentInfo(DokumentInfo persistedDokumentInfo, DokumentInfo newDokumentInfo) {
		assertEquals(persistedDokumentInfo.getDokumentInfoId(), newDokumentInfo.getDokumentInfoId());
		assertEquals(persistedDokumentInfo.getDokumentstatus(), newDokumentInfo.getDokumentstatus());
		assertEquals(persistedDokumentInfo.getDokumenttypeId(), newDokumentInfo.getDokumenttypeId());
		assertEquals(persistedDokumentInfo.getTittel(), newDokumentInfo.getTittel());
		assertEquals(persistedDokumentInfo.getBrevkode(), newDokumentInfo.getBrevkode());
		assertEquals(persistedDokumentInfo.getKategori(), newDokumentInfo.getKategori());
	}

	public static void assertKryssReferanse(Set<Kryssreferanse> kryssreferanser) {
		Kryssreferanse kryssreferanse = kryssreferanser.iterator().next();
		assertThat(kryssreferanse.getReferanseId(), is(KRYSSREFERANSE_ID));
		assertThat(kryssreferanse.getReferanseType(), is(KRYSSREFERANSE_TYPE));
		assertThat(kryssreferanse.getOpprettetKildeNavn(), is("itestuser"));
	}

	public static void assertDokumentinfoRelasjon(Set<JournalpostDokumentInfoRelasjon> domainDokumentInfoRelasjon) {

		domainDokumentInfoRelasjon.forEach(relasjon -> {
			assertThat(relasjon.getTilknyttetAvNavn(), is(OPPRETTET_AV_NAVN));
			assertDokumentInfo(relasjon.getDokumentInfo());
			assertThat(relasjon.getOpprettetKildeNavn(), is("itestuser"));
		});
	}

	public static void assertVedlegg(Set<JournalpostDokumentInfoRelasjon> domainDokumentInfoRelasjon, List<Vedlegg> vedleggDokumentInfo) {

		domainDokumentInfoRelasjon.forEach(relasjon -> {
			if (vedleggDokumentInfo.stream()
					.anyMatch(vedlegg -> vedlegg.getDokumentInfoId()
							.equals(String.valueOf(relasjon.getDokumentInfo().getDokumentInfoId())))) {
				assertThat(relasjon.getTilknyttetJournalpostSom(), is(VEDLEGG));
			}
		});
	}

	public static void assertDokumentInfo(DokumentInfo domainDokumentInfo) {
		assertThat(domainDokumentInfo.getDokumenttypeId(), is(DOKUMENT_TYPE_ID));
		assertThat(domainDokumentInfo.getDokumentstatus(), is(FERDIGSTILT));
		assertThat(domainDokumentInfo.getTittel(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.TITTEL));
		assertThat(domainDokumentInfo.getKategori().name(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), is(BREVKODE));
		assertThat(domainDokumentInfo.getFildetaljerListe().size(), is(1));
		assertTrue(domainDokumentInfo.getDokumentFerdigDato().toInstant().toEpochMilli() - getDateNow().toInstant()
				.toEpochMilli() < 1000);
		assertFildetaljer(domainDokumentInfo.getFildetaljerListe().iterator().next());
		assertThat(domainDokumentInfo.getOpprettetKildeNavn(), is("itestuser"));
	}

	public static void assertSaksrelasjon(Saksrelasjon saksrelasjon) {
		assertThat(saksrelasjon, is(notNullValue()));
		assertThat(saksrelasjon.getSakId(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.SAKSID));
		assertThat(saksrelasjon.getFagsystem().name(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.FAGSYSTEMKODE));
		assertThat(saksrelasjon.getOpprettetKildeNavn(), is("itestuser"));
	}

	public static void assertFildetaljer(FilDetaljer fildetaljer) {
		assertThat(fildetaljer, is(notNullValue()));
		assertThat(fildetaljer.getFiltype(), is(FilTypeCode.XML));
		assertThat(fildetaljer.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(fildetaljer.getFileContent(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DOKUMENT_INNHOLD.getBytes()));
		assertThat(fildetaljer.getFilstorrelse(), notNullValue());
		assertThat(fildetaljer.getOpprettetKildeNavn(), is("itestuser"));
	}

	public static void assertBruker(Set<Bruker> domainBrukere) {
		Bruker bruker = domainBrukere.iterator().next();
		assertThat(bruker.getBrukerId(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.PERSONIDENT));
		assertThat(bruker.getBrukerType(), is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.BRUKERTYPE));
	}

}
