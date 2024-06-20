package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.dokumentproduksjoninfo.AbstractDokumentproduksjoninfoItest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalOgDokumentStatusDokumentInfoIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalOgDokumentStatusJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test for HentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class HentJournalOgDokumentStatusIT extends AbstractDokumentproduksjoninfoItest {

	private static final long METAFORCE_INSTANCE_ID = 555L;
	private static final DokumentStatusCode DOKUMENT_INFO_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final JournalStatusCode JOURNAL_STATUS = JournalStatusCode.D;

	private Long journalpostId;
	private Long dokumentInfoId;

	private HentJournalOgDokumentStatusRequest request;

	@BeforeEach
	public void setUp() {
		Journalpost journalpost = journalpostTestRepository.persist(buildAndPersistJournalpost());
		journalpostId = journalpost.getId();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId();
		createRequest();
		stubMsGraphMemberOfEgenAnsatt(MS_ID_SAKSBEHANDLER);
	}

	@Test
	public void findJournalpostByTilleggsopplysningerContaining() {
		Journalpost journalpost = buildAndPersistJournalpost();
		Map<String, String> map = new HashMap<>();
		map.put("key", "val");
		journalpost.setTilleggsopplysninger(map);
		journalpostTestRepository.persist(journalpost);

		Journalpost journalpost2 = buildAndPersistJournalpost();
		Map<String, String> map2 = new HashMap<>();
		map2.put("key", "val2");
		journalpost.setTilleggsopplysninger(map2);
		journalpostTestRepository.persist(journalpost2);
	}


	@Test
	public void shouldThrowExceptionWhenJournalpostNotFound() {
		request.setJournalpostId(123L);

		assertThrows(HentJournalOgDokumentStatusJournalpostIkkeFunnet.class,
				() -> dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(request));
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() {
		request.setDokumentInfoId(123L);

		assertThrows(HentJournalOgDokumentStatusDokumentInfoIkkeFunnet.class,
				() -> dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(request));
	}

	@Test
	public void shouldReturnJournalStatusDokumentStatusAndMetaforceInstanceId() {
		HentJournalOgDokumentStatusResponse response = dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(request);

		assertThat(response.getJournalStatus(), is(JOURNAL_STATUS.name()));
		assertThat(response.getDokumentStatus(), is(DOKUMENT_INFO_STATUS.name()));
		assertThat(response.getMetaForceInstanceId(), is(METAFORCE_INSTANCE_ID));
	}

	@Test
	public void dokumentInfoMissingFromInput_shouldOnlyReturnJournalStatus() {
		request.setDokumentInfoId(0L);
		HentJournalOgDokumentStatusResponse response = dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(request);

		assertThat(response.getJournalStatus(), is(JOURNAL_STATUS.name()));
		assertThat(response.getDokumentStatus(), is(nullValue()));
		assertThat(response.getMetaForceInstanceId(), is(nullValue()));
	}

	private Journalpost buildAndPersistJournalpost() {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalStatus(JOURNAL_STATUS)
				.journalpostType(JournalpostTypeCode.U)
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder
						.getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetAvNavn("testuser")
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.opprettetKildeNavn("test")
						.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
								.opprettetKildeNavn("test")
								.dokumentstatus(DOKUMENT_INFO_STATUS)
								.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
										.filtype(FilTypeCode.PDF)
										.variantFormat(VariantFormatCode.PRODUKSJON)
										.opprettetKildeNavn("test")
										.metaforceInstanceId(METAFORCE_INSTANCE_ID)
										.build())
								.build())
						.build()).build();
	}

	private void createRequest() {
		request = new HentJournalOgDokumentStatusRequest();
		request.setJournalpostId(journalpostId);
		request.setDokumentInfoId(dokumentInfoId);
	}

}
