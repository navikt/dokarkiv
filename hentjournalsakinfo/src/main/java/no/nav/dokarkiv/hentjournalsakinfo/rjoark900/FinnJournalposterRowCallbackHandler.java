package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.hentjournalsakinfo.dto.BrukerDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.LogiskVedleggDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.TilleggsopplysningDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.UtsendingsInfoDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.VariantDto;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
public class FinnJournalposterRowCallbackHandler implements RowCallbackHandler {

	private static final Boolean OBSERVED_VALUE = true;
	private static final String ZONEID_NORGE = "Europe/Oslo";
	private final Map<Long, JournalpostDto> journalposter = new HashMap<>();
	private final MultiKeyMap<Object, Boolean> tilleggsopplysninger = new MultiKeyMap<>();
	private final MultiKeyMap<Long, Boolean> dokumenter = new MultiKeyMap<>();
	private final MultiKeyMap<Object, Boolean> varianter = new MultiKeyMap<>();
	private final MultiKeyMap<Object, Boolean> logiskeVedlegg = new MultiKeyMap<>();

	@Override
	public void processRow(ResultSet rs) throws SQLException {
		long journalpostId = rs.getLong("journalpostid");
		if (journalposter.containsKey(journalpostId)) {
			processEksisterendeJournalpostDto(rs, journalpostId);
		} else {
			JournalpostDto journalpostDto = mapJournalpostDto(journalpostId, rs);
			journalposter.put(journalpostId, journalpostDto);
		}
	}

	private void processEksisterendeJournalpostDto(ResultSet rs, long journalpostId) throws SQLException {
		JournalpostDto eksisterendeJournalpostDto = journalposter.get(journalpostId);
		addPossibleTilleggsopplysning(journalpostId, eksisterendeJournalpostDto, rs);
		addPossibleDokument(eksisterendeJournalpostDto, rs);
	}

	private JournalpostDto mapJournalpostDto(long journalpostId, ResultSet rs) throws SQLException {
		JournalpostDto journalpostDto = new JournalpostDto();
		journalpostDto.setJournalpostId(journalpostId);
		journalpostDto.setPrevJournalpostId(mapZeroToNull(rs.getLong("prevjournalpostid")));
		journalpostDto.setNextJournalpostId(mapZeroToNull(rs.getLong("nextjournalpostid")));
		journalpostDto.setTotaltAntall(rs.getLong("totaltAntall"));
		journalpostDto.setInnhold(rs.getString("innhold"));
		journalpostDto.setFagomrade(rs.getString("fagomrade"));
		journalpostDto.setBehandlingstema(rs.getString("behandlingstema"));
		journalpostDto.setBehandlingstemanavn(rs.getString("behandlingstemanavn"));
		journalpostDto.setJournalstatus(rs.getString("journalstatus"));
		journalpostDto.setAvsenderMottakerId(rs.getString("avsendermottakerid"));
		journalpostDto.setAvsenderMottakerIdType(rs.getString("avsendermottakeridtype"));
		journalpostDto.setAvsenderMottakerNavn(rs.getString("avsendermottakernavn"));
		journalpostDto.setAvsenderMottakerLand(rs.getString("avsendermottakerland"));
		journalpostDto.setJournalforendeEnhet(rs.getString("journalforendeenhet"));
		journalpostDto.setJournalfortAvNavn(rs.getString("journalfortavnavn"));
		journalpostDto.setOpprettetAvNavn(rs.getString("opprettetavnavn"));
		journalpostDto.setMottakskanal(rs.getString("mottakskanal"));
		journalpostDto.setUtsendingskanal(rs.getString("utsendingskanal"));
		journalpostDto.setJournalposttype(rs.getString("journalposttype"));
		journalpostDto.setSaksrelasjon(mapSaksrelasjonDto(rs));
		journalpostDto.setBruker(mapBrukerDto(rs));
		journalpostDto.setDatoOpprettet(rs.getTimestamp("datoopprettet"));
		journalpostDto.setMottattDato(rs.getTimestamp("mottattdato"));
		journalpostDto.setJournalDato(rs.getTimestamp("journaldato"));
		journalpostDto.setDokumentDato(rs.getTimestamp("dokumentdato"));
		journalpostDto.setAvsReturDato(rs.getTimestamp("avsreturdato"));
		journalpostDto.setSendtPrintDato(rs.getTimestamp("sendtprintdato"));
		journalpostDto.setEkspedertDato(rs.getTimestamp("ekspedertdato"));
		journalpostDto.setLestDato(rs.getTimestamp("lestdato"));
		journalpostDto.setSkjerming(rs.getString("skjerming"));
		journalpostDto.setAntallRetur(rs.getString("antallretur"));
		journalpostDto.setKanalReferanseId(rs.getString("kanalreferanseid"));
		journalpostDto.setInnsyn(rs.getString("innsyn"));
		journalpostDto.setInnsynbeskrivelse(rs.getString("innsynbeskrivelse"));
		journalpostDto.setUtsendingsInfo(mapUtsendingsInfoDto(rs));
		journalpostDto.setTilleggsopplysninger(mapInitialTilleggsopplysninger(rs));
		journalpostDto.setDokumenter(new ArrayList<>());
		addPossibleDokument(journalpostDto, rs);
		return journalpostDto;
	}

