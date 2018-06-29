package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DefaultKnyttDokumentTilJournalpostSomVedleggValidatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private DefaultKnyttDokumentTilJournalpostSomVedleggValidator validator;

	private KnyttDokumentTilJournalpostSomVedleggRequestTo request;

	@Before
	public void setUpHappyPath() {
		validator = new DefaultKnyttDokumentTilJournalpostSomVedleggValidator();

		request = new KnyttDokumentTilJournalpostSomVedleggRequestTo();
		request.setKnyttesFraJournalpostId(1337L);
		request.setKnyttesTilJournalpostId(1338L);
		request.setDokumentInfoId(1339L);
		request.setEndretAvNavn("Ola Nordmann");
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenRequestIsNull() {
		request = null;

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing request object");

		validator.validate(request);
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenKnyttesFraJournalpostIdIsZero() {
		request.setKnyttesFraJournalpostId(0L);

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing parameter in request: knyttesFraJournalpostId");

		validator.validate(request);
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenKnyttesTilJournalpostIdIsZero() {
		request.setKnyttesTilJournalpostId(0L);

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing parameter in request: knyttesTilJournalpostId");

		validator.validate(request);
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenDokumentInfoIdIsZero() {
		request.setDokumentInfoId(0L);

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing parameter in request: dokumentInfoId");

		validator.validate(request);
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenEndretAvNavnIsNull() {
		request.setEndretAvNavn(null);

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing parameter in request: endretAvNavn");

		validator.validate(request);
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenEndretAvNavnIsEmpty() {
		request.setEndretAvNavn("");

		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Missing parameter in request: endretAvNavn");

		validator.validate(request);
	}

	@Test
	public void doesNothingWhenRequestIsValid() {
		validator.validate(request);
	}
}
