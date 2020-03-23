package no.nav.dokarkiv.hentjournalsakinfo;

import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class JournalpostFilterTest {
    private static final String ORGNUMMMER = "999999999";
    private static final String FOEDSELSNUMMER = "***gammelt_fnr***";

    @Test
    public void shouldGetAlleIdenterForPersonWhenFoedselsnummerSupplied() {
        final FinnJournalposterRequestTo finnJournalposterRequestTo = createRequest();
        finnJournalposterRequestTo.setAlleIdenter(Collections.singletonList(FOEDSELSNUMMER));
        JournalpostFilter journalpostFilter = new JournalpostFilter(finnJournalposterRequestTo);
        assertThat(journalpostFilter.getAlleIdenter().get(0), is(FOEDSELSNUMMER));
    }

    @Test
    public void shouldRightPadOrganisasjonIdentWithSpacesWhenOrganisasjonsnummerSupplied() {
        final FinnJournalposterRequestTo finnJournalposterRequestTo = createRequest();
        finnJournalposterRequestTo.setAlleIdenter(Collections.singletonList(ORGNUMMMER));
        JournalpostFilter journalpostFilter = new JournalpostFilter(finnJournalposterRequestTo);
        assertThat(journalpostFilter.getAlleIdenter().get(0), is(ORGNUMMMER + "  "));
    }

    @Test
    public void shouldGetEmptyAlleIdenterWhenNullContents() {
        final FinnJournalposterRequestTo finnJournalposterRequestTo = createRequest();
        finnJournalposterRequestTo.setAlleIdenter(Collections.singletonList(null));
        JournalpostFilter journalpostFilter = new JournalpostFilter(finnJournalposterRequestTo);
        assertThat(journalpostFilter.getAlleIdenter(), hasSize(0));
    }

    private FinnJournalposterRequestTo createRequest() {
        final FinnJournalposterRequestTo finnJournalposterRequestTo = new FinnJournalposterRequestTo();
        finnJournalposterRequestTo.setFraDato(LocalDate.now().toString());
        finnJournalposterRequestTo.setInkluderJournalpostType(Collections.emptyList());
        finnJournalposterRequestTo.setInkluderJournalStatus(Collections.emptyList());
        finnJournalposterRequestTo.setFoerste(1);
        return finnJournalposterRequestTo;
    }
}