package no.nav.dokarkiv.core.util;

import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.ArsakReturCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.ReturInfo;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class TestDataGenerator {

	public static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	public static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	public static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	public static final String ENDRET_AV_NAVN = "Endret av navn";
	public static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	public static final String TITTEL = "FysiskSlettDokument";
	public static final String BREVGRUPPE = "Brevgruppe";
	public static final String BREVKODE = "Brevkode";
	public static final String FILNAVN = "filNavn";
	public static final String BRUKER_ID = "123213";
	public static final String KRYSSREFERANSE_ID = "123213";
	public static final String DOKUMENT_INFO_TITTEL = "TITTEL";
	public static final String DOKUMENT_TYPE_ID = "0000001";
	public static final String SAK_ID = "1232131233";
	public static final String FIL_NAVN = "navn";
	public static final String TILLEGGOPPLYSNINGER_KEY = "tillegg";
	public static final String TILLEGGOPPLYSNINGER_VAL = "tillegg_verdi";
	public static final byte[] FIL = "Test dokument".getBytes();

	public static Journalpost createJournalpostWithHoveddokument() {
		Journalpost journalpost = Journalpost.builder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.dokumentDato(new Date())
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalstatus(JournalStatusCode.FS)
				.journalposttype(JournalpostTypeCode.U)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.saksrelasjon(
						SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.mottakskanal(MottaksKanalCode.NAV_NO).build();

		journalpost.addBruker(createBruker());
		journalpost.addKryssReferanse(createKryssreferanse());
		journalpost.addReturInfo(createReturInfo());
		journalpost.setSaksrelasjon(createSaksrelasjon(journalpost));
		journalpost.setTilleggsopplysninger(createTilleggsopplysninger());
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpost));
		return journalpost;
	}

	public static Journalpost createJournalpostWithSplittetHoveddokument(Journalpost journalpostOriginal) {
		Journalpost journalpost = Journalpost.builder()
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.dokumentDato(new Date())
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.journalstatus(JournalStatusCode.FS)
				.journalposttype(JournalpostTypeCode.U)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.saksrelasjon(
						SaksrelasjonTestDataProvider.createSaksrelasjon().build())
				.mottakskanal(MottaksKanalCode.NAV_NO).build();

		journalpost.addBruker(createBruker());
		journalpost.addKryssReferanse(createKryssreferanse());
		journalpost.addReturInfo(createReturInfo());
		journalpost.setSaksrelasjon(createSaksrelasjon(journalpost));
		journalpost.setTilleggsopplysninger(createTilleggsopplysninger());
		journalpost.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);

		journalpost.addJournalpostDokumentInfoRelasjon(createHoveddokumentRelasjon(journalpostOriginal));
		return journalpost;
	}

	public static Map<String, String> createTilleggsopplysninger() {
		Map<String, String> tilleggsopplysninger = new HashMap<>();
		tilleggsopplysninger.put(TILLEGGOPPLYSNINGER_KEY, TILLEGGOPPLYSNINGER_VAL);
		return tilleggsopplysninger;
	}


	public static JournalpostDokumentInfoRelasjon createHoveddokumentRelasjon(Journalpost journalpost) {
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setOriginalJournalpost(journalpost);

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		return journalpostDokumentInfoRelasjon;
	}

	public static JournalpostDokumentInfoRelasjon createHoveddokumentRelasjon(Journalpost journalpost, DokumentInfo dokumentInfo) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		return journalpostDokumentInfoRelasjon;
	}

	public static JournalpostDokumentInfoRelasjon createDokumentInfoVedleggRelasjon(Journalpost journalpost) {
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setOriginalJournalpost(journalpost);

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		return journalpostDokumentInfoRelasjon;
	}

	public static JournalpostDokumentInfoRelasjon createVedleggRelasjon(Journalpost journalpost, DokumentInfo dokumentInfo) {

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon.builder()
				.journalpost(journalpost)
				.dokumentInfo(dokumentInfo)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		journalpostDokumentInfoRelasjon.setTilknyttetAvNavn(OPPRETTET_KILDE_NAVN);
		return journalpostDokumentInfoRelasjon;
	}

	public static Saksrelasjon createSaksrelasjon(Journalpost journalpost) {
		Saksrelasjon saksrelasjon = Saksrelasjon.builder()
				.fagsystem(FagsystemCode.AO01)
				.sakId(SAK_ID)
				.journalpost(journalpost)
				.build();
		saksrelasjon.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return saksrelasjon;
	}

	public static Bruker createBruker() {
		Bruker bruker = Bruker.builder()
				.brukerType(BrukerTypeCode.PERSON)
				.brukerId(BRUKER_ID)
				.build();
		bruker.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return bruker;
	}

	public static ReturInfo createReturInfo() {
		ReturInfo returInfo = new ReturInfo();
		returInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		returInfo.setAdresseSendtIgjen("GATE");
		returInfo.setArsakRetur(ArsakReturCode.ANNET);
		returInfo.setReturDato(new Date());
		return returInfo;
	}

	public static Kryssreferanse createKryssreferanse() {
		Kryssreferanse kryssreferanse = Kryssreferanse.builder()
				.referanseType(ReferanseTypeCode.SED_FLYT)
				.referanseId(KRYSSREFERANSE_ID)
				.referanseNr(1L)
				.build();
		kryssreferanse.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return kryssreferanse;
	}


	public static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(DOKUMENT_INFO_TITTEL)
				.dokumenttypeId(DOKUMENT_TYPE_ID)
				.build();
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.ARKIV));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.PRODUKSJON));
		dokumentInfo.addSkannetInnhold(createSkannetInnhold());
		dokumentInfo.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		dokumentInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		return dokumentInfo;
	}

	public static SkannetInnhold createSkannetInnhold() {
		SkannetInnhold skannetInnhold = SkannetInnhold.builder()
				.dokumenttypeid(DOKUMENT_TYPE_ID)
				.build();
		skannetInnhold.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return skannetInnhold;
	}

	public static FilDetaljer createFildetaljerOgFil(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode) {
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.dokumentInfo(dokumentInfo)
				.fileContent(FIL)
				.filnavn(FIL_NAVN)
				.filtype(FilTypeCode.PDF)
				.filUuid(FilDetaljer.generateUuid())
				.variantFormat(variantFormatCode)
				.build();
		filDetaljer.setOpprettetKildeNavn(OPPRETTET_KILDE_NAVN);
		return filDetaljer;
	}
}
