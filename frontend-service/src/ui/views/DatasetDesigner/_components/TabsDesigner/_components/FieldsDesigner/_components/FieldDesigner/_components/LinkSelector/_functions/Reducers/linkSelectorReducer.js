export const linkSelectorReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_LINK':
      console.log({ payload });
      return { ...state, link: payload };
    case 'SET_LINKED_AND_MASTER_FIELDS':
      return {
        ...state,
        pkLinkedTableLabel: {},
        pkMasterTableConditional: {},
        pkLinkedTableConditional: {},
        linkedTableFields: payload.linkedFields,
        masterTableFields: payload.masterFields
      };
    case 'SET_LINKED_TABLE_FIELDS':
      console.log({ payload });
      return {
        ...state,
        pkLinkedTableLabel: payload.label,
        pkLinkedTableConditional: payload.conditional
      };
    case 'SET_LINKED_TABLE_LABEL':
      console.log(payload);
      return {
        ...state,
        pkLinkedTableLabel: payload
        // link: {
        //   ...state.link,
        //   referencedField: { ...state.link.referencedField, linkedTableLabel: payload.fieldSchemaId }
        // }
      };
    case 'SET_LINKED_TABLE_CONDITIONAL':
      return {
        ...state,
        pkLinkedTableConditional: payload
        // link: {
        //   ...state.link,
        //   referencedField: { ...state.link.referencedField, linkedTableConditional: payload.fieldSchemaId }
        // }
      };
    case 'SET_MASTER_TABLE_CONDITIONAL':
      return {
        ...state,
        pkMasterTableConditional: payload
        // link: {
        //   ...state.link,
        //   referencedField: { ...state.link.referencedField, masterTableConditional: payload.fieldSchemaId }
        // }
      };
    default:
      return state;
  }
};
