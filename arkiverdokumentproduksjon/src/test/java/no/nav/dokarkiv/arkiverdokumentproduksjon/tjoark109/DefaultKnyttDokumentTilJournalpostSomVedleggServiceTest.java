package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoInnskrenketPartsinnsynException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoIsOrganInterntException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoSlettetException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilregistrertSaksrelasjonException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FilDetaljerOnDemandException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalFagomraadeException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalTilleggsopplysningerException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostIkkeFerdigstiltException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostNotFoundException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class DefaultKnyttDokumentTilJournalpostSomVedleggServiceTest {

	private static final String OLA_NORDMANN = "Ola Nordmann";
	private static final Long DOKUMENT_INFO_ID = 123L;
	private static final Long JOURNALPOST_SOURCE_ID = 234L;
	private static final Long JOURNALPOST_TARGET_ID = 345L;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@InjectMocks
	private DefaultKnyttDokumentTilJournalpostSomVedleggService service;

	@Mock
	private KnyttDokumentTilJournalpostSomVedleggValidator validatorMock;

	@Mock
    private JoarkRepositorySkjermet repositoryMock;

	@Mock
	private SporingPopulator sporingpopulatorMock;

	@Mock
	private DokumentInfo dokumentInfoMock;

	@Mock
	private Journalpost journalpostSourceMock;

	@Mock
	private Journalpost journalpostTargetMock;

	private KnyttDokumentTilJournalpostSomVedleggRequestTo request;

	@DataPoints
	public static JournalStatusCode[] journalstatusCodes() {
		JournalStatusCode[] journalstatusCodes = JournalStatusCode.values();
		return Arrays.copyOf(journalstatusCodes, journalstatusCodes.length + 1);
	}

	@DataPoints
	public static Boolean[] booleanTypeValues() {
		return new Boolean[]{TRUE, FALSE, null};
	}

	@DataPoints
	public static DokumentStatusCode[] dokumentstatusCodes() {
		DokumentStatusCode[] dokumentstatusCodes = DokumentStatusCode.values();
		return Arrays.copyOf(dokumentstatusCodes, dokumentstatusCodes.length + 1);
	}

	@DataPoints
	public static FagomradeCode[] fagomraadeCodes() {
		return FagomradeCode.values();
	}

	@Before
	public void setUpHappyPath() throws Exception {
		MockitoAnnotations.initMocks(this);

		createRequest();

		doNothing().when(validatorMock).validate(request);

		when(repositoryMock.findById(JOURNALPOST_SOURCE_ID)).thenReturn(Optional.of(journalpostSourceMock));
		when(repositoryMock.findById(JOURNALPOST_TARGET_ID)).thenReturn(Optional.of(journalpostTargetMock));

		mockDokumentInfo();
		mockJournalpostSource();
		mockJournalpostTarget();
	}

	@Test
	public void addsJournalpostDokumentInfoRelasjonToJournalpostTargetWhenProcessing() throws Exception {
		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatJournalpostDokumentInfoRelasjonWasAdded();
	}

	@Test
	public void updatesDokumentstatusOnDokumentInfoWhenProcessingAndDokumentstatusIsNull() throws Exception {
		mockDokumentstatus(dokumentInfoMock, null);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatDokumentstatatusWasSetToFerdigstilt();
	}

	@Test
	public void doesNotUpdateDokumentstatusOnDokumentInfoWhenProcessingAndDokumentstatusIsFerdigstilt() throws Exception {
		mockDokumentstatus(dokumentInfoMock, FERDIGSTILT);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatDokumentstatusWasNotUpdated();
	}

	@Test
	public void callsSporingPopulatorOnJournalpostTargetWhenProcessing() throws Exception {
		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatSporingPopulatorWasCalled();
	}

	@Test
	public void propagatesApplicationExceptionFromValidator() throws Exception {
		ApplicationException applicationException = new ApplicationException("Validation exception");

		doThrow(applicationException).when(validatorMock).validate(request);

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Validation exception");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Test
	public void propagatesJournalpostNotFoundExceptionWhenJournalpostSourceDoesNotExist() throws Exception {
		when(repositoryMock.findById(JOURNALPOST_SOURCE_ID)).thenReturn(Optional.ofNullable(null));

		expectedException.expect(JournalpostNotFoundException.class);
		expectedException.expectMessage("Journalpost with journalpostId=234 does not exist");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void throwsIllegalJournalStatusExceptionWhenJournalpostTargetJournalstatusIsNotD(JournalStatusCode journalstatus) throws Exception {
		assumeThat(journalstatus, is(not(JournalStatusCode.D)));

		mockJournalstatus(journalpostTargetMock, journalstatus);

		expectedException.expect(IllegalJournalStatusException.class);
		expectedException.expectMessage("Journalpost with journalpostId=345 must have journalStatus 'D'");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Test
	public void performsProcessingWhenJournalpostTargetJournalstatusIsD() throws Exception {
		mockJournalstatus(journalpostTargetMock, JournalStatusCode.D);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void throwsIllegalTilleggsopplysningerExceptionWhenJournalpostTargetIsMissingEksterneVedleggTilleggsopplysning() throws Exception {
		mockHoveddokumentWithoutTilleggsinformasjon(journalpostTargetMock);

		expectedException.expect(IllegalTilleggsopplysningerException.class);
		expectedException.expectMessage("Journalpost with journalpostId=345 needs hoveddokument with tilleggsopplysning 'EksterneVedlegg'='true'");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void throwsIllegalTilleggsopplysningerExceptionWhenJournalpostTargetHasEksterneVedleggDisabled(Boolean eksterneVedleggValue) throws Exception {
		assumeThat(eksterneVedleggValue, is(not(TRUE)));

		mockHoveddokumentWithTilleggsinformasjon(journalpostTargetMock, "EksterneVedlegg", eksterneVedleggValue);

		expectedException.expect(IllegalTilleggsopplysningerException.class);
		expectedException.expectMessage("Journalpost with journalpostId=345 needs hoveddokument with tilleggsopplysning 'EksterneVedlegg'='true'");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Test
	public void performsProcessingWhenJournalpostTargetHasEksterneVedleggEnabled() throws Exception {
		mockHoveddokumentWithTilleggsinformasjon(journalpostTargetMock, "EksterneVedlegg", TRUE);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void propagatesJournalpostNotFoundExceptionWhenJournalpostTargetDoesNotExist() throws Exception {
		when(repositoryMock.findById(JOURNALPOST_TARGET_ID)).thenReturn(Optional.ofNullable(null));

		expectedException.expect(JournalpostNotFoundException.class);
		expectedException.expectMessage("Journalpost with journalpostId=345 does not exist");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void throwsJournalpostIkkeFerdigstiltExceptionWhenJournalpostSourceHasUnallowedJournalstatus(JournalStatusCode journalstatus) throws Exception {
		assumeThat(journalstatus, is(not(equalTo(J))));
		assumeThat(journalstatus, is(not(equalTo(JournalStatusCode.FS))));
		assumeThat(journalstatus, is(not(equalTo(JournalStatusCode.FL))));
		assumeThat(journalstatus, is(not(equalTo(JournalStatusCode.E))));

		mockJournalstatus(journalpostSourceMock, journalstatus);

		expectedException.expect(JournalpostIkkeFerdigstiltException.class);
		expectedException.expectMessage("Journalpost with journalpostId=234 must have one of the following journalStatus: J, FS, FL, E");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void performsProcessingWhenJournalpostSourceHasAllowedJournalstatus(JournalStatusCode journalstatus) throws Exception {
		assumeThat(journalstatus, isOneOf(J, JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E));

		mockJournalstatus(journalpostSourceMock, journalstatus);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void throwsFeilregistrertSaksrelasjonExceptionWhenJournalpostSourceSaksrelasjonIsFeilregistrert() throws Exception {
		mockSaksrelasjonFeilregistrert(journalpostSourceMock, true);

		expectedException.expect(FeilregistrertSaksrelasjonException.class);
		expectedException.expectMessage("Journalpost with journalpostId=234 cannot have saksrelasjon that is feilregistrert");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void performsProcessingWhenJournalpostSourceSaksrelasjonIsNotFeilregistrert(Boolean feilregistrertValue) throws Exception {
		assumeThat(feilregistrertValue, is(not(TRUE)));

		mockSaksrelasjonFeilregistrert(journalpostSourceMock, feilregistrertValue);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void throwsDokumentInfoNotFoundExceptionWhenJournalpostSourceDoesNotContainTheSpecifiedDokumentInfo() throws Exception {
		when(journalpostSourceMock.findDokumentInfoById(DOKUMENT_INFO_ID)).thenReturn(null);

		expectedException.expect(DokumentInfoNotFoundException.class);
		expectedException.expectMessage("Journalpost with journalpostId=234 has no DokumentInfo with dokumentInfoId=123");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Test
	public void performsProcessingWhenJournalpostSourceContainsTheSpecifiedDokumentInfo() throws Exception {
		when(journalpostSourceMock.findDokumentInfoById(DOKUMENT_INFO_ID)).thenReturn(dokumentInfoMock);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Theory
	public void throwsIllegalDokumentstatusExceptionWhenDokumentInfoHasUnallowedDokumentstatus(DokumentStatusCode dokumentstatus) throws Exception {
		assumeThat(dokumentstatus, not(isOneOf(null, FERDIGSTILT)));

		mockDokumentstatus(dokumentInfoMock, dokumentstatus);

		expectedException.expect(IllegalDokumentstatusException.class);
		expectedException.expectMessage("DokumentInfo with dokumentInfoId=123 must have dokumentstatus 'FERDIGSTILT' or undefined");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Test
	public void throwsDokumentInfoSlettetExceptionWhenDokumentInfoIsSlettet() throws Exception {
		mockSlettet(dokumentInfoMock, true);

		expectedException.expect(DokumentInfoSlettetException.class);
		expectedException.expectMessage("DokumentInfo with dokumentInfoId=123 must not be deleted");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void performsProcessingWhenDokumentInfoIsNotSlettet(Boolean slettetValue) throws Exception {
		assumeThat(slettetValue, is(not(TRUE)));

		mockSlettet(dokumentInfoMock, slettetValue);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void throwsDokumentInfoIsOrganInterntExceptionWhenDokumentInfoIsOrganIntern() throws Exception {
		mockOrganInternt(dokumentInfoMock, true);

		expectedException.expect(DokumentInfoIsOrganInterntException.class);
		expectedException.expectMessage("DokumentInfo with dokumentInfoId=123 cannot be organ intern");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void performsProcessingWhenDokumentInfoIsNotOrganIntern(Boolean organInternValue) throws Exception {
		assumeThat(organInternValue, is(not(TRUE)));

		mockOrganInternt(dokumentInfoMock, organInternValue);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void throwsDokumentInfoInnskrenketPartsinnsynExceptionWhenDokumentInfoHasInnskrenketPartsinnsyn() throws Exception {
		mockInnskrenketPartsinnsyn(dokumentInfoMock, true);

		expectedException.expect(DokumentInfoInnskrenketPartsinnsynException.class);
		expectedException.expectMessage("DokumentInfo with dokumentInfoId=123 cannot have innskrenket partsinnsyn");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void performsProcessingWhenDokumentInfoDoesNotHaveInnskrenketPartsinnsyn(Boolean innskrenketPartsinnsynValue) throws Exception {
		assumeThat(innskrenketPartsinnsynValue, is(not(TRUE)));

		mockInnskrenketPartsinnsyn(dokumentInfoMock, innskrenketPartsinnsynValue);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void throwsDokumentInfoInnskrenketPartsinnsynExceptionWhenDokumentInfoHasInnskrenketPartsinnsynFraTredjepart() throws Exception {
		mockInnskrenketPartsinnsynFraTredjepart(dokumentInfoMock, true);

		expectedException.expect(DokumentInfoInnskrenketPartsinnsynException.class);
		expectedException.expectMessage("DokumentInfo with dokumentInfoId=123 cannot have innskrenket partsinnsyn fra tredjepart");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void performsProcessingWhenDokumentInfoDoesNotHaveInnskrenketPartsinnsynFraTredjepart(Boolean innskrenketPartsinnsynValue) throws Exception {
		assumeThat(innskrenketPartsinnsynValue, is(not(TRUE)));

		mockInnskrenketPartsinnsynFraTredjepart(dokumentInfoMock, innskrenketPartsinnsynValue);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void throwsFilDetaljerOnDemandExceptionWhenDokumentInfoContainsFildetaljerWithOnDemandIdSet() throws Exception {
		mockFildetaljerWithOnDemandId(dokumentInfoMock);

		expectedException.expect(FilDetaljerOnDemandException.class);
		expectedException.expectMessage("DokumentInfo with dokumentInfoId=123 cannot have fildetaljer with onDemandId defined");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Test
	public void performsProcessingWhenDokumentInfoHasNoFildetaljerWithOnDemandIdSet() throws Exception {
		mockNoFildetaljerOnDemandId(dokumentInfoMock);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Test
	public void throwsIllegalVariantFormatExceptionWhenDokumentInfoHasNoFildetaljerWithArkivVariantFormat() throws Exception {
		mockNoFildetaljerWithArkivVariantFormat(dokumentInfoMock);

		expectedException.expect(IllegalVariantFormatException.class);
		expectedException.expectMessage("DokumentInfo with dokumentInfoId=123 requires at least one fildetalj with variantFormat 'ARKIV'");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Test
	public void performsProcessingWhenDokumentInfoContainsFildetaljerWithArkivVariantFormat() throws Exception {
		mockFildetaljerWithArkivVariantFormat(dokumentInfoMock);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	@Theory
	public void throwsIllegalFagomraadeExceptionWhenJournalpostSourceFagomraadeIsIllegal(FagomradeCode fagomraadeSource, FagomradeCode fagomraadeTarget) throws Exception {
		assumeThat(fagomraadeSource, not(isOneOf(FagomradeCode.OPP, FagomradeCode.GEN, fagomraadeTarget)));

		mockFagomrade(journalpostSourceMock, fagomraadeSource);
		mockFagomrade(journalpostTargetMock, fagomraadeTarget);

		expectedException.expect(IllegalFagomraadeException.class);
		expectedException.expectMessage("Journalpost source with journalpostId=234 must have fagomrade 'OPP' or 'GEN',"
				+ " or it must be equal to fagomrade on the target journalpost (journalpostId=345)");

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasNotCompleted();
	}

	@Theory
	public void performsProcessingWhenJournalpostSourceFagomraadeIsLegal(FagomradeCode fagomraadeSource, FagomradeCode fagomraadeTarget) throws Exception {
		assumeThat(fagomraadeSource, isOneOf(FagomradeCode.OPP, FagomradeCode.GEN, fagomraadeTarget));

		mockFagomrade(journalpostSourceMock, fagomraadeSource);
		mockFagomrade(journalpostTargetMock, fagomraadeTarget);

		service.knyttDokumentTilJournalpostSomVedlegg(request);

		assertThatProcessingWasCompletedSuccessfully();
	}

	private void createRequest() {
		request = new KnyttDokumentTilJournalpostSomVedleggRequestTo();
		request.setDokumentInfoId(DOKUMENT_INFO_ID);
		request.setKnyttesFraJournalpostId(JOURNALPOST_SOURCE_ID);
		request.setKnyttesTilJournalpostId(JOURNALPOST_TARGET_ID);
		request.setEndretAvNavn(OLA_NORDMANN);
	}

	private void mockDokumentInfo() {
		when(dokumentInfoMock.getDokumentInfoId()).thenReturn(DOKUMENT_INFO_ID);

		mockDokumentstatus(dokumentInfoMock, null);
		mockSlettet(dokumentInfoMock, false);
		mockOrganInternt(dokumentInfoMock, false);
		mockInnskrenketPartsinnsyn(dokumentInfoMock, false);
		mockInnskrenketPartsinnsynFraTredjepart(dokumentInfoMock, false);
		mockNoFildetaljerOnDemandId(dokumentInfoMock);
		mockFildetaljerWithArkivVariantFormat(dokumentInfoMock);
	}

	private void mockJournalpostSource() {
		when(journalpostSourceMock.getJournalpostId()).thenReturn(JOURNALPOST_SOURCE_ID);
		when(journalpostSourceMock.findDokumentInfoById(DOKUMENT_INFO_ID)).thenReturn(dokumentInfoMock);

		mockJournalstatus(journalpostSourceMock, J);
		mockFagomrade(journalpostSourceMock, FagomradeCode.AAP);
		mockSaksrelasjonFeilregistrert(journalpostSourceMock, FALSE);
	}

	private void mockJournalpostTarget() {
		when(journalpostTargetMock.getJournalpostId()).thenReturn(JOURNALPOST_TARGET_ID);

		mockJournalstatus(journalpostTargetMock, JournalStatusCode.D);
		mockFagomrade(journalpostTargetMock, FagomradeCode.AAP);
		mockHoveddokumentWithTilleggsinformasjon(journalpostTargetMock, "EksterneVedlegg", true);
	}

	private void mockDokumentstatus(DokumentInfo dokumentInfoMock, DokumentStatusCode dokumentstatus) {
		when(dokumentInfoMock.getDokumentstatus()).thenReturn(dokumentstatus);
	}

	private void mockSlettet(DokumentInfo dokumentInfoMock, Boolean slettet) {
		when(dokumentInfoMock.getSlettet()).thenReturn(slettet);
	}

	private void mockOrganInternt(DokumentInfo dokumentInfoMock, Boolean organInternt) {
		when(dokumentInfoMock.getOrganInternt()).thenReturn(organInternt);
	}

	private void mockInnskrenketPartsinnsyn(DokumentInfo dokumentInfoMock, Boolean innskrenketPartsinnsyn) {
		when(dokumentInfoMock.getInnskrenketPartsinnsyn()).thenReturn(innskrenketPartsinnsyn);
	}

	private void mockInnskrenketPartsinnsynFraTredjepart(DokumentInfo dokumentInfoMock, Boolean innskrenketPartsinnsynFraTredjepart) {
		when(dokumentInfoMock.getInnskrenketPartsinnsynFraTredjepart()).thenReturn(innskrenketPartsinnsynFraTredjepart);
	}

	private void mockFildetaljerWithOnDemandId(DokumentInfo dokumentInfoMock) {
		FilDetaljer fildetaljerMock = mock(FilDetaljer.class);
		when(fildetaljerMock.getOnDemandId()).thenReturn("on-demand-id");
		when(fildetaljerMock.getOnDemandInstans()).thenReturn(OnDemandInstansCode.SYFO);

		mockNoFildetaljerOnDemandId(dokumentInfoMock);
		dokumentInfoMock.getFildetaljerListe().add(fildetaljerMock);
	}

	private void mockNoFildetaljerOnDemandId(DokumentInfo dokumentInfoMock) {
		Set<FilDetaljer> fildetaljer = new HashSet<>(1);
		when(dokumentInfoMock.getFildetaljerListe()).thenReturn(fildetaljer);
	}

	private void mockFildetaljerWithArkivVariantFormat(DokumentInfo dokumentInfoMock) {
		FilDetaljer fildetaljerMock = mock(FilDetaljer.class);
		when(dokumentInfoMock.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV)).thenReturn(fildetaljerMock);
	}

	private void mockNoFildetaljerWithArkivVariantFormat(DokumentInfo dokumentInfoMock) {
		when(dokumentInfoMock.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV)).thenReturn(null);
	}

	private void mockJournalstatus(Journalpost journalpostMock, JournalStatusCode journalstatus) {
		when(journalpostMock.getJournalstatus()).thenReturn(journalstatus);
	}

	private void mockFagomrade(Journalpost journalpostMock, FagomradeCode fagomrade) {
		when(journalpostMock.getFagomrade()).thenReturn(fagomrade);
	}

	private void mockSaksrelasjonFeilregistrert(Journalpost journalpostMock, Boolean saksrelasjonFeilregistrert) {
		Saksrelasjon saksrelasjonMock = mock(Saksrelasjon.class);
		when(saksrelasjonMock.getFeilregistrert()).thenReturn(saksrelasjonFeilregistrert);
		when(journalpostMock.getSaksrelasjon()).thenReturn(saksrelasjonMock);
	}

	private void mockHoveddokumentWithoutTilleggsinformasjon(Journalpost journalpostMock) {
		Map<String, String> tilleggsopplysninger = new HashMap<>(1);

		DokumentInfo hoveddokumentInfoMock = mock(DokumentInfo.class);
		when(hoveddokumentInfoMock.getTilleggsopplysninger()).thenReturn(tilleggsopplysninger);

		JournalpostDokumentInfoRelasjon relasjonMock = mock(JournalpostDokumentInfoRelasjon.class);
		when(relasjonMock.getDokumentInfo()).thenReturn(hoveddokumentInfoMock);
		when(journalpostMock.findHoveddokumentDokumentInfoRelasjon()).thenReturn(relasjonMock);
	}

	private void mockHoveddokumentWithTilleggsinformasjon(Journalpost journalpostMock, String key, Object value) {
		mockHoveddokumentWithoutTilleggsinformasjon(journalpostMock);

		DokumentInfo dokumentInfo = journalpostMock.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		Map<String, String> tilleggsopplysninger = dokumentInfo.getTilleggsopplysninger();

		if (value == null) {
			tilleggsopplysninger.put(key, null);
		} else {
			tilleggsopplysninger.put(key, value.toString());
		}
	}

	private void assertThatProcessingWasNotCompleted() {
		verify(journalpostTargetMock, never()).addJournalpostDokumentInfoRelasjon(any(JournalpostDokumentInfoRelasjon.class));
		assertThatDokumentstatusWasNotUpdated();
		assertThatSporingPopulatorWasNotCalled();
	}

	private void assertThatProcessingWasCompletedSuccessfully() {
		assertThatJournalpostDokumentInfoRelasjonWasAdded();
		assertThatDokumentstatatusWasSetToFerdigstilt();
		assertThatSporingPopulatorWasCalled();
	}

	private void assertThatJournalpostDokumentInfoRelasjonWasAdded() {
		ArgumentCaptor<JournalpostDokumentInfoRelasjon> captor = ArgumentCaptor.forClass(JournalpostDokumentInfoRelasjon.class);

		verify(journalpostTargetMock).addJournalpostDokumentInfoRelasjon(captor.capture());

		JournalpostDokumentInfoRelasjon capturedRelasjon = captor.getValue();
		assertThat(capturedRelasjon.getDokumentInfo(), is(sameInstance(dokumentInfoMock)));
		assertThat(capturedRelasjon.getTilknyttetJournalpostSom(), is(VEDLEGG));
		assertThat(capturedRelasjon.getTilknyttetAvNavn(), is(equalTo(OLA_NORDMANN)));
	}

	private void assertThatDokumentstatusWasNotUpdated() {
		verify(dokumentInfoMock, never()).setDokumentstatus(any(DokumentStatusCode.class));
	}

	private void assertThatDokumentstatatusWasSetToFerdigstilt() {
		verify(dokumentInfoMock).setDokumentstatus(FERDIGSTILT);
	}

	private void assertThatSporingPopulatorWasNotCalled() {
		verify(sporingpopulatorMock, never()).populateSporingInfo(any(Journalpost.class), anyString());
	}

	private void assertThatSporingPopulatorWasCalled() {
		verify(sporingpopulatorMock).populateSporingInfo(journalpostTargetMock, "Dokumentproduksjon");
	}
}
