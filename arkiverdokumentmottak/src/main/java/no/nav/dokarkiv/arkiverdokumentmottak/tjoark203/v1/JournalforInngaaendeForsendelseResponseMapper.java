package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import no.nav.dokarkiv.arkiverdokumentmottak.DokumentInfoIdVedleggTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.DokumentInfoIdVedlegg;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * JournalforInngaaendeForsendelseResponseMapper implementation
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 16.02.2017
 */
@Component
public class JournalforInngaaendeForsendelseResponseMapper {

	public JournalforInngaaendeForsendelseResponse map(JournalforInngaaendeForsendelseResponseTo to) {
		Assert.notNull(to, "Feil ved mapping av JournalforInngaaendeForsendelseResponseTo til JournalforInngaaendeForsendelseResponse: TO objektet er null");

		JournalforInngaaendeForsendelseResponse response = new JournalforInngaaendeForsendelseResponse();

		response.setJournalpostId(to.getJournalpostId());
		response.setDokumentInfoIdHoveddokument(to.getDokumentInfoIdHoveddokument());

		for (DokumentInfoIdVedleggTo vedlegg : to.getDokumentInfoIdVedleggTo()) {
			response.getDokumentInfoIdVedleggListe().add(createDokumentInfoIdVedleggFromTo(vedlegg));
		}

		return response;
	}

	private DokumentInfoIdVedlegg createDokumentInfoIdVedleggFromTo(DokumentInfoIdVedleggTo from) {
		DokumentInfoIdVedlegg v = new DokumentInfoIdVedlegg();
		v.setDokumentInfoId(from.getDokumentInfoId());
		v.setDokumentTypeId(from.getDokumentTypeId());
		return v;
	}
}