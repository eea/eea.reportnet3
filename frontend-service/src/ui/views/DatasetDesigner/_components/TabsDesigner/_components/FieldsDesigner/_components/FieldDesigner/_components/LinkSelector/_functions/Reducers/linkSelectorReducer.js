export const linkSelectorReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_LINK':
      return { ...state, link: payload };
    case 'SET_LINKED_AND_MASTER_FIELDS':
      return {
        ...state,
        linkedTableLabel: {},
        masterTableConditional: {},
        linkedTableConditional: {},
        linkedTableFields: payload.linkedFields,
        masterTableFields: payload.masterFields
      };
    case 'SET_LINKED_TABLE_LABEL':
      return {
        ...state,
        linkedTableLabel: payload
        // link: {
        //   ...state.link,
        //   referencedField: { ...state.link.referencedField, linkedTableLabel: payload.fieldSchemaId }
        // }
      };
    case 'SET_LINKED_TABLE_CONDITIONAL':
      return {
        ...state,
        linkedTableConditional: payload
        // link: {
        //   ...state.link,
        //   referencedField: { ...state.link.referencedField, linkedTableConditional: payload.fieldSchemaId }
        // }
      };
    case 'SET_MASTER_TABLE_CONDITIONAL':
      return {
        ...state,
        masterTableConditional: payload
        // link: {
        //   ...state.link,
        //   referencedField: { ...state.link.referencedField, masterTableConditional: payload.fieldSchemaId }
        // }
      };
    default:
      return state;
  }
};
