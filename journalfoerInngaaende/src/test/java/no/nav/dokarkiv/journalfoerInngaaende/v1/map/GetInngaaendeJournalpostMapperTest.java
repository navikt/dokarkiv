package no.nav.dokarkiv.journalfoerInngaaende.v1.map;

import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.AVSENDER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.BREVKODE2;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.BRUKER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.DATO_MOTTATT;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.DOKUMENTINFO_ID1;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.DOKUMENTINFO_ID2;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.DOKUMENT_TITTEL2;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.DOKUMNETTYPE_ID1;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.DOKUMNETTYPE_ID2;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.INNHOLD;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.KANALREFERANSE_ID;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.SKANNETINNHOLD_ID1;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.SKANNETINNHOLD_ID2;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.SKANNETINNHOLD_ID3;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.VEDLEGGINNHOLD1;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.VEDLEGGINNHOLD2;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.VEDLEGGINNHOLD3;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.createJournalpost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dok.tjenester.journalfoerinngaaende.ArkivSak;
import no.nav.dok.tjenester.journalfoerinngaaende.Avsender;
import no.nav.dok.tjenester.journalfoerinngaaende.Bruker;
import no.nav.dok.tjenester.journalfoerinngaaende.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.JournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.LogiskVedlegg;
import no.nav.dok.tjenester.journalfoerinngaaende.Variant;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class GetInngaaendeJournalpostMapperTest {

	private static final String JOURNALTILSTAND_ENDELIG = "ENDELIG";
	private static final String JOURNALTILSTAND_UTGAAR = "UTGAAR";
	private static final String JOURNALTILSTAND_MIDLERTIDIG = "MIDLERTIDIG";
	private static final String ARKIVSAK_SYSTEM_GSAK = "GSAK";
	private static final String ARKIVSAK_SYSTEM_PSAK = "PSAK";

	private GetInngaaendeJournalpostMapper mapper = new GetInngaaendeJournalpostMapper();

	@Test
	public void shouldMap() throws Exception {
		JournalpostResponse response = mapper.map(createJournalpost());
		assertJournalpostResponse(response);
	}

	@Test
	public void shouldMapJournaltilstandUtgaar() throws Exception {
		Journalpost journalpost = createJournalpost();
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		JournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand().value(), is(JOURNALTILSTAND_UTGAAR));
	}

	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusM() throws Exception {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		JournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand().value(), is(JOURNALTILSTAND_MIDLERTIDIG));
	}

	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusMO() throws Exception {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.MO);
		JournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand().value(), is(JOURNALTILSTAND_MIDLERTIDIG));
	}

	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusUB() throws Exception {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.UB);
		JournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand().value(), is(JOURNALTILSTAND_MIDLERTIDIG));
	}

	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusOD() throws Exception {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.OD);
		JournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand().value(), is(JOURNALTILSTAND_MIDLERTIDIG));
	}

	@Test
	public void shouldMapAvsendertyoeOrganisasjon() throws Exception {
		Journalpost journalpost = createJournalpost();
		journalpost.setAvsenderMottakerId(AVSENDER_ID_ORGANISASJON);
		JournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getAvsender().getAvsenderType().value(), is(BrukerTypeCode.ORGANISASJON.name()));
	}

	@Test
	public void shouldMapArkivsaksystemPsak() throws Exception {
		Journalpost journalpost = createJournalpost();
		journalpost.getSaksrelasjon().setFagsystem(FagsystemCode.PEN.PEN);
		JournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getArkivSak().getArkivSakSystem().value(), is(ARKIVSAK_SYSTEM_PSAK));
	}

	private void assertJournalpostResponse(JournalpostResponse response) {
		assertThat("response.journaltilstand", response.getJournalTilstand().toString(), is(JOURNALTILSTAND_ENDELIG));
		assertThat("response.tema", response.getTema(), is(FagomradeCode.FS22.name()));
		assertThat("response.tittel", response.getTittel(), is(INNHOLD));
		assertThat("response.kanalreferanseId", response.getKanalReferanseId(), is(KANALREFERANSE_ID));
//		assertThat("response.forsendelseMottatt", response.getForsendelseMottatt().toString(), is(DATO_MOTTATT)); //TODO Fix me
		assertThat("response.mottakskanal", response.getMottaksKanal(), is(MottaksKanalCode.ALTINN.name()));
		assertThat("response.journalfoerendeEnhet", response.getJournalfEnhet(), is(JOURNALFOERENDE_ENHET));

		assertBrukere(response.getBrukerListe());
		assertAvsender(response.getAvsender());
		assertArkivsak(response.getArkivSak());
		assertDokumenter(response.getDokumentListe());
	}

	private void assertBrukere(List<Bruker> brukere) {
		brukere.forEach(bruker -> {
			if (bruker.getBrukerType().value().equals(BrukerTypeCode.PERSON.name())) {
				assertThat("response.brukere.bruker1.type", bruker.getBrukerType().value(), is(BrukerTypeCode.PERSON.name()));
				assertThat("response.brukere.bruker1.brukerId", bruker.getIdentifikator(), is(BRUKER_ID_PERSON));
			} else {
				assertThat("response.brukere.bruker2.type", bruker.getBrukerType().value(), is(BrukerTypeCode.ORGANISASJON.name()));
				assertThat("response.brukere.bruker2.brukerId", bruker.getIdentifikator(), is(BRUKER_ID_ORGANISASJON));
			}
		});
	}

	private void assertAvsender(Avsender avsender) {
		assertThat("response.avsender.type", avsender.getAvsenderType().value(), is(BrukerTypeCode.PERSON.name()));
		assertThat("response.avsender.avsendeId", avsender.getIdentifikator(), is(AVSENDER_ID_PERSON));
		assertThat("response.avsender.navn", avsender.getNavn(), is(AVSENDER_NAVN));
	}

	private void assertArkivsak(ArkivSak arkivsak) {
		assertThat("response.arkivsak.arkivsakId", arkivsak.getArkivSakId(), is(SAK_ID));
		assertThat("response.brukerId", arkivsak.getArkivSakSystem().value(), is(ARKIVSAK_SYSTEM_GSAK));
	}

	private void assertDokumenter(List<Dokument> dokumentList) {
		dokumentList.stream().forEach(dokument -> {
			if (dokument.getDokumentId().equals(DOKUMENTINFO_ID1)) {
				assertDokument1(dokument);
			} else {
				assertDokument2(dokument);
			}
		});
	}

	private void assertDokument1(Dokument dokument) {
		assertThat("response.dokument1.dokumentId", dokument.getDokumentId(), is(DOKUMENTINFO_ID1));
		assertThat("response.dokument1.dokumenttypeId", dokument.getDokumentTypeId(), is(DOKUMNETTYPE_ID1));
		assertThat("response.dokument1.navSkjemaId", dokument.getNavSkjemaId(), is(BREVKODE1));
		assertThat("response.dokument1.tittel", dokument.getTittel(), is(DOKUMENT_TITTEL1));
		assertThat("response.dokument1.dokumentKategori", dokument.getDokumentKategori(), is(DokumentKategoriCode.ELEKTRONISK_DIALOG
				.name()));
		assertThat("response.dokument1.tilknyttetSom", dokument.getTilknyttetSom().value(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT
				.name()));
		assertVarianterDokument1(dokument.getVariant());
		assertLogiskVedleggDokument1(dokument.getLogiskVedleggListe());
	}

	private void assertVarianterDokument1(List<Variant> varianter) {
		varianter.stream().forEach(variant -> {
			if (variant.getArkivFilType().name().equals(FilTypeCode.XML.name())) {
				assertThat("response.dokument1.variant1.arkivfiltype1", variant.getArkivFilType().value(), is(FilTypeCode.XML.name()));
				assertThat("response.dokument1.variant1.variantformat1", variant.getVariantFormat().value(), is(VariantFormatCode.ORIGINAL
						.name()));
			} else {
				assertThat("response.dokument1.variant1.arkivfiltype2", variant.getArkivFilType().value(), is(FilTypeCode.PDFA.name()));
				assertThat("response.dokument1.variant1.variantformat2", variant.getVariantFormat().value(), is(VariantFormatCode.ARKIV
						.name()));
			}
		});
	}

	private void assertLogiskVedleggDokument1(List<LogiskVedlegg> logiskeVedlegg) {
		logiskeVedlegg.stream().forEach(logiskVedlegg -> {
			if (logiskVedlegg.getLogiskVedleggId().equals(SKANNETINNHOLD_ID1)) {
				assertThat("response.dokument1.logiskVedlegg1.arkivfiltype1", logiskVedlegg.getLogiskVedleggId(), is(SKANNETINNHOLD_ID1));
				assertThat("response.dokument1.logiskVedlegg1.variantformat1", logiskVedlegg.getLogiskVedleggTittel(), is(VEDLEGGINNHOLD1));
			} else {
				assertThat("response.dokument1.logiskVedlegg1.arkivfiltype2", logiskVedlegg.getLogiskVedleggId(), is(SKANNETINNHOLD_ID2));
				assertThat("response.dokument1.logiskVedlegg1.variantformat2", logiskVedlegg.getLogiskVedleggTittel(), is(VEDLEGGINNHOLD2));
			}
		});
	}

	private void assertDokument2(Dokument dokument) {
		assertThat("response.dokument2.dokumentId", dokument.getDokumentId(), is(DOKUMENTINFO_ID2));
		assertThat("response.dokument2.dokumenttypeId", dokument.getDokumentTypeId(), is(DOKUMNETTYPE_ID2));
		assertThat("response.dokument2.navSkjemaId", dokument.getNavSkjemaId(), is(BREVKODE2));
		assertThat("response.dokument2.tittel", dokument.getTittel(), is(DOKUMENT_TITTEL2));
		assertThat("response.dokument2.dokumentKategori", dokument.getDokumentKategori(), is(DokumentKategoriCode.FORVALTNINGSNOTAT
				.name()));
		assertThat("response.dokument2.tilknyttetSom", dokument.getTilknyttetSom().value(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT
				.name()));
		assertVarianterDokument2(dokument.getVariant());
		assertLogiskVedleggDokument2(dokument.getLogiskVedleggListe());
	}

	private void assertVarianterDokument2(List<Variant> varianter) {
		Variant variant = varianter.get(0);
		assertThat("response.dokument2.variant.arkivfiltype1", variant.getArkivFilType().value(), is(FilTypeCode.PDFA.name()));
		assertThat("response.dokument2.variant.variantformat1", variant.getVariantFormat().value(), is(VariantFormatCode.ARKIV.name()));
	}

	private void assertLogiskVedleggDokument2(List<LogiskVedlegg> logiskeVedlegg) {
		LogiskVedlegg logiskVedlegg = logiskeVedlegg.get(0);
		assertThat("response.dokument2.logiskVedlegg2.arkivfiltype", logiskVedlegg.getLogiskVedleggId(), is(SKANNETINNHOLD_ID3));
		assertThat("response.dokument2.logiskVedlegg2.variantformat", logiskVedlegg.getLogiskVedleggTittel(), is(VEDLEGGINNHOLD3));
	}
}