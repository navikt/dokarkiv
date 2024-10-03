package no.nav.dokarkiv.safintern.finnjournalposter;

import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.InvalidFieldRequestedException;
import no.nav.dokarkiv.safintern.KeysetPageSerializerDeserializer;
import no.nav.dokarkiv.safintern.UgyldigJournalpostQueryStartDatoException;
import no.nav.dokarkiv.safintern.views.FetchPaths;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import no.nav.dokarkiv.safintern.views.PaginatedJournalpostView;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.function.Predicate.not;

@Slf4j
@Component
@Transactional(readOnly = true)
public class SafinternFinnJournalposterService {
	public static final int DEFAULT_PAGE_SIZE = 200;
	public static final ZoneId NORGE_ZONE = ZoneId.of("Europe/Oslo");

	private final SafinternFinnJournalposterRepository repository;
	private final KeysetPageSerializerDeserializer<Long> keysetPageSerializerDeserializer;

	public SafinternFinnJournalposterService(SafinternFinnJournalposterRepository repository) {
		this.repository = repository;
		this.keysetPageSerializerDeserializer = new KeysetPageSerializerDeserializer.JournalpostIdKeysetPageSerializerDeserializer();
	}

	public PaginatedJournalpostView finnJournalposter(FinnJournalposterRequest finnJournalposterRequest, Set<String> fields) {
		List<JournalStatusCode> journalstatuser = nullSafeList(finnJournalposterRequest.journalstatuser());
		List<JournalpostTypeCode> journalposttyper = nullSafeList(finnJournalposterRequest.journalposttyper());
		boolean visFeilregistrerte = finnJournalposterRequest.visFeilregistrerte() == Boolean.TRUE;
		boolean visKunFeilregistrerte = visFeilregistrerte && journalstatuser.isEmpty();

		Integer antallRader = finnJournalposterRequest.antallRader();
		int rader = parseAntallRader(antallRader);
		KeysetPage keysetPage = keysetPageSerializerDeserializer.deserializeKeysetPage(finnJournalposterRequest.etterPeker());
		int currentPage = keysetPageSerializerDeserializer.parsePreviousPageNo(finnJournalposterRequest.etterPeker()) + 1;
		try {
			PagedList<JournalpostView> journalpostViews = repository.finnJournalposterStatus(
					nullSafeList(finnJournalposterRequest.psakSakIds()),
					nullSafeList(finnJournalposterRequest.gsakSakIds()),
					visFeilregistrerte,
					visKunFeilregistrerte,
					padUserIdents(finnJournalposterRequest.alleIdenter()),
					validateAndParseDate(finnJournalposterRequest.fraDato()).orElseThrow(UgyldigJournalpostQueryStartDatoException::new),
					validateAndParseDate(finnJournalposterRequest.tilDato()),
					journalstatuser, journalposttyper,
					fetchDokument(fields, rader, keysetPage));
			return new PaginatedJournalpostView(journalpostViews, journalpostViews.getSize(), journalpostViews.getTotalSize(),
					currentPage, journalpostViews.getTotalPages(),
					keysetPageSerializerDeserializer.serializeKeysetPage(journalpostViews.getKeysetPage(), journalpostViews.getTotalPages(), currentPage));
		} catch (EmptyResultDataAccessException | NoResultException e) {
			throw new DokumentInfoIkkeFunnetException("Fant ingen Journalposter 🤷‍");
		}
	}

	private static int parseAntallRader(Integer antallRader) {
		if (antallRader == null || antallRader < 1) {
			return DEFAULT_PAGE_SIZE;
		} else {
			return antallRader;
		}
	}

	private static Optional<Instant> validateAndParseDate(String dateString) {
		return Optional.ofNullable(dateString)
				.filter(not(String::isBlank))
				.map(date ->
						LocalDate.parse(date)
								.atStartOfDay()
								.atZone(NORGE_ZONE)
								.toInstant());
	}

	private static EntityViewSetting<JournalpostView, PaginatedCriteriaBuilder<JournalpostView>> fetchDokument(Set<String> fields, int maxResults, KeysetPage keysetPage) {
		if (fields == null || fields.isEmpty()) {
			return EntityViewSetting.create(JournalpostView.class, keysetPage != null ? 1 : 0, maxResults).withKeysetPage(keysetPage);
		}
		var evs = EntityViewSetting.create(JournalpostView.class, keysetPage != null ? 1 : 0, maxResults).withKeysetPage(keysetPage);

		for (String path : fields) {
			if (FetchPaths.erGyldig(path)) {
				evs.fetch(path);
			} else {
				String feilmelding = "safintern/finnjournalposter forsøker fetch på ugyldig path=" + path;
				log.error(feilmelding);
				throw new InvalidFieldRequestedException(feilmelding);
			}
		}
		return evs;
	}

	// i T_BRUKER er kolonnen BRUKER_ID av typen CHAR(11). I motsetning til en vanlig VARCHAR-kolonne blir verdiene
	// i denne kolonnen paddet til 11 tegn med space av databasen om verdien er kortere enn 11 tegn (dvs, et orgnr.).
	// Vi må padde ut alle verdiene som er kortere enn 11 tegn i queryet for at vi skal få treff på disse verdiene i
	// queryet vårt når vi gjør spørringen T_BRUKER.BRUKER_ID IN (identer fra queryparametre)
	private List<String> padUserIdents(List<String> strings) {
		if (strings == null) {
			return emptyList();
		}
		return strings.stream()
				.filter(Objects::nonNull)
				.map(SafinternFinnJournalposterService::padToEleven)
				.toList();
	}

	private static String padToEleven(String s) {
		if (s.length() < 11) {
			char[] newString = new char[11];
			Arrays.fill(newString, s.length(), 11, ' ');
			System.arraycopy(s.toCharArray(), 0, newString, 0, s.length());
			return new String(newString);
		}
		return s;
	}

	private static <T> List<T> nullSafeList(List<T> list) {
		return list != null ? list : emptyList();
	}
}
