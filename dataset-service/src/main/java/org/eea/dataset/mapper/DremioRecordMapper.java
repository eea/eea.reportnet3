package org.eea.dataset.mapper;

import org.eea.datalake.service.SpatialDataHandling;
import org.eea.interfaces.vo.dataset.FieldVO;
import org.eea.interfaces.vo.dataset.RecordVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.eea.utils.LiteralConstants.*;

@Component
public class DremioRecordMapper implements RowMapper<RecordVO> {

    private String datasetSchemaId;
    private String tableSchemaId;
    private RecordSchemaVO recordSchemaVO;

    private final SpatialDataHandling spatialDataHandling;

    public DremioRecordMapper(SpatialDataHandling spatialDataHandling) {
        this.spatialDataHandling = spatialDataHandling;
    }

    @Override
    public RecordVO mapRow(ResultSet resultSet, int i) throws SQLException {
        List<FieldSchemaVO> fieldSchemas = recordSchemaVO.getFieldSchema();
        RecordVO recordVO = new RecordVO();
        recordVO.setIdRecordSchema(recordSchemaVO.getIdRecordSchema());
        List<FieldVO> fields = new ArrayList<>();
        for (FieldSchemaVO fieldSchemaVO : fieldSchemas) {
            String value;
            byte[] byteArrayValue = null;
            try {
                value = resultSet.getString(fieldSchemaVO.getName());
                if (spatialDataHandling.getGeoJsonEnums().contains(fieldSchemaVO.getType())) {
                    byteArrayValue =  resultSet.getBytes(fieldSchemaVO.getName());
                }
            } catch (java.sql.SQLException e) {
                value = null;
            }
            FieldVO field = new FieldVO();
            field.setIdFieldSchema(fieldSchemaVO.getId());
            field.setType(fieldSchemaVO.getType());
            field.setValue(value);
            field.setByteArrayValue(byteArrayValue);
            field.setName(fieldSchemaVO.getName());
            fields.add(field);
        }
        recordVO.setFields(fields);
        recordVO.setId(resultSet.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
        recordVO.setDataProviderCode(resultSet.getString(PARQUET_PROVIDER_CODE_COLUMN_HEADER));
        return recordVO;
    }

    public String getDatasetSchemaId() {
        return datasetSchemaId;
    }

    public DremioRecordMapper setDatasetSchemaId(String datasetSchemaId) {
        this.datasetSchemaId = datasetSchemaId;
        return this;
    }

    public String getTableSchemaId() {
        return tableSchemaId;
    }

    public DremioRecordMapper setTableSchemaId(String tableSchemaId) {
        this.tableSchemaId = tableSchemaId;
        return this;
    }

    public RecordSchemaVO getRecordSchemaVO() {
        return recordSchemaVO;
    }

    public DremioRecordMapper setRecordSchemaVO(RecordSchemaVO recordSchemaVO) {
        this.recordSchemaVO = recordSchemaVO;
        return this;
    }
}
