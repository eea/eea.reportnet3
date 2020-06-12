export const createValidationReducerInitState = {
  candidateRule: {
    table: undefined,
    field: undefined,
    shortCode: '',
    description: '',
    errorMessage: '',
    errorLevel: undefined,
    active: true,
    expressions: [],
    allExpressions: [],
    allGroups: [],
    expressionType: '',
    relations: {
      isDoubleReferenced: false,
      originDatasetSchema: {},
      referencedDatasetSchema: {},
      referencedFields: [],
      referencedTable: {},
      referencedTables: [],
      links: [{ originField: '', referencedField: '' }]
    }
  },
  datasetSchemas: [],
  datasetSchema: {},
  schemaTables: [],
  validationRuleString: '',
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
    case 'SET_FIELD_AND_FIELD_TYPE':
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          [payload.key]: payload.value,
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
          allExpressions: payload.allExpressions
        }
      };
    case 'SET_EXPRESSIONS_STRING':
      return {
        ...state,
        validationRuleString: payload
      };
    case 'INIT_FORM':
      console.log({ payload });
      return {
        ...state,
        schemaTables: payload.tables,
        candidateRule: payload.candidateRule,
        datasetSchemas: payload.datasetSchemas
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
    case 'POPULATE_CREATE_FORM':
      console.log({ payload });
      return {
        ...state,
        candidateRule: {
          ...state.candidateRule,
          active: payload.enabled,
          allExpressions: payload.allExpressions,
          automatic: payload.automatic,
          description: payload.description,
          errorLevel: { label: payload.levelError, value: payload.levelError },
          errorMessage: payload.message,
          expressions: payload.expressions,
          id: payload.id,
          name: payload.name,
          shortCode: payload.shortCode,
          relations: payload.relations,
          tableFields: payload.relations.tableFields
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
        candidateRule: {
          ...state.candidateRule,
          relations: {
            ...state.candidateRule.relations,
            links: payload
          }
        }
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
          relations: {
            ...state.candidateRule.relations,
            isDoubleReferenced: payload
          }
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
            links: state.candidateRule.relations.links.map(link => {
              return { linkId: link.linkId, originField: '', referencedField: link.referencedField };
            })
          }
        }
      };
    default:
      return state;
  }
};
