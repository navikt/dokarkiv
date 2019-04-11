package no.nav.dokarkiv.core.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

@Data
public class RestConsumerExceptionResponse extends DokarkivFunctionalException {

    @JsonProperty
    public String timestamp;
    @JsonProperty
    public String status;
    @JsonProperty
    public String error;
    @JsonProperty
    public String message;
    @JsonProperty
    public String path;
}
