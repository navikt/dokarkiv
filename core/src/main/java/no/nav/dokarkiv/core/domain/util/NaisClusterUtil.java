package no.nav.dokarkiv.core.domain.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NaisClusterUtil {

	private static String clusterName;
	public static final String DEV_FSS = "dev-fss";
	public static final String PROD_FSS = "prod-fss";

	public static String getClusterName(){
		if (clusterName == null){
			log.info("Appen kjører i clustername:{} ", clusterName);
			clusterName = System.getenv("NAIS_CLUSTER_NAME");
		}
		return clusterName;
	}

	//For å gjøre itesten lettere
	public static void setClusterNameForTest(String clusterName){
		NaisClusterUtil.clusterName = clusterName;
	}

}
