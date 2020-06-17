import includes from 'lodash/includes';
import isNil from 'lodash/isNil';

import { Representative } from 'core/domain/model/Representative/Representative';

export const reducer = (state, { type, payload }) => {
  const emptyRepresentative = new Representative({ dataProviderId: '', providerAccount: '' });
  let updatedList = [];

  switch (type) {
    case 'ADD_REPRESENTATIVE':
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
      const providersNoSelect = [...payload.responseAllDataProviders];
      if (state.representatives.length <= payload.responseAllDataProviders.length) {
        payload.responseAllDataProviders.unshift({ dataProviderId: '', label: 'Select...' });
      }
      return {
        ...state,
        allPossibleDataProviders: payload.responseAllDataProviders,
        allPossibleDataProvidersNoSelect: providersNoSelect
      };

    case 'GET_PROVIDERS_TYPES_LIST':
      return { ...state, dataProvidersTypesList: payload.providerTypes };

    case 'REPRESENTATIVE_HAS_ERROR':
      let inputsWithErrors = state.representativeHasError;
      inputsWithErrors.unshift(payload.representativeIdThatHasError);

      return { ...state, representativeHasError: inputsWithErrors };

    case 'REPRESENTATIVE_HAS_NO_ERROR':
      const filteredInputsWithErrors = state.representativeHasError.filter(
        representativeId => representativeId !== payload.representativeId
      );

      return { ...state, representativeHasError: filteredInputsWithErrors };

    case 'HIDE_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: false,
        representativeIdToDelete: ''
      };

    case 'REFRESH_ON_HIDE_MANAGE_ROLES_DIALOG':
      return {
        ...state,
        refresher: !state.refresher
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
        representativeHasError: []
      };

    case 'UPDATE_ACCOUNT':
      return {
        ...state,
        refresher: !state.refresher
      };

    case 'UPDATE_DATA_PROVIDER':
      return {
        ...state,
        refresher: !state.refresher
      };

    case 'ON_ACCOUNT_CHANGE':
      return {
        ...state,
        representatives: payload.updatedList
      };

    case 'ON_PROVIDER_CHANGE':
      updatedList = state.representatives.map(representative => {
        if (representative.representativeId === payload.representativeId) {
          representative.dataProviderId = payload.dataProviderId;
        }
        return representative;
      });

      if (!isNil(payload.representativeId)) {
        return {
          ...state,
          refresher: !state.refresher
        };
      }
      return {
        ...state,
        representatives: updatedList
      };

    case 'SELECT_PROVIDERS_TYPE':
      return {
        ...state,
        selectedDataProviderGroup: payload
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
