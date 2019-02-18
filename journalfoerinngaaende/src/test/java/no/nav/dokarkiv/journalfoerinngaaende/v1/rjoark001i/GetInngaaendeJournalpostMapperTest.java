package no.nav.dokarkiv.journalfoerinngaaende.v1.rjoark001i;

import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.AVSENDER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.BREVKODE2;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.BRUKER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMENTINFO_ID1;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMENTINFO_ID2;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMENT_TITTEL2;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMNETTYPE_ID1;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.DOKUMNETTYPE_ID2;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.INNHOLD;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.KANALREFERANSE_ID;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.LOCAL_DATE_TIME;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.SKANNETINNHOLD_ID1;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.SKANNETINNHOLD_ID2;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.SKANNETINNHOLD_ID3;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.VEDLEGGINNHOLD1;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.VEDLEGGINNHOLD2;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.VEDLEGGINNHOLD3;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.createJournalpost;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.createJournalpostKassert;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import no.nav.dok.tjenester.journalfoerinngaaende.ArkivSakNoArkivsakSystemEnum;
import no.nav.dok.tjenester.journalfoerinngaaende.Avsender;
import no.nav.dok.tjenester.journalfoerinngaaende.Bruker;
import no.nav.dok.tjenester.journalfoerinngaaende.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dok.tjenester.journalfoerinngaaende.LogiskVedlegg;
import no.nav.dok.tjenester.journalfoerinngaaende.Variant;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetInngaaendeJournalpostMapperTest {

	private static final String JOURNALTILSTAND_ENDELIG = "ENDELIG";
	private static final String ARKIVSAK_SYSTEM_GSAK = "GSAK";
	private static final String ARKIVSAK_SYSTEM_PSAK = "PSAK";

	private GetInngaaendeJournalpostMapper mapper = new GetInngaaendeJournalpostMapper();

	/**
	 * HVIS Journalstatus i JOARK = ("J") OG Saksrelasjon.Feilregistrert <> "1" SÅ sett JournalTilstand = "ENDELIG" som output
	 **/
	@Test
	public void shouldMap() {
		GetJournalpostResponse response = mapper.map(createJournalpost());
		assertJournalpostResponse(response);
	}

	/**
	 * HVIS Saksrelasjon.Feilregistrert = "1", SÅ sett JournalTilstand = UTGAAR returneres som output
	 **/
	@Test
	public void shouldMapJournaltilstandUtgaarFeilregistrertTrue() {
		Journalpost journalpost = createJournalpost();
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.UTGAAR));
	}

	/**
	 * HVIS Journalstatus i JOARK= ("U"), OG Saksrelasjon.Feilregistrert <> "1" SÅ sett JournalTilstand = "UTGAAR" som output
	 **/
	@Test
	public void shouldMapJournaltilstandUtgaarJournalstatusU() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.U);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.UTGAAR));
	}

	/**
	 * HVIS Journalstatus i JOARK = ("M" eller "MO" eller "UB" eller "OD") OG Saksrelasjon.Feilregistrert <> "1" SÅ sett JournalTilstand = "MIDLERTIDIG" som output
	 **/
	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusM() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.M);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.MIDLERTIDIG));
	}

	/**
	 * HVIS Journalstatus i JOARK = ("M" eller "MO" eller "UB" eller "OD") OG Saksrelasjon.Feilregistrert <> "1" SÅ sett JournalTilstand = "MIDLERTIDIG" som output
	 **/
	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusMO() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.MO);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.MIDLERTIDIG));
	}

	/**
	 * HVIS Journalstatus i JOARK = ("M" eller "MO" eller "UB" eller "OD") OG Saksrelasjon.Feilregistrert <> "1" SÅ sett JournalTilstand = "MIDLERTIDIG" som output
	 **/
	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusUB() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.UB);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.MIDLERTIDIG));
	}

	/**
	 * HVIS Journalstatus i JOARK = ("M" eller "MO" eller "UB" eller "OD") OG Saksrelasjon.Feilregistrert <> "1" SÅ sett JournalTilstand = "MIDLERTIDIG" som output
	 **/
	@Test
	public void shouldMapJournaltilstandMidlertidigJournalstatusOD() {
		Journalpost journalpost = createJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.OD);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getJournalTilstand(), is(GetJournalpostResponse.JournalTilstand.MIDLERTIDIG));
	}

	@Test
	public void shouldMapAvsendertyoeOrganisasjon() {
		Journalpost journalpost = createJournalpost();
		journalpost.setAvsenderMottakerId(AVSENDER_ID_ORGANISASJON);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getAvsender().getAvsenderType().value(), is(BrukerTypeCode.ORGANISASJON.name()));
	}

	@Test
	public void shouldMapArkivsaksystemPsak() {
		Journalpost journalpost = createJournalpost();
		journalpost.getSaksrelasjon().setFagsystem(FagsystemCode.PEN.PEN);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getArkivSak().getArkivSakSystem(), is(ARKIVSAK_SYSTEM_PSAK));
	}

	private void assertJournalpostResponse(GetJournalpostResponse response) {
		LocalDateTime localDateTime = LocalDateTime.of(2017, 2, 3, 10, 37, 30);

		assertThat("response.journaltilstand", response.getJournalTilstand().toString(), is(JOURNALTILSTAND_ENDELIG));
		assertThat("response.tema", response.getTema(), is(FagomradeCode.FS22.name()));
		assertThat("response.tittel", response.getTittel(), is(INNHOLD));
		assertThat("response.kanalreferanseId", response.getKanalReferanseId(), is(KANALREFERANSE_ID));
		assertThat("response.forsendelseMottatt", response.getForsendelseMottatt(), is(Date.from(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC))));
		assertThat("response.mottakskanal", response.getMottaksKanal(), is(MottaksKanalCode.ALTINN.name()));
		assertThat("response.journalfoerendeEnhet", response.getJournalfEnhet(), is(JOURNALFOERENDE_ENHET));

		assertBrukere(response.getBrukerListe());
		assertAvsender(response.getAvsender());
		assertArkivsak(response.getArkivSak());
		assertDokumenter(response.getDokumentListe());
	}

	@Test
	public void shouldMapEmptyVarianterWhenKassert() {
		Journalpost journalpost = createJournalpostKassert();
		journalpost.getSaksrelasjon().setFagsystem(FagsystemCode.PEN.PEN);
		GetJournalpostResponse response = mapper.map(journalpost);
		assertThat(response.getDokumentListe().size(), is(2));
		assertThat(response.getDokumentListe().get(0).getVariant().size(), is(0));
		assertThat(response.getDokumentListe().get(1).getVariant().size(), is(0));
		assertThat(response.getArkivSak().getArkivSakSystem(), is(ARKIVSAK_SYSTEM_PSAK));
	}

	private void assertBrukere(List<Bruker> brukere) {
		brukere.forEach(bruker -> {
			if (bruker.getBrukerType().value().equals(BrukerTypeCode.PERSON.name())) {
				assertThat("response.brukere.bruker1.type", bruker.getBrukerType().value(), is(BrukerTypeCode.PERSON.name()));
				assertThat("response.brukere.bruker1.brukerId", bruker.getIdentifikator(), is(BRUKER_ID_PERSON));
			} else {
				assertThat("response.brukere.bruker2.type", bruker.getBrukerType()
						.value(), is(BrukerTypeCode.ORGANISASJON.name()));
				assertThat("response.brukere.bruker2.brukerId", bruker.getIdentifikator(), is(BRUKER_ID_ORGANISASJON));
			}
		});
	}

	private void assertAvsender(Avsender avsender) {
		assertThat("response.avsender.type", avsender.getAvsenderType().value(), is(BrukerTypeCode.PERSON.name()));
		assertThat("response.avsender.avsendeId", avsender.getIdentifikator(), is(AVSENDER_ID_PERSON));
		assertThat("response.avsender.navn", avsender.getNavn(), is(AVSENDER_NAVN));
	}

	private void assertArkivsak(ArkivSakNoArkivsakSystemEnum arkivsak) {
		assertThat("response.arkivsak.arkivsakId", arkivsak.getArkivSakId(), is(SAK_ID));
		assertThat("response.brukerId", arkivsak.getArkivSakSystem(), is(ARKIVSAK_SYSTEM_GSAK));
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
		assertVarianterDokument1(dokument.getVariant());
		assertLogiskVedleggDokument1(dokument.getLogiskVedleggListe());
	}

	private void assertVarianterDokument1(List<Variant> varianter) {
		varianter.stream().forEach(variant -> {
			if (variant.getArkivFilType().equals(FilTypeCode.XML.name())) {
				assertThat("response.dokument1.variant1.arkivfiltype1", variant.getArkivFilType(), is(FilTypeCode.XML.name()));
				assertThat("response.dokument1.variant1.variantformat1", variant.getVariantFormat(), is(VariantFormatCode.ORIGINAL
						.name()));
			} else {
				if (variant.getVariantFormat().equalsIgnoreCase(VariantFormatCode.SLADDET.name())) {
					assertThat("response.dokument1.variant1.arkivfiltype2", variant.getArkivFilType(), is(FilTypeCode.PDFA.name()));
					assertThat("response.dokument1.variant1.variantformat2", variant.getVariantFormat(), is(VariantFormatCode.SLADDET.name()));
				} else {
					assertThat("response.dokument1.variant1.arkivfiltype2", variant.getArkivFilType(), is(FilTypeCode.PDFA.name()));
					assertThat("response.dokument1.variant1.variantformat2", variant.getVariantFormat(), is(VariantFormatCode.ARKIV.name()));
				}
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
		assertVarianterDokument2(dokument.getVariant());
		assertLogiskVedleggDokument2(dokument.getLogiskVedleggListe());
	}

	private void assertVarianterDokument2(List<Variant> varianter) {
		varianter.stream().forEach(variant -> {
			if (variant.getArkivFilType().equals(FilTypeCode.XML.name())) {
				assertThat("response.dokument1.variant1.arkivfiltype1", variant.getArkivFilType(), is(FilTypeCode.XML.name()));
				assertThat("response.dokument1.variant1.variantformat1", variant.getVariantFormat(), is(VariantFormatCode.ORIGINAL
						.name()));
			} else {
				if (variant.getVariantFormat().equalsIgnoreCase(VariantFormatCode.SLADDET.name())) {
					assertThat("response.dokument1.variant1.arkivfiltype2", variant.getArkivFilType(), is(FilTypeCode.PDFA.name()));
					assertThat("response.dokument1.variant1.variantformat2", variant.getVariantFormat(), is(VariantFormatCode.SLADDET.name()));
				} else {
					assertThat("response.dokument1.variant1.arkivfiltype2", variant.getArkivFilType(), is(FilTypeCode.PDFA.name()));
					assertThat("response.dokument1.variant1.variantformat2", variant.getVariantFormat(), is(VariantFormatCode.ARKIV.name()));
				}
			}
		});
	}

	private void assertLogiskVedleggDokument2(List<LogiskVedlegg> logiskeVedlegg) {
		LogiskVedlegg logiskVedlegg = logiskeVedlegg.get(0);
		assertThat("response.dokument2.logiskVedlegg2.arkivfiltype", logiskVedlegg.getLogiskVedleggId(), is(SKANNETINNHOLD_ID3));
		assertThat("response.dokument2.logiskVedlegg2.variantformat", logiskVedlegg.getLogiskVedleggTittel(), is(VEDLEGGINNHOLD3));
	}
}