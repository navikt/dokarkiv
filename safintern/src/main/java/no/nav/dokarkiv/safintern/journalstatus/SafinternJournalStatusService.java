package no.nav.dokarkiv.safintern.journalstatus;

import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.KeysetPage;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.InvalidFieldRequestedException;
import no.nav.dokarkiv.safintern.KeysetPageSerializerDeserializer;
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
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;

@Slf4j
@Component
@Transactional(readOnly = true)
public class SafinternJournalStatusService {
	private static final EnumSet<JournalStatusCode> GYLDIGE_JOURNALSTATUSER = EnumSet.of(JournalStatusCode.U, JournalStatusCode.UB);
	public static final int MAX_PAGE_SIZE = 1000;
	public static final ZoneId NORGE_ZONE = ZoneId.of("Europe/Oslo");

	private final SafinternJournalStatusRepository repository;
	private final KeysetPageSerializerDeserializer<Long> keysetPageSerializerDeserializer;

	public SafinternJournalStatusService(SafinternJournalStatusRepository repository) {
		this.repository = repository;
		this.keysetPageSerializerDeserializer = new KeysetPageSerializerDeserializer.JournalpostIdKeysetPageSerializerDeserializer();
	}

	public PaginatedJournalpostView finnJournalposterStatus(FinnJournalposterStatusRequest finnJournalposterStatusRequest, Set<String> fields) {
		JournalStatusCode journalstatus = finnJournalposterStatusRequest.journalstatus();
		validateJournalstatus(journalstatus);
		Integer antallRader = finnJournalposterStatusRequest.antallRader();
		int rader = validateAndParseAntallRader(antallRader);
		KeysetPage keysetPage = keysetPageSerializerDeserializer.deserializeKeysetPage(finnJournalposterStatusRequest.etterPeker());
		int currentPage = keysetPageSerializerDeserializer.parsePreviousPageNo(finnJournalposterStatusRequest.etterPeker()) + 1;
		try {
			PagedList<JournalpostView> journalpostViews = repository.finnJournalposterStatus(
					journalstatus,
					finnJournalposterStatusRequest.journalposttyper(),
					validateAndParseDate(finnJournalposterStatusRequest),
					fetchDokument(fields, rader, keysetPage));
			return new PaginatedJournalpostView(journalpostViews, journalpostViews.getSize(), journalpostViews.getTotalSize(),
					currentPage, journalpostViews.getTotalPages(),
					keysetPageSerializerDeserializer.serializeKeysetPage(journalpostViews.getKeysetPage(), journalpostViews.getTotalPages(), currentPage));
		} catch (EmptyResultDataAccessException | NoResultException e) {
			throw new DokumentInfoIkkeFunnetException("Fant ingen Journalposter med status=" + journalstatus);
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

	private static Instant validateAndParseDate(FinnJournalposterStatusRequest finnJournalposterStatusRequest) {
		if (finnJournalposterStatusRequest.fraDato() != null) {
			return LocalDate.parse(finnJournalposterStatusRequest.fraDato())
					.atStartOfDay()
					.atZone(NORGE_ZONE)
					.toInstant();
		}
		throw new UgyldigJournalpostQueryStartDatoException();
	}

	private static void validateJournalstatus(JournalStatusCode journalstatus) {
		if (journalstatus == null || isUgyldigJournalstatus(journalstatus)) {
			throw new UgyldigJournalstatusException();
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
				String feilmelding = "safintern/finnjournalposterstatus forsøker fetch på ugyldig path=" + path;
				log.error(feilmelding);
				throw new InvalidFieldRequestedException(feilmelding);
			}
		}
		return evs;
	}

	public static boolean isUgyldigJournalstatus(JournalStatusCode journalstatus) {
		return !GYLDIGE_JOURNALSTATUSER.contains(journalstatus);
	}
}
