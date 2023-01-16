package no.nav.dokarkiv.hentjournalsakinfo.common;

import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PadUtilsTest {

	@Test
	void shouldPadBase2() {
		List<String> padded3 = PadUtils.inPaddingBase2(List.of("a", "b", "c"));
		assertThat(padded3).containsExactly("a", "b", "c", "c");

		List<String> padded4 = PadUtils.inPaddingBase2(List.of("a", "b", "c", "d"));
		assertThat(padded4).containsExactly("a", "b", "c", "d");

		List<String> padded5 = PadUtils.inPaddingBase2(List.of("a", "b", "c", "d", "e"));
		assertThat(padded5).containsExactly("a", "b", "c", "d", "e", "e", "e", "e");
	}

	@Test
	void shouldPadUpToFixed3() {
		List<String> padded1 = PadUtils.inPaddingFixed3(List.of("a"));
		assertThat(padded1).containsExactly("a", "a", "a");

		List<String> padded2 = PadUtils.inPaddingFixed3(List.of("a", "b"));
		assertThat(padded2).containsExactly("a", "b", "b");
	}

	@Test
	@DisplayName("Feiler hvis JournalpostTypeCode er utvidet uten at inPaddingFixed3 rettes")
	void shouldWarnWhenJournalposttypeIsExpanded() {
		assertThat(JournalpostTypeCode.values()).hasSize(3);
	}
}