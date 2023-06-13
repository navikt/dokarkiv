package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.experimental.UtilityClass;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
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
				isNull(tuple[5]) ? null : ((Timestamp) tuple[5]).toLocalDateTime(),
				(MottaksKanalCode) tuple[6],
				(SkjermingTypeCode) tuple[7],
				(String) tuple[8],
				new TilgangBrukerDto((String) tuple[9],
						(BrukerTypeCode) tuple[10]),
				new TilgangSakDto(tuple[11],
						(FagsystemCode) tuple[12],
						isNull(tuple[13]) ? null : (boolean) tuple[13],
						(String) tuple[14],
						(String) tuple[15],
						(String) tuple[16],
						(String) tuple[17],
						(String) tuple[18],
						(String) tuple[19],
						isNull(tuple[20]) ? null : (LocalDateTime) tuple[20]),
				new TilgangDokumentInfoDto(isNull(tuple[21]) ? null : ((Long) tuple[21]).toString(),
						isNull(tuple[22]) ? null : (DokumentStatusCode) tuple[22],
						(String) tuple[23],
						(DokumentKategoriCode) tuple[24],
						isNull(tuple[25]) ? null : (boolean) tuple[25],
						(SkjermingTypeCode) tuple[26],
						new TilgangVariantDto((VariantFormatCode) tuple[27],
								(SkjermingTypeCode) tuple[28])
				),
				(InnsynCode) tuple[29]);
	}
}
