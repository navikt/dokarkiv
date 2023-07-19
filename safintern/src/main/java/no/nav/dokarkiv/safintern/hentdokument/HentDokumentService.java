package no.nav.dokarkiv.safintern.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

@Slf4j
@Component
@Transactional(readOnly = true)
class HentDokumentService {
	private final HentDokumentRepository hentDokumentRepository;

	public HentDokumentService(HentDokumentRepository hentDokumentRepository) {
		this.hentDokumentRepository = hentDokumentRepository;
	}

	public HentDokumentResponse hentDokumentBy(Long dokumentInfoId, VariantFormatCode variant) {
		Optional<HentDokumentDto> hentDokumentDto = hentDokumentRepository.hentDokumentFromJoark(dokumentInfoId, variant);
		if (hentDokumentDto.isPresent()) {
			HentDokumentDto dto = hentDokumentDto.get();
			if (dto.harDokument()) {
				return mapHentDokument(dto);
			} else {
				String message = "safintern/hentDokument finner metadata og ikke dokument med dokumentInfoId=" + dokumentInfoId + ", variant=" + variant;
				log.warn(message);
				throw new DocumentNotFoundException(message);
			}
		} else {
			String message = "safintern/hentDokument finner ikke dokument med dokumentInfoId=" + dokumentInfoId + ", variant=" + variant;
			log.info(message);
			throw new DocumentNotFoundException(message);
		}
	}

	private HentDokumentResponse mapHentDokument(HentDokumentDto hentDokumentDto) {
		Blob dokument = hentDokumentDto.dokument();
		try {
			InputStream binaryStream = dokument.getBinaryStream();
			HentDokumentResponse hentDokumentResponse = new HentDokumentResponse(hentDokumentDto.filtype(), binaryStream);
			dokument.free();
			return hentDokumentResponse;
		} catch (SQLException e) {
			throw new DokarkivTechnicalException("Klarte ikke lese fra binaryStream", e);
		}
	}
}