	private static BrukerDto mapBrukerDto(ResultSet rs) throws SQLException {
		String brukerId = rs.getString("bruker_brukerid");
		if (isNotBlank(brukerId)) {
			return new BrukerDto(brukerId, rs.getString("bruker_brukeridtype"));
		}
		return null;
	}

	private static SaksrelasjonDto mapSaksrelasjonDto(ResultSet rs) throws SQLException {
		String sakId = rs.getString("saksrelasjon_sakid");
		if(sakId == null) {
			return null;
		}
		return SaksrelasjonDto.builder()
				.sakId(sakId)
				.feilregistrert(rs.getBoolean("saksrelasjon_feilregistrert"))
				.fagsystem(rs.getString("saksrelasjon_fagsystem"))
				.aktoerId(rs.getString("saksrelasjon_aktoerid"))
				.tema(rs.getString("saksrelasjon_tema"))
				.fagsakNr(rs.getString("saksrelasjon_fagsaknr"))
				.applikasjon(rs.getString("saksrelasjon_applikasjon"))
				.orgnr(rs.getString("saksrelasjon_orgnr"))
				.opprettetAv(rs.getString("saksrelasjon_opprettet_av"))
				.opprettetTid(mapOpprettetTid(rs.getTimestamp("saksrelasjon_opprettet_tid")))
				.build();
	}

	private static UtsendingsInfoDto mapUtsendingsInfoDto(ResultSet rs) throws SQLException {
		UtsendingsInfoDto.FysiskPostadresse fysiskPostadresse = mapFysiskPostadresse(rs);
		UtsendingsInfoDto.DigitalPostadresse digitalPostadresse = mapDigitalPostadresse(rs);
		UtsendingsInfoDto.NavNoVarsling navNoVarsling = mapNavNoVarsling(rs);
		String epostvarsel = rs.getString("utsendingsInfo_epostVarsel");
		String smsvarsel = rs.getString("utsendingsInfo_smsVarsel");

		if (fysiskPostadresse == null && digitalPostadresse == null && epostvarsel == null && smsvarsel == null && navNoVarsling == null) {
			return null;
		}
		return new UtsendingsInfoDto(fysiskPostadresse, digitalPostadresse, navNoVarsling, epostvarsel, smsvarsel);
	}

	private static UtsendingsInfoDto.FysiskPostadresse mapFysiskPostadresse(ResultSet rs) throws SQLException {
		String landkode = rs.getString("utsendingsInfo_fysiskpostadresse_landkode");
		if (isBlank(landkode)) {
			return null;
		}
		return new UtsendingsInfoDto.FysiskPostadresse(
				rs.getString("utsendingsInfo_fysiskpostadresse_adresselinje1"),
				rs.getString("utsendingsInfo_fysiskpostadresse_adresselinje2"),
				rs.getString("utsendingsInfo_fysiskpostadresse_adresselinje3"),
				rs.getString("utsendingsInfo_fysiskpostadresse_postnummer"),
				rs.getString("utsendingsInfo_fysiskpostadresse_poststed"),
				landkode);
	}

