package no.nav.dokarkiv.arkiverdokumentmottak.util;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Tilleggsopplysning;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ConverterUtils {

	public static <T extends Enum<T>> T stringToEnum(Class<T> clazz, String value) {
		if (value == null) {
			return null;
		}
		return Enum.valueOf(clazz, value);
	}

	public static Map<String, String> converTillegsopplysningerToMapV2(List<Tilleggsopplysning> source) {
		if (CollectionUtils.isEmpty(source)) {
			return new HashMap<>();
		}

		Map<String, String> destination = new HashMap<>();
		for (Tilleggsopplysning tilleggsopplysning : source) {
			destination.put(tilleggsopplysning.getOpplysningsnoekkel(), tilleggsopplysning.getOpplysningsverdi());
		}

		return destination;
	}

	public static Map<String, String> converTillegsopplysningerToMap(List<no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning> source) {
		if (CollectionUtils.isEmpty(source)) {
			return new HashMap<>();
		}

		Map<String, String> destination = new HashMap<>();
		for (no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning tilleggsopplysning : source) {
			destination.put(tilleggsopplysning.getOpplysningsnoekkel(), tilleggsopplysning.getOpplysningsverdi());
		}

		return destination;
	}
}
