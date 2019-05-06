package no.nav.dokarkiv.arkiverdokumentmottak.utils;

import static no.nav.dokarkiv.arkiverdokumentmottak.ArkiverDokumentmottakConstants.FORSENDELSE_MOTTAK_ID_KEY;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.VEDLEGG_INNHOLD;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.createTilleggsOpplysning;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.populateFildetaljerBase;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.JournalpostDokumentInfoRelasjon;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

/**
 * Util class for creating a {@link no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest}
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 21.02.2017
 */
public final class JournalforInngaaendeForsendelseRequestDataUtil {
	public static final String FORSENDELSE_MOTTA_VALUE = "forsendelse_motta_value";
	public static final String INNHOLD = "Innhold";
	public static final String AVSENDERMOTAKERID = "AvMottakerId";
	public static final String AVSENDERMOTAKER = "AvMottaker";
	public static final JournalStatusCode JOURNAL_STATUS = JournalStatusCode.D;
	public static final java.util.Date DATE = Date.from(LocalDate.now()
			.atStartOfDay()
			.atZone(ZoneOffset.systemDefault())
			.toInstant());
	public static final DokumentStatusCode DOKUMENT_INFO_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	public static final long METAFORCE_INSTANCE_ID = 555L;
	public static final String ENDRET_AV_KILDE_NAVN = "endret av navn";

	private JournalforInngaaendeForsendelseRequestDataUtil() {
	}


	public static no.nav.dokarkiv.core.domain.entities.Journalpost createJournalpostEntity() {
		no.nav.dokarkiv.core.domain.entities.Journalpost journalpost = no.nav.dokarkiv.core.domain.entities.Journalpost.builder()
				.journalstatus(JOURNAL_STATUS)
				.journalposttype(JournalpostTypeCode.U)
				.opprettetAvNavn("testuser")
				.journalfortAvNavn("test")
				.saksrelasjon(Saksrelasjon.builder()
						.endretAvNavn("test")
						.fagsystem(FagsystemCode.PEN)
						.feilregistrert(false)
						.build())
				.innhold(INNHOLD)
				.avsenderMottakerId(AVSENDERMOTAKERID)
				.avsenderMottaker(AVSENDERMOTAKER)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.journalDato(DATE).build();

		journalpost.addJournalpostDokumentInfoRelasjon(no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetAvNavn("testuser")
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(no.nav.dokarkiv.core.domain.entities.DokumentInfo.builder()
						.dokumentstatus(DOKUMENT_INFO_STATUS)
						.skannetInnholdListe(createSkannetInnholdListe())
						.fildetaljerListe(createFilDetaljerListe())
						.build())
				.build());

		return journalpost;
	}

	private static Set<SkannetInnhold> createSkannetInnholdListe() {
		Set<SkannetInnhold> skannetInnholds = new HashSet<>();
		skannetInnholds.add(SkannetInnhold.builder()
				.dokumenttypeid(DOKUMENT_TYPE_ID)
				.vedleggInnhold(VEDLEGG_INNHOLD).build());
		return skannetInnholds;
	}

	private static Set<FilDetaljer> createFilDetaljerListe() {
		Set<FilDetaljer> filDetaljerSet = new HashSet<>();

		filDetaljerSet.add(FilDetaljer.builder()
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.PRODUKSJON)
				.metaforceInstanceId(METAFORCE_INSTANCE_ID)
				.build());
		return filDetaljerSet;
	}

	public static Journalpost createJournalpost() {
		Journalpost journalpost = new Journalpost();

		ArkiverDokumentmottakRequestDataUtil.populateJournalpostBase(journalpost);
		journalpost.setTema(ArkiverDokumentmottakRequestDataUtil.FAGOMRADE.name());
		journalpost.setJournalforendeEnhet(ArkiverDokumentmottakRequestDataUtil.JOURNALFOERENDE_ENHET_REF);
		journalpost.setInnhold(ArkiverDokumentmottakRequestDataUtil.INNHOLD);
		journalpost.setAvsenderMottakerNavn(ArkiverDokumentmottakRequestDataUtil.EKSTERNPART_NAVN);
		journalpost.withJournalpostDokumentInfoRelasjon(createJournalpostDokumentInfoRelasjon());
		journalpost.setSaksrelasjon(ArkiverDokumentmottakRequestDataUtil.createSaksrelasjon());
		journalpost.setBruker(ArkiverDokumentmottakRequestDataUtil.createBruker());
		journalpost.getJournalpostTilleggsopplysninger()
				.add(createTilleggsOpplysning(FORSENDELSE_MOTTAK_ID_KEY, FORSENDELSE_MOTTA_VALUE));
		journalpost.setDatoDokument(ArkiverDokumentmottakRequestDataUtil.toXMLGregorianCalendar(ArkiverDokumentmottakRequestDataUtil.DATO_DOKUMENT));

		return journalpost;
	}

	public static Fildetaljer addFildetaljer(VariantFormatCode variantFormatCode) {
		Fildetaljer fildetaljer = new Fildetaljer();
		populateFildetaljerBase(fildetaljer, variantFormatCode);
		fildetaljer.setDokument(ArkiverDokumentmottakRequestDataUtil.BYTES);
		return fildetaljer;
	}

	private static Fildetaljer createFildetaljer() {
		Fildetaljer fildetaljer = new Fildetaljer();
		populateFildetaljerBase(fildetaljer, VariantFormatCode.valueOf(ArkiverDokumentmottakRequestDataUtil.VARIANTFORMAT));
		fildetaljer.setDokument(ArkiverDokumentmottakRequestDataUtil.DOKUMENT);
		return fildetaljer;
	}

	public static JournalpostDokumentInfoRelasjon addVedlegg() {
		JournalpostDokumentInfoRelasjon jpdir = new JournalpostDokumentInfoRelasjon();
		jpdir.setDokumentInfo(createDokumentInfo());
		jpdir.setTilknyttetJournalpostSom(TilknyttetJournalpostEnum.VEDLEGG);

		return jpdir;
	}

	private static JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon() {
		JournalpostDokumentInfoRelasjon jpdir = new JournalpostDokumentInfoRelasjon();
		jpdir.setDokumentInfo(createDokumentInfo());
		jpdir.setTilknyttetJournalpostSom(ArkiverDokumentmottakRequestDataUtil.TILKNYTTET_JOURNALPOST_SOM_CODE);
		return jpdir;
	}

	private static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setTittel(ArkiverDokumentmottakRequestDataUtil.TITTEL);
		dokumentInfo.getFildetaljerListe().add(createFildetaljer());
		ArkiverDokumentmottakRequestDataUtil.populateDokumentInfoBase(dokumentInfo);
		return dokumentInfo;
	}

}