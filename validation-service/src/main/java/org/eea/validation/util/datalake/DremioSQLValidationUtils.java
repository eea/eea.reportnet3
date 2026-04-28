package org.eea.validation.util.datalake;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.BooleanUtils;
import org.eea.utils.UtilityClass;
import org.eea.validation.configuration.DremioConfiguration;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.eea.utils.LiteralConstants.*;
import static org.eea.validation.util.FKValidationUtils.splitCommasRespectingQuotes;

@Import(DremioConfiguration.class)
@Component
public class DremioSQLValidationUtils {

    private JdbcTemplate dremioJdbcTemplate;
    private static DremioSQLValidationUtils instance;

    @Value(value = "${validation.maximumErrors}")
    private int maxErrors;

    public static synchronized DremioSQLValidationUtils getInstance() {
        if (instance == null) {
            instance = new DremioSQLValidationUtils();
        }
        return instance;
    }

    @PostConstruct
    public void init() {
        instance = this; // 🔹 Spring will inject this instance (with @Value loaded)
    }

    public List<String> isSQLSentenceWithCode(String sql) {
        StringBuilder query = new StringBuilder();
        sql = sql.concat(" limit " + maxErrors);
        query.append("select record_id from(").append(sql).append(")");
        return dremioJdbcTemplate.queryForList(query.toString(), String.class);
    }

    public List<Map<String, Object>> isSQLSentenceWithCodeMap(String sql) {
        StringBuilder query = new StringBuilder();
        sql = sql.concat(" limit " + maxErrors);
        query.append("select * from(").append(sql).append(")");
        return dremioJdbcTemplate.queryForList(query.toString());
    }

    public List<String> isUniqueConstraint(String fieldName, String tablePath) {
        //if the unique constraint is applied to multiple fields, fieldName will contain all field names separated by comma
        StringBuilder query = new StringBuilder();
        List<String> fieldNames = Arrays.asList(fieldName.split(","));
        String tFieldNames = String.join(",", fieldNames.stream().map(element -> "t." + element).collect(Collectors.toList()));
        String tabFieldNames = String.join(",", fieldNames.stream().map(element -> "tab." + element).collect(Collectors.toList()));

        query.append("with tableAux as (select ").append(fieldName).append(", count(*) from ")
                .append(tablePath).append(" group by ").append(fieldName).append(" having count(*)>1)")
                .append(" select t.record_id from ").append(tablePath).append(" t where (")
                .append(tFieldNames).append(") in (select ").append(tabFieldNames).append(" from tableAux tab)")
                .append(" limit " + maxErrors);
        return dremioJdbcTemplate.queryForList(query.toString(), String.class);
    }

    //the following method is being called via invoke from getIsFieldFKRecordIds.
    public List<String> isfieldFK(FieldSchema fkFieldSchema, boolean pkMustBeUsed, String fkTablePath, String pkTablePath, String foreignKey, String primaryKey,
                                  String optionalFk, String optionalPk, FieldSchema masterConditionalFieldSchema) {
        List<String> recordIds;
        // Optionals FK fields
        if (null != fkFieldSchema && null != fkFieldSchema.getReferencedField()
                && null != fkFieldSchema.getReferencedField().getLinkedConditionalFieldId()
                && null != fkFieldSchema.getReferencedField().getMasterConditionalFieldId()) {
            recordIds = calculateFKCompose(pkMustBeUsed, fkFieldSchema, fkTablePath, pkTablePath, foreignKey, primaryKey, optionalFk, optionalPk, masterConditionalFieldSchema);
        } else {
            recordIds = calculateFKsimple(pkMustBeUsed, fkFieldSchema, fkTablePath, pkTablePath, foreignKey, primaryKey);
        }
        return recordIds;
    }

