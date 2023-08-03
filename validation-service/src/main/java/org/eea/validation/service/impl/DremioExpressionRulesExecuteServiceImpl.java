package org.eea.validation.service.impl;

import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.annotation.ImportDataLakeCommons;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController.DatasetSchemaControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.rule.RuleVO;
import org.eea.validation.persistence.data.domain.FieldValue;
import org.eea.validation.persistence.data.domain.RecordValue;
import org.eea.validation.service.DremioRulesExecuteService;
import org.eea.validation.service.DremioRulesService;
import org.eea.validation.service.RulesService;
import org.eea.validation.util.RuleOperators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;
import static org.eea.utils.LiteralConstants.S3_VALIDATION;

@ImportDataLakeCommons
@Service
public class DremioExpressionRulesExecuteServiceImpl implements DremioRulesExecuteService {

    private JdbcTemplate dremioJdbcTemplate;
    private S3Service s3Service;
    private RulesService rulesService;
    private DatasetSchemaControllerZuul datasetSchemaControllerZuul;
    private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;
    private DremioRulesService dremioRulesService;
    private RepresentativeControllerZuul representativeControllerZuul;

    private static final Logger LOG = LoggerFactory.getLogger(DremioExpressionRulesExecuteServiceImpl.class);

    @Autowired
    public DremioExpressionRulesExecuteServiceImpl(@Qualifier("dremioJdbcTemplate") JdbcTemplate dremioJdbcTemplate, S3Service s3Service, RulesService rulesService, DatasetSchemaControllerZuul datasetSchemaControllerZuul,
                                                   DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul, DremioRulesService dremioRulesService, RepresentativeControllerZuul representativeControllerZuul) {
        this.dremioJdbcTemplate = dremioJdbcTemplate;
        this.s3Service = s3Service;
        this.rulesService = rulesService;
        this.datasetSchemaControllerZuul = datasetSchemaControllerZuul;
        this.dataSetMetabaseControllerZuul = dataSetMetabaseControllerZuul;
        this.dremioRulesService = dremioRulesService;
        this.representativeControllerZuul = representativeControllerZuul;
    }

