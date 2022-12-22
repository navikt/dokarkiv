package no.nav.dokarkiv.arkivervariant.rjoark103;


import no.nav.dokarkiv.arkivervariant.exception.VariantFormatAlreadyExistsException;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentInfoRepository;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_FILUUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;


@Service
public class ArkiverVariantService {

	private final DokumentInfoRepository dokumentInfoRepository;
	private final DokumentFilRepository dokumentFilRepository;
	private final LagreAksjonsLoggService lagreAksjonsLoggService;

	public ArkiverVariantService(DokumentInfoRepository dokumentInfoRepository,
								 DokumentFilRepository dokumentFilRepository, LagreAksjonsLoggService lagreAksjonsLoggService) {
		this.dokumentInfoRepository = dokumentInfoRepository;
		this.dokumentFilRepository = dokumentFilRepository;
		this.lagreAksjonsLoggService = lagreAksjonsLoggService;
	}

	public ArkiverVariantResponse arkiverVariant(ArkiverVariantRequest request, String melding, String utfoertAv, String hjemmel) {
		DokumentInfo dokumentInfo = dokumentInfoRepository.findById(request.getDokumentInfoId())
				.orElseThrow(() -> new DokumentInfoIkkeFunnetException(format("Kan ikke finne dokumentInfo med dokumentInfoId=%s",
						request.getDokumentInfoId())));

		sjekkOmVariantFinnes(dokumentInfo, request.getVariant());

		byte[] decodedFil = base64ToByte(request.getFil());
		FilDetaljer filDetaljer = lagreVariantFormat(dokumentInfo, request.getVariant(), decodedFil, request.getFilnavn(), request.getFilType());

		lagreAksjonsLoggService.lagreAksjonsLogg(AksjonsTypeCode.ARKIVERING, request.getDokumentInfoId(), hjemmel, melding, utfoertAv,
				Arrays.asList(
						ArkivElementEndringTO.builder()
								.arkivElement(FILDETALJER_FILUUID)
								.fraVerdi(null)
								.tilVerdi(filDetaljer.getFilUuid())
								.build(),
						ArkivElementEndringTO.builder()
								.arkivElement(FILDETALJER_VARIANTFORMAT)
								.fraVerdi(null)
								.tilVerdi(filDetaljer.getVariantFormat().name())
								.build()
				));


		return ArkiverVariantResponse.builder()
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.variantFormatCode(filDetaljer.getVariantFormat())
				.filUuid(filDetaljer.getFilUuid())
				.build();
	}

	private void sjekkOmVariantFinnes(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode) {
		FilDetaljer variantFildetaljer = dokumentInfo.findFilDetaljerByVariantFormat(variantFormatCode);
		if (Objects.nonNull(variantFildetaljer)) {
			throw new VariantFormatAlreadyExistsException(format("Det finnes allerede en variant: %s for dokumentInfoId: %s", variantFormatCode
					.name(), dokumentInfo.getDokumentInfoId()));
		}
	}

	private byte[] base64ToByte(String dokumentFilBase64) {
		return Base64.decodeBase64(dokumentFilBase64);
	}

	private FilDetaljer lagreVariantFormat(DokumentInfo dokumentInfo, VariantFormatCode variantFormatCode, byte[] fil, String filnavn, FilTypeCode filTypeCode) {
		FilDetaljer filDetaljer = FilDetaljer.builder()
				.filUuid(FilDetaljer.generateUuid())
				.filnavn(filnavn)
				.filtype(filTypeCode)
				.variantFormat(variantFormatCode)
				.fileContent(fil)
				.dokumentInfo(dokumentInfo)
				.build();
		filDetaljer.setOpprettetKildeNavn(MDC.get(MDCConstants.MDC_CONSUMER_ID));
		dokumentInfo.addFilDetaljer(filDetaljer);

		dokumentFilRepository.save(filDetaljer.createDokumentFil());
		dokumentInfoRepository.persist(dokumentInfo);
		return filDetaljer;
	}
}
