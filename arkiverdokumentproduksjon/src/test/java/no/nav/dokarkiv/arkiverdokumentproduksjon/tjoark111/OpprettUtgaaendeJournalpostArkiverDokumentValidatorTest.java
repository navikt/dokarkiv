package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.BRUKERTYPE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DATO_DOKUMENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DATO_JOURNAL;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.EKSTERNPART_NAVN;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.FAGOMRADE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.INNHOLD;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.JOURNALFOERENDE_ENHET_REF;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KANAL_REF_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KRYSSREFERANSE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KRYSSREFERANSE_TYPE;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.PERSONIDENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.SAKSID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.TITTEL;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ValideringAvVedleggFeiletException;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.journalbehandling.DefaultJournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.DefaultMandatoryFieldsVerifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DefaultMandatoryFieldsVerifier.class,
		OpprettUtgaaendeJournalpostArkiverDokumentValidator.class,
		DefaultJournalpostStructureVerifier.class})
public class OpprettUtgaaendeJournalpostArkiverDokumentValidatorTest {
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final boolean SENSITIVT_REQUEST = true;
	private static final boolean FERDIGSTILL_JOURNALPOST = true;
	private static final boolean IKKE_FERDIGSTILL_JOURNALPOST = false;

	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";

	@Rule
	public ExpectedException expected = ExpectedException.none();

	private Journalpost journalpost;

	@Inject
	private OpprettUtgaaendeJournalpostArkiverDokumentValidator validator;

	@Before
	public void setUp() {
		journalpost = createJournalpost();
	}

