package no.nav.dokarkiv.core.storage;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("nais")
public class S3StorageConfiguration {

	@Value("${dokarkiv_s3_creds_username}")
	private String accessKey;

	@Value("${dokarkiv_s3_creds_password}")
	private String secretKey;

	@Value("${storage_s3_url}")
	private String s3Endpoint;

	@Value("${dokprodmellomlager_s3_storage_crypto_password}")
	private String encryptionPassphrase;

	private final static String REGION_TO_USE_FOR_S3_TO_WORK_ONPREM = "us-east-1";

	@Bean
	@Lazy
	public Storage storage() {
		AmazonS3 s3 = initS3Client();
		ensureBucketExists(s3);
		return new DokprodMellomlagerS3Storage(s3, encryptionPassphrase);
	}

	private AmazonS3 initS3Client() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		return AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Endpoint, REGION_TO_USE_FOR_S3_TO_WORK_ONPREM))
				.enablePathStyleAccess()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}

	private void ensureBucketExists(AmazonS3 s3) {
		boolean bucketExists = s3.listBuckets().stream()
				.anyMatch(b -> b.getName().equals(DokprodMellomlagerS3Storage.DOKPRODMELLOMLAGER_BUCKET));
		if (!bucketExists) {
			throw new DokarkivTechnicalException("Fant ikke " + DokprodMellomlagerS3Storage.DOKPRODMELLOMLAGER_BUCKET + " i s3");
		}
	}
}
