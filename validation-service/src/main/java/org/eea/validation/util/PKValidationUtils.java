package org.eea.validation.util;

import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class PKValidation.
 */
@Component
public class PKValidationUtils {


  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The schemas repository. */
  @Autowired
  private SchemasRepository schemasRepository;


  public static Boolean isfieldPK(String value, Long datasetIdReference, String idFieldSchema) {

    return false;
  }


}
