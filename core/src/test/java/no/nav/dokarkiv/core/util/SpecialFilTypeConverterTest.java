package no.nav.dokarkiv.core.util;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.specialFiltypeJPG;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.specialFiltypeTIF;
import static no.nav.dokarkiv.core.util.SpecialFilTypeConverter.mapFiltype;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import org.junit.Test;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test of SpecialFilTypeConverter
 *
 * @author Ketill Fenne, Visma Consulting
 */
public class SpecialFilTypeConverterTest {

	@Test
	public void shouldMapPDF(){
		String mappedFiltype = mapFiltype(FilTypeCode.PDF.name());
		assertThat(mappedFiltype, is(FilTypeCode.PDF.name()));
	}

	@Test
	public void shouldMapTIFtoTIFF(){
		String mappedFiltype = mapFiltype(specialFiltypeTIF);
		assertThat(mappedFiltype, is(FilTypeCode.TIFF.name()));
	}

	@Test
	public void shouldMapJPGtoJPEG(){
		String mappedFiltype = mapFiltype(specialFiltypeJPG);
		assertThat(mappedFiltype, is(FilTypeCode.JPEG.name()));
	}

	@Test
	public void shouldMapEmptyFiltype(){
		String mappedFiltype = mapFiltype(null);
		assertThat(mappedFiltype, is(nullValue()));
	}

}
