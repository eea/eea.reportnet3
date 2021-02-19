import includes from 'lodash/includes';
import isNil from 'lodash/isNil';

import { Representative } from 'core/domain/model/Representative/Representative';

export const reducer = (state, { type, payload }) => {
  const emptyRepresentative = new Representative({ dataProviderId: '', providerAccount: '' });

  switch (type) {
    case 'REFRESH':
      return {
        ...state,
        refresher: !state.refresher,
        representativesHaveError: []
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
      const group = state.dataProvidersTypesList.filter(
        dataProviderType => dataProviderType.dataProviderGroupId === payload.response.group.dataProviderGroupId
      );

      if (!includes(state.representatives, emptyRepresentative)) {
        payload.response.representatives.push(emptyRepresentative);
      }

      const getSelectedProviderGroup = () => {
        let selectedGroup = null;
        if (isNil(state.selectedDataProviderGroup)) {
          selectedGroup = isNil(group[0]) ? null : group[0];
        } else {
          selectedGroup = state.selectedDataProviderGroup;
        }
        return selectedGroup;
      };
      return {
        ...state,
        representatives: payload.response.representatives,
        initialRepresentatives: payload.representativesByCopy,
        selectedDataProviderGroup: getSelectedProviderGroup(),
        representativeHaveError: []
      };

    case 'ON_ACCOUNT_CHANGE':
      return {
        ...state,
        representatives: payload.representatives,
        representativesHaveError: payload.representativesHaveError
      };

    case 'ON_PROVIDER_CHANGE':
      return {
        ...state,
        representatives: payload.representatives
      };

    case 'SELECT_PROVIDERS_TYPE':
      return {
        ...state,
        selectedDataProviderGroup: payload
      };

    case 'SET_IS_LOADING':
      return {
        ...state,
        isLoading: payload.isLoading
      };

    case 'SHOW_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: true,
        representativeIdToDelete: payload.representativeId
      };

    case 'ADD_NEW_LEAD_REPORTER':
      return {
        ...state,
        providerWithEmptyInput: payload.dataProviderId,
        representatives: payload.representatives
      };

    default:
      return state;
  }
};
