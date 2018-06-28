package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of OppdaterJournalpostArkiverDokumentRequestMapper
 *
 * @author Sigurd Midttun
 */
@Component
public class OppdaterJournalpostArkiverDokumentRequestMapper {

	public OppdaterJournalpostArkiverDokumentRequestTo map(OppdaterJournalpostArkiverDokumentRequest wsRequest) throws UgyldigInputException {
		try {
			return OppdaterJournalpostArkiverDokumentRequestTo.builder()
					.journalpostId(wsRequest.getJournalpostId())
					.ferdigstillJournalpost(wsRequest.isFerdigstillJournalpost())
					.dokumentInfoId(wsRequest.getDokumentInfoId())
					.utsendingskanal(wsRequest.getUtsendingskanal() == null ? null : UtsendingsKanalCode.valueOf(wsRequest.getUtsendingskanal()))
					.endretAvNavn(wsRequest.getEndretAvNavn())
					.datoDokument(wsRequest.getDatoDokument().toGregorianCalendar().getTime())
					.fildetaljerSet(wsRequest.getFildetaljerListe().stream()
							.map(fildetaljer -> FilDetaljer.builder()
									.filtype(fildetaljer.getFiltype() == null ? null : FilTypeCode.valueOf(fildetaljer.getFiltype()))
									.variantFormat(fildetaljer.getVariantformat() == null ? null : VariantFormatCode.valueOf(fildetaljer
											.getVariantformat()))
									.fileContent(fildetaljer.getRedigerbartDokument())
									.filstorrelse(fildetaljer.getRedigerbartDokument() == null ? null : String.valueOf(fildetaljer
											.getRedigerbartDokument().length))
									.filUuid(UUID.randomUUID().toString())
									.build())
							.collect(Collectors.toSet()))
					.build();

		} catch (Exception e) {
			throw new UgyldigInputException(e.getMessage(), wsRequest.getJournalpostId());
		}
	}
}
