package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.opprettjournalpost.Journalpost;

import static java.util.Arrays.asList;

/**
 * Creates a Journalpost used for testing OpprettJournalpost request in
 * the arkiverDokumeproduksjon service
 *
 * @author Stig Strøm
 */
public class OpprettJournalpostDataUtil {

	protected static final long METAFORCE_INSTANCE_ID = 123454321L;
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
	protected static final String PERSONIDENT = "22027838743";
	protected static final String EKSTERNPART_NAVN = "Jippi Hurra";
	protected static final String FAGSYSTEMKODE = FagsystemCode.FS22.name();
	protected static final Long SAKSID = 312L;
	protected static final String BRUKERID = "312273912";
	protected static final BrukerTypeCode BRUKERTYPE = BrukerTypeCode.PERSON;
	protected static final String TILLEGGSOPPLYSNING_KEY_1 = "tilleggsopplysning-1";
	protected static final String TILLEGGSOPPLYSNING_KEY_2 = "tilleggsopplysning-2";
	protected static final String TILLEGGSOPPLYSNING_VALUE_1 = "Tillegg 1";
	protected static final String TILLEGGSOPPLYSNING_VALUE_2 = "Tillegg 2";

	public static Journalpost createJournalpost() throws Exception {
		Journalpost journalpost = new Journalpost();
		journalpost.setFagomrade(FAGOMRADE.name());
		journalpost.setOpprettetAvNavn(OPPRETTET_AV_NAVN);
		journalpost.setJournalforendeEnhet(JOURNALFOERENDE_ENHET_REF);
		journalpost.setInnhold(INNHOLD);
		journalpost.setAvsenderMottakerNavn(EKSTERNPART_NAVN);
		journalpost.setAvsenderMottakerId(PERSONIDENT);
		journalpost.setLand(LAND);
		journalpost.setSaksrelasjon(createSaksrelasjon());
		journalpost.setBruker(createBruker());
		journalpost.setDokumentInfo(createDokumentInfo());
		return journalpost;
	}

	protected static Saksrelasjon createSaksrelasjon() {
		Saksrelasjon saksrelasjon = new Saksrelasjon();
		saksrelasjon.setFagsystem(FAGSYSTEMKODE);
		saksrelasjon.setSaksnummer(SAKSID.toString());
		return saksrelasjon;
	}

	private static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setKategori(KATEGORI);
		dokumentInfo.setTittel(TITTEL);
		dokumentInfo.setBrevkode(BREVKODE);
		dokumentInfo.setSensitivt(true);
		dokumentInfo.setDokumentTypeId(DOKUMENT_TYPE_ID);
		dokumentInfo.setFildetaljer(createFildetaljer());
		dokumentInfo.getTilleggsopplysninger().addAll(asList(
				createTilleggsopplysning(TILLEGGSOPPLYSNING_KEY_1, TILLEGGSOPPLYSNING_VALUE_1),
				createTilleggsopplysning(TILLEGGSOPPLYSNING_KEY_2, TILLEGGSOPPLYSNING_VALUE_2)));
		return dokumentInfo;
	}

	private static Fildetaljer createFildetaljer() {
		Fildetaljer fildetaljer = new Fildetaljer();
		fildetaljer.setFiltype(FilTypeCode.XML.name());
		fildetaljer.setVariantformat(VARIANTFORMAT);
		fildetaljer.setMetaForceInstanceId(METAFORCE_INSTANCE_ID);
		return fildetaljer;
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
}
