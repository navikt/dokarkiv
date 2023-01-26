package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122;

import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.dokumentproduksjoninfo.AbstractDokumentproduksjoninfoItest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoDokumentInfoIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test for HentJournalpostInfo.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class HentJournalpostInfoIT extends AbstractDokumentproduksjoninfoItest {

	private static final long METAFORCE_INSTANCE_ID = 555L;
	private static final DokumentStatusCode DOKUMENT_INFO_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final JournalStatusCode JOURNAL_STATUS = JournalStatusCode.D;
	private static final String JOURNALFOERENDE_ENHET = "9999";
	private static final FagomradeCode FAGOMRADE_CODE = FagomradeCode.AAP;
	private static final FagsystemCode FAGSYSTEM_CODE = FagsystemCode.FS22;
	private static final Long SAKID = 12L;
	private static final String BRUKERID = "999999999";
	private static final BrukerTypeCode BRUKER_TYPE_CODE = BrukerTypeCode.PERSON;
	private static final int ANTALL_RETUR = 1;

	private Long journalpostId;
	private Long dokumentInfoId;

	@BeforeEach
	public void setUp() {
		Journalpost journalpost = journalpostTestRepository.persist(buildJournalpost());
		journalpostId = journalpost.getId();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId();
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostNotFound() {
		assertThrows(HentJournalpostInfoJournalpostIkkeFunnet.class,
				() -> dokumentproduksjonInfoProvider.hentJournalpostInfo(new HentJournalpostInfoRequest()
						.withJournalpostId(123L)
						.withDokumentInfoId(null)));
	}

	@Test
	public void shouldThrowExceptionWhenDokumentInfoNotFoundOnJournalpost() {
		HentJournalpostInfoRequest request = createRequest();
		request.setDokumentInfoId(123L);

		assertThrows(HentJournalpostInfoDokumentInfoIkkeFunnet.class,
				() -> dokumentproduksjonInfoProvider.hentJournalpostInfo(request));
	}

	@Test
	public void shouldReturnJournalStatusDokumentStatusAndMetaforceInstanceId() {

		HentJournalpostInfoResponse response = dokumentproduksjonInfoProvider.hentJournalpostInfo(createRequest());

		assertThat(response.getJournalStatus(), is(JOURNAL_STATUS.name()));
		assertThat(response.getDokumentStatus(), is(DOKUMENT_INFO_STATUS.name()));
		assertThat(response.getMetaForceInstanceId(), is(METAFORCE_INSTANCE_ID));
		assertCommonMetadata(response);
	}

	@Test
	public void shouldOnlyReturnJournalStatusWhenDokumentInfoMissingFromInput() {
		HentJournalpostInfoRequest request = createRequest();
		request.setDokumentInfoId(0L);
		HentJournalpostInfoResponse response = dokumentproduksjonInfoProvider.hentJournalpostInfo(request);

		assertThat(response.getJournalStatus(), is(JOURNAL_STATUS.name()));
		assertThat(response.getDokumentStatus(), is(nullValue()));
		assertThat(response.getMetaForceInstanceId(), is(nullValue()));
		assertCommonMetadata(response);
	}

	private void assertCommonMetadata(HentJournalpostInfoResponse response) {
		assertThat(response.getJournalfEnhet(), is(JOURNALFOERENDE_ENHET));
		assertThat(response.getFagomrade(), is(FAGOMRADE_CODE.name()));
		assertThat(response.getBrukerId(), is(BRUKERID));
		assertThat(response.getBrukerType(), is(BRUKER_TYPE_CODE.name()));
		assertThat(response.getSaksNummer(), is(SAKID.toString()));
		assertThat(response.getFagsystem(), is(FAGSYSTEM_CODE.name()));
		assertThat(response.getAntallRetur(), is(ANTALL_RETUR));
	}

	private Journalpost buildJournalpost() {
		return JournalpostBuilder.getJournalpostBuilder()
				.journalStatus(JOURNAL_STATUS)
				.journalForendeEnhetId(JOURNALFOERENDE_ENHET)
				.fagomrade(FAGOMRADE_CODE)
				.journalpostType(JournalpostTypeCode.U)
				.antallRetur(ANTALL_RETUR)
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.saksrelasjon(SaksrelasjonBuilder.getSaksrelasjonBuilder()
						.fagsystem(FAGSYSTEM_CODE)
						.sakId(SAKID)
						.saknrfk(SAKID.toString())
						.opprettetKildeNavn("test")
						.build())
				.brukere(BrukerBuilder.getBrukerBuilder()
						.brukerId(BRUKERID)
						.brukerType(BRUKER_TYPE_CODE)
						.opprettetKildeNavn("test")
						.build())
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
						.build())
				.build();
	}

	private HentJournalpostInfoRequest createRequest() {
		return new HentJournalpostInfoRequest()
				.withJournalpostId(journalpostId)
				.withDokumentInfoId(dokumentInfoId);
	}

}
