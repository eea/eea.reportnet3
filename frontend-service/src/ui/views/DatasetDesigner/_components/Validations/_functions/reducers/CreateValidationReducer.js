export const createValidationReducerInitState = {
  candidateRule: {
    active: true,
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
    expressionType: '',
    field: undefined,
    shortCode: '',
    table: undefined
  },
  areRulesDisabled: true,
  areRulesDisabledIf: true,
  areRulesDisabledThen: true,
  datasetSchema: {},
  groupCandidate: [],
  groupExpressionsActive: 0,
  isRuleAddingDisabled: true,
  isRuleAddingDisabledIf: true,
  isRuleAddingDisabledThen: true,
  isValidationCreationDisabled: true,
  schemaTables: [],
  validationRuleString: '',
  ruleType: ''
};
export const createValidationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_FORM_FIELD':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          [payload.key]: payload.value
        }
      };

    case 'SET_RULE_TYPE':
      return {
        ...state,
        ruleType: payload
      };

    case 'SET_FIELD_AND_FIELD_TYPE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          [payload.key]: payload.value,
          fieldType: payload.fieldType
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
      return {
        ...state,
        schemaTables: payload
      };

    case 'SET_FIELDS':
      return {
        ...state,
        tableFields: payload,
        candidateRule: {
          ...state.candidateRule,
          field: null
        }
      };

    case 'UPDATE_RULES':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpressions: payload
        }
      };

    case 'UPDATE_IF_RULES':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpressionsIf: payload
        }
      };

    case 'UPDATE_THEN_RULES':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpressionsThen: payload
        }
      };

    case 'SET_ARE_RULES_DISABLED':
      return {
        ...state,
        areRulesDisabled: payload,
        areRulesDisabledIf: payload,
        areRulesDisabledThen: payload
      };

    case 'SET_ARE_RULES_DISABLED_IF':
      return {
        ...state,
        areRulesDisabledIf: payload
      };

    case 'SET_ARE_RULES_DISABLED_THEN':
      return {
        ...state,
        areRulesDisabledThen: payload
      };

    case 'SET_IS_VALIDATION_ADDING_DISABLED':
      return {
        ...state,
        isRuleAddingDisabled: payload
      };

    case 'SET_IS_VALIDATION_ADDING_DISABLED_IF':
      return {
        ...state,
        isRuleAddingDisabledIf: payload
      };

    case 'SET_IS_VALIDATION_ADDING_DISABLED_THEN':
      return {
        ...state,
        isRuleAddingDisabledThen: payload
      };

    case 'SET_IS_VALIDATION_CREATION_DISABLED':
      return {
        ...state,
        isValidationCreationDisabled: payload
      };

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
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpressions: payload
        }
      };

    case 'UPDATE_EXPRESSIONS_TREE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expressions: payload
        }
      };
    case 'UPDATE_EXPRESSIONS_IF_TREE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expressionsIf: payload
        }
      };
    case 'UPDATE_EXPRESSIONS_THEN_TREE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expressionsThen: payload
        }
      };

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

    case 'GROUP_RULES_ACTIVATOR':
      return {
        ...state,
        groupExpressionsActive: state.groupExpressionsActive + payload.groupExpressionsActive,
        groupCandidate: payload.groupCandidate,
        candidateRule: {
          ...state.candidateRule,
          allExpressions: payload.allExpressions
        }
      };

    case 'GROUP_IF_RULES_ACTIVATOR':
      return {
        ...state,
        groupExpressionsActive: state.groupExpressionsActive + payload.groupExpressionsActive,
        groupCandidate: payload.groupCandidate,
        candidateRule: {
          ...state.candidateRule,
          allExpressionsIf: payload.allExpressionsIf
        }
      };

    case 'GROUP_THEN_RULES_ACTIVATOR':
      return {
        ...state,
        groupExpressionsActive: state.groupExpressionsActive + payload.groupExpressionsActive,
        groupCandidate: payload.groupCandidate,
        candidateRule: {
          ...state.candidateRule,
          allExpressionsThen: payload.allExpressionsThen
        }
      };

    case 'SET_EXPRESSIONS_STRING':
      return {
        ...state,
        validationRuleString: payload
      };

    case 'INIT_FORM':
      return {
        ...state,
        candidateRule: payload.candidateRule,
        schemaTables: payload.tables
      };

    case 'RESET_CREATION_FORM':
      return {
        ...state,
        candidateRule: { ...payload },
        tableFields: []
      };
    //Not in use?
    case 'UPDATE_EXPRESSIONS':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpressions: payload
        }
      };

    case 'POPULATE_CREATE_FORM':

      const rowOptions = {};
      if (payload.expressionsIf && payload.expressionsIf.length > 0) {
        console.log('payload', payload);

        rowOptions.expressionType = 'ifThenClause';
        rowOptions.expressionsIf = payload.expressionsIf;
        rowOptions.allExpressionsIf = payload.allExpressionsIf;
        rowOptions.expressionsThen = payload.expressionsThen;
        rowOptions.allExpressionsThen = payload.allExpressionsThen;
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
          shortCode: payload.shortCode
        }
      };

    case 'ON_EXPRESSION_TYPE_TOGGLE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expressionType: payload
        }
      };
    default:
      return state;
  }
};
