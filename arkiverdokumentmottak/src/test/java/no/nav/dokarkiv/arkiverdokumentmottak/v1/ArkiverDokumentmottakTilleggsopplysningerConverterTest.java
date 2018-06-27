package no.nav.dokarkiv.arkiverdokumentmottak.v1;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ArkiverDokumentmottakTilleggsopplysningerConverterTest {

	private static final String KEY = "KEY";
	private static final String VALUE = "VALUE";
	private ArkiverDokumentmottakTilleggsopplysningerConverter converter = new ArkiverDokumentmottakTilleggsopplysningerConverter();

	@Test
	public void shouldReturnNullOnNullSourceConvertTo() {
		assertThat(converter.convertTo(null), is(nullValue()));
	}

	@Test
	public void shouldReturnNullOnNullSourceConvertFrom() {
		assertThat(converter.convertFrom(null), is(empty()));
	}

	@Test
	public void convertTo() throws Exception {
		List<Tilleggsopplysning> tilleggsopplysninger = new ArrayList<>();
		Tilleggsopplysning tilleggsopplysning = new Tilleggsopplysning();
		tilleggsopplysning.setOpplysningsnoekkel(KEY);
		tilleggsopplysning.setOpplysningsverdi(VALUE);
		tilleggsopplysninger.add(tilleggsopplysning);


		Map<String, String> map = converter.convertTo(tilleggsopplysninger);

		assertThat(map.keySet().iterator().next(), is(KEY));
		assertThat(map.get(KEY), is(VALUE));
	}

	@Test
	public void convertFrom() throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(KEY, VALUE);

		List<Tilleggsopplysning> tilleggsopplysning = converter.convertFrom(map);
		assertThat(tilleggsopplysning, hasSize(1));
		assertThat(tilleggsopplysning.get(0).getOpplysningsnoekkel(), is(KEY));
		assertThat(tilleggsopplysning.get(0).getOpplysningsverdi(), is(VALUE));
	}

}