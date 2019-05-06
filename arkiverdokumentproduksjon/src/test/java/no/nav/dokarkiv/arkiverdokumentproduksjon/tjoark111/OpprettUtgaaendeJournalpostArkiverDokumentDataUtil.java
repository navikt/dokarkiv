package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import static no.nav.dokarkiv.core.util.DateUtil.getDateNow;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Bruker;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Saksrelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.JournalpostDokumentInfoRelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettutgaaendejournalpostarkiverdokument.Vedlegg;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Util for creating Journalpost for the OpprettJournalpostArkiverDokument
 * operation
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class OpprettUtgaaendeJournalpostArkiverDokumentDataUtil {

	protected static final String BREVKODE = "brevkode";
	protected static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	protected static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	protected static final String JOURNALFOERENDE_ENHET_REF = "2009";
	protected static final String HOVEDDOKUMENT = TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name();
	protected static final String VEDLEGG = TilknyttetJournalpostSomCode.VEDLEGG.name();
	protected static final String OPPRETTET_AV_NAVN = "Max Mekker";
	protected static final String TITTEL = "The Sound of Music";
	protected static final String KATEGORI = DokumentKategoriCode.B.name();
	protected static final String INNHOLD = "Sanger fra verden";
	protected static final UtsendingsKanalCode UTSENDINGSKANAL = UtsendingsKanalCode.NAV_NO;
	protected static final String DOKUMENT_INNHOLD = "ustrukturertInnhold";
	protected static final String VARIANTFORMAT = "ARKIV";
	protected static final String PERSONIDENT = "***gammelt_fnr***";
	protected static final String EKSTERNPART_NAVN = "Jippi Hurra";
	protected static final String FAGSYSTEMKODE = FagsystemCode.PEN.name();
	protected static final String SAKSID = "312";
	protected static final String KANAL_REF_ID = "Kanal_ref_id_AAA";
	protected static final String TILLEGGSOPPLYSNING_NOKKEL = "bucId";
	protected static final String TILLEGGSOPPLYSNING_VERDI = "bucId-verdi";
	protected static final String VEDLEGG_DOK_INFO_ID = "1122";
	protected static final String VEDLEGG_JP_ID = "123213213";
	protected static final BrukerTypeCode BRUKERTYPE = BrukerTypeCode.PERSON;
	protected static final Date DATO_DOKUMENT = getDateNow();
	protected static final Date DATO_JOURNAL = getDateNow();

	public static Journalpost createJournalpostOnlyRequiredValues() {
		Journalpost journalpost = new Journalpost();
		journalpost.setOpprettetAvNavn(OPPRETTET_AV_NAVN);
		journalpost.setKanalreferanseId(KANAL_REF_ID);
		return journalpost;
	}

	public static Journalpost createJournalpost() {
		Journalpost journalpost = new Journalpost();
		journalpost.setTema(FAGOMRADE.name());
		journalpost.setOpprettetAvNavn(OPPRETTET_AV_NAVN);
		journalpost.setJournalforendeEnhet(JOURNALFOERENDE_ENHET_REF);
		journalpost.setInnhold(INNHOLD);
		journalpost.setDatoDokument(toXMLGregorianCalendar(DATO_DOKUMENT));
		journalpost.setAvsenderMottakerNavn(EKSTERNPART_NAVN);
		journalpost.setAvsenderMottakerId(PERSONIDENT);
		journalpost.setUtsendingskanal(UTSENDINGSKANAL.name());
		journalpost.setKanalreferanseId(KANAL_REF_ID);
		journalpost.setTilleggsopplysninger(createTilleggsopplysning());
		return journalpost;
	}

	public static Tilleggsopplysning createTilleggsopplysning() {
		return new Tilleggsopplysning()
				.withOpplysningsnoekkel(TILLEGGSOPPLYSNING_NOKKEL)
				.withOpplysningsverdi(TILLEGGSOPPLYSNING_VERDI);
	}

	public static Vedlegg createVedlegg(Long dokumentInfoId, Long jpId) {
		Vedlegg vedlegg = new Vedlegg();
		vedlegg.setDokumentInfoId(String.valueOf(dokumentInfoId));
		vedlegg.setKnyttesFraJournalpostId(String.valueOf(jpId));
		return vedlegg;
	}

	protected static Saksrelasjon createSaksrelasjon() {
		Saksrelasjon saksrelasjon = new Saksrelasjon();
		saksrelasjon.setFagsystem(FAGSYSTEMKODE);
		saksrelasjon.setSaksnummer(SAKSID);
		return saksrelasjon;
	}

	public static JournalpostDokumentInfoRelasjon createDokumentInfoRelasjon(String tilknyttetJournalpostSom) {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();
		journalpostDokumentInfoRelasjon.setDokumentInfo(createDokumentInfo());
		journalpostDokumentInfoRelasjon.setTilknyttetJournalpostSom(tilknyttetJournalpostSom);
		return journalpostDokumentInfoRelasjon;
	}

	public static JournalpostDokumentInfoRelasjon createDokumentInfoRelasjonOnlyRequired() {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = new JournalpostDokumentInfoRelasjon();
		DokumentInfo dokumentInfo = createDokumentInfo();
		dokumentInfo.setBrevkode(null);
		journalpostDokumentInfoRelasjon.setDokumentInfo(dokumentInfo);
		journalpostDokumentInfoRelasjon.setTilknyttetJournalpostSom(HOVEDDOKUMENT);
		return journalpostDokumentInfoRelasjon;
	}


	private static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setKategori(KATEGORI);
		dokumentInfo.setTittel(TITTEL);
		dokumentInfo.setBrevkode(BREVKODE);
		dokumentInfo.setDokumentTypeId(DOKUMENT_TYPE_ID);
		dokumentInfo.getFildetaljerListe().add(createFildetaljer());
		return dokumentInfo;
	}

	public static Fildetaljer createFildetaljer() {
		Fildetaljer fildetaljer = new Fildetaljer();
		fildetaljer.setFiltype(FilTypeCode.XML.name());
		fildetaljer.setVariantformat(VARIANTFORMAT);
		fildetaljer.setIkkeRedigerbartdokument(DOKUMENT_INNHOLD.getBytes());
		return fildetaljer;
	}

	protected static Bruker createBruker() {
		Bruker bruker = new Bruker();
		bruker.setBrukerId(PERSONIDENT);
		bruker.setBrukerType(BrukerTypeCode.PERSON.name());
		return bruker;
	}

	public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
		try {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
}
