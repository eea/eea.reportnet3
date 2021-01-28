package org.eea.interfaces.vo.dataset.schemas;

import java.util.List;
import java.util.Map;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class ImportSchemaVO {


  private Map<String, String> dictionaryOriginTargetObjectId;
  private List<RulesSchemaVO> rulesSchemaVO;
  private List<IntegrityVO> integritiesVO;
  private List<byte[]> qcrulesbytes;

}
