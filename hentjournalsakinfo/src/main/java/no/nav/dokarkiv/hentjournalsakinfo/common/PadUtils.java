package no.nav.dokarkiv.hentjournalsakinfo.common;

import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Padder lister til bruk som parametere i SQL spørringer til en base av 2
 * <p>
 * Dette for å
 * * Unngå at databasen gjør altfor mange "hard query parses".
 * * Unngå for mange kombinasjoner av statements slik at man får flest mulig hits i statement cache.
 */
public final class PadUtils {

	public static final int ORACLE_IN_SELECTION_MAX_ELEMENTS = 1000;

	public static List<String> inPaddingBase2(List<String> values) {
		return inPadding(values, 2);
	}

	public static List<String> inPadding(List<String> values, int base) {
		if (values.isEmpty()) {
			return new ArrayList<>();
		}
		int valuesSize = values.size();
		int padSize = Math.min(ORACLE_IN_SELECTION_MAX_ELEMENTS, (int) Math.round(Math.pow(base, Math.ceil(Math.log(valuesSize) / Math.log(base)))));
		return padList(values, valuesSize, padSize);
	}

	public static List<String> inPaddingFixed3(List<String> values) {
		return inPaddingFixed(values, 3);
	}

	public static List<String> inPaddingFixed(List<String> values, int padSize) {
		if (values.isEmpty()) {
			return new ArrayList<>();
		}
		int valuesSize = values.size();
		return padList(values, valuesSize, padSize);
	}

	private static List<String> padList(List<String> values, int valuesSize, int padSize) {
		int padNum = padSize - valuesSize;
		if (padNum == 0) {
			return values;
		} else {
			String lastValue = values.get(valuesSize - 1);
			// padder padNum elementer med siste element i values listen
			return ListUtils.union(values, IntStream.range(0, padNum).mapToObj(i -> lastValue).toList());
		}
	}
}
