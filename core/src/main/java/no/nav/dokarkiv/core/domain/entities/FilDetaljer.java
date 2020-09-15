package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.nav.dokarkiv.core.domain.AbstractPersistentVersionedDomainObjectWithKilde;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.OnDemandInstansCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain object that represents fildetaljer
 * 
 * @author Per Kristian Foss, Visma Sirius
 */
@Entity
@Table(name = "T_FIL_DETALJER")
@Builder
@AllArgsConstructor
public class FilDetaljer extends AbstractPersistentVersionedDomainObjectWithKilde {

	/** ID used for serialization. */
	private static final long serialVersionUID = -2125839946340061652L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fil_detaljer_seq")
	@GenericGenerator(name = "fil_detaljer_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", 
					  parameters = { @Parameter(name = "sequence_name", value = "T_FIL_DETALJER_SEQ") })
	@Column(name = "fil_detaljer_id", nullable = false)
	private Long fildetaljerId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "k_fil_t", nullable = false)
	private FilTypeCode filtype;
	
	@Column(name = "fil_uuid", nullable = false, length = 36)
	private String filUuid;

	@Column(name = "on_demand_id_fk")
	private String onDemandId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "k_ondemand_inst")
	private OnDemandInstansCode onDemandInstans;
	
	@Column(name = "fil_navn")
	private String filnavn;
	
	@Column(name = "batch_navn")
	private String batchNavn;
	
	@Column(name = "fil_storrelse")
	private String filstorrelse;
	
	@Column(name = "metaforce_instance_id")
	private Long metaforceInstanceId;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "k_variant_format", nullable = false)
	private VariantFormatCode variantFormat;

	@Column(name = "k_skjerming_type")
	@Enumerated(EnumType.STRING)
	private SkjermingTypeCode skjermingType;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dokument_info_id", nullable = false)
	private DokumentInfo dokumentInfo;
	
	/**
	 * File content is stored in this field when mapped from Web Service input.
	 * However it is not persisted here, but in the separate DokumentFil entity.
	 */
	@Transient
	private byte[] fileContent;
	
	/**
	 * Default constructor.
	 */
	public FilDetaljer() {
		this.filUuid = generateUuid();
	}

	/**
	 * Constructor that assigns immutable properties. Used for testing.
	 * 
	 * @param filDetaljerId
	 *            DB-id for the instance.
	 * @param version
	 *            DB-version for the instance.
	 */
	public FilDetaljer(Long filDetaljerId, long version) {
		this.fildetaljerId = filDetaljerId;
		setVersion(version);
		this.filUuid = generateUuid();
	}
	
	/** {@inheritDoc} */
	@Override
	public Long getId() {
		return getFildetaljerId();
	}
	
	/**
	 * Verify that all mandatory fields are set.
	 */
	public void verifyMandatoryFields() {
		verifyFieldNotNull(filtype, "filtype");
		verifyFieldNotNull(variantFormat, "variantFormat");
	}

	/**
	 * Create a DokumentFil based on the {@link #fileContent} and
	 * {@link #filUuid} fields.
	 * 
	 * @return The DokumentFil.
	 */
	public DokumentFil createDokumentFil() {
		DokumentFil dokumentFil = new DokumentFil(this.getFilUuid(), this.getFileContent());
		dokumentFil.setOpprettetKildeNavn(getOpprettetKildeNavnForDokumentFil());
		if (isBlank(this.getFilstorrelse())) {
			this.setFilstorrelse(String.valueOf(this.getFileContent().length));
		}
		return dokumentFil;
	}
	
	private String getOpprettetKildeNavnForDokumentFil() {
		if (this.hasId()) {
			return getEndretKildeNavn();
		} else {
			return getOpprettetKildeNavn();
		}
	}
	
	/**
	 * Check if the file is a pdf or a pdf/a
	 * 
	 * @return true if it is a pdf or pdf/a
	 * 
	 */
	public boolean isAPdf() {
		return FilTypeCode.PDF.equals(filtype) || FilTypeCode.PDFA.equals(filtype);
	}

	/**
	 * Checks if {@link #fileContent} is set.
	 * 
	 * @return True if there is fileContent, false otherwise.
	 */
	public boolean hasFileContent() {
		return !ArrayUtils.isEmpty(fileContent);
	}
	
	/**
	 * Utility method that incapsulates the creation of UUIDs for
	 * filereferences.
	 * 
	 * @return A UUID
	 */
	public static String generateUuid() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Getter for the filtype property.
	 *
	 * @return the filtype
	 */
	public FilTypeCode getFiltype() {
		return filtype;
	}

	/**
	 * Setter for the filtype property.
	 *
	 * @param filtype the filtype to set
	 */
	public void setFiltype(FilTypeCode filtype) {
		this.filtype = filtype;
	}

	/**
	 * Getter for the fildetaljerId property.
	 *
	 * @return the fildetaljerId
	 */
	public Long getFildetaljerId() {
		return fildetaljerId;
	}

	/**
	 * Getter for the filUuid property.
	 *
	 * @return the filUuid
	 */
	public String getFilUuid() {
		return filUuid;
	}

	/**
	 * Getter for the onDemandId property.
	 *
	 * @return the onDemandId
	 */
	public String getOnDemandId() {
		return onDemandId;
	}

	/**
	 * Setter for the onDemandId property.
	 *
	 * @param onDemandId the onDemandId to set
	 */
	public void setOnDemandId(String onDemandId) {
		this.onDemandId = onDemandId;
	}

	/**
	 * Getter for the onDemandInstans property.
	 *
	 * @return the onDemandInstans
	 */
	public OnDemandInstansCode getOnDemandInstans() {
		return onDemandInstans;
	}

	/**
	 * Setter for the onDemandInstans property.
	 *
	 * @param onDemandInstans the onDemandInstans to set
	 */
	public void setOnDemandInstans(OnDemandInstansCode onDemandInstans) {
		this.onDemandInstans = onDemandInstans;
	}

	/**
	 * Getter for the filnavn property.
	 *
	 * @return the filnavn
	 */
	public String getFilnavn() {
		return filnavn;
	}

	/**
	 * Setter for the filnavn property.
	 *
	 * @param filnavn the filnavn to set
	 */
	public void setFilnavn(String filnavn) {
		this.filnavn = filnavn;
	}

	/**
	 * Getter for the batchNavn property.
	 *
	 * @return the batchNavn
	 */
	public String getBatchNavn() {
		return batchNavn;
	}

	/**
	 * Setter for the batchNavn property.
	 *
	 * @param batchNavn the batchNavn to set
	 */
	public void setBatchNavn(String batchNavn) {
		this.batchNavn = batchNavn;
	}

	/**
	 * Getter for the filstorrelse property.
	 *
	 * @return the filstorrelse
	 */
	public String getFilstorrelse() {
		return filstorrelse;
	}

	/**
	 * Setter for the filstorrelse property.
	 *
	 * @param filstorrelse the filstorrelse to set
	 */
	public void setFilstorrelse(String filstorrelse) {
		this.filstorrelse = filstorrelse;
	}

	/**
	 * Getter for the metaforceInstanceId property.
	 *
	 * @return the metaforceInstanceId
	 */
	public Long getMetaforceInstanceId() {
		return metaforceInstanceId;
	}

	/**
	 * Setter for the metaforceInstanceId property.
	 *
	 * @param metaforceInstanceId the metaforceInstanceId to set
	 */
	public void setMetaforceInstanceId(Long metaforceInstanceId) {
		this.metaforceInstanceId = metaforceInstanceId;
	}

	/**
	 * Getter for the variantFormat property.
	 *
	 * @return the variantFormat
	 */
	public VariantFormatCode getVariantFormat() {
		return variantFormat;
	}

	/**
	 * Setter for the variantFormat property.
	 *
	 * @param variantFormat the variantFormat to set
	 */
	public void setVariantFormat(VariantFormatCode variantFormat) {
		this.variantFormat = variantFormat;
	}

	public SkjermingTypeCode getSkjermingType() {
		return skjermingType;
	}

	public void setSkjermingType(SkjermingTypeCode skjermingType) {
		throw new UnsupportedOperationException("Skjerming skal bare settes gjennom SkjermingService");
	}

	/**
	 * Getter for the dokumentInfo property.
	 *
	 * @return the dokumentInfo
	 */
	public DokumentInfo getDokumentInfo() {
		return dokumentInfo;
	}

	/**
	 * Setter for the dokumentInfo property.
	 *
	 * @param dokumentInfo the dokumentInfo to set
	 */
	void setDokumentInfo(DokumentInfo dokumentInfo) {
		this.dokumentInfo = dokumentInfo;
	}

	/**
	 * Getter for the fileContent property.
	 *
	 * @return the fileContent
	 */
	public byte[] getFileContent() {
		if (ArrayUtils.isEmpty(fileContent)) {
			return null;
		}
		return Arrays.copyOf(fileContent, fileContent.length);
	}

	/**
	 * Setter for the fileContent property.
	 *
	 * @param fileContent the fileContent to set
	 */
	public void setFileContent(byte[] fileContent) {
		if (ArrayUtils.isEmpty(fileContent)) {
			this.fileContent = null;
		} else {
			this.fileContent = Arrays.copyOf(fileContent, fileContent.length);
		}
	}

	public boolean isSkjermet() {
		return Objects.nonNull(skjermingType);
	}

	public boolean isArkivVariant() {
		return ARKIV.equals(variantFormat);
	}

	public boolean isSladdetVariant() {
		return SLADDET.equals(variantFormat);
	}

}
