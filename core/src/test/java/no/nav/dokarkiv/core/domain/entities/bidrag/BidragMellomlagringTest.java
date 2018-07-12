package no.nav.dokarkiv.core.domain.entities.bidrag;

import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringBuilder.getBidragMellomlagringBuilder;
import static no.nav.dokarkiv.core.domain.builder.BidragMellomlagringDokumentBuilder.getBidragMellomlagringDokumentBuilder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.Set;

/**
 * Unit tests for BidragMellomlagring.
 * 
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class BidragMellomlagringTest {

	@Test
	public void shouldReturnTrueForBidragMellomlagringId() {
		assertThat(BidragMellomlagring.isBidragMellomLagringId(***gammelt_fnr***1L), is(true));
	}

	@Test
	public void isBidragMellomlagringIdShouldReturnFalseForIdWithoutPrefix() throws Exception {
		assertThat(BidragMellomlagring.isBidragMellomLagringId(***gammelt_fnr***1L), is(false));
	}

	@Test
	public void isBidragMellomlagringIdShouldReturnFalseForIdWithWrongNumberOfDigits() throws Exception {
		assertThat(BidragMellomlagring.isBidragMellomLagringId(***gammelt_fnr***L), is(false));
	}

	@Test
	public void shouldCreateIdWithPrefixAndPaddedZeroes() throws Exception {
		Long expectedId = ***gammelt_fnr***8L;

		BidragMellomlagring bidragMellomlagring = getBidragMellomlagringBuilder().bidragMellomlagringId(88L).build();

		assertThat(bidragMellomlagring.getIdWithPrefix(), is(expectedId));
	}

	@Test
	public void shouldRemovePrefixFromId() throws Exception {
		Long id = ***gammelt_fnr***8L;

		assertThat(BidragMellomlagring.removePrefixFromId(id), is(88L));
	}

	@Test
	public void shouldFindBidragMellomlagringDokumentsByType() {
		BidragMellomlagring bidragMellomlagring = createBidragMellomlagringWithHoveddokumentAndVedlegg();

		Set<BidragMellomlagringDokument> vedlegg = bidragMellomlagring
				.findBidragMellomlagringDokumentByType(BidragMellomlagringDokumentType.VEDLEGG);

		assertThat(vedlegg.size(), is(2));
	}

	private BidragMellomlagring createBidragMellomlagringWithHoveddokumentAndVedlegg() {
		return getBidragMellomlagringBuilder().bidragMellomlagringDokuments(
				getBidragMellomlagringDokumentBuilder().dokumentType(BidragMellomlagringDokumentType.HOVEDDOKUMENT)
						.dokument("Hoveddokument".getBytes()).build(),
				getBidragMellomlagringDokumentBuilder().dokumentType(BidragMellomlagringDokumentType.VEDLEGG)
						.dokument("Vedlegg0".getBytes()).build(),
				getBidragMellomlagringDokumentBuilder().dokumentType(BidragMellomlagringDokumentType.VEDLEGG)
						.dokument("Vedlegg1".getBytes()).build()).build();
	}

}
