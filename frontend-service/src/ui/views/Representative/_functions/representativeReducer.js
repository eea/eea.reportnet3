export const representativeReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'LOAD_PERMISSIONS':
      return { ...state, hasWritePermissions: payload.hasWritePermissions, isCustodian: payload.isCustodian };

    case 'MANAGE_DIALOGS':
      return {
        ...state,
        [payload.dialog]: payload.value,
        [payload.secondDialog]: payload.secondValue,
        deleteInput: payload.deleteInput
      };

    default:
      return state;
  }
};
