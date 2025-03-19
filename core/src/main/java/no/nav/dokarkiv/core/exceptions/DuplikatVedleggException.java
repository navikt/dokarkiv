package no.nav.dokarkiv.core.exceptions;

import lombok.Getter;

@Getter
public class DuplikatVedleggException extends DokarkivFunctionalException {
	private final long dokumentInfoId;

	public DuplikatVedleggException(long dokumentInfoId) {
		this.dokumentInfoId = dokumentInfoId;
	}
}
