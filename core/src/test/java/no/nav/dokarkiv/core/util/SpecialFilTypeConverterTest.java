package no.nav.dokarkiv.core.util;

import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.specialFiltypeJPG;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.specialFiltypeTIF;
import static no.nav.dokarkiv.core.util.SpecialFilTypeConverter.convertFilType;

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
		String mappedFiltype = convertFilType(FilTypeCode.PDF.name());
		assertThat(mappedFiltype, is(FilTypeCode.PDF.name()));
	}

	@Test
	public void shouldMapTIFtoTIFF(){
		String mappedFiltype = convertFilType(specialFiltypeTIF);
		assertThat(mappedFiltype, is(FilTypeCode.TIFF.name()));
	}

	@Test
	public void shouldMapJPGtoJPEG(){
		String mappedFiltype = convertFilType(specialFiltypeJPG);
		assertThat(mappedFiltype, is(FilTypeCode.JPEG.name()));
	}

	@Test
	public void shouldMapEmptyFiltype(){
		String mappedFiltype = convertFilType(null);
		assertThat(mappedFiltype, is(nullValue()));
	}

}
