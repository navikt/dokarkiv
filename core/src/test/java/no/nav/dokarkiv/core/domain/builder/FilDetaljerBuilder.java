package no.nav.dokarkiv.core.domain.builder;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Builder for Fildetaljer.
 *
 * @author Thao Thanh Nguyen, Visma Sirius
 */
@Deprecated // bruk lombok builder istedet
public class FilDetaljerBuilder extends Builder<FilDetaljer> {

	private FilDetaljerBuilder(){
	}

	public static FilDetaljerBuilder getFilDetaljerBuilder(){
		return new FilDetaljerBuilder();
	}

	private Long fildetaljerId;
	private String filUuid;
	private String onDemandId;
	private OnDemandInstansCode onDemandInstans;
	private FilTypeCode filtype;
	private VariantFormatCode variantFormat;
	private String batchNavn;
	private String filnavn;
	private String filstorrelse;
	private Long metaforceInstanceId;
	private byte[] fileContent; //transient field

	public FilDetaljerBuilder fildetaljerId(Long value){ this.fildetaljerId = value; return this; }
	public FilDetaljerBuilder filUuid(String value){ this.filUuid = value; return this; }
	public FilDetaljerBuilder onDemandId(String value){ this.onDemandId = value; return this; }
	public FilDetaljerBuilder onDemandInstans(OnDemandInstansCode value){ this.onDemandInstans = value; return this; }
	public FilDetaljerBuilder filtype(FilTypeCode value){ this.filtype = value; return this; }
	public FilDetaljerBuilder variantFormat(VariantFormatCode value){ this.variantFormat = value; return this; }
	public FilDetaljerBuilder batchNavn(String value){ this.batchNavn = value; return this; }
	public FilDetaljerBuilder filnavn(String value){ this.filnavn = value; return this; }
	public FilDetaljerBuilder filstorrelse(String value){ this.filstorrelse = value; return this; }
	public FilDetaljerBuilder metaforceInstanceId(Long value){ this.metaforceInstanceId = value; return this; }
	public FilDetaljerBuilder fileContent(byte[] value) { this.fileContent = value; return this; }

	@Override
	public FilDetaljer build() {
		FilDetaljer filDetaljer = new FilDetaljer(fildetaljerId, 1);
		setFilUuid(filDetaljer);
		filDetaljer.setOnDemandId(onDemandId);
		filDetaljer.setOnDemandInstans(onDemandInstans);
		filDetaljer.setFiltype(filtype);
		filDetaljer.setVariantFormat(variantFormat);
		filDetaljer.setBatchNavn(batchNavn);
		filDetaljer.setFilnavn(filnavn);
		filDetaljer.setFilstorrelse(filstorrelse);
		filDetaljer.setMetaforceInstanceId(metaforceInstanceId);
		filDetaljer.setFileContent(fileContent);
		return filDetaljer;
	}

	/*
	 * FilUuid is not accessible, but for testing puposes it is very useful to
	 * be able to control the value, so we set it with reflection.
	 */
	private void setFilUuid(FilDetaljer filDetaljer) {
		if (!StringUtils.isBlank(filUuid)) {
			Field filUuidField = ReflectionUtils.findField(FilDetaljer.class, "filUuid");
			ReflectionUtils.makeAccessible(filUuidField);
			ReflectionUtils.setField(filUuidField, filDetaljer, filUuid);
		}
	}

}