	//Saksrelasjon
	@Test
	public void shouldThrowExceptionIfNoSaksrelasjonOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("saksrelasjon must be set");
		journalpost.setSaksrelasjon(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoSaksIDOnJournalpostIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("sakId must be set");
		journalpost.getSaksrelasjon().setSakId(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfSaksRelasjonFagsystemOnJournalpostIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("fagsystem must be set");
		journalpost.getSaksrelasjon().setFagsystem(null);
		validator.validate(journalpost);
	}


	//Journalpost
	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("fagomrade must be set");
		journalpost.setFagomrade(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfOpprettetAvNavnNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("opprettetAvNavn must be set");
		journalpost.setOpprettetAvNavn(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalforendeEnhetIdNotSetOnJournalpost() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Mangler påkrevd attributt: Journalpost.JournalForendeEnhetId");
		journalpost.setJournalForendeEnhetId(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfInnholdNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("innhold must be set");
		journalpost.setInnhold(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfAvsenderMottakerNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("avsenderMottaker must be set");
		journalpost.setAvsenderMottaker(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfDokumentDatoIsMissing() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Mangler påkrevd attributt: Journalpost.DokumentDato");

		journalpost.setDokumentDato(null);
		validator.validate(journalpost);
	}

	//Ekstra test for JP
	@Test
	public void shouldThrowExceptionIfJournalstatusIsNotSet() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("journalstatus must be set");
		journalpost.setJournalstatus(null);

		validator.validate(journalpost);
	}

	//Brukere
	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("Journalpost must have at least one Bruker");
		journalpost.clearBrukere();
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("brukerId must be set");
		journalpost.getBrukere().iterator().next().setBrukerId(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfBrukerTypeIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("brukerType must be set");
		journalpost.getBrukere().iterator().next().setBrukerType(null);
		validator.validate(journalpost);
	}

	//DokumentInfo
	@Test
	public void shouldThrowExceptionIfNoDokumentInfoObjectOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("dokumentInfo must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoKategoriOnDocumentInfo() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("DokumentInfo.kategori must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setKategori(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoTittelOnDocumentInfo() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("DokumentInfo.tittel must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoDokumenttypeIdOnDocumentInfo() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Mangler påkrevd attributt: DokumentInfo.DokumenttypeId");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumenttypeId(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoFilTypeOnFildetaljerOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("filtype must be set");
		journalpost.findAllFilDetaljer().get(0).setFiltype(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfGotArkivVariantButOneVarianIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("variantFormat must be set");
		Journalpost journalpost = createJournalpost();
		journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setVariantFormat(null);
		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionNoUtsendingskanal() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Mangler påkrevd attributt: Journalpost.Utsendingskanal");

		journalpost.setUtsendingskanal(null);
		validator.validate(journalpost);
	}

	//FilDetaljer
	@Test
	public void shouldThrowExceptionIfVariantFormatArkivIsMissing() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("DokumentInfos must contain an arkiv variant");
		journalpost.findAllFilDetaljer().get(0).setVariantFormat(null);
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfDokumentDuplicatesOnJournalpost() throws UgyldigInputException {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("DokumentInfo cannot contain dokumentvariant duplicates");
		addJournalpostDokumentInfoRelasjonWithTwoVariantFormats(journalpost, VariantFormatCode.ARKIV);
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfTwoHoveddokuments() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Journalpost cannot contain more than one hoveddokument when endelig journalforing");
		journalpost.addJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.build());
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}

	@Test
	public void shouldNotThrowExceptionIfGotArkivVariantButOneVarianIsNull() throws Exception {
		Journalpost journalpost = createJournalpost();
		addJournalpostDokumentInfoRelasjonWithTwoVariantFormats(journalpost, null);
		validator.validateVariantFormaterAndHoveddokument(journalpost);
	}

	@Test
	public void shouldThrowIfInputJournalStatusIsD() throws Exception {
		expected.expect(ValideringAvVedleggFeiletException.class);
		expected.expectMessage("Journalpost.JournalStatus kan ikke være D");
		journalpost.setJournalstatus(JournalStatusCode.D);
		validator.validateVedlegg(journalpost, journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), createVedlegg());
	}

	@Test
	public void shouldThrowIfInputDokumentInfoIdIsNotFerdigstilt() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("DokumentInfo.Dokumentstatus må være FERDIGSTILT men var UNDER_REDIGERING");
		validator.validateVedleggDokumentInfo(DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
				.build());
	}

	@Test
	public void shouldThrowIfInputDokumentInfoSlettetIsTrue() throws Exception {
		expected.expect(ValideringAvVedleggFeiletException.class);
		expected.expectMessage("DokumentInfo.slettet kan ikke være True");
		validator.validateVedlegg(journalpost, DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.slettet(true)
				.build(), createVedlegg());
	}

	@Test
	public void shouldThrowIfInputDokumentInfoOrganinterntIsTrue() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("DokumentInfo.OrganInternt kan ikke være True");
		validator.validateVedleggDokumentInfo(DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.organInternt(true)
				.build());
	}

	@Test
	public void shouldThrowIfInputDokumentInfoInnskrenketPartsinnsynIsTrue() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("DokumentInfo.innskrenketPartsinnsyn kan ikke være True");
		validator.validateVedleggDokumentInfo(DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.innskrenketPartsinnsyn(true)
				.build());
	}

	@Test
	public void shouldThrowIfInputDokumentInfoInnskrenketPartsinnsynFraTredjePartIsTrue() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("DokumentInfo.innskrenketPartsinnsynFraTredjepart kan ikke være True");
		validator.validateVedleggDokumentInfo(DokumentInfo.builder()
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.innskrenketPartsinnsynFraTredjepart(true)
				.build());
	}

	@Test
	public void shouldThrowIfInputDokumentInfoFildetaljerHasNoVariantFormatWithArkiv() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Vedlegg mangler Fildetaljer med variantFormat=ARKIV");
		DokumentInfo dokumentInfo = createDokumentInfoWithFildetaljer();
		dokumentInfo.getFildetaljerListe().forEach(filDetaljer -> filDetaljer.setVariantFormat(VariantFormatCode.ORIGINAL));
		validator.validateVedleggFildetaljer(dokumentInfo);
	}

	@Test
	public void shouldThrowIfInputDokumentInfoFildetaljerOnDemandIdIsNotNull() {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Fildetaljer.OnDemandId kan ikke være satt");
		DokumentInfo dokumentInfo = createDokumentInfoWithFildetaljer();
		dokumentInfo.getFildetaljerListe().iterator().next().setOnDemandId("adsad");
		validator.validateVedleggFildetaljer(dokumentInfo);
	}


	@Test
	public void shouldThrowIfInputIsMissingKanalreferanseId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("kanalReferanseId");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().setKanalReferanseId(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingOpprettetAvNavn() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("opprettetAvNavn");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().setOpprettetAvNavn(null);
		validator.validateRequiredFields(requestTo);
	}


