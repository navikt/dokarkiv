package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.service.SkjermingService;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokumentFilSkjermetRepository {
	public static final String FIL_UUID_DUMMY_DOKUMENT_KASSERT = "DUMMY_DOKUMENT_KASSERT";

	private final DokumentFilRepository dokumentFilRepository;
	private final SkjermingService skjermingService;

	public DokumentFilSkjermetRepository(DokumentFilRepository dokumentFilRepository, SkjermingService skjermingService) {
		this.dokumentFilRepository = dokumentFilRepository;
		this.skjermingService = skjermingService;
	}

	public DokumentFil findByFilUuid(String filUuid){
		String maybeDummyfilUuid = filUuid;

		/*Skal returnere DUMMY dokument hvis dokumentinfo.kassert er true. Sjekk @KasserDokumentService **/
		if (isKassertOrArkivVariantIsSkjermet(filUuid)) {
			maybeDummyfilUuid = FIL_UUID_DUMMY_DOKUMENT_KASSERT;
		}

		return dokumentFilRepository.findByFilUuid(maybeDummyfilUuid);
	}

	private boolean isKassertOrArkivVariantIsSkjermet(String filUuid) {
		return skjermingService.isKassertByFilUuidAndVariantFormat(filUuid, VariantFormatCode.ARKIV);
	}
}
