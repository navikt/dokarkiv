package no.nav.dokarkiv.core.consumer.aktoer;

import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.util.DateConverterUtil;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentListeRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentListeResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdListeRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdListeResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;

import java.util.Date;
import java.util.List;

/**
 * Mock class for AktoerV2
 *
 * @author Roar Bjurstrom, Visma Consulting.
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class AktoerConsumerV2Mock implements AktoerV2 {

	private static volatile List<HentAktoerIdForIdentRequest> identInspectionObjects = Lists.newArrayList();

	public static final String AKTOER_ID = "***gammelt_fnr******gammelt_fnr***00";
	public static final String FNR = "***gammelt_fnr***";
	public static final String CURRENT_IDENT = "***gammelt_fnr***1";
	public static final String FAIL_IDENT = "***gammelt_fnr***067";
	public static final List<String> HISTORICAL_IDENTS = Lists.newArrayList("***gammelt_fnr***0", "234567810");


	@Override
	public HentIdentForAktoerIdResponse hentIdentForAktoerId(HentIdentForAktoerIdRequest hentIdentForAktoerIdRequest) {
		HentIdentForAktoerIdResponse response = new HentIdentForAktoerIdResponse();
		response.setIdent(FNR);
		return response;
	}

	@Override
	public HentAktoerIdForIdentResponse hentAktoerIdForIdent(HentAktoerIdForIdentRequest request) throws HentAktoerIdForIdentPersonIkkeFunnet {
		identInspectionObjects.add(request);

		if (FAIL_IDENT.equals(request.getIdent())) {
			throw new HentAktoerIdForIdentPersonIkkeFunnet(request.getIdent(), null);
		}

		HentAktoerIdForIdentResponse response = new HentAktoerIdForIdentResponse();
		response.setAktoerId(AKTOER_ID);
		response.getIdentHistorikk().add(createIdentDetaljer(CURRENT_IDENT));
		for (String ident : HISTORICAL_IDENTS) {
			response.getIdentHistorikk().add(createIdentDetaljer(ident));
		}
		return response;
	}

	@Override
	public HentAktoerIdForIdentListeResponse hentAktoerIdForIdentListe(HentAktoerIdForIdentListeRequest hentAktoerIdForIdentListeRequest) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public HentIdentForAktoerIdListeResponse hentIdentForAktoerIdListe(HentIdentForAktoerIdListeRequest hentIdentForAktoerIdListeRequest) {
		throw new UnsupportedOperationException("not supported");
	}

	@Override
	public void ping() {

	}

	private IdentDetaljer createIdentDetaljer(String ident) {
		IdentDetaljer identDetaljer = new IdentDetaljer();
		identDetaljer.setTpsId(ident);
		identDetaljer.setDatoFom(DateConverterUtil.convertDateToXMLGregorianCalendar(new Date()));
		return identDetaljer;
	}
}
