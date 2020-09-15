package no.nav.dokarkiv.behandlejournal.v2.tjoark061;

import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringBuilder.getBidragMellomlagringBuilder;
import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringDokumentBuilder.getBidragMellomlagringDokumentBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.IS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalV2Itest;
import no.nav.dokarkiv.behandlejournal.v2.KodeverdiHelper;
import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.feil.ForretningsmessigUnntak;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.feil.JournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.DokumentInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Dokumenttyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.UstrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Variantformater;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.lagrevedleggpaajournalpost.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.LagreVedleggPaaJournalpostRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.LagreVedleggPaaJournalpostResponse;
import org.junit.Test;

import java.util.Date;
import java.util.Set;

/**
 * Integration test of operation LagreVedleggPaaJournalpost.
 *
 * @author Rune Romundstad, Visma Consulting
 */

public class LagreVedleggPaaJournalpostIT extends AbstractBehandleJournalV2Itest {
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";
	private static final String NONEXISTING_JOURNALPOST_ID = "12345";

	private static final Boolean INNSKRENKET_PARTSINNSYN = false;
	private static final String DOKUMENTTYPE_ID = "123123";
	private static final String DOKUMENTTYPE_ID_VEDLEGG = "458212";
	private static final String FILNAVN = "Attest";
	private static final String FILTYPE = "PDF";
	private static final String VARIANTFORMAT = "ARKIV";
	private static final byte[] FILECONTENT = "Jeg bekrefter herved at...".getBytes();
	private static final String TILLEGGSOPPLYSNINGER_KEY = "vedleggstittel";
	private static final String TILLEGGSOPPLYSNINGER_VALUE = "nytt vedlegg";

	private LagreVedleggPaaJournalpostRequest lagreVedleggPaaJournalpostRequest;
	private LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpostResponse;
	private Journalpost journalpost;
	private DokumentInfo persistedDokumentInfo;
	private FilDetaljer fildetaljer;
	private BidragMellomlagring bidragMellomlagring;

	private String dokumentTypeId;

	public void setUpJoark() throws Exception {
		journalpost = createAndPersistJournalpostWithHoveddokument();
		createRequest(journalpost.getJournalpostId().toString());
		lagreVedleggPaaJournalpostResponse = behandleJournalProvider
				.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
		persistedDokumentInfo = dokumentinfoRepository.findById(Long.valueOf(lagreVedleggPaaJournalpostResponse
				.getDokumentId())).get();
		fildetaljer = persistedDokumentInfo.getFildetaljerListe().iterator().next();
	}

	public void setUpBidrag() throws Exception {
		setUpBidrag(DOKUMENTTYPE_ID);
	}

	public void setUpBidrag(String dokumenttypeId) throws Exception {
		this.dokumentTypeId = dokumenttypeId;
		bidragMellomlagring = createAndPersistBidragMellomlagringWithHoveddokument();
		createRequest(bidragMellomlagring.getIdWithPrefix().toString());
		lagreVedleggPaaJournalpostResponse = behandleJournalProvider
				.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldThrowExceptionWhenTryingToAddVedleggToNonExistingJournalpost() throws Exception {
		DateProvider.configure(true, "2018-07-11T12:00");
		assertForretningsmessigUnntak(LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet.class,
				expectedJournalpostIkkeFunnet());

		createRequest(NONEXISTING_JOURNALPOST_ID);

		behandleJournalProvider.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
	}

	@Test
	public void shouldReturnDokumentIdForTheAddedDokumentInfo() throws Exception {
		setUpJoark();

		assertNotNull(lagreVedleggPaaJournalpostResponse.getDokumentId());
	}

	@Test
	public void shouldAddDokumentInfoAsVedleggToExistingJournalpost() throws Exception {
		setUpJoark();

		Set<JournalpostDokumentInfoRelasjon> vedleggRelasjoner = journalpost
				.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		assertThat(vedleggRelasjoner.size(), is(1));
	}

	@Test
	public void shouldVerifyDokumentInfoForTheAddedVedlegg() throws Exception {
		setUpJoark();

		assertThat(persistedDokumentInfo.getDokumentstatus(), is(DokumentStatusCode.FERDIGSTILT));
		assertThat(persistedDokumentInfo.getDokumentFerdigDato(), notNullValue());
		assertThat(persistedDokumentInfo.getInnskrenketPartsinnsyn(), is(INNSKRENKET_PARTSINNSYN));
		assertThat(persistedDokumentInfo.getOriginalJournalpost().getJournalpostId(),
				is(journalpost.getJournalpostId()));
		assertThat(persistedDokumentInfo.getTilleggsopplysninger().get(TILLEGGSOPPLYSNINGER_KEY),
				is(TILLEGGSOPPLYSNINGER_VALUE));
	}

	@Test
	public void shouldVerifyFildetaljerForAddedVedlegg() throws Exception {
		setUpJoark();

		assertNotNull(fildetaljer.getFilUuid());
		assertNotNull(fildetaljer.getFilstorrelse());
		assertThat(fildetaljer.getFilnavn(), is(FILNAVN));
		assertThat(fildetaljer.getFiltype().toString(), is(FILTYPE));
		assertThat(fildetaljer.getVariantFormat().toString(), is(VARIANTFORMAT));
	}

	@Test
	public void shouldVerifyDokumentKategoriIsISForAddedPensjonVedlegg() throws Exception {
		journalpost = createAndPersistJournalpostWithHoveddokument();
		journalpost.setFagomrade(FagomradeCode.PEN);
		createRequest(journalpost.getJournalpostId().toString());
		lagreVedleggPaaJournalpostResponse = behandleJournalProvider
				.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequest);
		persistedDokumentInfo = dokumentinfoRepository.findById(Long.valueOf(lagreVedleggPaaJournalpostResponse
				.getDokumentId())).get();

		assertNotNull(persistedDokumentInfo);
		assertNotNull(persistedDokumentInfo.getKategori());
		assertThat(persistedDokumentInfo.getKategori(), is(IS));
	}

