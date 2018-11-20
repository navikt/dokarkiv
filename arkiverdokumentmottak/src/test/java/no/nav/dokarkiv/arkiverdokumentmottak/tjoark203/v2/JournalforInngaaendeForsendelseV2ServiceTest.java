package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.journalbehandling.DokumentFilerDelegate;
import no.nav.dokarkiv.core.repository.JoarkRepositoryBegrenset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalforInngaaendeForsendelseV2ServiceTest {

	private static final String DOKUMENT_TYPE_VEDLEGG = "dokumentTypeVedlegg";
	private static final String OPPRETTET_AV = "opprettetAv";
	private static final String KANAL_REFERANSE_ID = "kanalReferanseID";
	private static final MottaksKanalCode MOTTAKSKANAL = MottaksKanalCode.ALTINN;
	private static final JournalStatusCode JOURNAL_STATUS_J = JournalStatusCode.J;
	private static final JournalStatusCode JOURNAL_STATUS_M = JournalStatusCode.M;
	private static final boolean FORSOK_ENDELIG_JF_TRUE = true;
	private static final boolean FORSOK_ENDELIG_JF_FALSE = false;
	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 56L;
	private static final Long DOKUMENTINFO_VEDLEGG = 78L;
	private static final String JOURNALTILSTAND_ENDELIG = "ENDELIG";
	private static final String JOURNALTILSTAND_MIDLERTIDIG = "MIDLERTIDIG";

	private Journalpost journalpost;

	@Rule
	public ExpectedException expected = ExpectedException.none();
	@Mock
	private JoarkRepositoryBegrenset repositoryMock;
	@Mock
	private DokumentFilerDelegate dokumentFilerDelegateMock;
	@Mock
	private JournalforInngaaendeForsendelseV2Validator validator;
	@InjectMocks
	public JournalforInngaaendeForsendelseV2Service service;

	@Test
	public void shouldJournalforNewInngaaendeForsendelseWithEndeligJFTrue() {
		journalpost = createJournalpost();
		JournalforInngaaendeForsendelseV2RequestTo requestTo = new JournalforInngaaendeForsendelseV2RequestTo(FORSOK_ENDELIG_JF_TRUE, journalpost);

		when(repositoryMock.save(journalpost)).thenReturn(journalpost);

		JournalforInngaaendeForsendelseV2ResponseTo response = service.journalforInngaaendeForsendelseV2(requestTo);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENTINFO_ID));
		assertThat(response.getDokumentInfoIdVedleggTo(), hasSize(1));
		assertThat(response.getJournalTilstand(), is(JOURNALTILSTAND_ENDELIG));
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.I));
		assertThat(journalpost.getJournalfortAvNavn(), is(OPPRETTET_AV));
		assertThat(journalpost.getJournalstatus(), is(JOURNAL_STATUS_J));
		assertThat(journalpost.getJournalForendeEnhetId(), is("9999"));
		assertNotNull(journalpost.getJournalDato());

		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThat(rel.getTilknyttetAvNavn(), is(OPPRETTET_AV));
		}

		verify(repositoryMock).save(journalpost);
		verify(repositoryMock).findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MOTTAKSKANAL.name());
		verifyNoMoreInteractions(repositoryMock);
		verify(validator).validate(journalpost);
		verify(validator).validateVariantFormaterAndHoveddokument(journalpost);
		verifyNoMoreInteractions(validator);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verifyNoMoreInteractions(dokumentFilerDelegateMock);
	}

	@Test
	public void shouldJournalforNewInngaaendeForsendelseWithEndeligJFFalse() {
		journalpost = createJournalpost();
		JournalforInngaaendeForsendelseV2RequestTo requestTo = new JournalforInngaaendeForsendelseV2RequestTo(FORSOK_ENDELIG_JF_FALSE, journalpost);

		when(repositoryMock.save(journalpost)).thenReturn(journalpost);

		JournalforInngaaendeForsendelseV2ResponseTo response = service.journalforInngaaendeForsendelseV2(requestTo);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENTINFO_ID));
		assertThat(response.getDokumentInfoIdVedleggTo(), hasSize(1));
		assertThat(response.getJournalTilstand(), is(JOURNALTILSTAND_MIDLERTIDIG));
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.I));
		assertThat(journalpost.getJournalstatus(), is(JOURNAL_STATUS_M));
		assertThat(journalpost.getJournalForendeEnhetId(), is("9999"));

		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThat(rel.getTilknyttetAvNavn(), is(OPPRETTET_AV));
		}

		verify(repositoryMock).save(journalpost);
		verify(repositoryMock).findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MOTTAKSKANAL.name());
		verifyNoMoreInteractions(repositoryMock);
		verify(validator).validateVariantFormaterAndHoveddokument(journalpost);
		verifyNoMoreInteractions(validator);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verifyNoMoreInteractions(dokumentFilerDelegateMock);
	}

	@Test
	public void shouldNotJournalforNewInngaaendeForsendelseBecauseAlreadyInDb() {
		journalpost = createJournalpost();
		JournalforInngaaendeForsendelseV2RequestTo requestTo = new JournalforInngaaendeForsendelseV2RequestTo(FORSOK_ENDELIG_JF_TRUE,
				createJournalpost());

		when(repositoryMock.findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MOTTAKSKANAL.name())).thenReturn(Optional
				.ofNullable(journalpost));

		JournalforInngaaendeForsendelseV2ResponseTo response = service.journalforInngaaendeForsendelseV2(requestTo);

		assertNotNull(response);

		verify(repositoryMock).findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MOTTAKSKANAL.name());
		verifyNoMoreInteractions(repositoryMock);
		verifyNoMoreInteractions(validator);
		verifyNoMoreInteractions(dokumentFilerDelegateMock);
	}

	@Test
	public void shouldNotJournalforNewInngaaendeForsendelseBecuaseOfMissingAttributes() {
		journalpost = createJournalpost();
		JournalforInngaaendeForsendelseV2RequestTo requestTo = new JournalforInngaaendeForsendelseV2RequestTo(FORSOK_ENDELIG_JF_TRUE, journalpost);

		when(repositoryMock.save(journalpost)).thenReturn(journalpost);
		doThrow(new IllegalArgumentException("Missing required attributes")).when(validator).validate(journalpost);

		JournalforInngaaendeForsendelseV2ResponseTo response = service.journalforInngaaendeForsendelseV2(requestTo);

		assertThat(response.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(DOKUMENTINFO_ID));
		assertThat(response.getDokumentInfoIdVedleggTo(), hasSize(1));
		assertThat(response.getJournalTilstand(), is(JOURNALTILSTAND_MIDLERTIDIG));
		assertThat(journalpost.getJournalposttype(), is(JournalpostTypeCode.I));
		assertThat(journalpost.getJournalstatus(), is(JOURNAL_STATUS_M));
		assertThat(journalpost.getJournalForendeEnhetId(), is("9999"));

		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThat(rel.getTilknyttetAvNavn(), is(OPPRETTET_AV));
		}

		verify(repositoryMock).save(journalpost);
		verify(repositoryMock).findJournalpostByKanalReferanseIdAndMottakskanal(KANAL_REFERANSE_ID, MOTTAKSKANAL.name());
		verifyNoMoreInteractions(repositoryMock);
		verify(validator).validate(journalpost);
		verify(validator).validateVariantFormaterAndHoveddokument(journalpost);
		verifyNoMoreInteractions(validator);
		verify(dokumentFilerDelegateMock).saveUpdateDokumentFiler(journalpost);
		verifyNoMoreInteractions(dokumentFilerDelegateMock);
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.journalForendeEnhetId("9999")
				.opprettetAvNavn(OPPRETTET_AV)
				.mottattDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
				.mottakskanal(MOTTAKSKANAL)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.saksrelasjon(createSaksrelasjon())
				.brukere(createBruker())
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENTINFO_ID)
												.kategori(DokumentKategoriCode.B)
												.dokumenttypeId("12345")
												.filDetaljerList(createFildetaljer())
												.skannetInnhold(createSkannetInnhold())
												.build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.kategori(DokumentKategoriCode.B)
												.dokumentInfoId(DOKUMENTINFO_VEDLEGG)
												.dokumenttypeId(DOKUMENT_TYPE_VEDLEGG)
												.filDetaljerList(createFildetaljer())
												.skannetInnhold(createSkannetInnhold())
												.build())
								.build())
				.build();
	}

	private Saksrelasjon createSaksrelasjon() {
		return getSaksrelasjonBuilder()
				.sakId("123")
				.fagsystem(FagsystemCode.AO01)
				.build();
	}

	private Bruker createBruker() {
		return getBrukerBuilder()
				.brukerId("***gammelt_fnr***")
				.brukerType(BrukerTypeCode.PERSON)
				.build();
	}

	private FilDetaljer createFildetaljer() {
		return getFilDetaljerBuilder()
				.filtype(FilTypeCode.AFP)
				.variantFormat(VariantFormatCode.ARKIV)
				.fileContent("filinnhold".getBytes())
				.build();
	}

	private SkannetInnhold createSkannetInnhold() {
		return getSkannetInnholdBuilder()
				.vedleggInnhold("innhold")
				.dokumenttypeId("12345")
				.build();
	}
}