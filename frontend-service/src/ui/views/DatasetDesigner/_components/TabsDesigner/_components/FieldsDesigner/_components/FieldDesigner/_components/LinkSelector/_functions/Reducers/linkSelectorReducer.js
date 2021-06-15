export const linkSelectorReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_LINK':
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
      return {
        ...state,
        pkLinkedTableLabel: payload.label,
        pkLinkedTableConditional: payload.conditional
      };
    case 'SET_LINKED_TABLE_LABEL':
      return {
        ...state,
        pkLinkedTableLabel: payload
      };
    case 'SET_LINKED_TABLE_CONDITIONAL':
      return {
        ...state,
        pkLinkedTableConditional: payload
      };
    case 'SET_MASTER_TABLE_CONDITIONAL':
      return {
        ...state,
        pkMasterTableConditional: payload
      };

    case 'SET_REFERENCE_DATAFLOW':
      return {
        ...state,
        selectedReferenceDataflow: payload
      };
    default:
      return state;
  }
};
