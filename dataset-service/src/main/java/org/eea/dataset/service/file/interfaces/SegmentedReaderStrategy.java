package org.eea.dataset.service.file.interfaces;

import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;

import java.io.InputStream;

public interface SegmentedReaderStrategy {
    void parseFile(InputStream inputStream,Long startLine,Long endLine, Long dataflowId, Long partitionId, String idTableSchema,
                   Long datasetId, String fileName, boolean replace, DataSetSchema schema,
                   ConnectionDataVO connectionDataVO) throws EEAException;

}
