package org.eea.ums.service.keycloak.model;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * The Class GroupInfo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"id", "name", "path", "attributes"})

public class GroupInfo {

  /**
   * The id.
   */
  @JsonProperty("id")
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "GroupInfo [id=" + id + ", name=" + name + ", path=" + path + ", attributes="
        + attributes + "]";
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, List<String>> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, List<String>> attributes) {
    this.attributes = attributes;
  }

  /**
   * The name.
   */
  @JsonProperty("name")
  private String name;

  /**
   * The path.
   */
  @JsonProperty("path")
  private String path;

  /**
   * The Attributes.
   */
  @JsonProperty("attributes")
  public Map<String, List<String>> attributes;
}
