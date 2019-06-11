package no.nav.dokarkiv.arkiverdokumentmottak.utils;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Bruker;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.DokumentInfoBase;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.FildetaljerBase;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.JournalpostBase;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Saksrelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Util for ArkiverDokumentmottak-operations
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public final class ArkiverDokumentmottakRequestDataUtil {

	private ArkiverDokumentmottakRequestDataUtil() {
	}

	static final String FILNAVN = "FILNAVN";
	static final byte[] DOKUMENT = "<xml/>".getBytes();
	static final boolean SENSITIVITET = true;
	static final String BREVKODE = "brevkode";
	static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	static final String JOURNALFOERENDE_ENHET_REF = "2009";
	static final String OPPRETTET_AV_NAVN = "Banjo Kazooie";
	static final String TITTEL = "Once Upon a Time In Mehico";
	static final String KATEGORI = DokumentKategoriCode.B.name();
	static final String INNHOLD = "Antonio Banderas";
	static final String VARIANTFORMAT = "ARKIV";
	static final String PERSONIDENT = "***gammelt_fnr***";
	static final String EKSTERNPART_NAVN = "Mario & Luigi";
	static final String FAGSYSTEMKODE = FagsystemCode.FS22.name();
	static final String SAKSID = "312";
	static final String BATCH_NAVN = "TJOARK201";
	static final String VEDLEGG_INNHOLD = "Vedlegg innhold";
	static final Date DATO_DOKUMENT = new Date(1234567890);
	static final Date DATO_MOTTATT = new Date(234567890);
	static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	static final MottaksKanalCode MOTTAKS_KANAL_CODE = MottaksKanalCode.ALTINN;
	static final TilknyttetJournalpostEnum TILKNYTTET_JOURNALPOST_SOM_CODE = TilknyttetJournalpostEnum.HOVEDDOKUMENT;
	static final FilTypeCode FIL_TYPE_CODE = FilTypeCode.XML;
	static final byte[] BYTES = "DEEZ_BYTES".getBytes();

	public static void populateJournalpostBase(JournalpostBase journalpostBase) {
		journalpostBase.setOpprettetAvNavn(OPPRETTET_AV_NAVN);
		journalpostBase.setAvsenderMottakerId(PERSONIDENT);
		journalpostBase.setDatoMottatt(toXMLGregorianCalendar(DATO_MOTTATT));
		journalpostBase.setMottakskanal(MOTTAKS_KANAL_CODE.name());
	}


	public static void populateFildetaljerBase(FildetaljerBase fildetaljerBase, VariantFormatCode variantFormatCode) {
		fildetaljerBase.setFiltype(ArkiverDokumentmottakRequestDataUtil.FIL_TYPE_CODE.name());
		fildetaljerBase.setVariantformat(variantFormatCode.name());
		fildetaljerBase.setFilNavn(FILNAVN);
	}

	/**
	 * Public due to test
	 *
	 * @param date The util.Date to be parsed
	 * @return the XMLGregorianCalendar parsed util.Date
	 */
	public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
		try {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(date);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Saksrelasjon createSaksrelasjon() {
		Saksrelasjon saksrelasjon = new Saksrelasjon();
		saksrelasjon.setFagsystem(FAGSYSTEMKODE);
		saksrelasjon.setSaksnummer(SAKSID);
		return saksrelasjon;
	}

	static void populateDokumentInfoBase(DokumentInfoBase dokumentInfoBase) {
		dokumentInfoBase.setKategori(KATEGORI);
		dokumentInfoBase.setBrevkode(BREVKODE);
		dokumentInfoBase.setDokumentTypeId(DOKUMENT_TYPE_ID);
		dokumentInfoBase.setSensitivt(SENSITIVITET);
	}

	static Tilleggsopplysning createTilleggsOpplysning(String noekkel, String verdi) {
		Tilleggsopplysning tilleggsopplysning = new Tilleggsopplysning();
		tilleggsopplysning.setOpplysningsnoekkel(noekkel);
		tilleggsopplysning.setOpplysningsverdi(verdi);
		return tilleggsopplysning;
	}

	static Bruker createBruker() {
		Bruker bruker = new Bruker();
		bruker.setBrukerId(PERSONIDENT);
		bruker.setBrukerType(BrukerTypeCode.PERSON.name());
		return bruker;
	}

	public static void assertFilDetaljer(FilDetaljer filDetaljer, boolean isMidlertidig) {
		assertThat(filDetaljer.getFileContent(), is(ArkiverDokumentmottakRequestDataUtil.DOKUMENT));
		assertThat(filDetaljer.getFiltype(), is(FilTypeCode.XML));
		assertThat(filDetaljer.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(filDetaljer.getFilnavn(), is(ArkiverDokumentmottakRequestDataUtil.FILNAVN));
		if (isMidlertidig) {
			assertThat(filDetaljer.getBatchNavn(), is(ArkiverDokumentmottakRequestDataUtil.BATCH_NAVN));
		}
	}

	public static void assertSaksrelasjon(no.nav.dokarkiv.core.domain.entities.Saksrelasjon saksrelasjon,
										  Saksrelasjon requestSaksrelasjon) {
		assertThat(saksrelasjon.getFagsystem().name(), is(requestSaksrelasjon.getFagsystem()));
		assertThat(saksrelasjon.getSakId(), is(requestSaksrelasjon.getSaksnummer()));
	}

	public static void assertBruker(no.nav.dokarkiv.core.domain.entities.Bruker bruker,
									Bruker requestBruker) {
		assertThat(bruker.getBrukerId(), is(requestBruker.getBrukerId()));
		assertThat(bruker.getBrukerType().name(), is(requestBruker.getBrukerType()));
	}
}
