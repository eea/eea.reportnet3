package org.eea.validation.util.geojsonvalidator.jackson;

import java.io.IOException;
import org.eea.validation.util.geojsonvalidator.LngLatAlt;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LngLatAltSerializer extends JsonSerializer<LngLatAlt> {

  @Override
  public void serialize(LngLatAlt value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
    jgen.writeStartArray();
    jgen.writeNumber(value.getLongitude());
    jgen.writeNumber(value.getLatitude());
    if (value.hasAltitude()) {
      jgen.writeNumber(value.getAltitude());

      for (double d : value.getAdditionalElements()) {
        jgen.writeNumber(d);
      }
    }
    jgen.writeEndArray();
  }
}
