package no.nav.dokarkiv.hentjournalsakinfo.common;

import org.apache.commons.collections4.ListUtils;

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

	public static List<String> inPaddingBase2(List<String> values) {
		return inPadding(values, 2);
	}

	public static List<String> inPadding(List<String> values, int base) {
		int valuesSize = values.size();
		int padSize = (int) Math.round(Math.pow(base, Math.ceil(Math.log(valuesSize) / Math.log(base))));
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
