package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public enum AvsenderMottakerIdType {
	FNR,
	ORGNR,
	HPRNR,
	UTL_ORG
}
