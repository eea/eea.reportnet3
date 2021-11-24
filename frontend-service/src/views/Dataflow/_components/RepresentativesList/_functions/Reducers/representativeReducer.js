import includes from 'lodash/includes';
import isNil from 'lodash/isNil';

import { Representative } from 'entities/Representative';

export const reducer = (state, { type, payload }) => {
  const emptyRepresentative = new Representative({ dataProviderId: '', leadReporters: [] });

  switch (type) {
    case 'REFRESH':
      return { ...state, refresher: !state.refresher, leadReportersErrors: {} };

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

    case 'HIDE_CONFIRM_DIALOG':
      return { ...state, isVisibleConfirmDeleteDialog: false, representativeIdToDelete: '' };

    case 'INITIAL_LOAD':
      const group = state.dataProvidersTypesList.filter(
        dataProviderType => dataProviderType.dataProviderGroupId === payload.response.group.dataProviderGroupId
      );

      if (!includes(state.representatives, emptyRepresentative)) {
        payload.response.representatives.push(emptyRepresentative);
      }

      // add empty leadReporter to all representative that have country
      payload.response.representatives.forEach(representative => {
        if (representative.dataProviderId) {
          representative.leadReporters.push({ id: 'empty', account: '' });
          return representative;
        }
        return representative;
      });

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
        leadReporters: payload.parsedLeadReporters,
        representatives: payload.response.representatives,
        selectedDataProviderGroup: getSelectedProviderGroup()
      };

    case 'ON_PROVIDER_CHANGE':
      return { ...state, representatives: payload.representatives };

    case 'SELECT_PROVIDERS_TYPE':
      return { ...state, selectedDataProviderGroup: payload };

    case 'SET_IS_LOADING':
      return { ...state, isLoading: payload.isLoading };

    case 'SHOW_CONFIRM_DIALOG':
      return { ...state, isVisibleConfirmDeleteDialog: true, representativeIdToDelete: payload.representativeId };

    case 'ON_CHANGE_LEAD_REPORTER':
      return {
        ...state,
        leadReporters: {
          ...state.leadReporters,
          [payload.dataProviderId]: {
            ...state.leadReporters[payload.dataProviderId],
            [payload.leadReporterId]: payload.inputValue
          }
        }
      };

    case 'CREATE_ERROR':
      return {
        ...state,
        leadReportersErrors: {
          ...state.leadReportersErrors,
          [payload.dataProviderId]: {
            ...state.leadReportersErrors[payload.dataProviderId],
            [payload.leadReporterId]: payload.hasErrors
          }
        }
      };

    case 'HANDLE_DIALOGS':
      return { ...state, isVisibleDialog: { ...state.isVisibleDialog, [payload.dialog]: payload.isVisible } };

    case 'LEAD_REPORTER_DELETE_ID':
      return { ...state, deleteLeadReporterId: payload.id };

    case 'CLEAN_UP_ERRORS':
      return {
        ...state,
        leadReportersErrors: {
          ...state.leadReportersErrors,
          [payload.dataProviderId]: {
            ...state.leadReportersErrors[payload.dataProviderId],
            [payload.leadReporterId]: payload.hasErrors
          }
        }
      };

    case 'SET_IS_DELETING':
      return { ...state, isDeleting: payload.isDeleting };

    case 'SET_FOCUSED_LEAD_REPORTER_ID':
      return { ...state, focusedLeadReporterId: payload.focusedLeadReporterId };

    default:
      return state;
  }
};
