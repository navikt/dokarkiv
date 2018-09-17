package no.nav.dokarkiv.core.storage;

import static no.nav.dokarkiv.core.storage.config.StorageConfiguration.BUCKET_NAME;
import static no.nav.dokarkiv.core.storage.config.StorageConfiguration.S3_DIRECTORY_NAME;
import static no.nav.dokarkiv.core.util.RetryConstants.MAX_ATTEMPTS_SHORT;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import no.nav.dokarkiv.core.document.DoksysDokument;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.storage.crypto.Crypto;
import no.nav.dokarkiv.core.util.JsonSerializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = S3StorageTest.Config.class)
public class S3StorageTest {

	private final byte[] pdf = "PDF test document".getBytes();
	private final byte[] xml = "XML test document".getBytes();
	private static final String encryptPsw = "psw";
	private final String key = "test_key-asdsdasdsad";

	@Inject
	private AmazonS3 s3;

	@Inject
	private Storage storage;

	@Before
	public void setUp() {
		reset(s3);
	}

	@Test
	public void shouldEncryptAndPutObjectWithShortKey() {
		storage.put(S3_DIRECTORY_NAME, "12", JsonSerializer.serialize(createDokument()));

		verify(s3).putObject(BUCKET_NAME, S3_DIRECTORY_NAME + "_" + "12", new Crypto(encryptPsw, "12").encrypt(JsonSerializer.serialize(createDokument())));
	}

	/**
	 * Hvis S3 kalles så skal DoksysDokument serialiseres til Json string og krypteres før den lagres i S3 med key=bestillingsId
	 */
	@Test
	public void shouldEncryptAndPutObject() {
		storage.put(S3_DIRECTORY_NAME, key, JsonSerializer.serialize(createDokument()));

		verify(s3).putObject(BUCKET_NAME, S3_DIRECTORY_NAME + "_" + key, new Crypto(encryptPsw, key).encrypt(JsonSerializer.serialize(createDokument())));
	}


	@Test
	public void shouldRetryPutWhenFailed() {
		when(s3.putObject(any(String.class), any(String.class), any(String.class))).thenThrow(new DokarkivTechnicalException("asd"));

		try {
			storage.put(S3_DIRECTORY_NAME, key, JsonSerializer.serialize(createDokument()));
		} catch (Exception e) {
			verify(s3, times(MAX_ATTEMPTS_SHORT)).putObject(any(String.class), any(String.class), any(String.class));
		}
	}

	@Test
	public void shouldRetryGetWhenFailed() {
		when(s3.getObject(any(String.class), any(String.class))).thenThrow(new DokarkivTechnicalException("asd"));

		try {
			storage.get(S3_DIRECTORY_NAME, key);
		} catch (Exception e) {
			verify(s3, times(MAX_ATTEMPTS_SHORT)).getObject(any(String.class), any(String.class));
		}
	}


	@Test
	public void shouldGetObjectAndDecrypt() {
		when(s3.getObject(any(String.class), any(String.class))).thenReturn(createEncryptedS3Object());
		String result = storage.get(S3_DIRECTORY_NAME, key).get();

		verify(s3).getObject(BUCKET_NAME, S3_DIRECTORY_NAME + "_" + key);
		assertThat(result, equalTo(JsonSerializer.serialize(createDokument())));
	}


	private S3Object createEncryptedS3Object() {
		S3Object s3Object = new S3Object();
		s3Object.setObjectContent(new ByteArrayInputStream(new Crypto(encryptPsw, key).encrypt(JsonSerializer.serialize(createDokument()))
				.getBytes()));
		return s3Object;
	}

	private DoksysDokument createDokument() {
		return DoksysDokument.builder()
				.pdf(pdf)
				.axml(xml)
				.build();
	}

	@EnableRetry
	@Configuration
	public static class Config {

		@Bean
		static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
			PropertySourcesPlaceholderConfigurer placeholder = new PropertySourcesPlaceholderConfigurer();
			placeholder.setIgnoreUnresolvablePlaceholders(true);

			return placeholder;
		}

		@Bean
		public AmazonS3 s3() {
			return mock(AmazonS3.class);
		}

		@Bean
		public Storage storage(AmazonS3 s3) {
			return new S3Storage(s3, encryptPsw);
		}

	}

}