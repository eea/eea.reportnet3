package org.eea.interfaces.vo.dremio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DremioPromotionRequestBody {

    @JsonProperty("entityType")
    private String entityType;

    @JsonProperty("id")
    private String id;

    @JsonProperty("path")
    private String[] path;

    @JsonProperty("type")
    private String type;

    public DremioPromotionRequestBody(String entityType, String id, String[] path, String type) {
        this.entityType = entityType;
        this.id = id;
        this.path = path;
        this.type = type;
    }
}
