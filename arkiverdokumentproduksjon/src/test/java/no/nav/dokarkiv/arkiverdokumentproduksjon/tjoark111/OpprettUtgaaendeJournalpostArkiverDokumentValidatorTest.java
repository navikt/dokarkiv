package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ValideringAvVedleggFeiletException;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.journalbehandling.DefaultJournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.DefaultMandatoryFieldsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.BRUKERTYPE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DATO_DOKUMENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DATO_JOURNAL;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.EKSTERNPART_NAVN;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.FAGOMRADE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.INNHOLD;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.JOURNALFOERENDE_ENHET_REF;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KANAL_REF_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.PERSONIDENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.SAKSID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.TILLEGGSOPPLYSNING_NOKKEL;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.TILLEGGSOPPLYSNING_VERDI;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.TITTEL;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DefaultMandatoryFieldsVerifier.class,
		OpprettUtgaaendeJournalpostArkiverDokumentValidator.class,
		DefaultJournalpostStructureVerifier.class})
public class OpprettUtgaaendeJournalpostArkiverDokumentValidatorTest {
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final boolean SENSITIVT_REQUEST = true;
	private static final boolean FERDIGSTILL_JOURNALPOST = true;
	private static final boolean IKKE_FERDIGSTILL_JOURNALPOST = false;

	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";

	private Journalpost journalpost;

	@Autowired
	private OpprettUtgaaendeJournalpostArkiverDokumentValidator validator;

	@BeforeEach
	public void setUp() {
		journalpost = createJournalpost();
	}

	//Saksrelasjon
	@Test
	public void shouldThrowExceptionIfNoSaksrelasjonOnJournalpost() {
		journalpost.setSaksrelasjon(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"saksrelasjon must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoSaksIDOnJournalpostIsNull() {
		journalpost.getSaksrelasjon().setSakId(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"sakId must be set");
	}

	@Test
	public void shouldThrowExceptionIfSaksRelasjonFagsystemOnJournalpostIsNull() {
		journalpost.getSaksrelasjon().setFagsystem(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"fagsystem must be set");
	}

	//Journalpost
	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		journalpost.setFagomrade(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionIfOpprettetAvNavnNotSetOnJournalpost() {
		journalpost.setOpprettetAvNavn(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"opprettetAvNavn must be set");
	}

	@Test
	public void shouldThrowExceptionIfJournalforendeEnhetIdNotSetOnJournalpost() {
		journalpost.setJournalForendeEnhetId(null);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validate(journalpost),
				"Mangler påkrevd attributt: Journalpost.JournalForendeEnhetId");
	}

	@Test
	public void shouldThrowExceptionIfInnholdNotSetOnJournalpost() {
		journalpost.setInnhold(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"innhold must be set");
	}

	@Test
	public void shouldThrowExceptionIfAvsenderMottakerNotSetOnJournalpost() {
		journalpost.setAvsenderMottaker(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"avsenderMottaker must be set");
	}

	@Test
	public void shouldThrowExceptionIfDokumentDatoIsMissing() {
		journalpost.setDokumentDato(null);

		assertThrows(NullPointerException.class,
				() -> validator.validate(journalpost),
				"Mangler påkrevd attributt: Journalpost.DokumentDato");
	}

	//Ekstra test for JP
	@Test
	public void shouldThrowExceptionIfJournalstatusIsNotSet() {
		journalpost.setJournalstatus(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"journalstatus must be set");
	}

	//Brukere
	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		journalpost.clearBrukere();

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"Journalpost must have at least one Bruker");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNull() {
		journalpost.getBrukere().iterator().next().setBrukerId(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"brukerId must be set");
	}

	@Test
	public void shouldThrowExceptionIfBrukerTypeIsNull() {
		journalpost.getBrukere().iterator().next().setBrukerType(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"brukerType must be set");
	}

