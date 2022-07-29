package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DefaultKnyttDokumentTilJournalpostSomVedleggValidatorTest {

	private DefaultKnyttDokumentTilJournalpostSomVedleggValidator validator;

	private KnyttDokumentTilJournalpostSomVedleggRequestTo request;

	@BeforeEach
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

		assertThrows(IllegalArgumentException.class,
				() -> validator.validate(request),
				"Missing request object");
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenKnyttesFraJournalpostIdIsZero() {
		request.setKnyttesFraJournalpostId(0L);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validate(request),
				"Missing parameter in request: knyttesFraJournalpostId");
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenKnyttesTilJournalpostIdIsZero() {
		request.setKnyttesTilJournalpostId(0L);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validate(request),
				"Missing parameter in request: knyttesTilJournalpostId");
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenDokumentInfoIdIsZero() {
		request.setDokumentInfoId(0L);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validate(request),
				"Missing parameter in request: dokumentInfoId");
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenEndretAvNavnIsNull() {
		request.setEndretAvNavn(null);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validate(request),
				"Missing parameter in request: endretAvNavn");
	}

	@Test
	public void throwsIllegalArgumentExceptionWhenEndretAvNavnIsEmpty() {
		request.setEndretAvNavn("");

		assertThrows(IllegalArgumentException.class,
				() -> validator.validate(request),
				"Missing parameter in request: endretAvNavn");
	}

	@Test
	public void doesNothingWhenRequestIsValid() {
		validator.validate(request);
	}
}
