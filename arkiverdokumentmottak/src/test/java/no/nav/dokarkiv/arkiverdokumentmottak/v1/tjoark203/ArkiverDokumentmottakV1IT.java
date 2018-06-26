package no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203;

import static no.nav.dokarkiv.arkiverdokumentmottak.ServiceConstants.FORSENDELSE_MOTTAK_ID_KEY;

import no.nav.dokarkiv.arkiverdokumentmottak.AbstractArkiverDokumentmottakItest;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.to.JournalforInngaaendeForsendelseRequestTo;
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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.JournalpostDokumentInfoRelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Integration test for HentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class ArkiverDokumentmottakV1IT extends AbstractArkiverDokumentmottakItest {

	private static final long METAFORCE_INSTANCE_ID = 555L;
	private static final DokumentStatusCode DOKUMENT_INFO_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final JournalStatusCode JOURNAL_STATUS = JournalStatusCode.D;

	private Long journalpostId;
	private Long dokumentInfoId;

	private JournalforInngaaendeForsendelseRequestTo request;


	@Before
	public void setUp() {
		Journalpost journalpost = joarkRepository.save(buildAndPersistJournalpost());
		journalpostId = journalpost.getId();
		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId();
	}

	@Test
	public void findJournalpostByTilleggsopplysningerContaining() throws Exception {
		Journalpost journalpost = buildAndPersistJournalpost();
		Map<String, String> map = new HashMap<>();
		map.put("key", "val");
		journalpost.setTilleggsopplysninger(map);
		//joarkRepository.save(journalpost);

		arkiverDokumentmottakProvider.journalforInngaaendeForsendelse(createRequest());

	}

	private JournalforInngaaendeForsendelseRequest createRequest() {
		return new JournalforInngaaendeForsendelseRequest()
				.withJournalpost(new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost()
						.withJournalpostTilleggsopplysninger(new Tilleggsopplysning()
								.withOpplysningsnoekkel(FORSENDELSE_MOTTAK_ID_KEY)
								.withOpplysningsverdi("VERDI"))
						.withJournalpostDokumentInfoRelasjon(
								new JournalpostDokumentInfoRelasjon()
										.withTilknyttetJournalpostSom(TilknyttetJournalpostEnum.HOVEDDOKUMENT)
										.withDokumentInfo(new DokumentInfo())));
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


}
