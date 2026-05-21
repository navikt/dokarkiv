package no.nav.dokarkiv.core.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstrakt klasse for sporingsinfo under oppretting og endring av entitet.
 */
@SuppressWarnings("serial")
@MappedSuperclass
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
public abstract class AbstractPersistentDomainObject implements Serializable {
	@Embedded
	private ChangeStamp changeStamp;

	@PrePersist
	void initChangeStampOnPersist() {
		if (changeStamp == null) {
			changeStamp = new ChangeStamp(resolveUserId(), LocalDateTime.now(), null, null);
		}
	}

	@PreUpdate
	void updateChangeStampOnUpdate() {
		if (changeStamp == null) {
			changeStamp = new ChangeStamp();
		}
		changeStamp.updatedBy(resolveUserId());
	}

	private static String resolveUserId() {
		if (RequestContextHolder.isRequestContextSet()) {
			String userId = RequestContextHolder.currentRequestContext().getUserId();
			if (userId != null) {
				return userId;
			}
		}
		return "DEFAULT_USER_ID";
	}
}
