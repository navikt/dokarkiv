package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111;

import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertBruker;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertDokumentinfoRelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertJournalpostFields;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertKryssReferanse;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertSaksrelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentAssertUtil.assertVedlegg;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.HOVEDDOKUMENT;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.OPPRETTET_AV_NAVN;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.VEDLEGG;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createBruker;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createDokumentInfoRelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createDokumentInfoRelasjonOnlyRequired;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createFildetaljer;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createJournalpost;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createJournalpostOnlyRequiredValues;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createKryssReferanse;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createSaksrelasjon;
import static no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark111.OpprettUtgaaendeJournalpostArkiverDokumentDataUtil.createVedlegg;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.utils.DateUtil.getDateNow;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ValideringAvVedleggFeiletException;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.informasjon.arkiverdokumentproduksjon.JournalTilstand;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class OpprettUtgaaendeJournalpostArkiverDokumentIT extends AbstractArkiverdokumentproduksjonItest {

	private Journalpost persistedJournalpost;

	@Before
	public void setUp() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpost().setKanalreferanseId("persistedJournalpost");
		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		persistedJournalpost = joarkRepository.findById(response.getJournalpostId()).get();
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
	public void shouldOppretteUtgaaendeJournalpostWithVedlegg() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		addVedleggToRequest(request);

		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		assertThat(response.getJournalTilstand(), is(JournalTilstand.FERDIGSTILT));
		assertThat(response.getJournalpostId(), notNullValue());
		assertThat(response.getDokumentInfoIdHoveddokument(), notNullValue());
		assertThat(response.getDokumentInfoIdVedleggListe()
				.get(request.getJournalpostDokumentInfoRelasjon().size() - 1)
				.toString(), is(request.getVedlegg().get(0).getDokumentInfoId()));

		Journalpost journalpost = joarkRepository.findById(response.getJournalpostId()).get();
		assertThat(response.getJournalTilstand(), is(JournalTilstand.FERDIGSTILT));
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.FS));
		assertThat(journalpost.getJournalfortAvNavn(), is(OPPRETTET_AV_NAVN));
		assertThat(journalpost.getJournalDato(), notNullValue());
		assertTrue(getDateNow().toInstant().toEpochMilli() - journalpost.getJournalDato().toInstant().toEpochMilli() < 1000);

		assertJournalpostFields(journalpost);
		assertBruker(journalpost.getBrukere());
		assertKryssReferanse(journalpost.getKryssreferanser());
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

	/**
	 * HVIS journalpost opprettes med status D SÅ skal IKKE datoJournal, journafFEnhet og journalfoertAvNavn settes
	 * HVIS tjenesten kalles med input.forsokFerdigstilling = true, OG alle attributter som kreves for endelig journalføring IKKE er satt SÅ skal journalStatus i Joark = "D" og output.JournalTilstand = UNDER_ARBEID
	 */
	@Test
	public void shouldSetStatusUnderArbeidWhenInvalidJournalpost() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.setSaksrelasjon(null);
		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		Journalpost journalpost = joarkRepository.findById(response.getJournalpostId()).get();

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
	public void shouldSetStatusUnderArbeidWhenForskFerdigstillingIsFalse() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.setForsokFerdigstilling(false);
		request.setSaksrelasjon(null);
		OpprettUtgaaendeJournalpostArkiverDokumentResponse response = arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
		Journalpost journalpost = joarkRepository.findById(response.getJournalpostId()).get();

		assertThat(response.getJournalTilstand(), is(JournalTilstand.UNDER_ARBEID));
		assertThat(journalpost.getJournalstatus(), is(JournalStatusCode.D));
	}

	/**
	 * HVIS to journalposter sendes inn med samme kanalReferanseId SÅ skal output for de to innsendingene være likt.
	 */
	@Test
	public void shouldReturnSameResponseWhenKanalReferanseIdIsEqual() {
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
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("Journalpost.JournalStatus kan ikke være D");

		persistedJournalpost.setJournalstatus(JournalStatusCode.D);
		persistedJournalpost = joarkRepository.save(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG saksrelasjonen for original journalposten er feilregistrert SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggRefersToJournalpostWithFeiletregistertSaksrelasjon() {
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("Journalpost.Saksrelasjon.Feilregistert kan ikke være True");

		persistedJournalpost.getSaksrelasjon().setFeilregistrert(true);
		persistedJournalpost = joarkRepository.save(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG dokumentet ikke finnes i Joark på den oppgitte journalposten SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfCannotFindVedleggDokumentInfo() {
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("Fant ingen vedlegg med dokumentInfoId=");

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg().add(createVedlegg(123L, persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG dokumentet har satt dokumentStatus, men den er ulik FERDIGSTILT, SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoStatusIsNotFerdigstilt() {
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("DokumentInfo.Dokumentstatus må være FERDIGSTILT men var UNDER_REDIGERING");

		persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.setDokumentstatus(UNDER_REDIGERING);
		persistedJournalpost = joarkRepository.save(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG det er registrert at dokumentet er slettet, organInternt eller har innskrenketPartsinnsyn SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoSlettetIsTrue() {
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("DokumentInfo.slettet kan ikke være True");

		persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.setSlettet(Boolean.TRUE);
		persistedJournalpost = joarkRepository.save(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG det er registrert at dokumentet er slettet, organInternt eller har innskrenketPartsinnsyn SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoOrganInterntIsTrue() {
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("DokumentInfo.OrganInternt kan ikke være True");

		persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.setOrganInternt(Boolean.TRUE);
		persistedJournalpost = joarkRepository.save(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG det er registrert at dokumentet er slettet, organInternt eller har innskrenketPartsinnsyn SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoInnskrenketPartInnsynIsTrue() {
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("DokumentInfo.innskrenketPartsinnsyn kan ikke være True");

		persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.setInnskrenketPartsinnsyn(Boolean.TRUE);
		persistedJournalpost = joarkRepository.save(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG dokumentet har et tilhørende filDetaljer-objekt som har satt ondemandId SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoHasFildetaljerObjectWithOndemanIdNotNull() {
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("Fildetaljer.OnDemandId kan ikke være satt");

		persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setOnDemandId("ads");
		persistedJournalpost = joarkRepository.save(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en peker til et vedlegg som allerede ligger i JOARK OG dokumentet IKKE har et tilhørende filDetaljer-objekt med variantFormat ARKIV SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfVedleggDokumentInfoMissingFildetaljerWithVariantFormatARKIV() {
		expectedException.expect(isA(ValideringAvVedleggFeiletException.class));
		expectedException.expectMessage("Vedlegg mangler Fildetaljer med variantFormat=ARKIV");

		persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getFildetaljerListe()
				.iterator()
				.next()
				.setVariantFormat(VariantFormatCode.PRODUKSJON);
		persistedJournalpost = joarkRepository.save(persistedJournalpost);

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med mer enn ett hoveddokument SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfRequestHasMoreThanOneHoveddokument() {
		expectedException.expect(isA(InvalidJournalpostStructureException.class));
		expectedException.expectMessage("Journalpost cannot contain more than one hoveddokument when endelig journalforing");


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

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles uten Fildetaljer med variantformat = ARKIV SÅ skal det returnere en feil
	 */
	@Test
	public void shouldThrowIfRequestHasDokumentWithNoARKIVVariantFormat() {
		expectedException.expect(isA(InvalidJournalpostStructureException.class));
		expectedException.expectMessage("All the Journalpost's DokumentInfos must contain an arkiv variant when endelig journalforing");

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

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS journalpost opprettes med flere Fildetaljer SÅ skal disse ha ulike variantformater
	 * HVIS operasjonen kalles med flere Fildetaljer OG to av disse har identiske variantformater SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfRequestHasDokumentWithMultipleEqualVariantFormats() {
		expectedException.expect(isA(InvalidJournalpostStructureException.class));
		expectedException.expectMessage("DokumentInfo cannot contain dokumentvariant duplicates, found 2 ARKIV varianter");

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

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
	}

	/**
	 * HVIS operasjonen kalles med en ugyldig kodeverdi i input SÅ skal det returneres en feil
	 */
	@Test
	public void shouldThrowIfRequestHasInvalidEnumValue() {
		expectedException.expect(isA(IllegalArgumentException.class));
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.adsad");

		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = createRequest();
		request.getJournalpostDokumentInfoRelasjon().get(0).setTilknyttetJournalpostSom("adsad");

		request.getVedlegg()
				.add(createVedlegg(persistedJournalpost.getJournalpostDokumentInfoRelasjoner()
						.iterator()
						.next()
						.getDokumentInfo()
						.getId(), persistedJournalpost.getJournalpostId()));

		arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(request);
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
		request.setKryssreferanse(createKryssReferanse());
		return request;
	}

	private OpprettUtgaaendeJournalpostArkiverDokumentRequest createRequestWihtOnlyRequiredValues() {
		OpprettUtgaaendeJournalpostArkiverDokumentRequest request = new OpprettUtgaaendeJournalpostArkiverDokumentRequest();

		request.setJournalpost(createJournalpostOnlyRequiredValues());
		request.getJournalpostDokumentInfoRelasjon().add(createDokumentInfoRelasjonOnlyRequired());
		return request;
	}

}