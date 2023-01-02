package no.nav.dokarkiv.core.util;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps filetypes to corresponding MIME type.
 * 
 * 
 * @author Hans Olav Loftum, BEKK
 */
public class MimeTypeMapper {

	private static final String TEXT_XML = "text/xml";
	private static final String APPLICATION_PDF = "application/pdf";
	private final Map<String, String> mimeTypes = new HashMap<>();
	
	private static final String MIMETYPE_BINARY = "application/binary";
	
	/**
	 * Should have mapping for all FileTypeCodes
	 */
	public MimeTypeMapper() {
		mimeTypes.put("pdf", APPLICATION_PDF);
		mimeTypes.put("pdfa", APPLICATION_PDF);
		mimeTypes.put("xml", TEXT_XML);
		mimeTypes.put("dxml", TEXT_XML);
		mimeTypes.put("axml", TEXT_XML);
		mimeTypes.put("rtf", "application/rtf");
		mimeTypes.put("afp", "application/afp");
		mimeTypes.put("meta", TEXT_XML);
		mimeTypes.put("dlf", "application/dlf");
		mimeTypes.put("jpeg", "image/jpeg");
		mimeTypes.put("tiff", "image/tiff");
		mimeTypes.put("doc", "application/msword");
		mimeTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		mimeTypes.put("xls", "application/vnd.ms-excel");
		mimeTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		mimeTypes.put("json", "application/json");
	}
	
	/**
	 * Maps file extension to corresponding MIME type.
	 * @param extension The file extension
	 * @return the corresponding MIME type.
	 */
	public String getMimeTypeForFileExtension(String extension) {
		String mimeType = mimeTypes.get(extension.toLowerCase());
		if (StringUtils.isBlank(mimeType)) {
			return MIMETYPE_BINARY;
		}
		return mimeType;
	}

}
