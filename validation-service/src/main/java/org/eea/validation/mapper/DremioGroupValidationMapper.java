package org.eea.validation.mapper;

import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DremioGroupValidationMapper implements RowMapper<GroupValidationVO> {
    @Override
    public GroupValidationVO mapRow(ResultSet resultSet, int i) throws SQLException {
        GroupValidationVO groupValidationVO = new GroupValidationVO();
        groupValidationVO.setLevelError(ErrorTypeEnum.valueOf(resultSet.getString("levelError")));
        groupValidationVO.setTypeEntity(EntityTypeEnum.valueOf(resultSet.getString("typeEntity")));
        groupValidationVO.setNumberOfRecords(resultSet.getInt("numberOfRecords"));
        groupValidationVO.setNameTableSchema(resultSet.getString("tableName"));
        groupValidationVO.setShortCode(resultSet.getString("shortCode"));
        groupValidationVO.setNameFieldSchema(resultSet.getString("fieldName"));
        groupValidationVO.setMessage(resultSet.getString("message"));
        return groupValidationVO;
    }
}
