package org.eea.dataset.service;

import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.TableVO;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;

public interface DataLakeDataRetriever {

    TableVO getTableResult(DataSetMetabaseVO dataset, TableSchemaVO tableSchemaVO, Pageable pageable, String fields, String fieldValue, ErrorTypeEnum[] levelError, String[] qcCodes) throws EEAException;

    boolean isApplicable(String datasetType);

    default void setEmptyResults(TableVO result) {
        result.setTotalFilteredRecords(0L);
        result.setTableValidations(new ArrayList<>());
        result.setTotalRecords(0L);
        result.setRecords(new ArrayList<>());
    }

}