	private static UtsendingsInfoDto.DigitalPostadresse mapDigitalPostadresse(ResultSet rs) throws SQLException {
		String digitalpostkasseadresse = rs.getString("utsendingsInfo_digitalpostadresse_digitalpostkasseadresse");
		if (isBlank(digitalpostkasseadresse)) {
			return null;
		}
		return new UtsendingsInfoDto.DigitalPostadresse(digitalpostkasseadresse, rs.getString("utsendingsInfo_digitalpostadresse_postkasseLeverandor"));
	}

	private static UtsendingsInfoDto.NavNoVarsling mapNavNoVarsling(ResultSet rs) throws SQLException {
		String varselsendttil = rs.getString("utsendingsInfo_navnovarsling_varselsendttil");
		if (isBlank(varselsendttil)) {
			return null;
		}
		return new UtsendingsInfoDto.NavNoVarsling(varselsendttil, rs.getString("utsendingsInfo_navnovarsling_varseltekst"));
	}

	private List<TilleggsopplysningDto> mapInitialTilleggsopplysninger(ResultSet rs) throws SQLException {
		String tilleggsopplysningerNokkel = rs.getString("tilleggsopplysninger_nokkel");
		if (isNotBlank(tilleggsopplysningerNokkel)) {
			List<TilleggsopplysningDto> tilleggsopplysningDtos = new ArrayList<>();
			tilleggsopplysningDtos.add(mapTilleggsOpplysning(rs));
			tilleggsopplysninger.put(rs.getLong("journalpostid"), tilleggsopplysningerNokkel, OBSERVED_VALUE);
			return tilleggsopplysningDtos;
		}
		return new ArrayList<>();
	}

	private static TilleggsopplysningDto mapTilleggsOpplysning(ResultSet rs) throws SQLException {
		return new TilleggsopplysningDto(rs.getString("tilleggsopplysninger_nokkel"), rs.getString("tilleggsopplysninger_verdi"));
	}

	private void addPossibleTilleggsopplysning(long journalpostId, JournalpostDto eksisterendeJournalpostDto, ResultSet rs) throws SQLException {
		String tilleggsopplysningerNokkel = rs.getString("tilleggsopplysninger_nokkel");
		if (!tilleggsopplysninger.containsKey(journalpostId, tilleggsopplysningerNokkel) && isNotBlank(tilleggsopplysningerNokkel)) {
			eksisterendeJournalpostDto.getTilleggsopplysninger().add(mapTilleggsOpplysning(rs));
			tilleggsopplysninger.put(journalpostId, tilleggsopplysningerNokkel, OBSERVED_VALUE);
		}
	}

	private void addPossibleDokument(JournalpostDto journalpostDto, ResultSet rs) throws SQLException {
		long dokumentInfoId = rs.getLong("dokumenter_dokumentinfoid");
		if (dokumentInfoId != 0L) {
			if (dokumenter.containsKey(journalpostDto.getJournalpostId(), dokumentInfoId)) {
				DokumentInfoDto dokumentInfoDto = journalpostDto.findDokumentInfoDto(dokumentInfoId);
				if (dokumentInfoDto == null) {
					log.error("Feil i mapping av dokumentInfo til journalpost. journalpostId={}, dokumentInfoId={}",
							journalpostDto.getJournalpostId(), dokumentInfoId);
				} else {
					addPossibleVariant(journalpostDto.getJournalpostId(), dokumentInfoDto, rs);
					addPossibleLogiskeVedlegg(journalpostDto.getJournalpostId(), dokumentInfoDto, rs);
				}
			} else {
				DokumentInfoDto dokumentInfoDto = mapDokumentInfoDto(journalpostDto.getJournalpostId(), rs);
				journalpostDto.getDokumenter().add(dokumentInfoDto);
				dokumenter.put(journalpostDto.getJournalpostId(), dokumentInfoId, OBSERVED_VALUE);
			}
		}
	}

