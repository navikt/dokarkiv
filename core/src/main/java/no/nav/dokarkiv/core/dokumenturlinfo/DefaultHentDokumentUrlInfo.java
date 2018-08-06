package no.nav.dokarkiv.core.dokumenturlinfo;

import no.nav.dokarkiv.core.dokumenturl.AbstractDocumentOperation;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.UrlNotValidException;
import no.nav.dokarkiv.core.repository.DokumentUrlInfoRepository;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Default implementation of the HentDokumentUrlInfo.
 * 
 * @author Magnus Skuland, Sirius IT
 * @author Eirik Bergande, Sirius IT
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultHentDokumentUrlInfo extends AbstractDocumentOperation implements HentDokumentUrlInfo {

	private Long defaultUrlTimeToLiveMinutes;

	private long defaultTimeToLiveMillis;

	@Inject
	private DokumentUrlInfoRepository dokumentUrlInfoRepository;

	/** {@inheritDoc} */
	@Override
	public HentDokumentUrlInfoResponse hentDokumentUrlInfo(HentDokumentUrlInfoRequest hentUrlRequest) {
		validateHentUrlRequest(hentUrlRequest);
		DokumentUrlInfo dokumentUrlInfo = dokumentUrlInfoRepository.findByDoctoken(hentUrlRequest.getDocToken());
		validateUrlStillValid(dokumentUrlInfo);
		return new HentDokumentUrlInfoResponse(dokumentUrlInfo);
	}

	private void validateHentUrlRequest(HentDokumentUrlInfoRequest hentDokumentUrlInfoRequest) {
		if (hentDokumentUrlInfoRequest == null) {
			throw new InvalidArgumentException("Missing parameter", "hentDokumentUrlInfoRequest", hentDokumentUrlInfoRequest);
		}
		hentDokumentUrlInfoRequest.validate();
	}

	private void validateUrlStillValid(DokumentUrlInfo dokumentUrlInfo) {
		long timeToLive;
		if (dokumentUrlInfo.getTimeToLiveMinutes() != null) {
			timeToLive = minutesToMillis(dokumentUrlInfo.getTimeToLiveMinutes());
		} else {
			timeToLive = defaultTimeToLiveMillis;
		}

		long timestamp = dokumentUrlInfo.getTidspunkt().getTime();
		long now = DateProvider.getToday().getTime();

		if (timestamp + timeToLive < now) {
			throw new UrlNotValidException(dokumentUrlInfo);
		}
	}

	private long minutesToMillis(Long timeToLiveMinutes) {
		return timeToLiveMinutes * 60 * 1000;
	}

	/**
	 * Setter for the defaultUrlTimeToLiveMinutes property.
	 * 
	 * @param defaultUrlTimeToLiveMinutes
	 *            the defaultUrlTimeToLiveMinutes to set
	 */
	public void setDefaultUrlTimeToLiveMinutes(Long defaultUrlTimeToLiveMinutes) {
		this.defaultUrlTimeToLiveMinutes = defaultUrlTimeToLiveMinutes;
		defaultTimeToLiveMillis = minutesToMillis(defaultUrlTimeToLiveMinutes);
	}


	public void setDokumentUrlInfoRepository(DokumentUrlInfoRepository dokumentUrlInfoRepository) {
		this.dokumentUrlInfoRepository = dokumentUrlInfoRepository;
	}

}
