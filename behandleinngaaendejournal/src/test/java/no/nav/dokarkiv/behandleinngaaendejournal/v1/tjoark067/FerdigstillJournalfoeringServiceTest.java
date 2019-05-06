package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067;

import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.DOKUMENT_INFO_ID_VEDLEGG;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.OPPRETTET_AV_NAVN;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.buildJournalpost;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.buildNoRelasjonJournalpost;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.createFilDetaljerArkiv;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.createFilDetaljerProduksjon;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.createHovedDokumentInfo;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.createHoveddokumentInfoNoFildetaljer;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.createVedleggDokumentInfo;
import static no.nav.dokarkiv.behandleinngaaendejournal.v1.BehandleInngaaendeJournalDataProvider.createVedleggDokumentInfoNoFildetaljer;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.FerdigstillingIkkeMuligException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.journalbehandling.DefaultJournalpostStructureVerifier;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(MockitoJUnitRunner.class)
public class FerdigstillJournalfoeringServiceTest {
	private static final String USER_ID = "A123456";
	private static final String LDAP_NAME = "ldapNavn";

	private static final String ENHET = "0128";

	public static final long DOKUMENT_INFO_ID = 1L;
	public static final String CONSUMER = "fpsak";

	@Mock
	private NavLdapService navLdapService;

	@Mock
    private JoarkRepositorySkjermet repository;

	private FerdigstillJournalfoeringService ferdigstillJournalfoeringService;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		when(navLdapService.findByUserId(eq(USER_ID))).thenReturn(NavUser.builder().description(LDAP_NAME).build());
		System.setProperty("no.nav.modig.security.systemuser.username", CONSUMER);
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
		SubjectHandlerUtils.setInternBruker(USER_ID);

		MDC.put(MDC_CONSUMER_ID, CONSUMER);
		MDC.put(MDC_USER_ID, USER_ID);