	private DokumentInfoDto mapDokumentInfoDto(long journalpostId, ResultSet rs) throws SQLException {
		DokumentInfoDto dokumentInfoDto = new DokumentInfoDto();
		dokumentInfoDto.setDokumentInfoId(rs.getLong("dokumenter_dokumentinfoid"));
		dokumentInfoDto.setTilknyttetSom(rs.getString("dokumenter_tilknyttetsom"));
		dokumentInfoDto.setJpRelasjonId(rs.getLong("dokumenter_jprelasjonid"));
		dokumentInfoDto.setDokumentstatus(rs.getString("dokumenter_dokumentstatus"));
		dokumentInfoDto.setDatoFerdigstilt(rs.getTimestamp("dokumenter_datoferdigstilt"));
		dokumentInfoDto.setBrevkode(rs.getString("dokumenter_brevkode"));
		dokumentInfoDto.setDokumenttypeId(rs.getString("dokumenter_dokumenttypeid"));
		dokumentInfoDto.setTittel(rs.getString("dokumenter_tittel"));
		dokumentInfoDto.setSkjerming(rs.getString("dokumenter_skjerming"));
		dokumentInfoDto.setOrigJournalpostId(mapZeroToNull(rs.getLong("dokumenter_origjournalpostid")));
		dokumentInfoDto.setKassert(rs.getBoolean("dokumenter_kassert"));
		dokumentInfoDto.setKategori(rs.getString("dokumenter_kategori"));
		dokumentInfoDto.setSensitivt(rs.getBoolean("dokumenter_sensitivt"));
		dokumentInfoDto.setVarianter(new ArrayList<>());
		dokumentInfoDto.setLogiske(new ArrayList<>());
		addPossibleVariant(journalpostId, dokumentInfoDto, rs);
		addPossibleLogiskeVedlegg(journalpostId, dokumentInfoDto, rs);
		return dokumentInfoDto;
	}

	private void addPossibleVariant(long journalpostId, DokumentInfoDto dokumentInfoDto, ResultSet rs) throws SQLException {
		long dokumentInfoId = dokumentInfoDto.getDokumentInfoId();
		String variantformat = rs.getString("dokumenter_varianter_variantf");
		if (!varianter.containsKey(journalpostId, dokumentInfoId, variantformat) && variantformat != null) {
			VariantDto variantDto = mapVariantDto(variantformat, rs);
			dokumentInfoDto.getVarianter().add(variantDto);
			varianter.put(journalpostId, dokumentInfoId, variantformat, OBSERVED_VALUE);
		}
	}

	private VariantDto mapVariantDto(String variantformat, ResultSet rs) throws SQLException {
		return new VariantDto(variantformat,
				rs.getString("dokumenter_varianter_filnavn"),
				rs.getString("dokumenter_varianter_filuuid"),
				rs.getString("dokumenter_varianter_filtype"),
				rs.getString("dokumenter_varianter_filstorrelse"),
				rs.getString("dokumenter_varianter_skjerming")
		);
	}

	private void addPossibleLogiskeVedlegg(long journalpostId, DokumentInfoDto dokumentInfoDto, ResultSet rs) throws SQLException {
		long dokumentInfoId = dokumentInfoDto.getDokumentInfoId();
		String vedleggId = rs.getString("dokumenter_logiske_vedleggid");
		if (!logiskeVedlegg.containsKey(journalpostId, dokumentInfoId, vedleggId) && vedleggId != null) {
			LogiskVedleggDto logiskVedleggDto = new LogiskVedleggDto(vedleggId, rs.getString("dokumenter_logiske_tittel"));
			dokumentInfoDto.getLogiske().add(logiskVedleggDto);
			logiskeVedlegg.put(journalpostId, dokumentInfoId, vedleggId, OBSERVED_VALUE);
		}
	}

	private static ZonedDateTime mapOpprettetTid(Timestamp date) throws SQLException {
		if (date == null) {
			return null;
		}
		return ZonedDateTime.from(date.toInstant().atZone(ZoneId.of(ZONEID_NORGE)));
	}

	private static Long mapZeroToNull(Long value) {
		if (value == 0L) {
			return null;
		}
		return value;
	}

	List<JournalpostDto> getJournalpostDtos() {
		return journalposter.entrySet().stream().sorted(Map.Entry.<Long, JournalpostDto>comparingByKey().reversed())
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}
}
