package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.skjermarkivenhet.AbstractSkjermArkivenhetIT;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;

public class Rjoark100IT extends AbstractSkjermArkivenhetIT {

	@Test
	public void skalSkjermeArkivenhet_medJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET, HttpMethod.POST, createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name()), String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AksjonTypeCode.ENDRE_BEGRENSNING));
	}
}
