package no.nav.dokarkiv.core.akjsonslogg;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.JournalpostDokumentInfoPair;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@RunWith(MockitoJUnitRunner.class)
public class LagreAksjonsLoggServiceTest {

	@Mock
	AksjonsLoggService aksjonsLoggService;

	@Mock
	JournalpostDokumentInfoRelasjonRepository relasjonRepository;

	@InjectMocks
	LagreAksjonsLoggService lagreAksjonsLoggService;

	private final String HJEMMEL="POL";
	private final String UTFOERT_AV="MEG";
	private final String MELDING="HEI";

	@Test
	public void test() throws UgyldigAksjonsLoggException {
		Map<JournalpostDokumentInfoPair, List<ArkivElementEndringTO>> aksjonsLoggMap = new HashMap<>();
		aksjonsLoggMap.put(JournalpostDokumentInfoPair.of(1L, 1L), new ArrayList<>());
		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ARKIVERING, aksjonsLoggMap, HJEMMEL, MELDING, UTFOERT_AV);

		verify(aksjonsLoggService).validateAndSaveAksjonsLogg(any(AksjonsLoggTO.class), any(List.class));

	}
}