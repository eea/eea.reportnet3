package org.eea.validation.util.model;

import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.validation.persistence.schemas.rule.Rule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * The Class QueryVO.
 */
@Getter
@Setter
@AllArgsConstructor
public class QueryVO {

  /** The new query. */
  private String newQuery;

  /** The rule. */
  private Rule rule;

  /** The entity name. */
  private String entityName;

  /** The data set metabase VO. */
  private DataSetMetabaseVO dataSetMetabaseVO;

  /** The id table. */
  private Long idTable;

  /** The ischeck DC. */
  private Boolean ischeckDC;
}
