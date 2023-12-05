package org.eea.dataset.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.springframework.data.domain.Pageable;

import java.util.Map;

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

    /**
     * builds filtered query for retrieving data
     * @param dataset
     * @param fields
     * @param fieldValue
     * @param fieldIdMap
     * @param levelError
     * @param qcCodes
     * @param validationTablePath
     * @return
     */
    StringBuilder buildFilteredQuery(DataSetMetabaseVO dataset, String fields, String fieldValue, Map<String, FieldSchemaVO> fieldIdMap,
                                     ErrorTypeEnum[] levelError, String[] qcCodes, String validationTablePath);

}
