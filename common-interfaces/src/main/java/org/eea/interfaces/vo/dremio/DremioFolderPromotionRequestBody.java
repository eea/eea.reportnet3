package org.eea.interfaces.vo.dremio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class DremioFolderPromotionRequestBody {

    @JsonProperty("entityType")
    private String entityType;

    @JsonProperty("id")
    private String id;

    @JsonProperty("path")
    private String[] path;

    @JsonProperty("type")
    private String type;

    @JsonProperty("format")
    private Format format;

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Format {
        @JsonProperty("type")
        private String type;
    }

}
