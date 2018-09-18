package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import static java.util.Arrays.asList;

import no.nav.dokarkiv.arkiverdokumentproduksjon.ArkiverDokumentproduksjonConstants;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Bruker;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalpostType;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Saksrelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Util for creating Journalpost for the OpprettJournalpostArkiverDokumenter
 * operation
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostArkiverDokumenterDataUtil {

	protected static final String BREVKODE = "brevkode";
	protected static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	protected static final String LAND = "Norge";
	protected static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	protected static final String JOURNALFOERENDE_ENHET_REF = "2009";
	protected static final String HOVEDDOKUMENT = TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name();
	protected static final String OPPRETTET_AV_NAVN = "Max Mekker";
	protected static final boolean SENSITIVITET = true;
	protected static final String TITTEL = "The Sound of Music";
	protected static final String KATEGORI = DokumentKategoriCode.B.name();
	protected static final String INNHOLD = "Sanger fra verden";
	protected static final UtsendingsKanalCode UTSENDINGSKANAL = UtsendingsKanalCode.NAV_NO;
	protected static final String DOKUMENT_INNHOLD = "ustrukturertInnhold";
	protected static final String VARIANTFORMAT = "ARKIV";
	protected static final String PERSONIDENT = "***gammelt_fnr***";
	protected static final String EKSTERNPART_NAVN = "Jippi Hurra";
	protected static final String FAGSYSTEMKODE = FagsystemCode.AO01.name();
	protected static final String SAKSID = "312";
	protected static final String BRUKERID = "312273912";
	protected static final BrukerTypeCode BRUKERTYPE = BrukerTypeCode.PERSON;
	protected static final Date DATO_DOKUMENT = new Date(1234567890);
	protected static final String TILLEGGSOPPLYSNING_KEY_2 = "tilleggsopplysning-2";
	protected static final String BESTILLINGS_ID = "id010101";
	protected static final String TILLEGGSOPPLYSNING_VALUE_2 = "Tillegg 2";

	public static no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.Journalpost createJournalpost() {
		return new Journalpost()
				.withJournalpostType(JournalpostType.U)
				.withFagomrade(FAGOMRADE.name())
				.withOpprettetAvNavn(OPPRETTET_AV_NAVN)
				.withJournalforendeEnhet(JOURNALFOERENDE_ENHET_REF)
				.withInnhold(INNHOLD)
				.withAvsenderMottakerNavn(EKSTERNPART_NAVN)
				.withAvsenderMottakerId(PERSONIDENT)
				.withLand(LAND)
				.withSaksrelasjon(createSaksrelasjon())
				.withBruker(createBruker())
				.withDokumentInfoHoveddokument(createDokumentInfo())
				.withDokumentInfoVedlegg(createDokumentInfo())
				.withDatoDokument(toXMLGregorianCalendar(DATO_DOKUMENT));
	}

	protected static Saksrelasjon createSaksrelasjon() {
		return new Saksrelasjon()
		.withFagsystem(FAGSYSTEMKODE);
		.withSaksnummer(SAKSID);
		return saksrelasjon;
	}

	private static no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfo createDokumentInfo() {
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpostarkiverdokumenter.DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setKategori(KATEGORI);
		dokumentInfo.setTittel(TITTEL);
		dokumentInfo.setBrevkode(BREVKODE);
		dokumentInfo.setSensitivt(true);
		dokumentInfo.setDokumentTypeId(DOKUMENT_TYPE_ID);
		dokumentInfo.getTilleggsopplysninger().addAll(asList(
				createTilleggsopplysning(ArkiverDokumentproduksjonConstants.BESTILLINGS_ID_KEY, BESTILLINGS_ID),
				createTilleggsopplysning(TILLEGGSOPPLYSNING_KEY_2, TILLEGGSOPPLYSNING_VALUE_2)));
		return dokumentInfo;
	}

	private static Tilleggsopplysning createTilleggsopplysning(String noekkel, String verdi) {
		Tilleggsopplysning tilleggsopplysning = new Tilleggsopplysning();
		tilleggsopplysning.setOpplysningsnoekkel(noekkel);
		tilleggsopplysning.setOpplysningsverdi(verdi);
		return tilleggsopplysning;
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