	@Test
	public void shouldVerifyFileContentForTheAddedVedlegg() throws Exception {
		setUpJoark();

		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(fildetaljer.getFilUuid());
		assertThat(dokumentFil.getFil(), is(FILECONTENT));
	}

	@Test
	public void shouldReturnBidragMellomLagringDokumentIdForTheAddedBidragsdokumentVedlegg() throws Exception {
		setUpBidrag();

		assertNotNull(lagreVedleggPaaJournalpostResponse.getDokumentId());
	}

	@Test
	public void shouldAddDokumentInfoAsVedleggToExistingBidragMellomlagring() throws Exception {
		setUpBidrag();

		BidragMellomlagring persistedBidragMellomlagring = bidragMellomlagringRepository.findById(bidragMellomlagring
				.getBidragMellomlagringId()).get();
		Set<BidragMellomlagringDokument> vedlegg = persistedBidragMellomlagring
				.findBidragMellomlagringDokumentByType(BidragMellomlagringDokumentType.VEDLEGG);

		assertThat(vedlegg.size(), is(1));
	}

	@Test
	public void shouldAddDokumentInfoAsKvitteringVedleggToExistingBidragMellomlagring() throws Exception {
		setUpBidrag(DOKUMENTTYPE_ID_VEDLEGG);

		BidragMellomlagring persistedBidragMellomlagring = bidragMellomlagringRepository.findById(bidragMellomlagring
				.getBidragMellomlagringId()).get();
		Set<BidragMellomlagringDokument> vedlegg = persistedBidragMellomlagring
				.findBidragMellomlagringDokumentByType(BidragMellomlagringDokumentType.VEDLEGG_KVITTERING);

		assertThat(vedlegg.size(), is(1));
	}

	@Test
	public void shouldVerifyFileContentForTheAddedBidragVedlegg() throws Exception {
		setUpBidrag();

		BidragMellomlagring persistedBidragMellomlagring = bidragMellomlagringRepository.findById(bidragMellomlagring
				.getBidragMellomlagringId()).get();
		Set<BidragMellomlagringDokument> vedlegg = persistedBidragMellomlagring
				.findBidragMellomlagringDokumentByType(BidragMellomlagringDokumentType.VEDLEGG);

		assertThat(vedlegg.iterator().next().getDokument(), is(FILECONTENT));
	}

	private ForretningsmessigUnntak expectedJournalpostIkkeFunnet() {
		JournalpostIkkeFunnet journalpostIkkeFunnet = new JournalpostIkkeFunnet();
		journalpostIkkeFunnet.setFeilaarsak("NoJournalpostFoundException");
		journalpostIkkeFunnet.setFeilkilde("JOARK:lagreVedleggPaaJournalpost");
		journalpostIkkeFunnet.setFeilmelding("Journalpost with id: " + NONEXISTING_JOURNALPOST_ID + " does not exist");
		journalpostIkkeFunnet.setTidspunkt(getXmlTimestamp());
		return journalpostIkkeFunnet;
	}

