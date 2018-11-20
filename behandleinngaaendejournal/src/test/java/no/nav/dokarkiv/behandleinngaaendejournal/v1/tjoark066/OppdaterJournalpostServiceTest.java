package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.Ordering;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.DokumentInfoIkkeTilknyttetJournalpostException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.OppdaterJournalpostIkkeMuligException;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.AktoerTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.ArkivSakTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.AvsenderTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.DokumentInformasjonTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostRequestTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostTo;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidBrukerException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Test for OppdaterJournalpostService
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 29.05.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class OppdaterJournalpostServiceTest {
	private static final String USER_ID = "tokenUserId";
	private static final String LDAP_NAME = "ldapNavn";
	private static final String ENDRINGSSPORING = "E149028";
	private static final String JOURNALPOST_ID = "1";
	private static final String CONSUMER = "fpsak";

	@Rule
	public ExpectedException expected = ExpectedException.none();
	@Mock
	private NavLdapService navLdapService;
	@Mock
    private JoarkRepositoryBegrenset repository;
	@InjectMocks
	private OppdaterJournalpostService service;
	
	private OppdaterJournalpostRequestTo requestTo;
	private Journalpost journalpost;

	@Before
	public void setUp() throws Exception {
		when(navLdapService.findByUserId(eq(USER_ID))).thenReturn(NavUser.builder().description(LDAP_NAME).build());
		System.setProperty("no.nav.modig.security.systemuser.username", CONSUMER);
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
		SubjectHandlerUtils.setInternBruker(USER_ID);

		MDC.put(MDCConstants.MDC_USER_ID, USER_ID);
		MDC.put(MDCConstants.MDC_CONSUMER_ID, CONSUMER);

		service = new OppdaterJournalpostService(repository, new OppdaterJournalpostValidator(), navLdapService);
		journalpost = BehandleInngaaendeJournalDataProvider.buildJournalpost().build();
		requestTo = createRequest(false);
	}

	@Test
	public void shouldHandleJournalpostWithoutBrukere() throws Exception {
		journalpost.clearBrukere();
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));
		service.oppdaterJournalpost(requestTo);
		assertThat(journalpost.getBrukere().size(), is(1));
		assertThat(journalpost.getEndretAvNavn(), is(nullValue()));
		Bruker newBruker = journalpost.getBrukere().iterator().next();
		assertThat(newBruker.getBrukerId(), is(requestTo.getOppdaterJournalpostTo().getAktoerTo().getAktoerId()));
		assertThat(newBruker.getBrukerType(), is(requestTo.getOppdaterJournalpostTo().getAktoerTo().getBrukerTypeCode()));
		assertThat(newBruker.getOpprettetKildeNavn(), is(CONSUMER));
	}

	@Test
	public void shouldUpdateValidInput() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));
		int numberOfBrukereBeforeUpdate = journalpost.getBrukere().size();
		service.oppdaterJournalpost(requestTo);
		assertThat(journalpost.getBrukere().size(), is(numberOfBrukereBeforeUpdate));
		assertThat(journalpost.getEndretAvNavn(), is(nullValue()));
	}

	@Test
	public void shouldUpdateBrukerInfo() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));
		requestTo.getOppdaterJournalpostTo().getAktoerTo().setAktoerId(BehandleInngaaendeJournalDataProvider.ORGNR2);
		int numberOfBrukereBeforeUpdate = journalpost.getBrukere().size();
		service.oppdaterJournalpost(requestTo);
		assertThat(journalpost.getEndretAvNavn(), is(nullValue()));
		assertThat(journalpost.getBrukere().size(), is(numberOfBrukereBeforeUpdate));
		assertThat(getLatestBruker(journalpost).getEndretKildeNavn(), is(CONSUMER));
	}

	@Test
	public void shouldUpdateValidInputAndLookUpUsernameInLdap() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));
		requestTo = createRequest(true);
		int numberOfBrukereBeforeUpdate = journalpost.getBrukere().size();
		service.oppdaterJournalpost(requestTo);
		assertThat(journalpost.getBrukere().size(), is(numberOfBrukereBeforeUpdate));
		assertThat(journalpost.getEndretAvNavn(), is(LDAP_NAME));
	}

	@Test
	public void shouldUpdateValidInputAndNotLookUpUsernameInLdap() throws Exception {
		SubjectHandlerUtils.setSystemressurs(USER_ID);
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));
		requestTo = createRequest(true);
		int numberOfBrukereBeforeUpdate = journalpost.getBrukere().size();
		service.oppdaterJournalpost(requestTo);
		assertThat(journalpost.getBrukere().size(), is(numberOfBrukereBeforeUpdate));
		assertThat(journalpost.getEndretAvNavn(), is(USER_ID));
	}

	@Test
	public void shouldFailOnMissingJournalpostId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Tjenesten kan ikke utføres fordi input er ugyldig.");

		requestTo.getOppdaterJournalpostTo().setJournalpostId(null);
		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnMissingJournalpost() throws Exception {
		expected.expect(JournalpostIkkeFunnetException.class);
		expected.expectMessage("Journalpost ikke funnet.");

		requestTo.getOppdaterJournalpostTo().setJournalpostId("15");
		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnHoveddokumentIdNotInJournalpostDokumentInfoRelasjon() throws Exception {
		expected.expect(DokumentInfoIkkeTilknyttetJournalpostException.class);
		expected.expectMessage("Innsendt hoveddokument er ikke knyttet til journalposten.");

		requestTo.getOppdaterJournalpostTo().getHoveddokument().setDokumentId(123456L);

		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnVedleggIdsNotInJournalpostDokumentInfoRelasjon() throws Exception {
		expected.expect(DokumentInfoIkkeTilknyttetJournalpostException.class);
		expected.expectMessage("Ett eller flere innsendte vedlegg er ikke knyttet til journalposten.");

		for (DokumentInformasjonTo dokumentInformasjonTo : requestTo.getOppdaterJournalpostTo().getVedlegg()) {
			dokumentInformasjonTo.setDokumentId(123456L);
		}

		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailIfNotInngaaendeJournalpost() throws Exception {
		expected.expect(JournalpostIkkeInngaaendeException.class);
		expected.expectMessage("Journalpost er ikke av type Inngående.");

		journalpost.setJournalposttype(JournalpostTypeCode.U);
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailIfNotMidlertidig() throws Exception {
		expected.expect(JournalpostIkkeMidlertidigException.class);
		expected.expectMessage("Journalpost er ikke av status Midlertidig.");

		journalpost.setJournalstatus(JournalStatusCode.U);
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnFeilregistrert() throws Exception {
		expected.expect(OppdaterJournalpostIkkeMuligException.class);
		expected.expectMessage("Journalpost saksrelasjon er markert som feilregistrert.");

		journalpost.getSaksrelasjon().setFeilregistrert(true);
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnSlettetDokumentInfo() throws Exception {
		expected.expect(OppdaterJournalpostIkkeMuligException.class);
		expected.expectMessage("Dokumentet som forsøkes oppdatert er slettet.");

		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setSlettet(true);
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailIfDokumentInfoUnderRedigering() throws Exception {
		expected.expect(OppdaterJournalpostIkkeMuligException.class);
		expected.expectMessage("Dokument har ugyldig status for oppdatering. dokumentStatus=");

		journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailIfDokumentInfoAvbrutt() throws Exception {
		expected.expect(OppdaterJournalpostIkkeMuligException.class);
		expected.expectMessage("Dokument har ugyldig status for oppdatering. dokumentStatus=");

		journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.setDokumentstatus(DokumentStatusCode.AVBRUTT);
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldNotUpdateJournalpostEndretAvOnMissingFields() throws Exception {
		String endretAv = "GREEN LANTERN";
		journalpost.setEndretAvNavn(endretAv);
		OppdaterJournalpostRequestTo request = createRequest(true);

		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(request);

		Assert.assertNotEquals(endretAv, ENDRINGSSPORING);
	}

	@Test
	public void shouldNotUpdateJournalpostEndretAvOnEmptyAvsender() throws Exception {
		String endretAv = "GREEN LANTERN";
		journalpost.setEndretAvNavn(endretAv);
		OppdaterJournalpostRequestTo request = createRequest(true);
		request.getOppdaterJournalpostTo().setAktoerTo(null);

		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(request);

		Assert.assertNotEquals(endretAv, ENDRINGSSPORING);
	}

	@Test
	public void shouldFailOnMissingArkivSakSystem() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler informasjon på ArkivSak for oppdatering av journalpost. journalpostId=");

		requestTo.getOppdaterJournalpostTo().getArkivSak().setArkivSakSystem(null);

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnMissingArkivSakId() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler informasjon på ArkivSak for oppdatering av journalpost. journalpostId=");

		requestTo.getOppdaterJournalpostTo().getArkivSak().setArkivSakId(null);

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnInvalidBrukerIdentPerson() throws Exception {
		expected.expect(InvalidBrukerException.class);
		expected.expectMessage("BrukerId is not a valid fnr: ");

		requestTo.getOppdaterJournalpostTo().getAktoerTo().setAktoerId("P");
		requestTo.getOppdaterJournalpostTo().getAktoerTo().setBrukerTypeCode(BrukerTypeCode.PERSON);

		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnInvalidBrukerIdentOrganisasjon() throws Exception {
		expected.expect(InvalidBrukerException.class);
		expected.expectMessage("BrukerId is not a valid orgnr: ");

		requestTo.getOppdaterJournalpostTo().getAktoerTo().setAktoerId("O");
		requestTo.getOppdaterJournalpostTo().getAktoerTo().setBrukerTypeCode(BrukerTypeCode.ORGANISASJON);

		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnMissingDokumentId() throws Exception {
		expected.expect(InvalidBrukerException.class);
		expected.expectMessage("BrukerId is not a valid orgnr: ");

		requestTo.getOppdaterJournalpostTo().getAktoerTo().setAktoerId("123");
		requestTo.getOppdaterJournalpostTo().getAktoerTo().setBrukerTypeCode(BrukerTypeCode.ORGANISASJON);

		when(repository.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		service.oppdaterJournalpost(requestTo);
	}

	@Test
	public void shouldFailOnMissingBrukerTypeCode() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler informasjon på Aktoer for oppdatering av journalpost. journalpostId=");

		requestTo.getOppdaterJournalpostTo().getAktoerTo().setAktoerId("999999999");
		requestTo.getOppdaterJournalpostTo().getAktoerTo().setBrukerTypeCode(null);

		service.oppdaterJournalpost(requestTo);
	}

	private Bruker getLatestBruker(Journalpost journalpost) {
		assert(!journalpost.getBrukere().isEmpty());
		List<Bruker> sortedCopy = Ordering.from(new Comparator<Bruker>() {
			@Override
			public int compare(Bruker o1, Bruker o2) {
				return LocalDateTime.ofInstant(o2.getChangeStamp().getCreatedDate().toInstant(), ZoneId.systemDefault())
						.compareTo(LocalDateTime.ofInstant(o1.getChangeStamp().getCreatedDate().toInstant(), ZoneId.systemDefault()));
			}
		}).sortedCopy(journalpost.getBrukere());
		return sortedCopy.get(0);
	}


	private OppdaterJournalpostRequestTo createRequest(boolean endre) {
		return OppdaterJournalpostRequestTo.builder()
				.endringssporing(ENDRINGSSPORING)
				.oppdaterJournalpostTo(
						OppdaterJournalpostTo.builder()
								.journalpostId(JOURNALPOST_ID)
								.avsenderTo(
										AvsenderTo.builder()
												.avsenderId(endre ? BehandleInngaaendeJournalDataProvider.AVSENDER_MOTTAKERID : null)
												.avsenderNavn(endre ? BehandleInngaaendeJournalDataProvider.AVSENDER_MOTTAKER_NAVN : null)
												.build()
								)
								.innhold(endre ? BehandleInngaaendeJournalDataProvider.INNHOLD : null)
								.arkivSak(
										ArkivSakTo.builder()
												.arkivSakId(BehandleInngaaendeJournalDataProvider.ARKIV_SAKID)
												.arkivSakSystem(BehandleInngaaendeJournalDataProvider.ARKIV_SAK_FAGSYSTEM)
												.build()
								)
								.tema(endre ? BehandleInngaaendeJournalDataProvider.JOURNALPOST_FAGOMRADE : null)
								.aktoerTo(
										AktoerTo.builder()
												.aktoerId(BehandleInngaaendeJournalDataProvider.ORGNR)
												.brukerTypeCode(BrukerTypeCode.ORGANISASJON)
												.build()
								)
								.hoveddokument(
										DokumentInformasjonTo.builder()
												.dokumentId(BehandleInngaaendeJournalDataProvider.DOKUMENT_INFO_ID)
												.dokumentkategori(BehandleInngaaendeJournalDataProvider.HOVEDDOKUMENT_KATEGORI_KODE)
												.tittel(BehandleInngaaendeJournalDataProvider.TITTEL)
												.build()
								)
								.vedlegg(
										Collections.singletonList(
												DokumentInformasjonTo.builder()
														.dokumentId(BehandleInngaaendeJournalDataProvider.DOKUMENT_INFO_ID_VEDLEGG)
														.dokumentkategori(BehandleInngaaendeJournalDataProvider.VEDLEGG_KATEGORI_KODE)
														.tittel(BehandleInngaaendeJournalDataProvider.TITTEL)
														.build()
										)
								)
								.build()
				)
				.build();
	}
}