package org.eea.validation.util.datalake;

import org.apache.commons.lang3.BooleanUtils;
import org.eea.validation.configuration.DremioConfiguration;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.*;

@Import(DremioConfiguration.class)
@Component
public class DremioSQLValidationUtils {

    private JdbcTemplate dremioJdbcTemplate;
    private static DremioSQLValidationUtils instance;

    public static synchronized DremioSQLValidationUtils getInstance() {
        if (instance == null) {
            instance = new DremioSQLValidationUtils();
        }
        return instance;
    }

    public List<String> isSQLSentenceWithCode(String sql) {
        StringBuilder query = new StringBuilder();
        query.append("select record_id from(").append(sql).append(")");
        return dremioJdbcTemplate.queryForList(query.toString(), String.class);
    }

    public List<Map<String, Object>> isSQLSentenceWithCodeMap(String sql) {
        StringBuilder query = new StringBuilder();
        query.append("select * from(").append(sql).append(")");
        return dremioJdbcTemplate.queryForList(query.toString());
    }

    public List<String> isUniqueConstraint(String fieldName, String tablePath) {
        StringBuilder query = new StringBuilder();
        query.append("with tableAux as (select ").append(fieldName).append(", count(").append(fieldName).append(") from ")
                .append(tablePath).append(" group by ").append(fieldName).append(" having count(").append(fieldName)
                .append(")>1)").append(" select t.record_id from ").append(tablePath).append(" t where t.")
                .append(fieldName).append(" in (select tab.").append(fieldName).append(" from tableAux tab)");
        return dremioJdbcTemplate.queryForList(query.toString(), String.class);
    }

    //the following method is being called via invoke from getIsFieldFKRecordIds.
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
            //FK_MULTIPLE_WRONG
            if(BooleanUtils.isTrue(fkFieldSchema.getPkHasMultipleValues())) {
                //PK_QUERY_VALUES
                StringBuilder pkQuery = new StringBuilder();
                pkQuery.append("select ").append(primaryKey).append(" from ").append(pkTablePath);
                List<String> pkValueList = dremioJdbcTemplate.query(pkQuery.toString(), (ResultSet rs) -> {
                    List<String> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(rs.getString(primaryKey));
                    }
                    return result;
                });

