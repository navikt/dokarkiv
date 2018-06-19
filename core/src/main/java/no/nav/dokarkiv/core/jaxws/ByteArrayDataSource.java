package no.nav.dokarkiv.core.jaxws;

import javax.activation.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copied from JVM
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class ByteArrayDataSource implements DataSource {
	private final String contentType;
	private final byte[] buf;
	private final int start;
	private final int len;

	public ByteArrayDataSource(byte[] buf, String contentType) {
		this(buf, 0, buf.length, contentType);
	}

	private ByteArrayDataSource(byte[] buf, int start, int length, String contentType) {
		this.buf = buf;
		this.start = start;
		this.len = length;
		this.contentType = contentType;
	}

	public String getContentType() {
		return this.contentType == null ? "application/octet-stream" : this.contentType;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.buf, this.start, this.len);
	}

	public String getName() {
		return null;
	}

	public OutputStream getOutputStream() {
		throw new UnsupportedOperationException();
	}
}
