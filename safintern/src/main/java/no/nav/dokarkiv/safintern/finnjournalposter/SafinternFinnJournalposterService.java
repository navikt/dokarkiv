package no.nav.dokarkiv.safintern.finnjournalposter;

import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.InvalidFieldRequestedException;
import no.nav.dokarkiv.safintern.UgyldigJournalpostQueryStartDatoException;
import no.nav.dokarkiv.safintern.UgyldigQueryPageSizeException;
import no.nav.dokarkiv.safintern.views.FetchPaths;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import no.nav.dokarkiv.safintern.views.PaginatedJournalpostView;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
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
	private static final EnumSet<JournalStatusCode> GYLDIGE_JOURNALSTATUSER = EnumSet.of(JournalStatusCode.U, JournalStatusCode.UB);
	public static final int MAX_PAGE_SIZE = 1000;
	public static final ZoneId NORGE_ZONE = ZoneId.of("Europe/Oslo");

	private final SafinternFinnJournalposterRepository repository;

	public SafinternFinnJournalposterService(SafinternFinnJournalposterRepository repository) {
		this.repository = repository;
	}

	public PaginatedJournalpostView finnJournalposter(FinnJournalposterRequest finnJournalposterRequest, Set<String> fields) {
		List<JournalStatusCode> journalstatuser = nullSafeList(finnJournalposterRequest.journalstatuser());
		List<JournalpostTypeCode> journalposttyper = nullSafeList(finnJournalposterRequest.journalposttyper());
		boolean visFeilregistrerte = finnJournalposterRequest.visFeilregistrerte() == Boolean.TRUE;
		boolean visKunFeilregistrerte = visFeilregistrerte && journalstatuser.isEmpty();

		Integer antallRader = finnJournalposterRequest.antallRader();
		int rader = validateAndParseAntallRader(antallRader);
		KeysetPage keysetPage = parseKeysetPage(finnJournalposterRequest.etterPeker());
		int currentPage = parsePreviousPageNo(finnJournalposterRequest.etterPeker()) + 1;
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
					serializeKeysetPage(journalpostViews.getKeysetPage(), currentPage));
		} catch (EmptyResultDataAccessException | NoResultException e) {
			throw new DokumentInfoIkkeFunnetException("Fant ingen Journalposter 🤷‍");
		}
	}

	private static int validateAndParseAntallRader(Integer antallRader) {
		if (antallRader == null || antallRader < 1) {
			return MAX_PAGE_SIZE;
		} else if (antallRader > MAX_PAGE_SIZE) {
			throw new UgyldigQueryPageSizeException(antallRader);
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

	private void validateJournalstatuserAndVisFeilregistrerte(boolean visKunFeilregistrerte, List<JournalStatusCode> journalstatuser) {
		if (visKunFeilregistrerte && !journalstatuser.isEmpty()) {
			throw new RuntimeException("Naughty naughty!");
		}
	}

	private int parsePreviousPageNo(String nextPage) {
		if (nextPage == null || nextPage.isEmpty()) {
			return 0;
		}
		var s = new String(Base64.getDecoder().decode(nextPage));
		return Integer.parseInt(s.split(":")[2]);
	}

	private KeysetPage parseKeysetPage(String nextPage) {
		if (nextPage == null || nextPage.isEmpty()) {
			return null;
		}
		var s = new String(Base64.getDecoder().decode(nextPage));
		Long[] lowestJPID = new Long[]{Long.parseLong(s.split(":")[0])};
		Long[] highestJPID = new Long[]{Long.parseLong(s.split(":")[1])};
		return new DefaultKeysetPage(0, 1, lowestJPID, highestJPID, null);
	}

	private String serializeKeysetPage(KeysetPage keysetPage, int currentPage) {
		if (keysetPage == null) {
			return "";
		}
		return Base64.getEncoder().encodeToString(
				(keysetPage.getLowest().getTuple()[0] + ":" + keysetPage.getHighest().getTuple()[0] + ":" + currentPage).getBytes(StandardCharsets.UTF_8)
		);
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

	public static boolean isUgyldigJournalstatus(JournalStatusCode journalstatus) {
		return !GYLDIGE_JOURNALSTATUSER.contains(journalstatus);
	}

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
		if(s.length() < 11) {
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