	@Test
	public void shouldThrowIfInputIsMissinJournalpostDokumentInfoRelasjoner() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("journalpostDokumentInfoRelasjoner");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.setJournalpost(Journalpost.builder().kanalReferanseId("ads").build());
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingDokumentInfo() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("DokumentInfo");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().setDokumentInfo(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingJournalpostDokumentInfoKategori() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Kategori");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setKategori(null);
		validator.validateRequiredFields(requestTo);
	}


	@Test
	public void shouldThrowIfInputIsMissingJournalpostDokumentInfoTittel() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Tittel");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setTittel(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingJournalpostDokumentInfoDokumenttypeId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("DokumenttypeId");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().setDokumenttypeId(null);
		validator.validateRequiredFields(requestTo);
	}


	@Test
	public void shouldThrowIfInputIsMissingJournalpostFildetaljerFiltype() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Filtype");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost()
				.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setFiltype(null);
		validator.validateRequiredFields(requestTo);
	}


	@Test
	public void shouldThrowIfInputIsMissingJournalpostFildetaljerVariantFormat() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("VariantFormat");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost()
				.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setVariantFormat(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingJournalpostFildetaljerIkkeRedigerbartDokument() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("IkkeRedigerbartDokument");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost()
				.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setFileContent(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingVedleggKnyttesFraJournalpost() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("KnyttesFraJournalpostId");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getVedleggList().get(0).setKnyttesFraJournalpostId(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingVedleggDokumentinfoId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("DokumentInfoId");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getVedleggList().get(0).setDokumentInfoId(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingSaksrelasjonSaksnummer() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Saksnummer");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().getSaksrelasjon().setSakId(null);
		validator.validateRequiredFields(requestTo);
	}


	@Test
	public void shouldThrowIfInputIsMissingSaksrelasjonFagsystem() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Fagsystem");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().getSaksrelasjon().setFagsystem(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingKryssreferanseReferanseId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("ReferanseId");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().getKryssreferanser().iterator().next().setReferanseId(null);
		validator.validateRequiredFields(requestTo);
	}

	@Test
	public void shouldThrowIfInputIsMissingKryssreferanseReferanseType() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("ReferanseType");
		OpprettUtgaaendeJournalpostArkiverDokumentRequestTo requestTo = createRequestTo();
		requestTo.getJournalpost().getKryssreferanser().iterator().next().setReferanseType(null);
		validator.validateRequiredFields(requestTo);
	}


	private OpprettUtgaaendeJournalpostArkiverDokumentRequestTo createRequestTo() {
		return OpprettUtgaaendeJournalpostArkiverDokumentRequestTo.builder()
				.journalpost(createJournalpost())
				.journalforendeEnhet("ads")
				.vedleggList(Arrays.asList(createVedlegg())).build();
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
						.sakId("1")
						.fagsystem(FagsystemCode.AO01)
						.build()).build();
		journalpost.addBruker(Bruker.builder().brukerId(PERSONIDENT).brukerType(BRUKERTYPE).build());
		journalpost.addKryssReferanse(Kryssreferanse.builder()
				.referanseType(KRYSSREFERANSE_TYPE)
				.referanseId(KRYSSREFERANSE_ID)
				.build());
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
								.filtype(FilTypeCode.AFP)
								.fileContent("test".getBytes())
								.build(),
						FilDetaljer.builder()
								.variantFormat(VariantFormatCode.ORIGINAL)
								.filtype(FilTypeCode.DOC)
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
										.innskrenketPartsinnsyn(true)
										.brevkode("brevkode")
										.organInternt(false)
										.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
										.kategori(DokumentKategoriCode.ES)
										.fildetaljerListe(new HashSet<>(Arrays.asList(
												FilDetaljer.builder()
														.variantFormat(VariantFormatCode.ARKIV)
														.filtype(FilTypeCode.AFP)
														.fileContent("test".getBytes())
														.build(),
												FilDetaljer.builder()
														.variantFormat(secondVariantFormat)
														.filtype(FilTypeCode.DOC)
														.fileContent("test".getBytes())
														.build())))
										.build())
						.build());
	}
}