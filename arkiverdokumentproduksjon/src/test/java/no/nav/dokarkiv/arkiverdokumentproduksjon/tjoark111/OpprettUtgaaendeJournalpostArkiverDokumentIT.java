package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.OpprettUtgaaendeJournalpostUgyldigInput;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.OpprettUtgaaendeJournalpostValideringAvVedleggFeilet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalTilstand;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentResponse;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertBruker;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertDokumentinfoRelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertFildetaljer;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertJournalpostFields;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertSaksrelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertVedlegg;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.DOKUMENT_TYPE_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.HOVEDDOKUMENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KANAL_REF_ID;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.OPPRETTET_AV_NAVN;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.VEDLEGG;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createBruker;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createDokumentInfoRelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createDokumentInfoRelasjonOnlyRequired;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createFildetaljer;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createJournalpost;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createJournalpostOnlyRequiredValues;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createSaksrelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createVedlegg;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode.SYFO;
import static no.nav.dokarkiv.core.util.DateUtil.getDateNow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class OpprettUtgaaendeJournalpostArkiverDokumentIT extends AbstractArkiverdokumentproduksjonItest {

	private Journalpost persistedJournalpost;

	@BeforeEach
	public void setUp() throws OpprettUtgaaendeJournalpostUgyldigInput, OpprettUtgaaendeJournalpostValideringAvVedleggFeilet {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setKanalreferanseId("persistedJournalpost");
		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		persistedJournalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();
	}

	/**
	 * HVIS journalpost opprettes SÅ skal dagens dato settes for dokumentFerdigDato
	 * HVIS journalpost opprettes SÅ skal filstorrelse settes for alle Fildetaljer
	 * HVIS journalpost opprettes SÅ skal det være definert en Fildetaljer med variantformat = ARKIV
	 * HVIS journalpost opprettes med status FS SÅ skal datoJournal, journafFEnhet og journalfoertAvNavn settes
	 * HVIS journalpost opprettes SÅ skal alle attributtene som er med i input lagres på journalposten ihht persisteringstabell i behandlingssteg 7
	 * HVIS tjenesten kalles med input.forsokFerdigstilling = true, OG alle attributter som kreves for endelig journalføring er satt SÅ skal journalStatus i Joark = "FS" og output.JournalTilstand = FERDIGSTILT
	 */
	@Test
	public void shouldOppretteUtgaaendeJournalpostWithVedlegg() throws OpprettUtgaaendeJournalpostUgyldigInput, OpprettUtgaaendeJournalpostValideringAvVedleggFeilet {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		addVedleggToRequest(request);

		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		assertThat(response.getJournalTilstand(), is(JournalTilstand.FERDIGSTILT));
		assertThat(response.getJournalpostId(), notNullValue());
		assertThat(response.getDokumentInfoIdHoveddokument(), notNullValue());
		assertThat(response.getDokumentInfoIdVedleggListe()
				.get(request.getJournalpostDokumentInfoRelasjon().size() - 1)
				.toString(), is(request.getVedlegg().get(0).getDokumentInfoId()));

		Journalpost journalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();
		assertThat(response.getJournalTilstand(), is(JournalTilstand.FERDIGSTILT));
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FS));
		assertThat(journalpost.getJournalfortAvNavn(), is(OPPRETTET_AV_NAVN));
		assertThat(journalpost.getJournalDato(), notNullValue());
		assertTrue(getDateNow().toInstant().toEpochMilli() - journalpost.getJournalDato().toInstant().toEpochMilli() < 1000);

		assertJournalpostFields(journalpost);
		assertBruker(journalpost.getBrukere());
		assertSaksrelasjon(journalpost.getSaksrelasjon());

		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), is(request.getJournalpostDokumentInfoRelasjon()
				.size() + request.getVedlegg().size()));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(dok -> dok.getTilknyttetJournalpostSom() == TilknyttetJournalpostSomCode.VEDLEGG)
				.count(), is(request.getJournalpostDokumentInfoRelasjon().size() + request.getVedlegg().size() - 1L));
		assertTrue(journalpost.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.anyMatch(relasjon -> String.valueOf(relasjon.getDokumentInfo().getDokumentInfoId())
						.equals(request.getVedlegg().get(0).getDokumentInfoId())));
		assertDokumentinfoRelasjon(journalpost.getJournalpostDokumentInfoRelasjoner());
		assertVedlegg(journalpost.getJournalpostDokumentInfoRelasjoner(), request.getVedlegg());
	}

	@Test
	public void shouldOppretteUtgaaendeJournalpostWithOnlyRequiredValues() throws OpprettUtgaaendeJournalpostUgyldigInput, OpprettUtgaaendeJournalpostValideringAvVedleggFeilet {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequestWihtOnlyRequiredValues();

		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		Journalpost journalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();
		assertThat(journalpost.getUtsendingskanal(), IsNull.nullValue());
		assertThat("JournalforendeEnhet", journalpost.getJournalForendeEnhetId(), IsNull.nullValue());
		assertThat(journalpost.getOpprettetAvNavn(), CoreMatchers.is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.OPPRETTET_AV_NAVN));
		assertThat(journalpost.getInnhold(), IsNull.nullValue());
		assertThat(journalpost.getAvsenderMottaker(), IsNull.nullValue());
		assertThat(journalpost.getAvsenderMottakerId(), IsNull.nullValue());
		assertThat(journalpost.getKanalReferanseId(), Matchers.is(KANAL_REF_ID));
		assertThat(journalpost.getJournalposttype(), CoreMatchers.is(JournalpostTypeCode.U));
		assertThat(journalpost.getJournalpostDokumentInfoRelasjoner().size(), CoreMatchers.is(1));

		DokumentInfo domainDokumentInfo = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo();
		assertThat(domainDokumentInfo.getDokumenttypeId(), CoreMatchers.is(DOKUMENT_TYPE_ID));
		assertThat(domainDokumentInfo.getDokumentstatus(), CoreMatchers.is(FERDIGSTILT));
		assertThat(domainDokumentInfo.getTittel(), CoreMatchers.is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.TITTEL));
		assertThat(domainDokumentInfo.getKategori()
				.name(), CoreMatchers.is(OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.KATEGORI));
		assertThat(domainDokumentInfo.getBrevkode(), IsNull.nullValue());
		assertFildetaljer(domainDokumentInfo.getFildetaljerListe().iterator().next());

	}

	@Test
	public void shouldThrowForInvalidInput() {
		assertThrows(OpprettUtgaaendeJournalpostUgyldigInput.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(new OpprettUtgaaendeJournalpostArkiverDokumentRequest()),
				"kanalReferanseId, opprettetAvNavn, journalpostDokumentInfoRelasjoner");
	}

	/**
	 * HVIS journalpost opprettes med status D SÅ skal IKKE datoJournal, journafFEnhet og journalfoertAvNavn settes
	 * HVIS tjenesten kalles med input.forsokFerdigstilling = true, OG alle attributter som kreves for endelig journalføring IKKE er satt SÅ skal journalStatus i Joark = "D" og output.JournalTilstand = UNDER_ARBEID
	 */
	@Test
	public void shouldSetStatusUnderArbeidWhenInvalidJournalpost() throws OpprettUtgaaendeJournalpostUgyldigInput, OpprettUtgaaendeJournalpostValideringAvVedleggFeilet {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.setSaksrelasjon(null);
		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		Journalpost journalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();

		assertThat(response.getJournalTilstand(), is(JournalTilstand.UNDER_ARBEID));
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.D));
		assertThat(journalpost.getJournalForendeEnhetId(), nullValue());
		assertThat(journalpost.getJournalfortAvNavn(), nullValue());
		assertThat(journalpost.getJournalDato(), nullValue());
	}

	/**
	 * HVIS journalpost opprettes med status D SÅ skal IKKE datoJournal, journafFEnhet og journalfoertAvNavn settes
	 * HVIS tjenesten kalles med input.forsokFerdigstilling = false, OG alle attributter som kreves for endelig journalføring IKKE er satt SÅ skal journalStatus i Joark = "D" og output.JournalTilstand = UNDER_ARBEID
	 */
	@Test
	public void shouldSetStatusUnderArbeidWhenForskFerdigstillingIsFalse() throws OpprettUtgaaendeJournalpostUgyldigInput, OpprettUtgaaendeJournalpostValideringAvVedleggFeilet {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.setForsokFerdigstilling(false);
		request.setSaksrelasjon(null);
		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		Journalpost journalpost = journalpostTestRepository.findById(response.getJournalpostId()).get();

		assertThat(response.getJournalTilstand(), is(JournalTilstand.UNDER_ARBEID));
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.D));
	}

	/**
	 * HVIS to journalposter sendes inn med samme kanalReferanseId SÅ skal output for de to innsendingene være likt.
	 */
	@Test
	public void shouldReturnSameResponseWhenKanalReferanseIdIsEqual() throws OpprettUtgaaendeJournalpostUgyldigInput, OpprettUtgaaendeJournalpostValideringAvVedleggFeilet {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		addVedleggToRequest(request);

		OpprettUtgaaendeJournalpostArkiverDokumentResponse response1 = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		OpprettUtgaaendeJournalpostArkiverDokumentResponse response2 = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		assertThat(response1.getDokumentInfoIdHoveddokument(), is(response2.getDokumentInfoIdHoveddokument()));
		assertThat(response1.getJournalpostId(), is(response2.getJournalpostId()));
		assertThat(response1.getJournalTilstand(), is(response2.getJournalTilstand()));
		assertThat(response1.getDokumentInfoIdVedleggListe(), is(response2.getDokumentInfoIdVedleggListe()));
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG journalstatus for original journalposten != J, FS eller FS SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggRefersToJournalpostWithStatusD() {
		persistedJournalpost.setJournalstatus(JournalStatusCode.D);
		persistedJournalpost = journalpostTestRepository.persist(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostValideringAvVedleggFeilet.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"Journalpost.JournalStatus kan ikke være D");
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG saksrelasjonen for original journalposten er feilregistrert SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggRefersToJournalpostWithFeiletregistertSaksrelasjon() {
		persistedJournalpost.getSaksrelasjon().setFeilregistrert(true);
		persistedJournalpost = journalpostTestRepository.persist(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostValideringAvVedleggFeilet.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"Journalpost.Saksrelasjon.Feilregistert kan ikke være Sann");
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG dokumentet ikke finnes i Joark på den oppgitte journalposten SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfCannotFindVedleggDokumentInfo() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg().add(createVedlegg(123L, persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostValideringAvVedleggFeilet.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"Fant ingen DokumentInfo for vedlegg med dokumentInfoId");
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG dokumentet har satt dokumentStatus, men den er ulik FERDIGSTILT, SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoStatusIsNotFerdigstilt() {
		persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.setDokumentstatus(UNDER_REDIGERING);
		persistedJournalpost = journalpostTestRepository.persist(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostValideringAvVedleggFeilet.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"DokumentInfo.Dokumentstatus må være FERDIGSTILT men var UNDER_REDIGERING");
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG dokumentet har et tilhørende filDetaljer-objekt som har satt ondemandId SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoHasFildetaljerObjectWithOndemanIdNotNull() {
		FilDetaljer detaljer = persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next();
		detaljer.setOnDemandInstans(SYFO);
		detaljer.setOnDemandId("ondemandid");
		persistedJournalpost = journalpostTestRepository.persist(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostValideringAvVedleggFeilet.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"Fildetaljer.OnDemandId kan ikke være satt");
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG dokumentet IKKE har et tilhørende filDetaljer-objekt med variantFormat ARKIV SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoMissingFildetaljerWithVariantFormatARKIV() {
		persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setVariantFormat(VariantFormatCode.PRODUKSJON);
		persistedJournalpost = journalpostTestRepository.persist(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostValideringAvVedleggFeilet.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"Vedlegg mangler Fildetaljer med variantFormat=ARKIV");
	}

	/**
	 * HVIS operasjonen kalles med mer enn ett hoveddokument SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfRequestHasMoreThanOneHoveddokument() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpostDokumentInfoRelasjon().forEach(relasjon -> {
			relasjon.setTilknyttetJournalpostSom(HOVEDDOKUMENT);
		});

		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostUgyldigInput.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"Journalpost cannot contain more than one hoveddokument when endelig journalforing");
	}

	/**
	 * HVIS operasjonen kalles uten Fildetaljer med variantformat = ARKIV SÅ skal det returnere en feil
	 */
	@Test
	public void shouldThrowIfRequestHasDokumentWithNoARKIVVariantFormat() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpostDokumentInfoRelasjon().forEach(relasjon -> {
			relasjon.getDokumentInfo().getFildetaljerListe().add(createFildetaljer());
			relasjon.getDokumentInfo()
					.getFildetaljerListe()
					.forEach(fildetaljer -> fildetaljer.setVariantformat(VariantFormatCode.ORIGINAL.name()));
		});

		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostUgyldigInput.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"All the Journalpost's DokumentInfos must contain an arkiv variant when endelig journalforing");
	}

	/**
	 * HVIS journalpost opprettes med flere Fildetaljer SÅ skal disse ha ulike variantformater
	 * HVIS operasjonen kalles med flere Fildetaljer OG to av disse har identiske variantformater SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfRequestHasDokumentWithMultipleEqualVariantFormats() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpostDokumentInfoRelasjon().forEach(relasjon -> {
			relasjon.getDokumentInfo()
					.getFildetaljerListe()
					.add(createFildetaljer()); //Create one more fildetaljer with Variantformat=ARKIV
		});

		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostUgyldigInput.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"DokumentInfo cannot contain dokumentvariant duplicates, found 2 ARKIV varianter");
	}

	/**
	 * HVIS operasjonen kalles med en ugyldig kodeverdi i input SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfRequestHasInvalidEnumValue() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpostDokumentInfoRelasjon().get(0).setTilknyttetJournalpostSom("adsad");

		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		assertThrows(OpprettUtgaaendeJournalpostUgyldigInput.class,
				() -> arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request),
				"No enum constant no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.adsad");
	}

	private OpprettUtgaaendeJournalpostArkiverDokumentRequest addVedleggToRequest(OpprettUtgaaendeJournalpostArkiverDokumentRequest request) {
		persistedJournalpost.getJournalpostDokumentInfoRelasjoner().forEach(relasjon -> {
			request.getVedlegg()
					.add(createVedlegg(relasjon.getDokumentInfo().getId(), persistedJournalpost.getJournalpostId()));
		});
		return request;
	}

	private OpprettUtgaaendeJournalpostArkiverDokumentRequest createRequest() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = new OpprettUtgaaendeJournalpostArkiverDokumentRequest();
		request.setForsokFerdigstilling(true);
		request.setBruker(createBruker());
		request.setJournalpost(createJournalpost());
		request.setSaksrelasjon(createSaksrelasjon());
		request.getJournalpostDokumentInfoRelasjon().add(createDokumentInfoRelasjon(HOVEDDOKUMENT));
		request.getJournalpostDokumentInfoRelasjon().add(createDokumentInfoRelasjon(VEDLEGG));
		return request;
	}

	private OpprettUtgaaendeJournalpostArkiverDokumentRequest createRequestWihtOnlyRequiredValues() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = new OpprettUtgaaendeJournalpostArkiverDokumentRequest();

		request.setJournalpost(createJournalpostOnlyRequiredValues());
		request.getJournalpostDokumentInfoRelasjon().add(createDokumentInfoRelasjonOnlyRequired());
		return request;
	}

}