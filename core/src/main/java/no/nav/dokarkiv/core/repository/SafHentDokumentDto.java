package no.nav.dokarkiv.core.repository;

import lombok.AllArgsConstructor;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;

@Value
@AllArgsConstructor
public class SafHentDokumentDto {
	private byte[] dokument;
	private FilTypeCode dokumentVariant;
}
