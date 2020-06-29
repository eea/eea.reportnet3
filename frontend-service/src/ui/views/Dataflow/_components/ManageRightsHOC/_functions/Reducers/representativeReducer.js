export const reducer = (state, { type, payload }) => {
  switch (type) {
    case 'REFRESH':
      return {
        ...state,
        refresher: !state.refresher
      };

    case 'CREATE_UNUSED_OPTIONS_LIST':
      const unusedDataProvidersOptions = state.allPossibleDataProviders.filter(dataProviderOption => {
        let result = true;
        for (let index = 0; index < state.representatives.length; index++) {
          if (state.representatives[index].dataProviderId === dataProviderOption.dataProviderId) {
            result = false;
          }
        }
        return result;
      });

      return { ...state, unusedDataProvidersOptions };

    case 'DELETE_REPRESENTATIVE':
      return {
        ...state,
        representatives: payload.updatedList,
        refresher: !state.refresher,
        isVisibleConfirmDeleteDialog: false
      };

    case 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID':
      return {
        ...state,
        allPossibleDataProviders: payload.responseAllDataProviders,
        allPossibleDataProvidersNoSelect: payload.providersNoSelect
      };

    case 'GET_PROVIDERS_TYPES_LIST':
      return { ...state, dataProvidersTypesList: payload.providerTypes };

    case 'MANAGE_ERRORS':
      return { ...state, representativesHaveError: payload.representativesHaveError };

    case 'HIDE_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: false,
        representativeIdToDelete: ''
      };

    case 'INITIAL_LOAD':
      return {
        ...state,
        representatives: payload.representatives,
        initialRepresentatives: payload.representativesByCopy,
        representativesHaveError: []
      };

    case 'ON_ACCOUNT_CHANGE':
      return {
        ...state,
        representatives: payload.representatives
      };

    case 'ON_PERMISSIONS_CHANGE':
      return {
        ...state,
        representatives: payload.representatives
      };

    case 'SHOW_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: true,
        representativeIdToDelete: payload.representativeId
      };

    default:
      return state;
  }
};