    private List<String> calculateFKsimple(boolean pkMustBeUsed, FieldSchema fkFieldSchema, String fkTablePath, String pkTablePath,
                                           String foreignKey, String primaryKey) {
        //Handling of multiple/single value, pk must/mustn't be used and ignore/not ignore case
        String quotedPrimaryKey = UtilityClass.addQuotesToFieldNames(primaryKey);
        String quotedForeignKey = UtilityClass.addQuotesToFieldNames(foreignKey);

        StringBuilder query = new StringBuilder();
        List<String> recordIds = new ArrayList<>();
        pkMustBeUsed = pkMustBeUsed && null != fkFieldSchema && null != fkFieldSchema.getPkMustBeUsed();
        //FK_MULTIPLE_WRONG
        if(BooleanUtils.isTrue(fkFieldSchema.getPkHasMultipleValues())) {
            //PK_QUERY_VALUES
            StringBuilder pkQuery = new StringBuilder();
            pkQuery.append("select ").append(quotedPrimaryKey).append(" from ").append(pkTablePath);
            List<String> pkValueList = dremioJdbcTemplate.query(pkQuery.toString(), (ResultSet rs) -> {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rs.getString(primaryKey).trim());
                }
                return result;
            });
            List<String> lowercasePkValues;
            List<String> pkValueListForPkMustBeUsed;
            if(BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())){
                lowercasePkValues = pkValueList.stream().map(String::toLowerCase).collect(Collectors.toList());
                pkValueListForPkMustBeUsed = new ArrayList<>(lowercasePkValues);
            }
            else{
                lowercasePkValues = new ArrayList<>(pkValueList);
                pkValueListForPkMustBeUsed = new ArrayList<>(pkValueList);
            }
            //FK_QUERY_VALUES
            StringBuilder fkQuery = new StringBuilder();
            fkQuery.append("select ").append("record_id").append(",").append(quotedForeignKey).append(" from ").append(fkTablePath)
                    .append(" where ").append(quotedForeignKey).append(" is not NULL and ").append(quotedForeignKey).append(" != ''");
            SqlRowSet fkValues = dremioJdbcTemplate.queryForRowSet(fkQuery.toString());
            while (fkValues.next()) {
                List<String> recordValues = new ArrayList<>(Arrays.asList(fkValues.getString(foreignKey).split(";")))
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());
                for(String recordValue: recordValues){
                    if(pkMustBeUsed){
                        //PK_MUST_BE_USED
                        //remove items from pk that are included in fk
                        if (BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())) {
                            //FK_MULTIPLE_WRONG_IGNORE_CASE_LINK
                            if (lowercasePkValues.contains(recordValue.toLowerCase())) {
                                pkValueListForPkMustBeUsed.removeIf(value -> Objects.equals(value, recordValue.toLowerCase()));
                            }
                        } else {
                            if (pkValueList.contains(recordValue)) {
                                pkValueListForPkMustBeUsed.removeIf(value -> Objects.equals(value, recordValue));
                            }
                        }
                    }
                    else {
                        if (BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())) {
                            //FK_MULTIPLE_WRONG_IGNORE_CASE_LINK
                            if (!lowercasePkValues.contains(recordValue.toLowerCase())) {
                                recordIds.add(fkValues.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
                            }
                        } else {
                            if (!pkValueList.contains(recordValue)) {
                                recordIds.add(fkValues.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
                            }
                        }
                    }
                }
            }
            if(pkMustBeUsed && pkValueListForPkMustBeUsed.size() > 0){
                //there are fields in pk list that were not used as fk
                recordIds.add(PK_NOT_USED);
            }
        }
        else{
            //FK_SINGLE_WRONG
            if(BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())){
                //FK_SINGLE_WRONG_IGNORE_CASE_LINK
                if(pkMustBeUsed){
                    //PK_MUST_BE_USED
                    query.append("select count(").append("pk.").append(quotedPrimaryKey).append(") from ").append(fkTablePath).append(" fk right join ").append(pkTablePath)
                            .append(" pk on LOWER(fk.").append(quotedForeignKey).append(") = LOWER(pk.").append(quotedPrimaryKey).append(") where fk.").append(quotedForeignKey).append(" is null");
                    query.append(" limit " + maxErrors);
                    Long pkNotUsed = dremioJdbcTemplate.queryForObject(query.toString(), Long.class);
                    if (pkNotUsed > 0) {
                        recordIds.add(PK_NOT_USED);
                    }
                }
                else{
                    query.append("select fk.record_id from ").append(fkTablePath).append(" fk where fk.").append(quotedForeignKey).append(" is not NULL and fk.")
                            .append(quotedForeignKey).append(" != '' and LOWER(fk.").append(quotedForeignKey).append(") not in (select LOWER(pk.").append(quotedPrimaryKey)
                            .append(") from ").append(pkTablePath).append(" pk)");
                    query.append(" limit " + maxErrors);
                    recordIds = dremioJdbcTemplate.queryForList(query.toString(), String.class);
                }
            }
            else{
                if(pkMustBeUsed){
                    //PK_MUST_BE_USED
                    query.append("select count(").append("pk.").append(quotedPrimaryKey).append(") from ").append(fkTablePath).append(" fk right join ").append(pkTablePath)
                            .append(" pk on fk.").append(quotedForeignKey).append("=pk.").append(quotedPrimaryKey).append(" where fk.").append(quotedForeignKey).append(" is null");
                    query.append(" limit " + maxErrors);
                    Long pkNotUsed = dremioJdbcTemplate.queryForObject(query.toString(), Long.class);
                    if (pkNotUsed > 0) {
                        recordIds.add(PK_NOT_USED);
                    }
                }
                else{
                    String fieldName = "fk." + quotedForeignKey;
                    query.append("select fk.record_id from ")
                        .append(fkTablePath).append(" fk ")
                        .append("where ")
                        .append(fieldName)
                        .append(" IS NOT NULL").append(" AND ")
                        .append(fieldName)
                        .append(" <> ''").append(" AND ")
                        .append(fieldName)
                        .append(" not in (select pk.").append(quotedPrimaryKey)
                        .append(" from ")
                        .append(pkTablePath).append(" pk)")
                        .append(" limit " + maxErrors);
                    recordIds = dremioJdbcTemplate.queryForList(query.toString(), String.class);
                }
            }
        }
        return recordIds;
    }

    private List<String> calculateFKCompose(boolean pkMustBeUsed, FieldSchema fkFieldSchema, String fkTablePath, String pkTablePath,
                                      String foreignKey, String primaryKey, String optionalFk, String optionalPk, FieldSchema masterConditionalFieldSchema) {
        String quotedPrimaryKey = UtilityClass.addQuotesToFieldNames(primaryKey);
        String quotedForeignKey = UtilityClass.addQuotesToFieldNames(foreignKey);

        String quotedOptionalPk = UtilityClass.addQuotesToFieldNames(optionalPk);
        String quotedOptionalFk = UtilityClass.addQuotesToFieldNames(optionalFk);


        StringBuilder query = new StringBuilder();
        List<String> recordIds = new ArrayList<>();
        if (Boolean.FALSE.equals(fkFieldSchema.getPkHasMultipleValues())) {
            //FK_SINGLE_WRONG
            if(pkMustBeUsed){
                //COMPOSE_PK_MUST_BE_USED_LIST
                query.append("select pk.").append(quotedPrimaryKey).append(" from ").append(fkTablePath).append(" fk right join ").append(pkTablePath)
                        .append(" pk on ");
                if(BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())){
                    //FK_SINGLE_WRONG_IGNORE_CASE_LINK
                    query.append(" lower(fk.").append(quotedForeignKey).append(")=lower(pk.").append(quotedPrimaryKey).append(")");
                }
                else{
                    query.append(" fk.").append(quotedForeignKey).append("=pk.").append(quotedPrimaryKey);
                }
                query.append(" and fk.").append(quotedOptionalFk)
                        .append("=").append(quotedOptionalPk).append(" where fk.").append(quotedForeignKey).append(" is null or fk.").append(quotedOptionalFk).append(" is null");
                query.append(" limit " + maxErrors);
                List<String> res = dremioJdbcTemplate.queryForList(query.toString(), String.class);
                if (res.size()>0) {
                    recordIds.add(PK_NOT_USED);
                }
            }
            else{
                query.append("select fk.record_id from ").append(fkTablePath).append(" fk left join ").append(pkTablePath).append(" pk on ");
                if(BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())){
                    //FK_SINGLE_WRONG_IGNORE_CASE_LINK
                    query.append(" lower(fk.").append(quotedForeignKey).append(")=lower(pk.").append(quotedPrimaryKey).append(")");
                }
                else{
                    query.append(" fk.").append(quotedForeignKey).append("=pk.").append(quotedPrimaryKey);
                }
                query.append(" and ").append("fk.").append(quotedOptionalFk).append("=").append("pk.").append(quotedOptionalPk)
                        .append(" where (pk.").append(quotedPrimaryKey).append(" is null or pk.").append(quotedOptionalPk).append(" is null)")
                        .append("and fk.").append(quotedForeignKey).append(" is not NULL and fk.").append(quotedForeignKey).append(" != ''");
                query.append(" limit " + maxErrors);
                recordIds = dremioJdbcTemplate.queryForList(query.toString(), String.class);
            }
        } else {
            //PK_QUERY_VALUES
            StringBuilder pkQuery = new StringBuilder();
            pkQuery.append("select ").append(quotedOptionalPk).append(",").append(quotedPrimaryKey).append(" from ").append(pkTablePath);
            Map<String, String> pkWithOptionalMap = dremioJdbcTemplate.query(pkQuery.toString(), (ResultSet rs) -> {
                HashMap<String,String> result = new HashMap<>();
                while (rs.next()) {
                    if(result.get(rs.getString(optionalPk)) != null){
                        String hashmapValues = result.get(rs.getString(optionalPk)) + "," + "\"" +  rs.getString(primaryKey) + "\"" ;
                        result.put(rs.getString(optionalPk), hashmapValues);
                    }
                    else{
                        result.put(rs.getString(optionalPk), "\"" + rs.getString(primaryKey) + "\"" );
                    }

                }
                return result;
            });
            Map<String, String> pkMapAux = pkWithOptionalMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            List<String> pkValueListForPkMustBeUsed;
            if (BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())) {
                pkValueListForPkMustBeUsed = pkMapAux.values().stream()
                        .flatMap(value -> splitCommasRespectingQuotes(value).stream())
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            }
            else{
                pkValueListForPkMustBeUsed = pkMapAux.values().stream()
                        .flatMap(value -> splitCommasRespectingQuotes(value).stream())
                        .collect(Collectors.toList());
            }

            //FK_QUERY_VALUES
            StringBuilder fkQuery = new StringBuilder();
            fkQuery.append("select ").append("record_id").append(",").append(quotedOptionalFk).append(",").append(quotedForeignKey).append(" from ").append(fkTablePath)
                    .append(" where ").append(quotedForeignKey).append(" is not NULL and ").append(quotedForeignKey).append(" != ''");
            SqlRowSet fkWithOptionalRS = dremioJdbcTemplate.queryForRowSet(fkQuery.toString());
            while (fkWithOptionalRS.next()) {
                //  resolve conditional values - only splits if masterConditionalField has pkHasMultipleValues=true
                String conditionalRawValue = fkWithOptionalRS.getString(optionalFk);
                List<String> conditionalValues = getConditionalValues(conditionalRawValue, masterConditionalFieldSchema);

                if (conditionalValues.size() > 1) {
                    //  multi-value
                    processMultiValueConditional(conditionalValues, fkWithOptionalRS.getString(foreignKey),
                            fkWithOptionalRS.getString(PARQUET_RECORD_ID_COLUMN_HEADER), pkWithOptionalMap,
                            pkMapAux, pkValueListForPkMustBeUsed, recordIds, fkFieldSchema, pkMustBeUsed);
                } else {
                    if (pkWithOptionalMap.get(fkWithOptionalRS.getString(optionalFk)) != null) {
                        List<String> pksByOptionalValue = splitCommasRespectingQuotes(pkWithOptionalMap.get(fkWithOptionalRS.getString(optionalFk)));
                        List<String> fksByOptionalValue = Arrays.asList(fkWithOptionalRS.getString(foreignKey).split(";"));

                        if (BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())) {
                            //FK_MULTIPLE_WRONG_IGNORE_CASE_LINK
                            //set all items of pk and fk lists to lowercase
                            pksByOptionalValue = pksByOptionalValue.stream().map(String::toLowerCase).collect(Collectors.toList());
                            fksByOptionalValue = fksByOptionalValue.stream().map(String::toLowerCase).collect(Collectors.toList());
                        }
                        pksByOptionalValue.replaceAll(String::trim);
                        fksByOptionalValue.replaceAll(String::trim);

                        for (String value : fksByOptionalValue) {
                            if (pkMustBeUsed) {
                                String valueToCheckIfExists;
                                if (BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())) {
                                    valueToCheckIfExists = value.toLowerCase();
                                } else {
                                    valueToCheckIfExists = value;
                                }

                                if (pkValueListForPkMustBeUsed.contains(valueToCheckIfExists)) {
                                    pkValueListForPkMustBeUsed.removeIf(x -> Objects.equals(x, valueToCheckIfExists));
                                }
                            } else {
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
                    } else {
                        if (!pkMustBeUsed) {
                            recordIds.add(fkWithOptionalRS.getString(PARQUET_RECORD_ID_COLUMN_HEADER));
                        }
                    }
                }
            }
            if(pkMustBeUsed && pkValueListForPkMustBeUsed.size() > 0){
                recordIds.add(PK_NOT_USED);
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
        query.append(" limit " + maxErrors);
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
            query.append(" limit " + maxErrors);
            List<String> rs = dremioJdbcTemplate.queryForList(isDoubleReferQuery.toString(), String.class);
            if (rs.size()>0) {
                recordIds.add(COMISSION);
            }
        }
        return recordIds;
    }

 /**
     * Resolves the conditional field value into a list of individual keys.
     * If the master conditional field has pkHasMultipleValues=true, the raw value
     * is split by ";" (e.g. "R1;R2" -> ["R1", "R2"]).
     * If not, the raw value is returned as a single-element list.
     * Returns an empty list if the raw value is null.
     *
     * @param conditionalRawValue        the raw value read from the FK row's conditional column
     * @param masterConditionalFieldSchema the FieldSchema of the masterConditionalFieldId field,
     *                                   used to check pkHasMultipleValues
     * @return list of individual conditional keys to look up in pkWithOptionalMap
     */
    private List<String> getConditionalValues(String conditionalRawValue, FieldSchema masterConditionalFieldSchema) {
        if (conditionalRawValue == null) {
            return Collections.emptyList();
        }
        if (BooleanUtils.isTrue(masterConditionalFieldSchema != null
                ? masterConditionalFieldSchema.getPkHasMultipleValues() : null)) {
            return Arrays.stream(conditionalRawValue.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        return Collections.singletonList(conditionalRawValue);
    }

    /**
     * Merges the PK values from all matching conditional keys into a single list.
     * For each conditional key that exists in pkWithOptionalMap, its associated
     * PK values (comma-separated, quoted) are split and added to the result.
     * This produces the full set of valid PK values for a FK row whose conditional
     * field holds multiple values (OR logic: valid if found in any group).
     *
     * @param conditionalValues   list of individual conditional keys (e.g. ["R1", "R2"])
     * @param pkWithOptionalMap   map of conditionalKey -> quoted comma-separated PK values
     *                            built from the PK table query
     * @return merged list of all PK values across all matching conditional keys
     */
    private List<String> mergePksByConditionalValues(List<String> conditionalValues, Map<String, String> pkWithOptionalMap) {
        return conditionalValues.stream()
                .filter(cv -> pkWithOptionalMap.get(cv) != null)
                .flatMap(cv -> splitCommasRespectingQuotes(pkWithOptionalMap.get(cv)).stream())
                .collect(Collectors.toList());
    }

    /**
     * Handles the error-check case for a single FK value.
     * Adds the record to the error list if the FK value is not found in the
     * merged set of valid PKs across all conditional groups (SP1,SP2, SP5).
     *
     * @param value               single FK value being evaluated
     * @param pksByOptionalValue  merged list of valid PK values across all conditional groups
     * @param recordIds           list collecting record_ids of invalid FK rows
     * @param recordId            record_id of the current FK row
     */
    private void processErrorCheck(String value, List<String> pksByOptionalValue,
                                   List<String> recordIds, String recordId) {
        if (!pksByOptionalValue.contains("\"" + value + "\"") && !pksByOptionalValue.contains(value)) {
            if (!recordIds.contains(recordId)) {
                recordIds.add(recordId);
            }
        }
    }

    /**
     * R1 SP1
     * R2 SP2
     * R3 SP5
     * Handles FK validation for a row where the conditional field holds multiple semicolon-separated
     * values (e.g. "R1;R2;R3"). PK values from all matching conditional groups are merged (OR logic)
     * and each FK value i.g. SP1;SP2 is checked against the merged set(SP1,SP2, SP5) created from ("R1;R2;R3").
     *
     * @param conditionalValues          list of individual conditional keys resolved from the raw value (e.g. ["R1","R2"])
     * @param foreignKeyValue            the FK field value of the current row (may be semicolon-separated)
     * @param recordId                   the record_id of the current FK row
     * @param pkWithOptionalMap          map of conditionalKey -> quoted comma-separated PK values from the PK table
     * @param pkMapAux                   auxiliary copy of pkWithOptionalMap, updated as values are matched
     * @param pkValueListForPkMustBeUsed mutable list of PK values not yet referenced by any FK row
     * @param recordIds                  list collecting record_ids of invalid FK rows
     * @param fkFieldSchema              FK field schema, used to check ignoreCaseInLinks
     * @param pkMustBeUsed               whether every PK value must be referenced by at least one FK row
     */
    private void processMultiValueConditional(List<String> conditionalValues, String foreignKeyValue,
                                              String recordId, Map<String, String> pkWithOptionalMap, Map<String, String> pkMapAux,
                                              List<String> pkValueListForPkMustBeUsed, List<String> recordIds,
                                              FieldSchema fkFieldSchema, boolean pkMustBeUsed) {

        if (conditionalValues.stream().anyMatch(cv -> pkWithOptionalMap.get(cv) != null)) {
            List<String> pksByOptionalValue = mergePksByConditionalValues(conditionalValues, pkWithOptionalMap);
            List<String> fksByOptionalValue = Arrays.asList(foreignKeyValue.split(";"));

            if(BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())) {
                //FK_MULTIPLE_WRONG_IGNORE_CASE_LINK
                //set all items of pk and fk lists to lowercase
                pksByOptionalValue = pksByOptionalValue.stream().map(String::toLowerCase).collect(Collectors.toList());
                fksByOptionalValue = fksByOptionalValue.stream().map(String::toLowerCase).collect(Collectors.toList());
            }
            pksByOptionalValue.replaceAll(String::trim);
            fksByOptionalValue.replaceAll(String::trim);

            for (String value : fksByOptionalValue) {
                if(pkMustBeUsed){
                    String valueToCheckIfExists;
                    if (BooleanUtils.isTrue(fkFieldSchema.getIgnoreCaseInLinks())) {
                        valueToCheckIfExists = value.toLowerCase();
                    }
                    else{
                        valueToCheckIfExists = value;
                    }
                    if(pkValueListForPkMustBeUsed.contains(valueToCheckIfExists)){
                        pkValueListForPkMustBeUsed.removeIf(x -> Objects.equals(x, valueToCheckIfExists));
                    }
                }
                else{
                    processErrorCheck(value, pksByOptionalValue, recordIds, recordId);
                }
            }
        }
        else{
            if(!pkMustBeUsed){
                recordIds.add(recordId);
            }
        }
    }
}


















