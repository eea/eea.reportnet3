package org.eea.dataset.mapper;

import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.eea.interfaces.vo.validation.DremioValidationVO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DremioValidationMapper implements RowMapper<DremioValidationVO> {

    @Override
    public DremioValidationVO mapRow(ResultSet resultSet, int i) throws SQLException {
        DremioValidationVO dremioValidationVO = new DremioValidationVO();
        dremioValidationVO.setPk(Long.valueOf(resultSet.getString("pk")));
        dremioValidationVO.setRecordId(resultSet.getString("record_id"));
        dremioValidationVO.setValidationLevel(ErrorTypeEnum.valueOf(resultSet.getString("validation_level")));
        dremioValidationVO.setValidationArea(EntityTypeEnum.valueOf(resultSet.getString("validation_area")));
        dremioValidationVO.setMessage(resultSet.getString("message"));
        dremioValidationVO.setTableName(resultSet.getString("table_name"));
        dremioValidationVO.setFieldName(resultSet.getString("field_name"));
        dremioValidationVO.setDatasetId(Long.valueOf(resultSet.getString("dataset_id")));
        dremioValidationVO.setQcCode(resultSet.getString("qc_code"));
        return dremioValidationVO;
    }
}
