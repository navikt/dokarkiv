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
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.ArkivsakTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.AvsenderTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.BrukerTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.DokumentTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.JournalpostResponseTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.LogiskVedleggTo;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.VariantTo;
import org.junit.Test;

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
	public void shouldMap() {
		JournalpostResponseTo response = mapper.map(createJournalpost());
		assertJournalpostResponseTo(response);
	}

	@Test
	public void shouldMapJournaltilstandUtgaar() {
		Journalpost journalpost = createJournalpost();
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		JournalpostResponseTo response = mapper.map(journalpost);
		assertThat(response.getJournaltilstand(), is(JOURNALTILSTAND_UTGAAR));
	}

	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusM() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		JournalpostResponseTo response = mapper.map(journalpost);
		assertThat(response.getJournaltilstand(), is(JOURNALTILSTAND_MIDLERTIDIG));
	}

	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusMO() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.MO);
		JournalpostResponseTo response = mapper.map(journalpost);
		assertThat(response.getJournaltilstand(), is(JOURNALTILSTAND_MIDLERTIDIG));
	}

	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusUB() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.UB);
		JournalpostResponseTo response = mapper.map(journalpost);
		assertThat(response.getJournaltilstand(), is(JOURNALTILSTAND_MIDLERTIDIG));
	}

	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusOD() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.OD);
		JournalpostResponseTo response = mapper.map(journalpost);
		assertThat(response.getJournaltilstand(), is(JOURNALTILSTAND_MIDLERTIDIG));
	}

	@Test
	public void shouldMapAvsendertyoeOrganisasjon() {
		Journalpost journalpost = createJournalpost();
		journalpost.setAvsenderMottakerId(AVSENDER_ID_ORGANISASJON);
		JournalpostResponseTo response = mapper.map(journalpost);
		assertThat(response.getAvsender().getType(), is(BrukerTypeCode.ORGANISASJON.name()));
	}

	@Test
	public void shouldMapArkivsaksystemPsak() {
		Journalpost journalpost = createJournalpost();
		journalpost.getSaksrelasjon().setFagsystem(FagsystemCode.PEN.PEN);
		JournalpostResponseTo response = mapper.map(journalpost);
		assertThat(response.getArkivsak().getArkivsaksystem(), is(ARKIVSAK_SYSTEM_PSAK));
	}

	private void assertJournalpostResponseTo(JournalpostResponseTo response) {
		assertThat("response.journaltilstand", response.getJournaltilstand(), is(JOURNALTILSTAND_ENDELIG));
		assertThat("response.tema", response.getTema(), is(FagomradeCode.FS22.name()));
		assertThat("response.tittel", response.getTittel(), is(INNHOLD));
		assertThat("response.kanalreferanseId", response.getKanalreferanseId(), is(KANALREFERANSE_ID));
		assertThat("response.forsendelseMottatt", response.getForsendelseMottatt().toString(), is(DATO_MOTTATT));
		assertThat("response.mottakskanal", response.getMottakskanal(), is(MottaksKanalCode.ALTINN.name()));
		assertThat("response.journalfoerendeEnhet", response.getJournalfoerendeEnhet(), is(JOURNALFOERENDE_ENHET));

		assertBrukere(response.getBrukere());
		assertAvsender(response.getAvsender());
		assertArkivsak(response.getArkivsak());
		assertDokumenter(response.getDokumenter());
	}

	private void assertBrukere(List<BrukerTo> brukere) {
		brukere.stream().forEach(bruker -> {
			if (bruker.getType().equals(BrukerTypeCode.PERSON.name())) {
				assertThat("response.brukere.bruker1.type", bruker.getType(), is(BrukerTypeCode.PERSON.name()));
				assertThat("response.brukere.bruker1.brukerId", bruker.getIdentifikator(), is(BRUKER_ID_PERSON));
			} else {
				assertThat("response.brukere.bruker2.type", bruker.getType(), is(BrukerTypeCode.ORGANISASJON.name()));
				assertThat("response.brukere.bruker2.brukerId", bruker.getIdentifikator(), is(BRUKER_ID_ORGANISASJON));
			}
		});
	}

	private void assertAvsender(AvsenderTo avsender) {
		assertThat("response.avsender.type", avsender.getType(), is(BrukerTypeCode.PERSON.name()));
		assertThat("response.avsender.avsendeId", avsender.getIdentifikator(), is(AVSENDER_ID_PERSON));
		assertThat("response.avsender.navn", avsender.getNavn(), is(AVSENDER_NAVN));
	}

	private void assertArkivsak(ArkivsakTo arkivsak) {
		assertThat("response.arkivsak.arkivsakId", arkivsak.getArkivsakId(), is(SAK_ID));
		assertThat("response.brukerId", arkivsak.getArkivsaksystem(), is(ARKIVSAK_SYSTEM_GSAK));
	}

	private void assertDokumenter(List<DokumentTo> dokumentList) {
		dokumentList.stream().forEach(dokument -> {
			if (dokument.getDokumentId().equals(DOKUMENTINFO_ID1)) {
				assertDokument1(dokument);
			} else {
				assertDokument2(dokument);
			}
		});
	}

	private void assertDokument1(DokumentTo dokument) {
		assertThat("response.dokument1.dokumentId", dokument.getDokumentId(), is(DOKUMENTINFO_ID1));
		assertThat("response.dokument1.dokumenttypeId", dokument.getDokumenttypeId(), is(DOKUMNETTYPE_ID1));
		assertThat("response.dokument1.navSkjemaId", dokument.getNavSkjemaId(), is(BREVKODE1));
		assertThat("response.dokument1.tittel", dokument.getTittel(), is(DOKUMENT_TITTEL1));
		assertThat("response.dokument1.dokumentKategori", dokument.getDokumentkategori(), is(DokumentKategoriCode.ELEKTRONISK_DIALOG
				.name()));
		assertThat("response.dokument1.tilknyttetSom", dokument.getTilknyttetSom(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT
				.name()));
		assertVarianterDokument1(dokument.getVarianter());
		assertLogiskVedleggDokument1(dokument.getLogiskeVedlegg());
	}

	private void assertVarianterDokument1(List<VariantTo> varianter) {
		varianter.stream().forEach(variant -> {
			if (variant.getArkivfiltype().equals(FilTypeCode.PDF.name())) {
				assertThat("response.dokument1.variant1.arkivfiltype1", variant.getArkivfiltype(), is(FilTypeCode.PDF.name()));
				assertThat("response.dokument1.variant1.variantformat1", variant.getVariantformat(), is(VariantFormatCode.ORIGINAL
						.name()));
			} else {
				assertThat("response.dokument1.variant1.arkivfiltype2", variant.getArkivfiltype(), is(FilTypeCode.PDFA.name()));
				assertThat("response.dokument1.variant1.variantformat2", variant.getVariantformat(), is(VariantFormatCode.FULLVERSJON
						.name()));
			}
		});
	}

	private void assertLogiskVedleggDokument1(List<LogiskVedleggTo> logiskeVedlegg) {
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

	private void assertDokument2(DokumentTo dokument) {
		assertThat("response.dokument2.dokumentId", dokument.getDokumentId(), is(DOKUMENTINFO_ID2));
		assertThat("response.dokument2.dokumenttypeId", dokument.getDokumenttypeId(), is(DOKUMNETTYPE_ID2));
		assertThat("response.dokument2.navSkjemaId", dokument.getNavSkjemaId(), is(BREVKODE2));
		assertThat("response.dokument2.tittel", dokument.getTittel(), is(DOKUMENT_TITTEL2));
		assertThat("response.dokument2.dokumentKategori", dokument.getDokumentkategori(), is(DokumentKategoriCode.FORVALTNINGSNOTAT
				.name()));
		assertThat("response.dokument2.tilknyttetSom", dokument.getTilknyttetSom(), is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT
				.name()));
		assertVarianterDokument2(dokument.getVarianter());
		assertLogiskVedleggDokument2(dokument.getLogiskeVedlegg());
	}

	private void assertVarianterDokument2(List<VariantTo> varianter) {
		VariantTo variant = varianter.get(0);
		assertThat("response.dokument2.variant.arkivfiltype1", variant.getArkivfiltype(), is(FilTypeCode.AXML.name()));
		assertThat("response.dokument2.variant.variantformat1", variant.getVariantformat(), is(VariantFormatCode.ARKIV.name()));
	}

	private void assertLogiskVedleggDokument2(List<LogiskVedleggTo> logiskeVedlegg) {
		LogiskVedleggTo logiskVedlegg = logiskeVedlegg.get(0);
		assertThat("response.dokument2.logiskVedlegg2.arkivfiltype", logiskVedlegg.getLogiskVedleggId(), is(SKANNETINNHOLD_ID3));
		assertThat("response.dokument2.logiskVedlegg2.variantformat", logiskVedlegg.getLogiskVedleggTittel(), is(VEDLEGGINNHOLD3));
	}
}