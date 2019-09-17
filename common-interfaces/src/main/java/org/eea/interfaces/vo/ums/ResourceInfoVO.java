package org.eea.interfaces.vo.ums;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The type Group info vo.
 */
@Getter
@Setter
@ToString
public class ResourceInfoVO {

  /**
   * The id.
   */
  private String id;

  /**
   * The name.
   */
  private String name;

  /**
   * The path.
   */
  private String path;

  /**
   * The Attributes.
   */
  public Map<String, List<String>> attributes;
}
