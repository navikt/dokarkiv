package no.nav.dokarkiv.core.consumer.azure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AzureGroupResponse(List<AzureGroup> value) {
	public record AzureGroup(String id) {
	}
}