                //FK_QUERY_VALUES
                StringBuilder fkQuery = new StringBuilder();
                fkQuery.append("select ").append("record_id").append(",").append(foreignKey).append(" from ").append(fkTablePath);
                SqlRowSet fkValues = dremioJdbcTemplate.queryForRowSet(fkQuery.toString());
                while (fkValues.next()) {
                    List<String> recordValues = new ArrayList<>(Arrays.asList(fkValues.getString(foreignKey).split(";")));
                    for(String recordValue: recordValues){
                        if(BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())){
                            //FK_MULTIPLE_WRONG_IGNORE_CASE_LINK
                            List<String> lowercasePkValues = pkValueList.stream().map(String::toLowerCase) .collect(Collectors.toList());
                            if (!lowercasePkValues.contains(recordValue.toLowerCase())) {
                                recordIds.add(fkValues.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
                            }
                        }
                        else{
                            if (!pkValueList.contains(recordValue)) {
                                recordIds.add(fkValues.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
                            }
                        }
                    }
                }
            }
            else{
                //FK_SINGLE_WRONG
                if(BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())){
                    //FK_SINGLE_WRONG_IGNORE_CASE_LINK
                    query.append("select fk.record_id from ").append(fkTablePath).append(" fk where LOWER(fk.").append(foreignKey).append(") not in (select LOWER(pk.").append(primaryKey)
                            .append(") from ").append(pkTablePath).append(" pk)");
                }
                else{
                    query.append("select fk.record_id from ").append(fkTablePath).append(" fk where fk.").append(foreignKey).append(" not in (select pk.").append(primaryKey)
                            .append(" from ").append(pkTablePath).append(" pk)");
                }
                recordIds = dremioJdbcTemplate.queryForList(query.toString(), String.class);
            }
        } else {
            if (null != fkFieldSchema && null != fkFieldSchema.getPkMustBeUsed()) {
                //PK_MUST_BE_USED
                query.append("select count(").append("pk.").append(primaryKey).append(") from ").append(fkTablePath).append(" fk right join ").append(pkTablePath)
                        .append(" pk on fk.").append(foreignKey).append("=pk.").append(primaryKey).append(" where fk.").append(foreignKey).append(" is null");
                Long pkNotUsed = dremioJdbcTemplate.queryForObject(query.toString(), Long.class);
                if (pkNotUsed > 0) {
                    recordIds.add(PK_NOT_USED);
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
                        if(result.get(rs.getString(optionalPk)) != null){
                            String hashmapValues = result.get(rs.getString(optionalPk)) + "," + rs.getString(primaryKey);
                            result.put(rs.getString(optionalPk), hashmapValues);
                        }
                        else{
                            result.put(rs.getString(optionalPk), rs.getString(primaryKey));
                        }

                    }
                    return result;
                });
                Map<String, String> pkMapAux = pkWithOptionalMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                //FK_QUERY_VALUES
                StringBuilder fkQuery = new StringBuilder();
                fkQuery.append("select ").append("record_id").append(",").append(optionalFk).append(",").append(foreignKey).append(" from ").append(fkTablePath);
                SqlRowSet fkWithOptionalRS = dremioJdbcTemplate.queryForRowSet(fkQuery.toString());
                while (fkWithOptionalRS.next()) {
                    if (pkWithOptionalMap.get(fkWithOptionalRS.getString(optionalFk))!=null) {
                        List<String> pksByOptionalValue = Arrays.asList(pkWithOptionalMap.get(fkWithOptionalRS.getString(optionalFk)).split(","));
                        List<String> fksByOptionalValue = Arrays.asList(fkWithOptionalRS.getString(foreignKey).split(";"));
                        pksByOptionalValue.replaceAll(String::trim);
                        fksByOptionalValue.replaceAll(String::trim);

                        for (String value : fksByOptionalValue) {
                            List<String> pksByOptionalValueAux =
                                    new ArrayList<>(Arrays.asList(pkMapAux.get(fkWithOptionalRS.getString(optionalFk)).split(",")));
                            pksByOptionalValueAux.replaceAll(String::trim);

                            if (!pksByOptionalValue.contains("\"" + value + "\"")
                                    && !pksByOptionalValue.contains(value)) {
                                if (!recordIds.contains(fkWithOptionalRS.getString(PARQUET_RECORD_ID_COLUMN_HEADER))) {
                                    recordIds.add(fkWithOptionalRS.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
                                }
                            }
                            if (pksByOptionalValue.contains("\"" + value + "\"")
                                    || pksByOptionalValue.contains(value)) {
                                pksByOptionalValueAux.remove(value);
                                pksByOptionalValueAux.remove("\"" + value + "\"");
                            }
                            pkMapAux.put(fkWithOptionalRS.getString(optionalFk),
                                    pksByOptionalValueAux.toString().replace("]", "").replace("[", "").trim());
                        }
                    }
                    else{
                        recordIds.add(fkWithOptionalRS.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
                    }
                }
                if (pkMustBeUsed && !pkMapAux.entrySet().isEmpty()) {
                    recordIds = new ArrayList<>();
                }
            } else {
                //COMPOSE_PK_MUST_BE_USED_LIST
                query.append("select pk.").append(primaryKey).append(" from ").append(fkTablePath).append(" fk right join ").append(pkTablePath)
                        .append(" pk on fk.").append(foreignKey).append("=").append(" pk.").append(primaryKey).append(" and fk.").append(optionalFk)
                        .append("=").append(optionalPk).append(" where fk.").append(foreignKey).append(" is null or fk.").append(optionalFk).append(" is null");
                List<String> res = dremioJdbcTemplate.queryForList(query.toString(), String.class);
                if (res.size()>0) {
                    recordIds.add(PK_NOT_USED);
                }
            }
        }
        return recordIds;
    }

    public List<String> checkIntegrityConstraint(String originTablePath, String referTablePath, List<String> originFields, List<String> referFields, boolean isDoubleReferenced) {
        List<String> recordIds = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("select ").append("pk.record_id").append(" from ").append(originTablePath).append(" fk ").append("right join ").append(referTablePath).append(" pk on ");
        for (int i = 0; i < referFields.size(); i++) {
            if (i != 0) {
                query.append(" and ");
            }
            query.append("pk.").append(referFields.get(i)).append("=").append("fk.").append(originFields.get(i));
        }
        query.append(" where fk.").append(originFields.get(0)).append(" is null and pk.").append(referFields.get(0)).append(" is not null");
        List<String> res = dremioJdbcTemplate.queryForList(query.toString(), String.class);
        if (res.size()>0) {
            recordIds.add(OMISSION);
        }
        StringBuilder isDoubleReferQuery = new StringBuilder();
        if (isDoubleReferenced) {
            isDoubleReferQuery.append("select fk.record_id").append(" from ").append(referTablePath).append(" pk ").append("right join ").append(originTablePath).append(" fk on ");
            for (int i=0; i<originFields.size(); i++) {
                if (i!=0) {
                    isDoubleReferQuery.append(" and ");
                }
                isDoubleReferQuery.append("fk.").append(originFields.get(i)).append("=").append("pk.").append(referFields.get(i));
            }
            isDoubleReferQuery.append(" where pk.").append(referFields.get(0)).append(" is null and fk.").append(originFields.get(0)).append(" is not null");
            List<String> rs = dremioJdbcTemplate.queryForList(isDoubleReferQuery.toString(), String.class);
            if (rs.size()>0) {
                recordIds.add(COMISSION);
            }
        }
        return recordIds;
    }
}


















