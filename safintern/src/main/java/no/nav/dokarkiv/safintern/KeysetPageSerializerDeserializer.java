package no.nav.dokarkiv.safintern;

import com.blazebit.persistence.DefaultKeysetPage;
import com.blazebit.persistence.KeysetPage;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class KeysetPageSerializerDeserializer<T extends Serializable> {

	private KeysetPageSerializerDeserializer() {}

	protected abstract T parseFirst(String s);

	public int parsePreviousPageNo(String nextPage) {
		if (nextPage == null || nextPage.isEmpty()) {
			return 0;
		}
		var s = new String(Base64.getDecoder().decode(nextPage));
		return Integer.parseInt(s.split(":")[0]);
	}

	public KeysetPage deserializeKeysetPage(String nextPage) {
		if (nextPage == null || nextPage.isEmpty()) {
			return null;
		}
		var s = new String(Base64.getDecoder().decode(nextPage)).split(":");
		String lowest = s[1];
		T lowestFirst = parseFirst(lowest);

		String highest = s[2];
		T highestFirst = parseFirst(highest);

		return new DefaultKeysetPage(0, 1,
				new Serializable[]{lowestFirst}, new Serializable[]{highestFirst}, null);
	}

	public String serializeKeysetPage(KeysetPage keysetPage, int totalPages, int currentPage) {
		if (keysetPage == null) {
			return "";
		}
		if (totalPages == currentPage) {
			return "";
		}
		return Base64.getEncoder().encodeToString((
						currentPage + ":" +
								Stream.of(keysetPage.getLowest().getTuple()).map(Objects::toString).collect(Collectors.joining(";"))
								+ ":" +
								Stream.of(keysetPage.getHighest().getTuple()).map(Objects::toString).collect(Collectors.joining(";"))
				).getBytes(StandardCharsets.UTF_8)
		);
	}

	public static class JournalpostIdKeysetPageSerializerDeserializer extends KeysetPageSerializerDeserializer<Long> {
		@Override
		protected Long parseFirst(String s) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				throw new UgyldigKeysetPagePaginationException("Ugyldig peker for jounalpostId i keyset page!");
			}
		}
	}
}
