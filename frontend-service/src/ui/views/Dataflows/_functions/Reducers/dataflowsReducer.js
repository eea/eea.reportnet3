export const dataflowsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'HAS_PERMISSION':
      return { ...state, isCustodian: payload.isCustodian, isNationalCoordinator: payload.isNationalCoordinator };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'MANAGE_DIALOGS':
      return { ...state, [payload.dialog]: payload.value, [payload.secondDialog]: payload.secondValue };

    default:
      return state;
  }
};
