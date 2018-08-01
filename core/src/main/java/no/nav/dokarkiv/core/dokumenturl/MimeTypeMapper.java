package no.nav.dokarkiv.core.dokumenturl;

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

	private Map<String, String> mimeTypes = new HashMap<String, String>();
	
	private static final String MIMETYPE_BINARY = "application/binary";
	
	/**
	 * Should have mapping for all FileTypeCodes
	 */
	public MimeTypeMapper() {
		mimeTypes.put("pdf", "application/pdf");
		mimeTypes.put("pdfa", "application/pdf");
		mimeTypes.put("xml", "text/xml");
		mimeTypes.put("dxml", "text/xml");
		mimeTypes.put("axml", "text/xml");
		mimeTypes.put("rtf", "application/rtf");
		mimeTypes.put("afp", "application/afp");
		mimeTypes.put("meta", "text/xml");
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
