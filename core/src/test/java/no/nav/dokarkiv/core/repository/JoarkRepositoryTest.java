package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.Journalpost;
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
import org.dozer.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class})
@AutoConfigureTestEntityManager
@AutoConfigureTestDatabase
@ActiveProfiles("itest")
public class JoarkRepositoryTest {

	private static final long METAFORCE_INSTANCE_ID = 555L;
	private static final DokumentStatusCode DOKUMENT_INFO_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final JournalStatusCode JOURNAL_STATUS = JournalStatusCode.D;

	@Inject
	protected JoarkRepository joarkRepository;

	@Test
	public void findJournalpostByTilleggsopplysningerContaining() {
		Journalpost journalpost = buildAndPersistJournalpost();
		Map<String, String> map = new HashMap<>();
		map.put("key", "val");
		journalpost.setTilleggsopplysninger(map);
		joarkRepository.save(journalpost);
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