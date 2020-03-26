package no.nav.dokarkiv.innsynjournal.v2.tjoark059;

import no.nav.dokarkiv.core.datautil.SkannetInnholdTestDataProvider;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.innsynjournal.v2.AbstractInnsynJournalV2Itest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostUgyldigAntallJournalposter;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Dokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentInnhold;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.SkannetInnhold;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.CURRENT_IDENT;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.FAIL_IDENT;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.HISTORICAL_IDENTS;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.DOKUMENT_TITTEL;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createVedleggDokumentInfo;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.FIL_TYPE;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.VARIANT_FORMAT;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.FNR;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.JANUARY_1_2020;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.createJournalpost;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.createJournalpostWithoutHoveddokument;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument.JA;
import static no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument.KAN_IKKE_AVGJOERES;
import static no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument.NEI;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration test for TJOARK053 IdentifiserJournapost.
 *
 * @author Ketill Fenne, Visma Consulting AS
 */
public class IdentifiserJournalpostIT extends AbstractInnsynJournalV2Itest {

	private static final String KANAL_REFERANSE_ID = "kanalReferanseId";
	private static final String MOTTAKS_KANAL = "NAV_NO";
	private static final String FEIL_KANAL_REFERANSE_ID = "feilKanalReferanseId";


	@BeforeClass
	public static void setUpSecurity() {
		System.setProperty("no.nav.modig.security.systemuser.username", "JOARK");
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
		SubjectHandlerUtils.setEksternBruker(FNR, 4, "");
	}

	@Before
	public void setUpSubjectHandler() {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
	}

	@After
	public void resetSubjectHandler() {
		SubjectHandlerUtils.reset();
	}

