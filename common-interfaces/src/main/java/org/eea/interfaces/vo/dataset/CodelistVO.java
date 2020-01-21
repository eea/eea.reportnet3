package org.eea.interfaces.vo.dataset;

import java.util.List;
import java.util.Objects;
import org.eea.interfaces.vo.dataset.enums.CodelistStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class CodelistVO.
 */
@Getter
@Setter
@ToString
public class CodelistVO {

  /** The id. */
  private Long id;

  /** The name. */
  private String name;

  /** The description. */
  private String description;

  /** The category. */
  private CodelistCategoryVO category;

  /** The version. */
  private String version;

  /** The items id. */
  private List<CodelistItemVO> items;

  /** The status. */
  private CodelistStatusEnum status;

  /**
   * Equals.
   *
   * @param object the object
   * @return true, if successful
   */
  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    final CodelistVO codelist = (CodelistVO) object;
    return id.equals(codelist.id) && name.equals(codelist.name)
        && description.equals(codelist.description) && version.equals(codelist.version)
        && status.equals(codelist.status);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, version, status);
  }
}
