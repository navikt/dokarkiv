package no.nav.dokarkiv.journalpost.v1.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.function.Function;

public class FunctionalMatcher {

	public static <T, V> BaseMatcher<T> where(Function<T, V> function, Matcher<V> matcher) {
		return new BaseMatcher<T>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("a lambda returning ").appendDescriptionOf(matcher);
			}

			@Override
			public void describeMismatch(Object item, Description description) {
				description.appendText("was a lambda returning ").appendValue(function.apply((T) item));
			}

			@Override
			public boolean matches(Object thing) {
				try {
					if (thing != null) {
						return matcher.matches(function.apply((T) thing));
					}
				} catch (ClassCastException e) {
				}
				return false;
			}
		};
	}
}