	/**
	 * Alles gut
	 */
	@Test
	public void happyPath() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.journalpostType(JournalpostTypeCode.I)
				.dokumentInfoRelasjoner(
							getJournalpostDokumentInfoRelasjonBuilder()
									.opprettetKildeNavn("itest")
									.tilknyttetAvNavn("itest")
									.tilknyttetJournalpostSom(VEDLEGG)
									.dokumentInfo(createVedleggDokumentInfo().build()).build()
				));

		IdentifiserJournalpostRequest request = createRequest(KANAL_REFERANSE_ID, MOTTAKS_KANAL);

		IdentifiserJournalpostResponse response = innsynJournalV2Provider.identifiserJournalpost(request);
		assertThat(Long.valueOf(response.getJournalpostId()), is(journalpost.getJournalpostId()));
		assertThat(Long.valueOf(response.getHoveddokument().getDokumentId()), is(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(HOVEDDOKUMENT).iterator().next().getDokumentInfo().getDokumentInfoId()));
		assertThat(response.getHoveddokument().getTittel(), is(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(HOVEDDOKUMENT).iterator().next().getDokumentInfo().getTittel()));
		assertDokumentInnsyn(response.getHoveddokument(), is(NEI));

		assertThat(response.getVedleggListe(), hasSize(1));
		assertThat(Long.valueOf(response.getVedleggListe().get(0).getDokumentId()), is(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG).iterator().next().getDokumentInfo().getDokumentInfoId()));
		assertThat(response.getVedleggListe().get(0).getTittel(), is(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG).iterator().next().getDokumentInfo().getTittel()));
		assertDokumentInnsyn(response.getVedleggListe().get(0), is(NEI));
	}

	@Test
	public void shouldIdentifiserJournalpostWhenMottakskanalNotIncluded() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.journalpostType(JournalpostTypeCode.I)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("itest")
								.tilknyttetAvNavn("itest")
								.tilknyttetJournalpostSom(VEDLEGG)
								.dokumentInfo(createVedleggDokumentInfo().build()).build()
				));

		IdentifiserJournalpostRequest request = createRequest(KANAL_REFERANSE_ID, null);

		IdentifiserJournalpostResponse response = innsynJournalV2Provider.identifiserJournalpost(request);
		assertThat(Long.valueOf(response.getJournalpostId()), is(journalpost.getJournalpostId()));
	}

	/**
	 * Hvis journalpost ikke er Inngående, skal feil kastes
	 */
	@Test
	public void shouldThrowExceptionFeilJournalpostType() throws Exception {
		expectedException.expect(IdentifiserJournalpostJournalpostIkkeInngaaende.class);
		expectedException.expectMessage("som ble funnet er ikke inngående");
		buildAndPersist(aJournalpost().journalpostType(JournalpostTypeCode.U));

		IdentifiserJournalpostRequest request = createRequest(KANAL_REFERANSE_ID, MOTTAKS_KANAL);

		innsynJournalV2Provider.identifiserJournalpost(request);
	}

	/**
	 * Hvis journalpost ikke har hoveddokument skal feil kastes.
	 */
	@Test
	public void shouldThrowExceptionFeilJournalpostUtenHoveddokument() throws Exception {
		expectedException.expect(IdentifiserJournalpostObjektIkkeFunnet.class);
		expectedException.expectMessage("mangler hoveddokument");
		buildAndPersist(aJournalpostWithoutHoveddokument()
				.journalpostType(JournalpostTypeCode.I));

		IdentifiserJournalpostRequest request = createRequest(KANAL_REFERANSE_ID, MOTTAKS_KANAL);

		innsynJournalV2Provider.identifiserJournalpost(request);
	}

	/**
	 * Hvis søket ikke returnerer nøyektig 1 journalpost
	 */
	@Test
	public void shouldThrowExceptionFeilReturnereFeilAntallJournaposter() throws Exception {
		expectedException.expect(IdentifiserJournalpostUgyldigAntallJournalposter.class);
		expectedException.expectMessage("Uthenting av journalposter med kanalReferanseId=" + FEIL_KANAL_REFERANSE_ID + " og mottakskanal=" + MOTTAKS_KANAL + " resulterte ikke i nøyaktig én journalpost");
		buildAndPersist(aJournalpost()
				.journalpostType(JournalpostTypeCode.I));

		IdentifiserJournalpostRequest request = createRequest(FEIL_KANAL_REFERANSE_ID, MOTTAKS_KANAL);

		innsynJournalV2Provider.identifiserJournalpost(request);
	}

	/**
	 * Hvis journalpost.avsenderMottakerId er lik eksternbruker
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#JA}.
	 */
	@Test
	public void shouldSetDokumentInnsynToJAWhenMottakskanalIsNAVAndAvsenderMottakerIdIsEksternBruker() throws Exception {
		SubjectHandlerUtils.setEksternBruker(FNR, 4, "");
		buildAndPersist(aJournalpost()
				.mottakskanal(NAV_NO)
				.journalpostType(JournalpostTypeCode.I));
		IdentifiserJournalpostRequest request = createRequest(KANAL_REFERANSE_ID, MOTTAKS_KANAL);
		IdentifiserJournalpostResponse response = innsynJournalV2Provider.identifiserJournalpost(request);
		assertDokumentInnsyn(response.getHoveddokument(), is(JA));
	}

	/**
	 * Hvis journalpost.avsenderMottakerId er ulik eksternbruker
	 * og journalpost.avsenderMottakerId finnes i listen som er returnert fra AktoerId
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#JA}.
	 */
	@Test
	public void shouldSetDokumentInnsynToJAWhenMottakskanalIsNAVAndAvsenderMottakerIdIsNotEksternBrukerAndInAktoerId() throws Exception {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
		buildAndPersist(aJournalpost()
				.mottakskanal(NAV_NO)
				.journalpostType(JournalpostTypeCode.I)
				.avsenderMottakerId(HISTORICAL_IDENTS.get(0)));
		IdentifiserJournalpostRequest request = createRequest(KANAL_REFERANSE_ID, MOTTAKS_KANAL);

		IdentifiserJournalpostResponse response = innsynJournalV2Provider.identifiserJournalpost(request);
		assertDokumentInnsyn(response.getHoveddokument(), is(JA));
	}

	/**
	 * Hvis journalpost.avsenderMottakerId er lik eksternbruker og AktoerId feiler
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#KAN_IKKE_AVGJOERES}.
	 */
	@Test
	public void shouldSetDokumentInnsynToKANIKKEAVGJOERESWhenMottakskanalIsNAVAndAvsenderMottakerIdIsNotEksternAndAktoerIdFeiler() throws Exception {
		SubjectHandlerUtils.setEksternBruker(FAIL_IDENT, 4, "");
		buildAndPersist(aJournalpost()
				.mottakskanal(NAV_NO)
				.journalpostType(JournalpostTypeCode.I));
		IdentifiserJournalpostRequest request = createRequest(KANAL_REFERANSE_ID, MOTTAKS_KANAL);

		IdentifiserJournalpostResponse response = innsynJournalV2Provider.identifiserJournalpost(request);
		assertDokumentInnsyn(response.getHoveddokument(), is(KAN_IKKE_AVGJOERES));
	}

	/**
	 * Hvis journalpost.avsenderMottakerId er ulik eksternbruker
	 * og journalpost.avsenderMottakerId ikke finnes i listen som er returnert fra AktoerId
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#NEI}.
	 */
	@Test
	public void shouldSetDokumentInnsynToNEIWhenMottakskanalIsNAVAndAvsenderMottakerIdIsNotEksternBrukerAndNotInAktoerId() throws Exception {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
		buildAndPersist(aJournalpost()
				.mottakskanal(NAV_NO)
				.avsenderMottakerId("***gammelt_fnr***")
				.journalpostType(JournalpostTypeCode.I));
		IdentifiserJournalpostRequest request = createRequest(KANAL_REFERANSE_ID, MOTTAKS_KANAL);

		IdentifiserJournalpostResponse response = innsynJournalV2Provider.identifiserJournalpost(request);
		assertDokumentInnsyn(response.getHoveddokument(), is(NEI));
	}

	public void assertDokumentInfoRels(List<DokumentinfoRelasjon> dokumentinfoRelasjonListe) {
		assertThat(dokumentinfoRelasjonListe.size(), is(2));

		DokumentinfoRelasjon hoveddok = dokumentinfoRelasjonListe.get(0);
		DokumentinfoRelasjon vedlegg = dokumentinfoRelasjonListe.get(1);
		assertThat(hoveddok.getDokumentTilknyttetJournalpost().getValue(), is(HOVEDDOKUMENT.name()));
		assertThat(vedlegg.getDokumentTilknyttetJournalpost().getValue(), is(VEDLEGG.name()));
		assertDokument(hoveddok.getJournalfoertDokument());
		assertDokument(vedlegg.getJournalfoertDokument());
	}

	private void assertDokument(JournalfoertDokumentInfo journalfoertDokument) {
		assertThat(journalfoertDokument.getTittel(), is(DOKUMENT_TITTEL));
		DokumentInnhold beskriverInnhold = journalfoertDokument.getBeskriverInnhold();
		assertThat(beskriverInnhold.getVariantformat().getValue(), is(VARIANT_FORMAT.name()));
		assertThat(beskriverInnhold.getFiltype().getValue(), is(FIL_TYPE.name()));
		assertSkannetInnhold(journalfoertDokument.getSkannetInnholdListe());
	}

	private void assertSkannetInnhold(List<SkannetInnhold> skannetInnholds) {
		assertThat(skannetInnholds.size(), is(1));
		assertThat(skannetInnholds.get(0).getVedleggInnhold(), is(SkannetInnholdTestDataProvider.VEDLEGG_INNHOLD));
	}

	private JournalpostBuilder aJournalpost() {
		return createJournalpost(FIL_UUID)
				.avsenderMottakerId(FNR)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.journalDato(JANUARY_1_2020);
	}

	private JournalpostBuilder aJournalpostWithoutHoveddokument() {
		return createJournalpostWithoutHoveddokument()
				.avsenderMottakerId(FNR)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.journalDato(JANUARY_1_2020);
	}

	private IdentifiserJournalpostRequest createRequest(String kanalReferanseId, String mottaksKanal) {
		IdentifiserJournalpostRequest request = new IdentifiserJournalpostRequest();

		request.setKanalReferanseId(kanalReferanseId);
		request.setMottakskanal(mottaksKanal);

		return request;
	}

	private Journalpost buildAndPersist(JournalpostBuilder journalpost) {
		return joarkRepository.save(journalpost.build());
	}

	private void assertDokumentInnsyn(Dokument dokument, Matcher<InnsynDokument> matcher) {
		assertThat(dokument.getInnsynDokument(), matcher);
	}

}
