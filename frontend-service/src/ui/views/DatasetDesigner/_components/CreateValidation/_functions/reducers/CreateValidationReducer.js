export const createValidationReducerInitState = {
  candidateRule: {
    table: undefined,
    field: undefined,
    shortCode: '',
    description: '',
    errorMessage: '',
    errorLevel: undefined,
    active: false,
    expressions: [],
    allExpressions: [],
    allGroups: []
  },
  datasetSchema: {},
  schemaTables: [],
  errorLevels: [],
  areRulesDisabled: true,
  isRuleAddingDisabled: true,
  isValidationCreationDisabled: true,
  groupExpressionsActive: 0,
  groupCandidate: []
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
    case 'SET_TABLES':
      return {
        ...state,
        schemaTables: payload
      };
    case 'SET_FIELDS':
      return {
        ...state,
        tableFields: payload
      };
    case 'UPDATE_RULES':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpressions: payload
        }
      };
    case 'SET_ARE_RULES_DISABLED':
      return {
        ...state,
        areRulesDisabled: payload
      };
    case 'SET_IS_VALIDATION_ADDING_DISABLED':
      return {
        ...state,
        isRuleAddingDisabled: payload
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
          expressions: payload.expressions
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
        schemaTables: payload.tables,
        errorLevels: payload.errorLevels,
        candidateRule: payload.candidateRule
      };
    case 'RESET_CREATION_FORM':
      return {
        ...state,
        tableFields: [],
        candidateRule: {
          ...payload
        }
      };
    case 'UPDATE_EXPRESSIONS':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpressions: payload
        }
      };
    default:
      return state;
  }
};
