package org.eea.validation.persistence.data.repository;

import org.eea.interfaces.vo.dataset.TableVO;

public interface DatasetExtendedRepository {

  TableVO queryRSExecution(String query);

}
