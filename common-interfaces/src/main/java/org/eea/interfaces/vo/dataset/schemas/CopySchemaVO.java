package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The Class CopySchemaVO.
 */
@Getter
@Setter
@ToString
public class CopySchemaVO {

  /** The origin dataset schema ids. */
  private List<String> originDatasetSchemaIds;

  /** The dictionary origin target object id. */
  private Map<String, String> dictionaryOriginTargetObjectId;

  /** The dataflow id destination. */
  private Long dataflowIdDestination;

  /** The dictionary origin target datasets id. */
  private Map<Long, Long> dictionaryOriginTargetDatasetsId;

}