    @Override
    public void execute(Long dataflowId, Long datasetId, String datasetSchemaId, String tableName, String tableSchemaId, String ruleId, Long dataProviderId, Long taskId) throws Exception {
        try {
            S3PathResolver dataTableResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, tableName);
            S3PathResolver validationResolver = new S3PathResolver(dataflowId, dataProviderId != null ? dataProviderId : 0, datasetId, S3_VALIDATION);
            DataSetMetabaseVO dataset = dataSetMetabaseControllerZuul.findDatasetMetabaseById(datasetId);
            String providerCode = null;
            if (dataset.getDataProviderId()!=null) {
                DataProviderVO provider = representativeControllerZuul.findDataProviderById(dataset.getDataProviderId());
                providerCode = provider.getCode();
            }
            StringBuilder query = new StringBuilder();
            RuleVO ruleVO = rulesService.findRule(datasetSchemaId, ruleId);
            int parameterStartIndex = ruleVO.getWhenConditionMethod().indexOf("(");
            int parameterEndIndex = ruleVO.getWhenConditionMethod().indexOf(")");
            int pmEndSecOccurrenceIndex = ruleVO.getWhenConditionMethod().indexOf(")", parameterEndIndex+1);
            int methodStartIndex = ruleVO.getWhenConditionMethod().indexOf(".");
            String ruleMethodName = ruleVO.getWhenConditionMethod().substring(methodStartIndex+1, parameterStartIndex);
            String parameterString = ruleVO.getWhenConditionMethod().substring(parameterStartIndex +1, pmEndSecOccurrenceIndex!=-1 ? pmEndSecOccurrenceIndex : parameterEndIndex);
            parameterString = parameterString.replace("\"","");
            String[] params = parameterString.split(",");
            params = Arrays.stream(params).map(p -> p.trim()).toArray(String[]::new);
            List<String> parameters = new ArrayList<>();
            for (int i=0; i<params.length;) {
                if (params[i].contains("RuleOperators")) {
                    parameters.add(params[i]+","+params[i+1]);
                    i+=2;
                } else {
                    parameters.add(params[i]);
                    i++;
                }
            }

            LinkedHashMap<String, List<String>> parameterMethods = new LinkedHashMap<>();
            parameters.forEach(parameter -> {
                if (parameter.contains("RuleOperators")) {
                    //RuleOperators method in when condition contains nested RuleOperators methods
                    // e.g. RuleOperators.recordIfThen(RuleOperators.recordNumberGreaterThanRecord("642d6e2cde03c136f869180e", "642d6e25de03c136f869180d"), RuleOperators.recordNumberGreaterThan("643fecc9b6f2e4cb5f9cb04b", 50))
                    int methodStartIdx = parameter.indexOf(".");
                    int startIndex = parameter.indexOf("(");
                    int endIndex = parameter.indexOf(")");
                    String parameterMethodName = parameter.substring(methodStartIdx+1, startIndex);
                    String internalParameters;
                    if (endIndex!=-1) {
                        internalParameters = parameter.substring(startIndex+1, endIndex);
                    } else {
                        internalParameters = parameter.substring(startIndex+1);
                    }
                    //put in the map the method name as the key and the parameters of the internal RuleOperators methods as value
                    List<String> internals = new ArrayList<>(Arrays.asList(internalParameters.split(",")));
                    internals = internals.stream().map(i -> i.trim()).collect(Collectors.toList());
                    List<String> existingInternals = parameterMethods.get(parameterMethodName);
                    if (existingInternals!=null) { //whenCondition contains the same method twice e.g. RuleOperators.fieldOr(RuleOperators.fieldNumberEquals(value, 13), RuleOperators.fieldNumberEquals(value, 14))
                        internals.forEach(i -> existingInternals.add(i));
                        internals = existingInternals;
                    }
                    parameterMethods.put(parameterMethodName, internals);
                }
            });

            String fieldName;
            if (ruleVO.getType().equals(EntityTypeEnum.FIELD)) {
                fieldName = datasetSchemaControllerZuul.getFieldName(datasetSchemaId, tableSchemaId, parameters, ruleVO.getReferenceId(), ruleVO.getReferenceFieldSchemaPKId());
            } else {
                fieldName = "";
            }

            Map<String, List<String>> headerNames = new HashMap<>();  //map of method as key and list of fields that exist as parameters in method as values
            query.append("select record_id");
            if (!fieldName.equals("")) {
                query.append(",").append(fieldName);
            }
            if (parameterMethods.size()>0) { //whenCondition contains nested RuleOperators methods
                parameterMethods.entrySet().forEach(e -> e.getValue().forEach(v -> {
                    createHeaderNames(v, e.getKey(), headerNames, fieldName, datasetSchemaId, query);
                }));
            } else {
                int idx = ruleMethodName.indexOf("Record");
                List<String> hNames = new ArrayList<>();
                if (idx!=-1) {
                    //record type
                    //RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("64ac0c0de5f082645bab2f07", "64ac0c1ae5f082645bab2f09")
                    parameters.forEach(p -> {
                        FieldSchemaVO fieldSchema = datasetSchemaControllerZuul.getFieldSchema(datasetSchemaId, p);
                        hNames.add(fieldSchema.getName());
                        query.append(",").append(fieldSchema.getName());
                    });
                    headerNames.put(ruleMethodName, hNames);
                }
            }

            query.append(" from ").append(s3Service.getTableAsFolderQueryPath(dataTableResolver, S3_TABLE_AS_FOLDER_QUERY_PATH));
            SqlRowSet rs = dremioJdbcTemplate.queryForRowSet(query.toString());
            int count = 0;
            boolean createRuleFolder = false;
            StringBuilder validationQuery = dremioRulesService.getS3RuleFolderQueryBuilder(datasetId, tableName, dataTableResolver, validationResolver, ruleVO, fieldName);

            Class<?> cls = Class.forName("org.eea.validation.util.RuleOperators");
            Method factoryMethod = cls.getDeclaredMethod("getInstance");
            Object object = factoryMethod.invoke(null, null);

            while (rs.next()) {
                boolean isValid = false;
                Method method = dremioRulesService.getRuleMethodFromClass(ruleMethodName, cls);
                List<Boolean> internalResults = new ArrayList<>();
                boolean record = false;
                for (Map.Entry entry : parameterMethods.entrySet()) {
                    List<String> values = (List<String>) entry.getValue();
                    record = isRecord(((String) entry.getKey()));
                    Method md = dremioRulesService.getRuleMethodFromClass((String) entry.getKey(), cls);
                    if (values.size()>2) { //whenCondition contains the same method twice
                        for (int i = 0; i <= 2;) {
                            List<String> newValues = new ArrayList<>();
                            if (values.size() % 2 == 0) {
                                newValues.add(values.get(i));
                                newValues.add(values.get(i+1));
                                i+=2;
                            } else {
                                if (i==2) {
                                    break;
                                }
                                newValues.add(values.get(i));
                                i++;
                            }
                            boolean result = getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record, (String) entry.getKey(), md, newValues, providerCode);
                            internalResults.add(result);
                        }
                    } else {
                        boolean result = getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record, (String) entry.getKey(), md, values, providerCode);
                        internalResults.add(result);
                    }
                }
                if (internalResults.size()>0) {
                    switch (internalResults.size()) {
                        case 1:
                            isValid = (boolean) method.invoke(object, internalResults.get(0));
                            break;
                        case 2:
                            isValid = (boolean) method.invoke(object, internalResults.get(0), internalResults.get(1));
                            break;
                    }
                } else {
                    record = isRecord(method.getName());
                    isValid = getMethodExecutionResult(ruleVO, fieldName, headerNames, rs, object, record, method.getName(), method, parameters, providerCode);
                }
                if (!isValid) {
                    if (count != 0) {
                        validationQuery.append(",'");
                    }
                    validationQuery.append(rs.getString("record_id")).append("'");
                    if (count == 0) {
                        count++;
                        createRuleFolder = true;
                    }
                }
            }
            if (createRuleFolder) {
                validationQuery.append("))");
                dremioJdbcTemplate.execute(validationQuery.toString());
            }
        } catch (Exception e) {
            LOG.error("Error creating validation folder for ruleId {}, datasetId {} and tableName {}", ruleId, datasetId, tableName);
            throw e;
        }
    }

    /**
     * Creates headerNames map
     * @param parameter
     * @param methodName
     * @param headerNames
     * @param fieldName
     * @param datasetSchemaId
     * @param query
     */
    private void createHeaderNames(String parameter, String methodName, Map<String, List<String>> headerNames, String fieldName, String datasetSchemaId, StringBuilder query) {
        parameter = parameter.trim();
        if (parameter.equals("value")) {
            List<String> list = headerNames.get(methodName);
            if (list!=null && !list.contains(fieldName)) {
                list.add(fieldName);
            } else {
                list = new ArrayList<>();
                list.add(fieldName);
            }
            headerNames.put(methodName, list);
        } else if (!isNumeric(parameter)) {
            FieldSchemaVO fieldSchema = datasetSchemaControllerZuul.getFieldSchema(datasetSchemaId, parameter);
            List<String> list = headerNames.get(methodName);
            if (list!=null) {
                list.add(fieldSchema.getName());
            } else {
                list = new ArrayList<>();
                list.add(fieldSchema.getName());
            }
            headerNames.put(methodName, list);
            query.append(",").append(fieldSchema.getName());
        }
    }

    /**
     * Checks if rule method name contains "Record" string
     * e.g. RuleOperators.recordStringLengthGreaterThanOrEqualsThanRecord("64ac0c0de5f082645bab2f07", "64ac0c1ae5f082645bab2f09")
     * @param value
     * @return
     */
    private static boolean isRecord(String value) {
        boolean record = false;
        int idx = value.indexOf("Record");
        if (idx!=-1) {
            //record type
            record = true;
        }
        return record;
    }

    /**
     * Executes method
     * @param ruleVO
     * @param fieldName
     * @param headerNames
     * @param rs
     * @param object
     * @param record
     * @param methodName
     * @param md
     * @param pm
     * @param providerCode
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Boolean getMethodExecutionResult(RuleVO ruleVO, String fieldName, Map<String, List<String>> headerNames, SqlRowSet rs, Object object,
                                             boolean record, String methodName, Method md, List<String> pm, String providerCode) throws IllegalAccessException, InvocationTargetException {
        List<FieldValue> fields = new ArrayList<>();
        RecordValue recordValue = new RecordValue();
        recordValue.setDataProviderCode(providerCode);
        boolean result = false;
        List<String> intHeaders = headerNames.get(md.getName());
        if (pm.size()==1) {
            FieldValue fieldValue = new FieldValue();
            fieldValue.setValue(rs.getString(fieldName));
            fieldValue.setIdFieldSchema(ruleVO.getReferenceId());
            fields.add(fieldValue);
            recordValue.setFields(fields);
            fieldValue.setRecord(recordValue);
            RuleOperators.setEntity(fieldValue);
            RuleOperators.setEntity(recordValue);
            result = (Boolean) md.invoke(object, pm.get(0).contains("value") ? rs.getString(fieldName) : pm.get(0));
        } else if (pm.size()==2) {
            if (record) {
                FieldValue fieldValue1 = new FieldValue();
                fieldValue1.setValue(rs.getString(intHeaders.get(0)));
                fieldValue1.setIdFieldSchema(pm.get(0));
                FieldValue fieldValue2 = new FieldValue();
                fieldValue2.setValue(rs.getString(intHeaders.get(1)));
                fieldValue2.setIdFieldSchema(pm.get(1));
                fields.add(fieldValue1);
                fields.add(fieldValue2);
                recordValue.setFields(fields);
                RuleOperators.setEntity(recordValue);
                result = (Boolean) md.invoke(object, pm.get(0), pm.get(1));
            } else {
                String firstValue;
                FieldValue fieldValue = new FieldValue();
                if (pm.get(0).contains("value")) {
                    firstValue = rs.getString(fieldName);
                    fieldValue.setIdFieldSchema(ruleVO.getReferenceId());
                    fieldValue.setValue(firstValue);
                } else {
                    firstValue = pm.get(0);
                    fieldValue.setIdFieldSchema(pm.get(0));
                    fieldValue.setValue(rs.getString(intHeaders.get(0)));
                }
                fields.add(fieldValue);
                recordValue.setFields(fields);
                fieldValue.setRecord(recordValue);
                RuleOperators.setEntity(fieldValue);
                RuleOperators.setEntity(recordValue);
                if (methodName.contains("Number")) {
                    result = (Boolean) md.invoke(object, firstValue, Double.parseDouble(pm.get(1)));
                } else if (methodName.contains("Length")) {
                    result = (Boolean) md.invoke(object, firstValue, Integer.parseInt(pm.get(1)));
                } else if (methodName.contains("Day") || methodName.contains("Month") || methodName.contains("Year")) {
                    result = (Boolean) md.invoke(object, firstValue, Long.parseLong(pm.get(1)));
                } else {
                    result = (Boolean) md.invoke(object, firstValue, pm.get(1));
                }
            }
        }
        return result;
    }

    /**
     * Checks if value is numeric
     * @param value
     * @return
     */
    private boolean isNumeric(String value) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (value == null) {
            return false;
        }
        return pattern.matcher(value.trim()).matches();
    }
}
