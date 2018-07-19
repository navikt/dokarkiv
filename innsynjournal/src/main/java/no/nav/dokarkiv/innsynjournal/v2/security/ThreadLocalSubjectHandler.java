package no.nav.dokarkiv.innsynjournal.v2.security;

import no.nav.modig.core.context.SubjectHandler;

import javax.security.auth.Subject;

/**
 * <p>
 * A SubjectHandler that holds the Subject in a ThreadLocal field.
 * </p>
 * <p>
 * Use this SubjectHandler in Jetty and tests where the Subject matters.
 * </p>
 */
public class ThreadLocalSubjectHandler extends SubjectHandler {

	private static ThreadLocal<Subject> subjectHolder = new ThreadLocal<>();

	@Override
	public Subject getSubject() {
		return subjectHolder.get();
	}

	public void setSubject(Subject subject) {
		subjectHolder.set(subject);
	}

	public void reset() {
		setSubject(null);
	}
}