	//DokumentInfo
	@Test
	public void shouldThrowExceptionIfNoDokumentInfoObjectOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"dokumentInfo must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoKategoriOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setKategori(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"DokumentInfo.kategori must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoTittelOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"DokumentInfo.tittel must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoDokumenttypeIdOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumenttypeId(null);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validate(journalpost),
				"Mangler påkrevd attributt: DokumentInfo.DokumenttypeId");
	}

	@Test
	public void shouldThrowExceptionIfNoFilTypeOnFildetaljerOnJournalpost() {
		journalpost.findAllFilDetaljer().get(0).setFiltype(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"filtype must be set");
	}

	@Test
	public void shouldThrowExceptionIfGotArkivVariantButOneVarianIsNull() {
		Journalpost journalpost = createJournalpost();
		journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setVariantFormat(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"variantFormat must be set");
	}

	@Test
	public void shouldThrowExceptionNoUtsendingskanal() {
		journalpost.setUtsendingskanal(null);

		assertThrows(NullPointerException.class,
				() -> validator.validate(journalpost),
				"Mangler påkrevd attributt: Journalpost.Utsendingskanal");
	}

	//FilDetaljer
	@Test
	public void shouldThrowExceptionIfVariantFormatArkivIsMissing() {
		journalpost.findAllFilDetaljer().get(0).setVariantFormat(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateVariantFormaterAndHoveddokument(journalpost),
				"DokumentInfos must contain an arkiv variant");
	}

	@Test
	public void shouldThrowExceptionIfDokumentDuplicatesOnJournalpost() throws UgyldigInputException {
		addJournalpostDokumentInfoRelasjonWithTwoVariantFormats(journalpost, VariantFormatCode.ARKIV);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateVariantFormaterAndHoveddokument(journalpost),
				"DokumentInfo cannot contain dokumentvariant duplicates");
	}

	@Test
	public void shouldThrowExceptionIfTwoHoveddokuments() {
		journalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.build());

		assertThrows(UgyldigInputException.class,
				() -> validator.validateVariantFormaterAndHoveddokument(journalpost),
				"Journalpost cannot contain more than one hoveddokument when endelig journalforing");
	}

	@Test
	public void shouldNotThrowExceptionIfGotArkivVariantButOneVarianIsNull() {
		Journalpost journalpost = createJournalpost();
		addJournalpostDokumentInfoRelasjonWithTwoVariantFormats(journalpost, null);
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}

	@Test
	public void shouldThrowIfInputJournalStatusIsD() {
		journalpost.setJournalstatus(JournalStatusCode.D);

		assertThrows(ValideringAvVedleggFeiletException.class,
				() -> validator.validateVedlegg(journalpost, journalpost.findHoveddokumentDokumentInfoRelasjon()
						.getDokumentInfo(), createVedlegg()),
				"Journalpost.JournalStatus kan ikke være D");
	}

	@Test
	public void shouldThrowIfInputDokumentInfoIdIsNotFerdigstilt() {
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateVedleggDokumentInfo(DokumentInfo.builder()
						.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
						.build()),
				"DokumentInfo.Dokumentstatus må være FERDIGSTILT men var UNDER_REDIGERING");
	}

	@Test
	public void shouldThrowIfInputDokumentInfoFildetaljerHasNoVariantFormatWithArkiv() {
		DokumentInfo dokumentInfo = createDokumentInfoWithFildetaljer();
		dokumentInfo.getFildetaljerListe().forEach(filDetaljer -> filDetaljer.setVariantFormat(VariantFormatCode.ORIGINAL));

		assertThrows(IllegalArgumentException.class,
				() -> validator.validateVedleggFildetaljer(dokumentInfo),
				"Vedlegg mangler Fildetaljer med variantFormat=ARKIV");
	}

	@Test
	public void shouldThrowIfInputDokumentInfoFildetaljerOnDemandIdIsNotNull() {
		DokumentInfo dokumentInfo = createDokumentInfoWithFildetaljer();
		dokumentInfo.getFildetaljerListe().iterator().next().setOnDemandId("adsad");
		dokumentInfo.getFildetaljerListe().iterator().next().setOnDemandInstans(OnDemandInstansCode.SYFO);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validateVedleggFildetaljer(dokumentInfo),
				"Fildetaljer.OnDemandId kan ikke være satt");
	}


	@Test
	public void shouldThrowIfInputIsMissingKanalreferanseId() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().setKanalReferanseId(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"kanalReferanseId");
	}

	@Test
	public void shouldThrowIfInputIsMissingOpprettetAvNavn() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().setOpprettetAvNavn(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"opprettetAvNavn");
	}


	@Test
	public void shouldThrowIfInputIsMissinJournalpostDokumentInfoRelasjoner() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.setJournalpost(Journalpost.builder().kanalReferanseId("ads").build());

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"journalpostDokumentInfoRelasjoner");
	}

	@Test
	public void shouldThrowIfInputIsMissingDokumentInfo() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().setDokumentInfo(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"DokumentInfo");
	}

	@Test
	public void shouldThrowIfInputIsMissingJournalpostDokumentInfoKategori() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setKategori(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"Kategori");
	}


	@Test
	public void shouldThrowIfInputIsMissingJournalpostDokumentInfoTittel() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setTittel(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"Tittel");
	}

	@Test
	public void shouldThrowIfInputIsMissingJournalpostDokumentInfoDokumenttypeId() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setDokumenttypeId(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"DokumenttypeId");
	}


	@Test
	public void shouldThrowIfInputIsMissingJournalpostFildetaljerFiltype() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost()
				.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setFiltype(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"Filtype");
	}


	@Test
	public void shouldThrowIfInputIsMissingJournalpostFildetaljerVariantFormat() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost()
				.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setVariantFormat(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"VariantFormat");
	}

	@Test
	public void shouldThrowIfInputIsMissingJournalpostFildetaljerIkkeRedigerbartDokument() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost()
				.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setFileContent(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"IkkeRedigerbartDokument");
	}

	@Test
	public void shouldThrowIfInputIsMissingVedleggKnyttesFraJournalpost() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getVedleggList().get(0).setKnyttesFraJournalpostId(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"KnyttesFraJournalpostId");
	}

	@Test
	public void shouldThrowIfInputIsMissingVedleggDokumentinfoId() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getVedleggList().get(0).setDokumentInfoId(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"DokumentInfoId");
	}

	@Test
	public void shouldThrowIfInputIsMissingSaksrelasjonSaksnummer() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().getSaksrelasjon().setSakId(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"Saksnummer");
	}

	@Test
	public void shouldThrowIfInputIsMissingSaksrelasjonFagsystem() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().getSaksrelasjon().setFagsystem(null);

		assertThrows(UgyldigInputException.class,
				() -> validator.validateRequiredFields(requestTo),
				"Fagsystem");
	}

	private OpprettUtgaaendeJournalpostArkiverDokumentRequestTo createRequestTo() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo to = OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.builder()
				.journalpost(createJournalpost())
				.journalforendeEnhet("ads").build();
		to.getVedleggList().add(createVedlegg());
		return to;
	}

	private Journalpost createJournalpost() {
		Journalpost journalpost = Journalpost.builder()
				.journalposttype(JournalpostTypeCode.U)
				.journalstatus(JournalStatusCode.FS)
				.journalDato(DATO_JOURNAL)
				.fagomrade(FAGOMRADE)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET_REF)
				.journalfortAvNavn(EKSTERNPART_NAVN)
				.innhold(INNHOLD)
				.dokumentDato(DATO_DOKUMENT)
				.avsenderMottaker(EKSTERNPART_NAVN)
				.avsenderMottakerId(EKSTERNPART_NAVN)
				.utsendingskanal(UtsendingsKanalCode.NAV_NO)
				.kanalReferanseId(KANAL_REF_ID)
				.saksrelasjon(Saksrelasjon.builder()
						.sakId(SAKSID)
						.fagsystem(FagsystemCode.FS22)
						.build())
				.tilleggsopplysninger(createTilleggsopplysningMap())
				.build();
		journalpost.addBruker(Bruker.builder().brukerId(PERSONIDENT).brukerType(BRUKERTYPE).build());
		journalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
				.dokumentInfo(DokumentInfo.builder()
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.kategori(DokumentKategoriCode.B)
						.tittel(TITTEL)
						.dokumenttypeId(DOKUMENT_TYPE_ID)
						.fildetaljerListe(new HashSet<>(Arrays.asList(FilDetaljer.builder()
								.filtype(FilTypeCode.PDF)
								.variantFormat(VariantFormatCode.ARKIV)
								.filstorrelse("1")
								.fileContent(new byte[1000])
								.build())))
						.build())
				.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.build());

		return journalpost;
	}

	private Map<String, String> createTilleggsopplysningMap() {
		Map<String, String> map = new HashMap<>();
		map.put(TILLEGGSOPPLYSNING_NOKKEL, TILLEGGSOPPLYSNING_VERDI);
		return map;
	}

	private OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg createVedlegg() {
		return new OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.Vedlegg(1l, 1l);
	}

	private DokumentInfo createDokumentInfoWithFildetaljer() {
		return DokumentInfo.builder()
				.dokumenttypeId("dokumentTypeId")
				.tittel("tittel")
				.brevkode("brevkode")
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.kategori(DokumentKategoriCode.ES)
				.fildetaljerListe(new HashSet<>(Arrays.asList(
						FilDetaljer.builder()
								.variantFormat(VariantFormatCode.ARKIV)
								.filtype(FilTypeCode.PDF)
								.fileContent("test".getBytes())
								.build(),
						FilDetaljer.builder()
								.variantFormat(VariantFormatCode.ORIGINAL)
								.filtype(FilTypeCode.RTF)
								.fileContent("test".getBytes())
								.build())))
				.build();
	}

	protected void addJournalpostDokumentInfoRelasjonWithTwoVariantFormats(Journalpost journalpost, VariantFormatCode secondVariantFormat) {
		journalpost.addJournalpostDokumentInfoRelasjon(
				JournalpostDokumentInfoRelasjon.builder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn("Tester")
						.dokumentInfo(
								DokumentInfo.builder()
										.dokumenttypeId("dokumentTypeId")
										.tittel("tittel")
										.brevkode("brevkode")
										.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
										.kategori(DokumentKategoriCode.ES)
										.fildetaljerListe(new HashSet<>(Arrays.asList(
												FilDetaljer.builder()
														.variantFormat(VariantFormatCode.ARKIV)
														.filtype(FilTypeCode.PDF)
														.fileContent("test".getBytes())
														.build(),
												FilDetaljer.builder()
														.variantFormat(secondVariantFormat)
														.filtype(FilTypeCode.RTF)
														.fileContent("test".getBytes())
														.build())))
										.build())
						.build());
	}
}