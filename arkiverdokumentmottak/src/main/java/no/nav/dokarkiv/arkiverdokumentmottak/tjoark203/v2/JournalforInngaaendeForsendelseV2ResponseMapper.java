package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;


import no.nav.dokarkiv.core.nsb.DokumentInfoIdVedleggTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.DokumentInfoIdVedlegg;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.JournalTilstandEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Mapper for TJOARK203 request
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class JournalforInngaaendeForsendelseV2ResponseMapper {

	public JournalforInngaaendeForsendelseResponse map(JournalforInngaaendeForsendelseV2ResponseTo to) {
		Assert.notNull(to, "Feil ved mapping av JournalforInngaaendeForsendelseV2ResponseTo til JournalforInngaaendeForsendelseResponse: TO objektet er null");

		JournalforInngaaendeForsendelseResponse response = new JournalforInngaaendeForsendelseResponse();

		response.setJournalpostId(to.getJournalpostId());
		response.setDokumentInfoIdHoveddokument(to.getDokumentInfoIdHoveddokument());
		response.setJournalTilstand(JournalTilstandEnum.valueOf(to.getJournalTilstand()));

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
