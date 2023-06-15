package org.eea.dataset.mapper;

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

@Component
public class DremioRecordMapper implements RowMapper<RecordVO> {

    private String datasetSchemaId;
    private String tableSchemaId;
    private RecordSchemaVO recordSchemaVO;

    @Override
    public RecordVO mapRow(ResultSet resultSet, int i) throws SQLException {
        List<FieldSchemaVO> fieldSchemas = recordSchemaVO.getFieldSchema();
        RecordVO recordVO = new RecordVO();
        recordVO.setIdRecordSchema(recordSchemaVO.getIdRecordSchema());
        List<FieldVO> fields = new ArrayList<>();
        for (FieldSchemaVO fieldSchemaVO : fieldSchemas) {
            FieldVO field = new FieldVO();
            field.setIdFieldSchema(fieldSchemaVO.getId());
            field.setType(fieldSchemaVO.getType());
            field.setValue(resultSet.getString(fieldSchemaVO.getName()));
            field.setName(fieldSchemaVO.getName());
            fields.add(field);
        }
        recordVO.setFields(fields);
        recordVO.setId(resultSet.getString("A"));
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
