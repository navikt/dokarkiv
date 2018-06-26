package no.nav.dokarkiv.arkiverdokumentmottak.arkiverdokumentmottakV1;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Tilleggsopplysning;
import org.dozer.DozerConverter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Custom converter from {@link Tilleggsopplysning} to
 * {@link no.nav.domain.dok.joark.DokumentInfo}.tilleggsOpplysninger
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 24.02.2017
 * @author Stig Strøm, Acando
 */
public class ArkiverDokumentmottakTilleggsopplysningerConverter
		extends DozerConverter<List<Tilleggsopplysning>, Map<String, String>> {

	@SuppressWarnings("unchecked")
	public ArkiverDokumentmottakTilleggsopplysningerConverter() {
		super((Class<List<Tilleggsopplysning>>) (Class<?>) List.class, (Class<Map<String, String>>) (Class<?>) Map.class);
	}

	/**
	 * Convert from Tilleggsopplysning to String Map
	 *
	 * @param source the Tilleggsopplysning source
	 * @param stringStringMap the Map destination
	 * @return a new instance of String Map
	 */
	@Override
	public Map<String, String> convertTo(List<Tilleggsopplysning> source, Map<String, String> stringStringMap) {
		if (CollectionUtils.isEmpty(source)) {
			return null;
		}

		Map<String, String> destination = new HashMap<>();
		for (Tilleggsopplysning tilleggsopplysning : source) {
			destination.put(tilleggsopplysning.getOpplysningsnoekkel(), tilleggsopplysning.getOpplysningsverdi());
		}

		return destination;
	}

	/**
	 * Convert to Tilleggsopplysning from Map, will always only take one and the
	 * first element.
	 *
	 * @param source The Map source
	 * @param tilleggsopplysning The Tilleggsopplysning destination
	 * @return a new instance of Tilleggsopplysning
	 */
	@Override
	public List<Tilleggsopplysning> convertFrom(Map<String, String> source, List<Tilleggsopplysning> tilleggsopplysning) {
		if (CollectionUtils.isEmpty(source)) {
			return new LinkedList<>();
		}
		List<Tilleggsopplysning> destination = new ArrayList<>();

		for (Map.Entry<String, String> entrySet : source.entrySet()) {
			Tilleggsopplysning tilleggsOpplysning = new Tilleggsopplysning();
			tilleggsOpplysning.setOpplysningsnoekkel(entrySet.getKey());
			tilleggsOpplysning.setOpplysningsverdi(entrySet.getValue());
			destination.add(tilleggsOpplysning);
		}

		return destination;
	}
}
