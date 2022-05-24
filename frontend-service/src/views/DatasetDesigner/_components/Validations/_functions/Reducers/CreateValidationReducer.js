import isNil from 'lodash/isNil';

export const createValidationReducerInitState = {
  candidateRule: {
    active: false,
    allExpressions: [],
    allExpressionsIf: [],
    allExpressionsThen: [],
    allGroups: [],
    description: '',
    errorLevel: undefined,
    errorMessage: '',
    expressions: [],
    expressionsIf: [],
    expressionsThen: [],
    expressionText: '',
    expressionType: '',
    field: undefined,
    relations: {
      isDoubleReferenced: false,
      originDatasetSchema: {},
      referencedDatasetSchema: {},
      referencedFields: [],
      referencedTable: {},
      referencedTables: [],
      links: [{ originField: '', referencedField: '' }]
    },
    shortCode: '',
    sqlSentence: '',
    sqlSentenceCost: 0,
    table: undefined
  },
  areRulesDisabled: true,
  areRulesDisabledIf: true,
  areRulesDisabledThen: true,
  datasetSchema: {},
  datasetSchemas: [],
  expressionText: '',
  groupCandidate: [],
  groupCandidateIf: [],
  groupCandidateThen: [],
  groupExpressionsActive: 0,
  groupExpressionsIfActive: 0,
  groupExpressionsThenActive: 0,
  isRuleAddingDisabled: true,
  isRuleAddingDisabledIf: true,
  isRuleAddingDisabledThen: true,
  isValidationCreationDisabled: true,
  schemaTables: [],
  ruleType: ''
};
export const createValidationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_FORM_FIELD':
      return { ...state, candidateRule: { ...state.candidateRule, [payload.key]: payload.value } };

    case 'SET_RULE_TYPE':
      return { ...state, ruleType: payload };

    case 'SET_FIELD_AND_FIELD_TYPE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          [payload.key]: payload.value,
          fieldType: payload.fieldType,
          expressionType: null
        }
      };

    case 'SET_TABLE_ID_FIELD_ID_AND_FIELD_TYPE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          table: payload.table,
          field: { code: payload.field },
          fieldType: payload.fieldType
        }
      };

    case 'SET_TABLES':
      return { ...state, schemaTables: payload };

    case 'SET_FIELDS':
      return {
        ...state,
        tableFields: payload.tableNonSqlFields,
        tableSqlFields: payload.tableSqlFields,
        candidateRule: { ...state.candidateRule, field: null }
      };

    case 'UPDATE_RULES':
      return { ...state, candidateRule: { ...state.candidateRule, allExpressions: payload } };

    case 'UPDATE_IF_RULES':
      return { ...state, candidateRule: { ...state.candidateRule, allExpressionsIf: payload } };

    case 'UPDATE_THEN_RULES':
      return { ...state, candidateRule: { ...state.candidateRule, allExpressionsThen: payload } };

    case 'SET_ARE_RULES_DISABLED':
      return { ...state, areRulesDisabled: payload, areRulesDisabledIf: payload, areRulesDisabledThen: payload };

    case 'SET_ARE_RULES_DISABLED_IF':
      return { ...state, areRulesDisabledIf: payload };

    case 'SET_ARE_RULES_DISABLED_THEN':
      return { ...state, areRulesDisabledThen: payload };

    case 'SET_IS_VALIDATION_ADDING_DISABLED':
      return { ...state, isRuleAddingDisabled: payload };

    case 'SET_IS_VALIDATION_ADDING_DISABLED_IF':
      return { ...state, isRuleAddingDisabledIf: payload };

    case 'SET_IS_VALIDATION_ADDING_DISABLED_THEN':
      return { ...state, isRuleAddingDisabledThen: payload };

    case 'SET_IS_VALIDATION_CREATION_DISABLED':
      return { ...state, isValidationCreationDisabled: payload };

    case 'ADD_EMPTY_RULE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expressions: [...state.candidateRule.expressions, payload],
          allExpressions: [...state.candidateRule.allExpressions, payload]
        }
      };

    case 'ADD_EMPTY_IF_RULE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expressionsIf: [...state.candidateRule.expressionsIf, payload],
          allExpressionsIf: [...state.candidateRule.allExpressionsIf, payload]
        }
      };

    case 'ADD_EMPTY_THEN_RULE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expressionsThen: [...state.candidateRule.expressionsThen, payload],
          allExpressionsThen: [...state.candidateRule.allExpressionsThen, payload]
        }
      };

    case 'DELETE_RULE':
      return { ...state, candidateRule: { ...state.candidateRule, allExpressions: payload } };

    case 'UPDATE_EXPRESSIONS_TREE':
      return { ...state, candidateRule: { ...state.candidateRule, expressions: payload } };

    case 'UPDATE_EXPRESSIONS_IF_TREE':
      return { ...state, candidateRule: { ...state.candidateRule, expressionsIf: payload } };

    case 'UPDATE_EXPRESSIONS_THEN_TREE':
      return { ...state, candidateRule: { ...state.candidateRule, expressionsThen: payload } };

    case 'GROUP_EXPRESSIONS':
      return {
        ...state,
        groupExpressionsActive: 0,
        groupCandidate: [],
        candidateRule: {
          ...state.candidateRule,
          expressions: payload.expressions,
          allExpressions: payload.allExpressions
        }
      };

    case 'GROUP_EXPRESSIONS_IF':
      return {
        ...state,
        groupExpressionsIfActive: 0,
        groupCandidateIf: [],
        candidateRule: {
          ...state.candidateRule,
          expressionsIf: payload.expressionsIf,
          allExpressionsIf: payload.allExpressionsIf
        }
      };

    case 'GROUP_EXPRESSIONS_THEN':
      return {
        ...state,
        groupExpressionsThenActive: 0,
        groupCandidateThen: [],
        candidateRule: {
          ...state.candidateRule,
          expressionsThen: payload.expressionsThen,
          allExpressions: payload.allExpressionsThen
        }
      };

    case 'GROUP_RULES_ACTIVATOR':
      return {
        ...state,
        groupExpressionsActive: state.groupExpressionsActive + payload.groupExpressionsActive,
        groupCandidate: payload.groupCandidate,
        candidateRule: { ...state.candidateRule, allExpressions: payload.allExpressions }
      };

    case 'GROUP_IF_RULES_ACTIVATOR':
      return {
        ...state,
        groupExpressionsIfActive: state.groupExpressionsIfActive + payload.groupExpressionsIfActive,
        groupCandidateIf: payload.groupCandidateIf,
        candidateRule: { ...state.candidateRule, allExpressionsIf: payload.allExpressionsIf }
      };

    case 'GROUP_THEN_RULES_ACTIVATOR':
      return {
        ...state,
        groupExpressionsThenActive: state.groupExpressionsThenActive + payload.groupExpressionsThenActive,
        groupCandidateThen: payload.groupCandidateThen,
        candidateRule: { ...state.candidateRule, allExpressionsThen: payload.allExpressionsThen }
      };

    case 'SET_EXPRESSIONS_STRING':
      return { ...state, expressionText: payload };

    case 'INIT_FORM':
      return {
        ...state,
        candidateRule: payload.candidateRule,
        datasetSchemas: payload.datasetSchemas,
        schemaTables: payload.tables
      };

    case 'RESET_CREATION_FORM':
      return { ...state, candidateRule: { ...payload }, tableFields: [] };

    case 'UPDATE_EXPRESSIONS':
      return { ...state, candidateRule: { ...state.candidateRule, allExpressions: payload } };

    case 'POPULATE_CREATE_FORM':
      const rowOptions = {};

      if (isNil(payload.sqlSentence)) {
        if (payload.expressionsIf && payload.expressionsIf.length > 0) {
          rowOptions.expressionType = 'ifThenClause';
          rowOptions.expressionsIf = payload.expressionsIf;
          rowOptions.allExpressionsIf = payload.allExpressionsIf;
          rowOptions.expressionsThen = payload.expressionsThen;
          rowOptions.allExpressionsThen = payload.allExpressionsThen;
        }

        if (payload.entityType === 'TABLE') {
          rowOptions.expressionType = 'fieldRelations';
        }

        if (payload.entityType === 'RECORD' && payload.expressions.length > 0) {
          rowOptions.expressionType = 'fieldComparison';
        }

        if (payload.entityType === 'FIELD' && payload.expressions.length > 0) {
          rowOptions.expressionType = 'fieldTab';
        }
      } else {
        rowOptions.expressionType = 'sqlSentence';
      }

      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          ...rowOptions,
          active: payload.enabled,
          allExpressions: payload.allExpressions,
          automatic: payload.automatic,
          description: payload.description,
          errorLevel: { label: payload.levelError, value: payload.levelError },
          errorMessage: payload.message,
          expressions: payload.expressions,
          id: payload.id,
          name: payload.name,
          relations: payload.relations,
          ruleType: payload.entityType,
          shortCode: payload.shortCode,
          sqlError: payload.sqlError,
          sqlSentence: payload.sqlSentence,
          sqlSentenceCost: payload.sqlSentenceCost,
          tableFields: isNil(payload.sqlSentence) && !isNil(payload.relations) ? payload.relations.tableFields : null
        }
      };

    case 'ON_EXPRESSION_TYPE_TOGGLE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expressionType: payload,
          sqlSentence: payload !== 'sqlSentence' ? null : state.candidateRule.sqlSentence
        }
      };

    case 'SET_REFERENCED_TABLES':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          relations: {
            ...state.candidateRule.relations,
            referencedDatasetSchema: payload.candidateRule.relations.referencedDatasetSchema,
            referencedTables: payload.candidateRule.relations.referencedTables,
            links: state.candidateRule.relations.links.map(link => {
              return { linkId: link.linkId, originField: link.originField, referencedField: '' };
            })
          }
        }
      };

    case 'SET_REFERENCED_FIELDS':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          relations: {
            ...state.candidateRule.relations,
            referencedFields: payload.referencedFields,
            referencedTable: payload.referencedTable
          }
        }
      };

    case 'UPDATE_LINKS':
      return {
        ...state,
        candidateRule: { ...state.candidateRule, relations: { ...state.candidateRule.relations, links: payload } }
      };

    case 'ADD_EMPTY_RELATION':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          relations: {
            ...state.candidateRule.relations,
            links: [...state.candidateRule.relations.links, payload]
          }
        }
      };

    case 'UPDATE_IS_DOUBLE_REFERENCED':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          relations: { ...state.candidateRule.relations, isDoubleReferenced: payload }
        }
      };

    case 'SET_FORM_FIELD_RELATION':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          [payload.key]: payload.value,
          relations: {
            ...state.candidateRule.relations,
            links:
              state.candidateRule.expressionType !== 'sqlSentence'
                ? state.candidateRule.relations.links.map(link => {
                    return { linkId: link.linkId, originField: '', referencedField: link.referencedField };
                  })
                : null
          }
        }
      };

    default:
      return state;
  }
};
