package org.eea.validation.util.datalake;

import org.eea.validation.configuration.DremioConfiguration;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.*;

@Import(DremioConfiguration.class)
@Component
public class DremioSQLValidationUtils {

    private JdbcTemplate dremioJdbcTemplate;


    public List<String> isSQLSentenceWithCode(String sql) {
        StringBuilder query = new StringBuilder();
        query.append("select record_id from(").append(sql).append(")");
        return dremioJdbcTemplate.queryForList(query.toString(), String.class);
    }

    public List<String> isUniqueConstraint(String fieldName, String tablePath) {
        StringBuilder query = new StringBuilder();
        query.append("with tableAux as (select ").append(fieldName).append(", count(").append(fieldName).append(") from ")
                .append(tablePath).append(" group by ").append(fieldName).append(" having count(").append(fieldName)
                .append(")>1)").append(" select t.record_id from ").append(tablePath).append(" t where t.")
                .append(fieldName).append(" in (select tab.").append(fieldName).append(" from tableAux tab)");
        return dremioJdbcTemplate.queryForList(query.toString(), String.class);
    }

    public List<String> isfieldFK(FieldSchema fkFieldSchema, boolean pkMustBeUsed, String fkTablePath, String pkTablePath, String foreignKey, String primaryKey,
                                  String optionalFk, String optionalPk) {
        List<String> recordIds;
        // Optionals FK fields
        if (null != fkFieldSchema && null != fkFieldSchema.getReferencedField()
                && null != fkFieldSchema.getReferencedField().getLinkedConditionalFieldId()
                && null != fkFieldSchema.getReferencedField().getMasterConditionalFieldId()) {
            recordIds = calculateFKCompose(pkMustBeUsed, fkFieldSchema, fkTablePath, pkTablePath, foreignKey, primaryKey, optionalFk, optionalPk);
        } else {
            recordIds = calculateFKsimple(pkMustBeUsed, fkFieldSchema, fkTablePath, pkTablePath, foreignKey, primaryKey);
        }
        return recordIds;
    }

    private List<String> calculateFKsimple(boolean pkMustBeUsed, FieldSchema fkFieldSchema, String fkTablePath, String pkTablePath,
                                           String foreignKey, String primaryKey) {
        StringBuilder query = new StringBuilder();
        List<String> recordIds = new ArrayList<>();
        if (!pkMustBeUsed) {
            if (fkFieldSchema.getIgnoreCaseInLinks() != null && fkFieldSchema.getIgnoreCaseInLinks()) {
                //FK_SINGLE_WRONG_IGNORE_CASE_LINK
                query.append("select fk.record_id from ").append(fkTablePath).append(" fk where LOWER(fk.").append(foreignKey).append(") not in (select LOWER(pk.").append(primaryKey)
                        .append(") from ").append(pkTablePath).append(" pk");
            } else {
                //FK_SINGLE_WRONG
                query.append("select fk.record_id from ").append(fkTablePath).append(" fk where fk.").append(foreignKey).append(" not in (select pk.").append(primaryKey)
                        .append(" from ").append(pkTablePath).append(" pk)");
            }
            recordIds = dremioJdbcTemplate.queryForList(query.toString(), String.class);
        } else {
            if (null != fkFieldSchema && null != fkFieldSchema.getPkMustBeUsed()) {
                //PK_MUST_BE_USED
                query.append("select count(").append("pk.").append(primaryKey).append(") from ").append(fkTablePath).append(" fk right join ").append(pkTablePath)
                        .append(" pk on fk.").append(foreignKey).append("=pk.").append(primaryKey).append(" where fk.").append(foreignKey).append(" is null");
                Long pkNotUsed = dremioJdbcTemplate.queryForObject(query.toString(), Long.class);
                if (pkNotUsed > 0) {
                    recordIds.add("pkNotUsed");
                }
            }
        }
        return recordIds;
    }

    private List<String> calculateFKCompose(boolean pkMustBeUsed, FieldSchema fkFieldSchema, String fkTablePath, String pkTablePath,
                                      String foreignKey, String primaryKey, String optionalFk, String optionalPk) {
        StringBuilder query = new StringBuilder();
        List<String> recordIds = new ArrayList<>();
        if (!pkMustBeUsed && Boolean.FALSE.equals(fkFieldSchema.getPkHasMultipleValues())) {
            //COMPOSE_PK_LIST
            query.append("select fk.record_id from ").append(fkTablePath).append(" fk left join ").append(pkTablePath).append(" pk on fk.").append(foreignKey)
                    .append("=pk.").append(primaryKey).append(" and ").append("fk.").append(optionalFk).append("=").append("pk.").append(optionalPk)
                    .append(" where pk.").append(primaryKey).append(" is null or pk.").append(optionalPk).append(" is null");
            recordIds = dremioJdbcTemplate.queryForList(query.toString(), String.class);
        } else {
            if (Boolean.TRUE.equals(fkFieldSchema.getPkHasMultipleValues())) {
                //PK_QUERY_VALUES
                StringBuilder pkQuery = new StringBuilder();
                pkQuery.append("select ").append(optionalPk).append(",").append(primaryKey).append(" from ").append(pkTablePath);
                Map<String, String> pkWithOptionalMap = dremioJdbcTemplate.query(pkQuery.toString(), (ResultSet rs) -> {
                    HashMap<String,String> result = new HashMap<>();
                    while (rs.next()) {
                        result.put(rs.getString(optionalPk), rs.getString(primaryKey));
                    }
                    return result;
                });
                //FK_QUERY_VALUES
                StringBuilder fkQuery = new StringBuilder();
                fkQuery.append("select ").append("record_id").append(",").append(optionalFk).append(",").append(foreignKey).append(" from ").append(fkTablePath);
                SqlRowSet fkWithOptionalRS = dremioJdbcTemplate.queryForRowSet(fkQuery.toString());
                while (fkWithOptionalRS.next()) {
                    List<String> pksByOptionalValue = Arrays.asList(pkWithOptionalMap.get(fkWithOptionalRS.getString(optionalFk)).split(","));
                    List<String> fksByOptionalValue = Arrays.asList(fkWithOptionalRS.getString(foreignKey).split(";"));
                    pksByOptionalValue.replaceAll(String::trim);
                    fksByOptionalValue.replaceAll(String::trim);

                    for (String value : fksByOptionalValue) {
                        if (!pksByOptionalValue.contains("\"" + value + "\"")
                                && !pksByOptionalValue.contains(value)) {
                            if (!recordIds.contains(fkWithOptionalRS.getString("record_id"))) {
                                recordIds.add(fkWithOptionalRS.getString("record_id"));
                            }
                        }
                    }
                }
            } else {
                //COMPOSE_PK_MUST_BE_USED_LIST
                query.append("select pk.").append(primaryKey).append(" from ").append(fkTablePath).append(" fk right join ").append(pkTablePath)
                        .append(" pk on fk.").append(foreignKey).append("=").append(" pk.").append(primaryKey).append(" and fk.").append(optionalFk)
                        .append("=").append(optionalPk).append(" where fk.").append(foreignKey).append(" is null or fk.").append(optionalFk).append(" is null");
                List<String> res = dremioJdbcTemplate.queryForList(query.toString(), String.class);
                if (res.size()>0) {
                    recordIds.add("pkNotUsed");
                }
            }
        }
        return recordIds;
    }
}


















