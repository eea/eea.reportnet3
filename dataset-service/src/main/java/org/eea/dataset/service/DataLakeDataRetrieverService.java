package org.eea.dataset.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.springframework.data.domain.Pageable;

public interface DataLakeDataRetrieverService {

    /**
     *
     * @param datasetId
     * @param mongoID
     * @param pageable
     * @param fields
     * @param levelError
     * @param qcCodes
     * @param fieldSchema
     * @param fieldValue
     * @return
     * @throws EEAException
     */
    TableVO getTableValuesDLById(Long datasetId, String mongoID, Pageable pageable, String fields, ErrorTypeEnum[] levelError, String[] qcCodes, String fieldSchema,
                                 String fieldValue) throws EEAException;
}
