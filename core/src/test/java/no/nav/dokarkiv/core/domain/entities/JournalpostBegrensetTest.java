package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for Journalpost.
 *
 * @author Per Kristian Foss, Visma Sirius
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalpostBegrensetTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private Journalpost journalpost;


    private Journalpost createJournalpostWithTwoDokumentInfoRelasjoner(JournalStatusCode journalstatus,
                                                                       TilknyttetJournalpostSomCode tilknyttet1, TilknyttetJournalpostSomCode tilknyttet2) {
        return getJournalpostBuilder()
                .journalStatus(journalstatus)
                .dokumentInfoRelasjoner(
                        getJournalpostDokumentInfoRelasjonBuilder().tilknyttetJournalpostSom(tilknyttet1).build(),
                        getJournalpostDokumentInfoRelasjonBuilder().tilknyttetJournalpostSom(tilknyttet2).build()).build();
    }

    private void assertExceptionThrownWithMessage(Journalpost journalpost, String... messages) {
        try {
            journalpost.verifyStructureForEndeligJournalforing();
            fail();
        } catch (InvalidJournalpostStructureException e) {
            for (String message : messages) {
                assertThat(e.getMessage(), containsString(message));
            }
        }
    }

}
