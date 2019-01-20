package no.nav.dokarkiv.hentdokument.dokumenturlinfo;

import no.nav.dokarkiv.core.dokumenturl.AbstractDocumentOperation;
import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.UrlNotValidException;
import no.nav.dokarkiv.core.repository.DokumentUrlInfoRepositorySkjermet;
import no.nav.dokarkiv.hentdokument.exceptions.DokumentUrlNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the HentDokumentUrlInfo.
 *
 * @author Magnus Skuland, Sirius IT
 * @author Eirik Bergande, Sirius IT
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Component
public class DefaultHentDokumentUrlInfo extends AbstractDocumentOperation implements HentDokumentUrlInfo {

	private final long defaultTimeToLiveMillis;
    private final DokumentUrlInfoRepositorySkjermet dokumentUrlInfoRepository;

	@Inject
	public DefaultHentDokumentUrlInfo(@Value("${hentdokument.dokumenturl.urlTimeToLiveMinutes:1}") long urlTimeToLiveMinutes,
									  DokumentUrlInfoRepositorySkjermet dokumentUrlInfoRepository) {
		this.defaultTimeToLiveMillis = minutesToMillis(urlTimeToLiveMinutes);
		this.dokumentUrlInfoRepository = dokumentUrlInfoRepository;
	}

	@Override
	public HentDokumentUrlInfoResponse hentDokumentUrlInfo(HentDokumentUrlInfoRequest hentUrlRequest) {
		validateHentUrlRequest(hentUrlRequest);
		DokumentUrlInfo dokumentUrlInfo = dokumentUrlInfoRepository.findByDoctoken(hentUrlRequest.getDocToken()).orElseThrow(() -> new DokumentUrlNotFoundException(hentUrlRequest.getDocToken()));
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
		if (dokumentUrlInfo.getTimeToLiveMinutes() == null) {
			timeToLive = defaultTimeToLiveMillis;
		} else {
			timeToLive = minutesToMillis(dokumentUrlInfo.getTimeToLiveMinutes());
		}

		long timestamp = dokumentUrlInfo.getTidspunkt().getTime();
		long now = DateProvider.getToday().getTime();

		if (timestamp + timeToLive < now) {
			throw new UrlNotValidException(dokumentUrlInfo);
		}
	}

	private long minutesToMillis(long timeToLiveMinutes) {
		return TimeUnit.MINUTES.toMillis(timeToLiveMinutes);
	}
}
