package org.eea.validation.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.eea.interfaces.vo.dataset.GroupValidationVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.enums.ErrorTypeEnum;
import org.springframework.jdbc.core.RowMapper;

public class DremioGroupValidationMapperWithoutRuleId implements RowMapper<GroupValidationVO> {

    /** The max errors. */
    private final int maxErrors;

    public DremioGroupValidationMapperWithoutRuleId(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    @Override
    public GroupValidationVO mapRow(ResultSet resultSet, int i) throws SQLException {
        GroupValidationVO groupValidationVO = new GroupValidationVO();
        groupValidationVO.setLevelError(ErrorTypeEnum.valueOf(resultSet.getString("levelError")));
        groupValidationVO.setTypeEntity(EntityTypeEnum.valueOf(resultSet.getString("typeEntity")));
        groupValidationVO.setNumberOfRecords(resultSet.getString("numberOfRecords").equals(String.valueOf(maxErrors)) ? maxErrors - 1 + "+" : resultSet.getString("numberOfRecords"));
        groupValidationVO.setNameTableSchema(resultSet.getString("tableName"));
        groupValidationVO.setShortCode(resultSet.getString("shortCode"));
        groupValidationVO.setNameFieldSchema(resultSet.getString("fieldName"));
        groupValidationVO.setMessage(resultSet.getString("message"));
        return groupValidationVO;
    }
}