	private void createRequest(String journalpostId) {
		lagreVedleggPaaJournalpostRequest = new LagreVedleggPaaJournalpostRequest();
		lagreVedleggPaaJournalpostRequest.setJournalpostId(journalpostId);
		lagreVedleggPaaJournalpostRequest.setJournalfortDokumentInfo(createInputJournalfoertDokumentInfo());
		lagreVedleggPaaJournalpostRequest.setPersonFornavn(SPORING_FORNAVN);
		lagreVedleggPaaJournalpostRequest.setPersonEtternavn(SPORING_ETTERNAVN);
		lagreVedleggPaaJournalpostRequest.setApplikasjonsID("applikasjonsid");
	}

	private Journalpost createAndPersistJournalpostWithHoveddokument() {
		Journalpost journalpostWithHoveddokument = createJournalpostWithHoveddokument();
		return joarkRepository.save(journalpostWithHoveddokument);
	}

	private Journalpost createJournalpostWithHoveddokument() {
		Journalpost persistedJournalpost = getJournalpostBuilder()
				.brukere(
						BrukerBuilder.getBrukerBuilder().brukerId("").brukerType(BrukerTypeCode.PERSON)
								.opprettetKildeNavn("test").build())
				.journalpostType(JournalpostTypeCode.U)
				.journalStatus(JournalStatusCode.OD)
				.opprettetKildeNavn("test")
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder
								.getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.opprettetKildeNavn("test")
								.tilknyttetAvNavn("test")
								.dokumentInfo(
										DokumentInfoBuilder
												.getDokumentInfoBuilder()
												.innskrenketPartsinnsyn(INNSKRENKET_PARTSINNSYN)
												.tittel("tittel")
												.brukeroppgittTittel("brukerOppgittTittel")
												.opprettetKildeNavn("test")
												.filDetaljerList(
														FilDetaljerBuilder.getFilDetaljerBuilder()
																.opprettetKildeNavn("test").filnavn("TestFil")
																.filtype(FilTypeCode.PDF)
																.variantFormat(VariantFormatCode.ARKIV)
																.fileContent("hoveddokument".getBytes()).build())
												.build()).build()).build();
		persistedJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo()
				.setOriginalJournalpost(persistedJournalpost);
		return persistedJournalpost;
	}

	private BidragMellomlagring createAndPersistBidragMellomlagringWithHoveddokument() {
		BidragMellomlagring bidragMellomlagring = getBidragMellomlagringBuilder()
				.avsenderFnr("12312312312")
				.mottattDato(new Date())
				.status(BidragMellomlagringStatus.DOKUMENTOPPLASTING)
				.bidragMellomlagringDokuments(
						getBidragMellomlagringDokumentBuilder()
								.dokumentType(BidragMellomlagringDokumentType.HOVEDDOKUMENT)
								.dokument("Hoveddokument".getBytes()).build()).build();
		return bidragMellomlagringRepository.save(bidragMellomlagring);
	}

	private JournalfoertDokumentInfo createInputJournalfoertDokumentInfo() {
		JournalfoertDokumentInfo dokumentInfo = new JournalfoertDokumentInfo();
		dokumentInfo.setBegrensetPartsInnsyn(false);
		dokumentInfo.setDokumentType(KodeverdiHelper.kodeVerdi(dokumentTypeId, Dokumenttyper.class));
		dokumentInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		dokumentInfo.getBeskriverInnhold().add(createDokumentInnhold());
		return dokumentInfo;
	}

	private NoekkelVerdiSett createTilleggsopplysninger() {
		NoekkelVerdiSett noekkelVerdiSett = new NoekkelVerdiSett();
		noekkelVerdiSett.getInneholderNoekkelVerdiPar().add(createNoekkelVerdiPar());
		return noekkelVerdiSett;
	}

	private NoekkelVerdiPar createNoekkelVerdiPar() {
		NoekkelVerdiPar noekkelVerdiPar = new NoekkelVerdiPar();
		noekkelVerdiPar.setNoekkel(TILLEGGSOPPLYSNINGER_KEY);
		noekkelVerdiPar.setVerdi(TILLEGGSOPPLYSNINGER_VALUE);
		return noekkelVerdiPar;
	}

	private DokumentInnhold createDokumentInnhold() {
		UstrukturertInnhold innhold = new UstrukturertInnhold();
		innhold.setFilnavn(FILNAVN);
		innhold.setFiltype(KodeverdiHelper.kodeVerdi(FILTYPE, Arkivfiltyper.class));
		innhold.setVariantformat(KodeverdiHelper.kodeVerdi(VARIANTFORMAT, Variantformater.class));
		innhold.setInnhold(FILECONTENT);
		return innhold;
	}

}
