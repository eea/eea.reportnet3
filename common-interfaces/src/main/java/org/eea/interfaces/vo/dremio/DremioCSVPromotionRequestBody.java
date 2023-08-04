package org.eea.interfaces.vo.dremio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DremioCSVPromotionRequestBody extends DremioPromotionRequestBody{

    private Format format;

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Format {
        @JsonProperty("type")
        private String type;

        @JsonProperty("extractHeader")
        private Boolean extractHeader;
    }

    public DremioCSVPromotionRequestBody(String entityType, String id, String[] path, String type, Format format){
        super(entityType, id, path, type);
        this.format = format;
    }
}
