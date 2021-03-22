package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.experimental.UtilityClass;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static java.util.Objects.isNull;

@UtilityClass
class HentTilgangJournalpostDtoMapper {

	static TilgangJournalpostDto mapTupleTilgangJournalPost(Object[] tuple) {
		return new TilgangJournalpostDto(
				isNull(tuple[0]) ? null : ((Long) tuple[0]).toString(),
				(JournalStatusCode) tuple[1],
				(JournalpostTypeCode) tuple[2],
				(FagomradeCode) tuple[3],
				isNull(tuple[4]) ? null : ((Timestamp) tuple[4]).toLocalDateTime(),
				(MottaksKanalCode) tuple[5],
				(SkjermingTypeCode) tuple[6],
				(String) tuple[7],
				new TilgangBrukerDto((String) tuple[8],
						(BrukerTypeCode) tuple[9]),
				new TilgangSakDto((String) tuple[10],
						(FagsystemCode) tuple[11],
						(String) tuple[12],
						(String) tuple[13],
						(String) tuple[14],
						(String) tuple[15],
						(String) tuple[16],
						(String) tuple[17],
						isNull(tuple[18]) ? null : (LocalDateTime) tuple[18]),
				new TilgangDokumentInfoDto(isNull(tuple[19]) ? null : ((Long) tuple[19]).toString(),
						isNull(tuple[20]) ? null : (DokumentStatusCode) tuple[20],
						(String) tuple[21],
						(DokumentKategoriCode) tuple[22],
						isNull(tuple[23]) ? null : (boolean) tuple[23],
						isNull(tuple[24]) ? null : (boolean) tuple[24],
						isNull(tuple[25]) ? null : (boolean) tuple[25],
						(SkjermingTypeCode) tuple[26],
						new TilgangVariantDto((VariantFormatCode) tuple[27],
								(SkjermingTypeCode) tuple[28])
				));
	}
}
