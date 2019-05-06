package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalforInngaaendeForsendelseV2RequestToTest {
	public static final boolean FORSOK_ENDELIG_JF = true;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private JournalforInngaaendeForsendelseV2RequestTo requestTo;

	@Before
	public void setUp() {
		requestTo = createRequestTo(FORSOK_ENDELIG_JF);
	}

	@Test
	public void shouldValidate() {
		requestTo.validate();
	}

	@Test
	public void shouldThrowMissingOpprettetAvNavn() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("OpprettetAvNavn");

		requestTo.getJournalpost().setOpprettetAvNavn(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowMissingMottattDato() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("MottattDato");

		requestTo.getJournalpost().setMottattDato(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowMissingMottakskanal() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Mottakskanal");

		requestTo.getJournalpost().setMottakskanal(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowMissingKanalReferanseId() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("KanalReferanseId");

		requestTo.getJournalpost().setKanalReferanseId(null);
		requestTo.validate();
	}

	@Test
	public void shouldNotThrowMissingSaksrelasjon() {
		requestTo.getJournalpost().setSaksrelasjon(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowEmptySaksrelasjon() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Saksrelasjon");

		requestTo.getJournalpost().setSaksrelasjon(new Saksrelasjon());
		requestTo.validate();
	}

	@Test
	public void shouldThrowMissingSaksNummer() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("SaksNummer");

		requestTo.getJournalpost().getSaksrelasjon().setSakId(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowMissingFagsystem() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Fagsystem");

		requestTo.getJournalpost().getSaksrelasjon().setFagsystem(null);
		requestTo.validate();
	}

	@Test
	public void shouldNotThrowEmptyBrukerList() {
		requestTo.getJournalpost().clearBrukere();
		requestTo.validate();
	}

	@Test
	public void shouldThrowEmptyBruker() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Bruker");

		requestTo.getJournalpost().addBruker(new Bruker());
		requestTo.validate();
	}

	@Test
	public void shouldThrowMissingSakId() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("BrukerId");

		requestTo.getJournalpost().getBrukere().iterator().next().setBrukerId(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowMissingBrukerType() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("BrukerType");

		requestTo.getJournalpost().getBrukere().iterator().next().setBrukerType(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingJournalpostInfoRelasjoner() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner");

		requestTo.getJournalpost().clearJournalpostDokumentInfoRelasjoner();
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionEmptyJournalpostInfoRelasjoner() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner");

		requestTo.getJournalpost().addJournalpostDokumentInfoRelasjon(new JournalpostDokumentInfoRelasjon());
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingTilknyttetJournalpostSom() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("TilknyttetJournalpostSom");

		requestTo.getJournalpost().getJournalpostDokumentInfoRelasjoner().iterator().next().setTilknyttetJournalpostSom(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingDokumentInfo() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo");

		requestTo.getJournalpost().getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingDokumentInfoKategori() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("DokumentInfo.Kategori");

		requestTo.getJournalpost().getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setKategori(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingFilDetaljer() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer");

		requestTo.getJournalpost()
				.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.clearFildetaljerListe();
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingFilDetaljerFiltype() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.Filtype");

		requestTo.getJournalpost().getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setFiltype(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingFilDetaljerVariantFormat() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.VariantFormat");

		requestTo.getJournalpost().getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setVariantFormat(null);
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingFilDetaljerDokument() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.Dokument");

		requestTo.getJournalpost().getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setFileContent(null);
		requestTo.validate();
	}

	@Test
	public void shouldNotThrowExceptionMissingSkannetInnhold() {
		requestTo.getJournalpost()
				.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.clearSkannetInnholdListe();
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionEmptySkannetInnhold() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("SkannetInnhold");

		requestTo.getJournalpost()
				.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.addSkannetInnhold(new SkannetInnhold());
		requestTo.validate();
	}

	@Test
	public void shouldThrowExceptionMissingSkannetInnholdVedleggInnhold() {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("SkannetInnhold");

		requestTo.getJournalpost()
				.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.setVedleggInnhold(null);
		requestTo.validate();
	}

	private JournalforInngaaendeForsendelseV2RequestTo createRequestTo(boolean forsokEndeligJf) {
		return new JournalforInngaaendeForsendelseV2RequestTo(
				forsokEndeligJf,
				getJournalpostBuilder()
						.opprettetAvNavn("Skrue McDuck")
						.mottattDato(new Date())
						.mottakskanal(MottaksKanalCode.ALTINN)
						.kanalReferanseId("KanalreferanseId")
						.saksrelasjon(createSaksrelasjon())
						.brukere(createBruker())
						.dokumentInfoRelasjoner(createDokumentInfoRelasjoner())
						.build()
		);
	}

	private Saksrelasjon createSaksrelasjon() {
		return getSaksrelasjonBuilder()
				.sakId("123")
				.fagsystem(FagsystemCode.PEN)
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
				.filtype(FilTypeCode.PDF)
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

	private JournalpostDokumentInfoRelasjon createDokumentInfoRelasjoner() {
		return getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
				.dokumentInfo(
						getDokumentInfoBuilder()
								.kategori(DokumentKategoriCode.B)
								.dokumenttypeId("12345")
								.filDetaljerList(createFildetaljer())
								.skannetInnhold(createSkannetInnhold())
								.build())
				.build();
	}
}