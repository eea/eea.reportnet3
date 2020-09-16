package org.eea.validation.persistence.data.repository;

import java.util.List;
import org.eea.validation.persistence.data.domain.FieldValidation;
import org.eea.validation.persistence.data.domain.RecordValidation;
import org.eea.validation.persistence.data.domain.TableValue;

public interface DatasetExtendedRepository {

  TableValue queryRSExecution(String query);

  List<RecordValidation> queryRecordValidationExecution(String query);

  List<FieldValidation> queryFieldValidationExecution(String query);

}
