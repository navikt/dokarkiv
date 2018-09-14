package no.nav.dokarkiv.core.storage.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import no.nav.dokarkiv.core.storage.S3Storage;
import no.nav.dokarkiv.core.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Profile("nais")
public class StorageConfiguration {

	@Value("${dokarkiv_s3_creds_username}")
	private String accessKey;

	@Value("${dokarkiv_s3_creds_password}")
	private String secretKey;

	@Value("${storage_s3_url}")
	private String s3Endpoint;

	@Value("${dokprodmellomlager_s3_storage_crypto_password}")
	private String encryptionPassphrase;

	private final static String REGION_TO_USE_FOR_S3_TO_WORK_ONPREM = "us-east-1";

	public static final String BUCKET_NAME = "dokprodmellomlager";
	public static final String S3_DIRECTORY_NAME = "dokarkiv";

	@Bean
	@Lazy
	public Storage storage() {
		AmazonS3 s3 = s3();
		ensureBucketExists(s3);
		configureBucketRules(s3);
		return new S3Storage(s3, encryptionPassphrase);
	}

	private AmazonS3 s3() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		return AmazonS3ClientBuilder.standard()
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(s3Endpoint, REGION_TO_USE_FOR_S3_TO_WORK_ONPREM))
				.enablePathStyleAccess()
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}

	private void ensureBucketExists(AmazonS3 s3) {
		boolean bucketExists = s3.listBuckets().stream()
				.anyMatch(b -> b.getName().equals(BUCKET_NAME));
		if (!bucketExists) {
			createBucket(s3);
		}
	}

	private void configureBucketRules(AmazonS3 s3) {
		List<BucketLifecycleConfiguration.Rule> ruleList = new ArrayList<>();
		ruleList.add(new BucketLifecycleConfiguration.Rule()
				.withId("Object lifecycle rule")
				.withFilter(new LifecycleFilter())
				.withStatus(BucketLifecycleConfiguration.ENABLED)
				.withExpirationInDays(60));

		BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration();
		configuration.setRules(ruleList);

		s3.setBucketLifecycleConfiguration(BUCKET_NAME, configuration);

	}

	private void createBucket(AmazonS3 s3) {
		s3.createBucket(new CreateBucketRequest(BUCKET_NAME)
				.withCannedAcl(CannedAccessControlList.Private));
	}
}
