export const createValidationReducerInitState = {
  candidateRule: {
    table: undefined,
    field: undefined,
    shortCode: undefined,
    description: undefined,
    errorMessage: undefined,
    errorLevel: undefined,
    active: false,
    expresions: []
  },
  datasetSchema: {},
  schemaTables: [],
  errorLevels: [],
  areRulesDisabled: true,
  isRuleAddingDisabled: true,
  isValidationCreationDisabled: true,
  groupRulesActive: 0,
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
          expresions: payload
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
          expresions: [...state.candidateRule.expresions, payload]
        }
      };
    case 'DELETE_RULE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          expresions: payload
        }
      };
    case 'GROUP_RULES_ACTIVATOR':
      return {
        ...state,
        groupRulesActive: state.groupRulesActive + payload.groupRulesActive
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
    default:
      return state;
  }
};
