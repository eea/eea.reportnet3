export const createValidationReducerInitState = {
  candidateRule: {
    table: undefined,
    field: undefined,
    shortCode: '',
    description: '',
    errorMessage: '',
    errorLevel: undefined,
    active: false,
    expresions: [],
    allExpresions: [],
    allGroups: []
  },
  datasetSchema: {},
  schemaTables: [],
  errorLevels: [],
  areRulesDisabled: true,
  isRuleAddingDisabled: true,
  isValidationCreationDisabled: true,
  groupExpresionsActive: 0,
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
          allExpresions: payload
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
          expresions: [...state.candidateRule.expresions, payload],
          allExpresions: [...state.candidateRule.allExpresions, payload]
        }
      };
    case 'DELETE_RULE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpresions: payload
        }
      };
    case 'UPDATE_EXPRESIONS_TREE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expresions: payload
        }
      };
    case 'GROUP_EXPRESIONS':
      return {
        ...state,
        groupExpresionsActive: 0,
        candidateRule: {
          ...state.candidateRule,
          expresions: payload.expresions
        }
      };
    case 'GROUP_RULES_ACTIVATOR':
      return {
        ...state,
        groupExpresionsActive: state.groupExpresionsActive + payload.groupExpresionsActive,
        groupCandidate: payload.groupCandidate,
        candidateRule: {
          ...state.candidateRule,
          expresions: payload.expresions
        }
      };
    case 'SET_EXPRESIONS_STRING':
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
    case 'UPDATE_EXPRESIONS':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          allExpresions: payload
        }
      };
    default:
      return state;
  }
};
