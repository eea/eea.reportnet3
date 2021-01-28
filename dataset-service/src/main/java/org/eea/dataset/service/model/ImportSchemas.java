package org.eea.dataset.service.model;

import java.util.List;
import java.util.Map;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.uniqueconstraints.UniqueConstraintSchema;
import org.eea.interfaces.vo.dataset.schemas.rule.IntegrityVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;



@Getter
@Setter
@ToString
public class ImportSchemas {


  private List<DataSetSchema> schemas;
  private Map<String, String> schemaNames;
  private List<IntegrationVO> externalIntegrations;
  private List<UniqueConstraintSchema> uniques;
  private List<IntegrityVO> integrities;
  private List<byte[]> qcrulesBytes;

}