		ferdigstillJournalfoeringService = new FerdigstillJournalfoeringService(repository,
				new FerdigstillJournalfoeringFieldValidator(),
				new DefaultJournalpostStructureVerifier(),
				navLdapService);
	}

	@Test
	public void should_throw_ugyldiginputexception_when_input_missing_journalpostId() throws Exception {
		thrown.expect(UgyldigInputException.class);
		thrown.expectMessage("journalpostId");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(FerdigstillJournalfoeringTo.builder().enhetId(ENHET).build());
	}

	@Test
	public void should_throw_ugyldiginputexception_when_input_missing_enhetsId() throws Exception {
		thrown.expect(UgyldigInputException.class);
		thrown.expectMessage("enhetId");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(FerdigstillJournalfoeringTo.builder().journalpostId("1").build());
	}

	@Test
	public void should_ferdigstille_journalpost_when_valid_input_and_journalpost() throws Exception {
		Journalpost actualJournalpost = buildJournalpost().journalDato(null).build();
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(actualJournalpost));

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());

		assertThat(actualJournalpost.getJournalstatus(), is(JournalStatusCode.J));
		assertThat(actualJournalpost.getJournalForendeEnhetId(), is(ENHET));
		assertThat(actualJournalpost.getJournalDato(), notNullValue());
		assertThat(actualJournalpost.getJournalfortAvNavn(), is(LDAP_NAME));
		assertThat(actualJournalpost.getEndretKildeNavn(), is(CONSUMER));
	}

	@Test
	public void should_ferdigstille_journalpost_when_valid_input_and_journalpost_not_LldapLookUp() throws Exception {
		SubjectHandlerUtils.setSystemressurs(USER_ID);
		Journalpost actualJournalpost = buildJournalpost().journalDato(null).build();
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(actualJournalpost));

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());

		assertThat(actualJournalpost.getJournalstatus(), is(JournalStatusCode.J));
		assertThat(actualJournalpost.getJournalForendeEnhetId(), is(ENHET));
		assertThat(actualJournalpost.getJournalDato(), notNullValue());
		assertThat(actualJournalpost.getJournalfortAvNavn(), is(USER_ID));
		assertThat(actualJournalpost.getEndretKildeNavn(), is(CONSUMER));
	}

	@Test
	public void should_ferdigstille_journalpost_when_dato_dokument_is_null() throws Exception {
		Journalpost actualJournalpost = buildJournalpost().journalDato(null).dokumentDato(null).build();
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(actualJournalpost));

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());

		assertThat(actualJournalpost.getJournalstatus(), is(JournalStatusCode.J));
		assertThat(actualJournalpost.getJournalForendeEnhetId(), is(ENHET));
		assertThat(actualJournalpost.getJournalDato(), notNullValue());
		assertThat(actualJournalpost.getJournalfortAvNavn(), is(LDAP_NAME));
		assertThat(actualJournalpost.getEndretKildeNavn(), is(CONSUMER));
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_fields_missing() throws Exception {
		Journalpost actualJournalpost = buildJournalpost().innhold(null).build();
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(actualJournalpost));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("innhold");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_journalpostikkefunnetexception_when_journalpostid_not_found() throws Exception {
		thrown.expect(JournalpostIkkeFunnetException.class);

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_ferdigstillikkemuligexception_when_no_hoveddokument_exists() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildNoRelasjonJournalpost()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
						.dokumentInfo(createVedleggDokumentInfo(DOKUMENT_INFO_ID_VEDLEGG).build())
						.build())
				.build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("Journalpost must contain a hoveddokument");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_ferdigstillikkemuligexception_when_no_arkivvariant_on_hoveddokument() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildNoRelasjonJournalpost()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
						.dokumentInfo(createHoveddokumentInfoNoFildetaljer()
								.filDetaljerList(createFilDetaljerProduksjon()).build())
						.build())
				.build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("All the Journalpost's DokumentInfos must contain an arkiv variant");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_ferdigstillikkemuligexception_when_no_arkivvariant_on_vedlegg() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildNoRelasjonJournalpost()
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createHovedDokumentInfo().build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createVedleggDokumentInfoNoFildetaljer(DOKUMENT_INFO_ID_VEDLEGG)
										.filDetaljerList(createFilDetaljerProduksjon()).build())
								.build())
				.build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("All the Journalpost's DokumentInfos must contain an arkiv variant");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}


	@Test
	public void should_throw_ferdigstillikkemuligexception_when_duplicate_variantformat_on_hoveddokument() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildNoRelasjonJournalpost()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
						.dokumentInfo(createHoveddokumentInfoNoFildetaljer()
								.filDetaljerList(createFilDetaljerArkiv(), createFilDetaljerArkiv()).build())
						.build())
				.build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("DokumentInfo cannot contain dokumentvariant duplicates");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_ferdigstillikkemuligexception_when_duplicate_variantformat_on_vedlegg() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildNoRelasjonJournalpost()
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createHoveddokumentInfoNoFildetaljer()
										.filDetaljerList(createFilDetaljerArkiv(), createFilDetaljerProduksjon()).build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createVedleggDokumentInfoNoFildetaljer(DOKUMENT_INFO_ID_VEDLEGG)
										.filDetaljerList(createFilDetaljerArkiv(), createFilDetaljerArkiv()).build())
								.build())
				.build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("DokumentInfo cannot contain dokumentvariant duplicates");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_journalpost_not_midlertidig_journalfoert() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildJournalpost().journalStatus(JournalStatusCode.J).build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("Journalpost er ikke midlertidig journalført. journalpostId=");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_ferdigstillingikkemuligexception_when_journalpost_sak_is_feilregistrert() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildJournalpost().saksrelasjon(getSaksrelasjonBuilder().feilregistrert(true).build()).build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("Sak tilknyttet Journalpost er feilregistrert. journalpostId=");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_journalpostikkeinngaaendeexception_when_journalpost_not_inngaaende() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildJournalpost().journalpostType(JournalpostTypeCode.U).build()));

		thrown.expect(JournalpostIkkeInngaaendeException.class);
		thrown.expectMessage("Journalpost gjelder ikke for en inngående forsendelse. journalpostId=");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_ferdigstillingikkemulig_when_hoveddokument_is_under_redigering() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildNoRelasjonJournalpost()
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createHovedDokumentInfo().dokumentstatus(DokumentStatusCode.UNDER_REDIGERING).build())
								.build())
				.build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("Journalpost cannot contain DokumentInfos with status 'under redigering'");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	@Test
	public void should_throw_ferdigstillingikkemulig_when_vedlegg_is_under_redigering() throws Exception {
		when(repository.findById(any(Long.class))).thenReturn(Optional.of(buildNoRelasjonJournalpost()
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createHovedDokumentInfo().dokumentstatus(DokumentStatusCode.FERDIGSTILT).build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createVedleggDokumentInfo(DOKUMENT_INFO_ID_VEDLEGG).dokumentstatus(DokumentStatusCode.FERDIGSTILT).build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.tilknyttetAvNavn(OPPRETTET_AV_NAVN)
								.dokumentInfo(createVedleggDokumentInfo(3L).dokumentstatus(DokumentStatusCode.UNDER_REDIGERING).build())
								.build())
				.build()));

		thrown.expect(FerdigstillingIkkeMuligException.class);
		thrown.expectMessage("Journalpost cannot contain DokumentInfos with status 'under redigering'");

		ferdigstillJournalfoeringService.ferdigstillJournalfoering(defaultTo());
	}

	private FerdigstillJournalfoeringTo defaultTo() {
		return FerdigstillJournalfoeringTo.builder().journalpostId("1").enhetId(ENHET).build();
	}
}