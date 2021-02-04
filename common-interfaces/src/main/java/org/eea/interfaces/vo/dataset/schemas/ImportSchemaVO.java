package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import java.util.Map;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * The Class ImportSchemaVO.
 */
@Getter
@Setter
@ToString
public class ImportSchemaVO {


  /** The dictionary origin target object id. */
  private Map<String, String> dictionaryOriginTargetObjectId;

  /** The rules schema VO. */
  private List<RulesSchemaVO> rulesSchemaVO;

  /** The integrities VO. */
  private List<IntegrityVO> integritiesVO;

  /** The qc rules bytes. */
  private List<byte[]> qcRulesBytes;

}
