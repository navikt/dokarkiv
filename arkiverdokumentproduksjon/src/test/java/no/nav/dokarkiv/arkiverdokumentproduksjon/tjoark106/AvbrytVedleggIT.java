package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;


import no.nav.dokarkiv.arkiverdokumentproduksjon.AbstractArkiverdokumentproduksjonItest;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentIkkeVedlegg;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytVedleggRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the AvbrytVedlegg
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public class AvbrytVedleggIT extends AbstractArkiverdokumentproduksjonItest {

	private static final String OPPRETTET_KILDE_NAVN = "opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Tester";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Tester2";

	@BeforeEach
	public void setUp() throws Exception {
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldAvbrytVedlegg() throws Exception {
		Journalpost journalpost = buildAndPersistJournalpost(DokumentStatusCode.UNDER_REDIGERING);

		AvbrytVedleggRequest request = createRequest(journalpost);
		arkiverDokumentproduksjonProvider.avbrytVedlegg(request);

		Journalpost resultJournalpost = journalpostTestRepository.findById(journalpost.getJournalpostId()).get();
		assertThat(resultJournalpost.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(resultJournalpost.getSaksrelasjon().getEndretAvNavn(), is(ENDRET_AV_NAVN));

		DokumentInfo dokumentInfo = findDokumentInfo(resultJournalpost);
		assertThat(dokumentInfo.getDokumentstatus(), is(DokumentStatusCode.AVBRUTT));
		assertThat(dokumentInfo.getEndretAvNavn(), is(ENDRET_AV_NAVN));
	}

	@Test
	public void shouldDeleteJournalpostDokumentInfoRelation() throws Exception {
		DokumentInfo dokumentInfo = createDokumentInfo(DokumentStatusCode.UNDER_REDIGERING);
		Journalpost processedJp = buildAndPersistJournalpost(JournalStatusCode.D, VEDLEGG, dokumentInfo);
		Journalpost nonProcessedJp = buildAndPersistJournalpost(JournalStatusCode.D, HOVEDDOKUMENT, dokumentInfo);

		AvbrytVedleggRequest request = createRequest(processedJp);

		arkiverDokumentproduksjonProvider.avbrytVedlegg(request);

		Journalpost resultJournalpost = journalpostTestRepository.findById(processedJp.getJournalpostId()).get();
		assertThat(resultJournalpost.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(resultJournalpost.getSaksrelasjon().getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(resultJournalpost.getJournalpostDokumentInfoRelasjoner().isEmpty(), is(true));

		DokumentInfo resultDokumentInfo = dokumentInfoRepository.findById(dokumentInfo.getDokumentInfoId()).get();
		assertThat(resultDokumentInfo.getJournalpostRelasjoner().size(), is(1));
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = getJournalpostRelasjon(resultDokumentInfo);
		assertThat(journalpostDokumentInfoRelasjon.getJournalpost().getId(), is(nonProcessedJp.getId()));
	}


	@Test
	public void shouldThrowIllegalArgumentException() {
		assertThrows(IllegalArgumentException.class,
				() -> arkiverDokumentproduksjonProvider.avbrytVedlegg(new AvbrytVedleggRequest()),
				"JournalpostId cannot be empty or missing");
	}

	@Test
	public void shouldThrowDokumentAlleredeAvbrutt() {
		Journalpost journalpost = buildAndPersistJournalpost(DokumentStatusCode.AVBRUTT);

		assertThrows(AvbrytVedleggDokumentAlleredeAvbrutt.class,
				() -> arkiverDokumentproduksjonProvider.avbrytVedlegg(createRequest(journalpost)),
				"dokumentinfoid=" + findDokumentInfo(journalpost).getDokumentInfoId()
						+ " is already Avbrutt");
	}

	@Test
	public void shouldThrowJournalpostIkkeFunnet() throws Exception {
		AvbrytVedleggRequest avbrytVedleggRequest = new AvbrytVedleggRequest()
				.withJournalpostId(1L)
				.withDokumentInfoId(1L)
				.withEndretAvNavn(ENDRET_AV_NAVN);

		assertThrows(AvbrytVedleggJournalpostIkkeFunnet.class,
				() -> arkiverDokumentproduksjonProvider.avbrytVedlegg(avbrytVedleggRequest),
				"journalpostid=1 does not exist");
	}

	@Test
	public void shouldThrowDokumentIkkeFunnet() throws Exception {
		Journalpost journalpost = buildAndPersistJournalpost(DokumentStatusCode.UNDER_REDIGERING);

		AvbrytVedleggRequest avbrytVedleggRequest = createRequest(journalpost);
		avbrytVedleggRequest.setDokumentInfoId(1L);

		assertThrows(AvbrytVedleggDokumentIkkeFunnet.class,
				() -> arkiverDokumentproduksjonProvider.avbrytVedlegg(avbrytVedleggRequest),
				"Journalpost missing DokumentInfo with dokumentinfoid=1");
	}

	@Test
	public void shouldThrowJournalpostIkkeUnderArbeid() throws Exception {
		Journalpost journalpost = buildAndPersistJournalpost(DokumentStatusCode.UNDER_REDIGERING,
				JournalStatusCode.A, VEDLEGG);

		assertThrows(AvbrytVedleggJournalpostIkkeUnderArbeid.class,
				() -> arkiverDokumentproduksjonProvider.avbrytVedlegg(createRequest(journalpost)),
				"Invalid JournalStatus for journalpostid=" + journalpost.getJournalpostId());
	}

	@Test
	public void shouldThrowDokumentIkkeVedlegg() throws Exception {
		Journalpost journalpost = buildAndPersistJournalpost(DokumentStatusCode.UNDER_REDIGERING,
				JournalStatusCode.D, HOVEDDOKUMENT);

		assertThrows(AvbrytVedleggDokumentIkkeVedlegg.class,
				() -> arkiverDokumentproduksjonProvider.avbrytVedlegg(createRequest(journalpost)),
				"tilknyttetjournalpostsom=HOVEDDOKUMENT is not Vedlegg on relasjon journalpostid="
						+ journalpost.getJournalpostId());
	}

	private JournalpostDokumentInfoRelasjon getJournalpostRelasjon(DokumentInfo resultDokumentInfo) {
		return resultDokumentInfo.getJournalpostRelasjoner().iterator().next();
	}

	private DokumentInfo findDokumentInfo(Journalpost resultJournalpost) {
		return resultJournalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo();
	}

	private AvbrytVedleggRequest createRequest(Journalpost journalpost) throws Exception {
		return new AvbrytVedleggRequest()
				.withJournalpostId(journalpost.getJournalpostId())
				.withDokumentInfoId(findDokumentInfo(journalpost).getDokumentInfoId())
				.withEndretAvNavn(ENDRET_AV_NAVN);
	}

	private Journalpost buildAndPersistJournalpost(DokumentStatusCode dokumentStatusCode) {
		return buildAndPersistJournalpost(JournalStatusCode.D,
				VEDLEGG,
				createDokumentInfo(dokumentStatusCode));
	}

	private Journalpost buildAndPersistJournalpost(DokumentStatusCode dokumentStatusCode,
												   JournalStatusCode journalStatusCode,
												   TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode) {
		return buildAndPersistJournalpost(journalStatusCode,
				tilknyttetJournalpostSomCode,
				createDokumentInfo(dokumentStatusCode));
	}

	private Journalpost buildAndPersistJournalpost(JournalStatusCode journalStatusCode,
												   TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode,
												   DokumentInfo dokumentInfo) {
		Journalpost journalpost = getJournalpostBuilder()
				.avsenderMottakerId("02016126007")
				.journalStatus(journalStatusCode)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fagomrade(FagomradeCode.PEN)
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.saknrfk("1")
								.fagsystem(FagsystemCode.PEN)
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.build())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
								.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
								.dokumentInfo(dokumentInfo)
								.build())
				.build();

		journalpostTestRepository.persist(journalpost);
		return journalpost;
	}

	private DokumentInfo createDokumentInfo(DokumentStatusCode dokumentStatusCode) {
		return getDokumentInfoBuilder()
				.dokumentstatus(dokumentStatusCode)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}


}
