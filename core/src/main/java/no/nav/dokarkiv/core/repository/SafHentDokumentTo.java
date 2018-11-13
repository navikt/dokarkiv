package no.nav.dokarkiv.core.repository;

import lombok.Getter;

import java.util.Base64;


@Getter
public class SafHentDokumentTo {
	private Base64 dokumentinfoId;
	private String dokumentVariant;

	public SafHentDokumentTo(Base64 dokumentinfoId, String dokumentVariant) {
		this.dokumentinfoId = dokumentinfoId;
		this.dokumentVariant = dokumentVariant;
	}
}
