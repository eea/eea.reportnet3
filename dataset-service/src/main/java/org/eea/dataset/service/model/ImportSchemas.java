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



/**
 * The Class ImportSchemas.
 */
@Getter
@Setter
@ToString
public class ImportSchemas {


  /** The schemas. */
  private List<DataSetSchema> schemas;

  /** The schema names. */
  private Map<String, String> schemaNames;

  /** The external integrations. */
  private List<IntegrationVO> externalIntegrations;

  /** The uniques. */
  private List<UniqueConstraintSchema> uniques;

  /** The integrities. */
  private List<IntegrityVO> integrities;


  /** The qc rules bytes. */
  private List<byte[]> qcRulesBytes;

  /** The schema ids. */
  private Map<String, Long> schemaIds;

}
