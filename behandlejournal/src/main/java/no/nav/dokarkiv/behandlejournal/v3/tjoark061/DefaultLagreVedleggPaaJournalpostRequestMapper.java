package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import no.nav.dokarkiv.behandlejournal.v2.SporingsMetaData;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.DokumentInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.StrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.UstrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.lagrevedleggpaajournalpost.JournalfoertDokumentInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link DefaultLagreVedleggPaaJournalpostRequestMapper}
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Component
public class DefaultLagreVedleggPaaJournalpostRequestMapper implements LagreVedleggPaaJournalpostRequestMapper {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LagreVedleggPaaJournalpostRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostRequest wsRequest) {
		JournalfoertDokumentInfo journalfortDokumentInfo = wsRequest.getJournalfortDokumentInfo();
		DokumentInfo dokumentInfo = DokumentInfo.builder()
				.innskrenketPartsinnsyn(journalfortDokumentInfo.isBegrensetPartsInnsyn())
				.brevkode(journalfortDokumentInfo.getDokumentType().getValue())
				.dokumenttypeId(journalfortDokumentInfo.getDokumentType().getValue())
				.tittel(isEmpty(journalfortDokumentInfo.getBrukerOppgittTittel()) ? null : journalfortDokumentInfo.getBrukerOppgittTittel())
				.tilleggsopplysninger(convertNoekkelVerdiSettToMap(journalfortDokumentInfo.getTilleggsopplysninger()))
				.build();
		journalfortDokumentInfo.getBeskriverInnhold().forEach(dokumentInnhold -> dokumentInfo.addFilDetaljer(convertDokumentInnhold(dokumentInnhold)));
		return LagreVedleggPaaJournalpostRequest.builder()
				.journalpostId(wsRequest.getJournalpostId() == null ? null : Long.parseLong(wsRequest.getJournalpostId()))
				.sporingsMetaData(SporingsMetaData.builder()
						.applikasjonsID(wsRequest.getApplikasjonsID())
						.personFornavn(wsRequest.getPersonFornavn())
						.personEtternavn(wsRequest.getPersonEtternavn())
						.build())
				.dokumentInfo(dokumentInfo)
				.build();
	}

	private Map<String, String> convertNoekkelVerdiSettToMap(NoekkelVerdiSett source) {
		if (source == null) {
			return null;
		}
		Map<String, String> tilleggsopplysninger = new HashMap<>();
		for (NoekkelVerdiPar noekkelVerdiPar : source.getInneholderNoekkelVerdiPar()) {
			tilleggsopplysninger.put(noekkelVerdiPar.getNoekkel(), noekkelVerdiPar.getVerdi());
		}
		return tilleggsopplysninger;
	}

	private FilDetaljer convertDokumentInnhold(DokumentInnhold dokumentInnhold) {
		FilDetaljer filDetaljer = new FilDetaljer();
		filDetaljer.setFiltype(FilTypeCode.valueOf(dokumentInnhold.getFiltype().getValue()));
		filDetaljer.setVariantFormat(VariantFormatCode.valueOf(dokumentInnhold.getVariantformat().getValue()));
		filDetaljer.setFilnavn(dokumentInnhold.getFilnavn());

		if (dokumentInnhold instanceof UstrukturertInnhold) {
			UstrukturertInnhold ustrukturertInnhold = (UstrukturertInnhold) dokumentInnhold;
			filDetaljer.setFileContent(ustrukturertInnhold.getInnhold());
		}

		if (dokumentInnhold instanceof StrukturertInnhold) {
			StrukturertInnhold strukturertInnhold = (StrukturertInnhold) dokumentInnhold;
			filDetaljer.setFileContent(strukturertInnhold.getInnhold());
		}
		return filDetaljer;
	}
}
