package no.nav.dokarkiv.arkiverdokumentmottak.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Bruker;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Saksrelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.SkannetInnhold;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;
import org.hamcrest.Matchers;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Util for ArkiverDokumentmottakV2-operations (TJOARK203)
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
public class ArkiverDokumentmottakV2RequestDataUtil {

	private ArkiverDokumentmottakV2RequestDataUtil() {
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
	static final String KANALREFERANSE_ID = "kanalReferanseId";
	static final String VEDLEGGINNHOLD = "vedleggInnhold";
	static final String BATCHNAVN = "Batchnavn";
	static final String FAGSYSTEMKODE = FagsystemCode.AO01.name();
	static final String SAKSID = "312";
	static final Date DATO_DOKUMENT = new Date(1234567890);
	static final Date DATO_MOTTATT = new Date(234567890);
	static final FagomradeCode FAGOMRADE = FagomradeCode.PEN;
	static final MottaksKanalCode MOTTAKS_KANAL_CODE = MottaksKanalCode.ALTINN;
	static final TilknyttetJournalpostEnum TILKNYTTET_JOURNALPOST_SOM_CODE = TilknyttetJournalpostEnum.HOVEDDOKUMENT;
	static final FilTypeCode FIL_TYPE_CODE = FilTypeCode.XML;
	static final byte[] BYTES = "DEEZ_BYTES".getBytes();

	public static void populateJournalpostBase(Journalpost journalpost) {
		journalpost.setOpprettetAvNavn(OPPRETTET_AV_NAVN);
		journalpost.setAvsenderMottakerId(PERSONIDENT);
		journalpost.setDatoMottatt(toXMLGregorianCalendar(DATO_MOTTATT));
		journalpost.setMottakskanal(MOTTAKS_KANAL_CODE.name());
	}

	public static void populateFildetaljerBase(Fildetaljer fildetaljer, VariantFormatCode variantFormatCode) {
		fildetaljer.setFiltype(ArkiverDokumentmottakV2RequestDataUtil.FIL_TYPE_CODE.name());
		fildetaljer.setVariantformat(variantFormatCode.name());
		fildetaljer.setFilNavn(FILNAVN);
		fildetaljer.setBatchNavn(BATCHNAVN);
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

	static void populateDokumentInfoBase(DokumentInfo dokumentInfo) {
		dokumentInfo.setKategori(KATEGORI);
		dokumentInfo.setBrevkode(BREVKODE);
		dokumentInfo.setDokumentTypeId(DOKUMENT_TYPE_ID);
		dokumentInfo.setSensitivt(SENSITIVITET);
	}

	static SkannetInnhold createSkannetInnhold() {
		SkannetInnhold skannetInnhold = new SkannetInnhold();
		skannetInnhold.setDokumentTypeId(DOKUMENT_TYPE_ID);
		skannetInnhold.setVedleggInnhold(VEDLEGGINNHOLD);
		return skannetInnhold;
	}

	static Bruker createBruker() {
		Bruker bruker = new Bruker();
		bruker.setBrukerId(PERSONIDENT);
		bruker.setBrukerType(BrukerTypeCode.PERSON.name());
		return bruker;
	}

	public static void assertSkannetInnhold(no.nav.dokarkiv.core.domain.entities.SkannetInnhold skannetInnhold) {
		assertThat(skannetInnhold.getVedleggInnhold(), is(VEDLEGGINNHOLD));
		assertThat(skannetInnhold.getDokumenttypeid(), is(DOKUMENT_TYPE_ID));
	}

	public static void assertFilDetaljer(FilDetaljer filDetaljer) {
		assertThat(filDetaljer.getFileContent(), is(ArkiverDokumentmottakV2RequestDataUtil.DOKUMENT));
		assertThat(filDetaljer.getFiltype(), is(FilTypeCode.XML));
		assertThat(filDetaljer.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(filDetaljer.getFilnavn(), is(ArkiverDokumentmottakV2RequestDataUtil.FILNAVN));
		assertThat(filDetaljer.getBatchNavn(), is(ArkiverDokumentmottakV2RequestDataUtil.BATCHNAVN));

	}

	public static void assertSaksrelasjon(no.nav.dokarkiv.core.domain.entities.Saksrelasjon saksrelasjon, Saksrelasjon requestSaksrelasjon) {
		assertThat(saksrelasjon.getFagsystem().name(), is(requestSaksrelasjon.getFagsystem()));
		assertThat(saksrelasjon.getSakId(), is(requestSaksrelasjon.getSaksnummer()));
	}

	public static void assertBruker(no.nav.dokarkiv.core.domain.entities.Bruker bruker, Bruker requestBruker) {
		assertThat(bruker.getBrukerId(), Matchers.is(requestBruker.getBrukerId()));
		assertThat(bruker.getBrukerType().name(), Matchers.is(requestBruker.getBrukerType()));
	}
}
