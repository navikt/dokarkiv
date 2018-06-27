package no.nav.dokarkiv.arkiverdokumentmottak.v1.tjoark203;

import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseRequestDataUtil.createJournalpost;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.arkiverdokumentmottak.AbstractArkiverDokumentmottakItest;
import no.nav.dokarkiv.arkiverdokumentmottak.v1.to.JournalforInngaaendeForsendelseRequestTo;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for HentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class Tjoark203V1IT extends AbstractArkiverDokumentmottakItest {



	private Long journalpostId;
	private Long dokumentInfoId;
	private Journalpost journalpost;
	private JournalforInngaaendeForsendelseRequestTo request;


	@Before
	public void setUp() {
//		journalpost = joarkRepository.save(createJournalpostEntity());
//		Map<String, String> map = new HashMap<>();
//		map.put("key", "val");
//		journalpost.setTilleggsopplysninger(map);
//		joarkRepository.save(journalpost);
//		journalpostId = journalpost.getId();
//		dokumentInfoId = journalpost.findAllDokumentInfos().iterator().next().getId();
	}

	@Test
	public void happy() throws Exception {

		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakProvider.journalforInngaaendeForsendelse(createRequest());
		//assertResponse(journalpost, response);

	}

	@Test
	public void findJournalpostByTilleggsopplysningerContaining() throws Exception {
//		Long id = joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi("key", "val").get();

		arkiverDokumentmottakProvider.journalforInngaaendeForsendelse(createRequest());

	}

	private JournalforInngaaendeForsendelseRequest createRequest() {
		return new JournalforInngaaendeForsendelseRequest()
				.withJournalpost(createJournalpost());
	}

	private void assertResponse(Journalpost journalpost, JournalforInngaaendeForsendelseResponse response) {
		Assert.assertThat(response, Matchers.notNullValue());
		Assert.assertThat(response.getJournalpostId(), is(journalpost.getId()));

		no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon pDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		Assert.assertThat(response.getDokumentInfoIdHoveddokument(), is(pDokumentInfoRelasjon.getDokumentInfo()
				.getDokumentInfoId()));

		for (no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (jdir.getTilknyttetJournalpostSom().equals(TilknyttetJournalpostSomCode.VEDLEGG)) {
				Assert.assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentInfoId(), is(jdir.getDokumentInfo()
						.getDokumentInfoId()));
				Assert.assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentTypeId(), is(jdir.getDokumentInfo()
						.getDokumenttypeId()));
			}
		}
	}



}
