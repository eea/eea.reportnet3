import { includes, isUndefined } from 'lodash';

import { Representative } from 'core/domain/model/Representative/Representative';

export const reducer = (state, { type, payload }) => {
  const emptyRepresentative = new Representative(null, '', '');

  let updatedList = [];

  switch (type) {
    case 'ADD_DATA_PROVIDER':
      console.log('ADD_DATA_PROVIDER');
      return {
        ...state,
        responseStatus: payload,
        refresher: !state.refresher
      };
    case 'CREATE_UNUSED_OPTIONS_LIST':
      console.log('CREATE_UNUSED_OPTIONS_LIST');

      let unusedDataProvidersOptions = state.allPossibleDataProviders.filter(dataProviderOption => {
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
      console.log('state.representatives', state.representatives);

      updatedList = state.representatives.map(representative => {
        if (representative.representativeId === null) {
          representative.providerAccount = '';
        }
        return representative;
      });

      return {
        ...state,
        representatives: updatedList,
        isVisibleConfirmDeleteDialog: false,
        responseStatus: payload,
        refresher: !state.refresher
      };

    case 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID':
      console.log('GET_DATA_PROVIDERS_LIST_BY_GROUP_ID');

      return { ...state, allPossibleDataProviders: payload };

    case 'GET_PROVIDERS_TYPES_LIST':
      console.log('GET_PROVIDERS_TYPES_LIST');

      return { ...state, dataProvidersTypesList: payload };

    case 'HIDE_CONFIRM_DIALOG':
      console.log('HIDE_CONFIRM_DIALOG');
      return {
        ...state,
        isVisibleConfirmDeleteDialog: false,
        representativeIdToDelete: ''
      };

    case 'INITIAL_LOAD':
      console.log('INITIAL_LOAD');
      const group = state.dataProvidersTypesList.filter(
        dataProviderType => dataProviderType.dataProviderGroupId === payload.group.dataProviderGroupId
      );
      if (!includes(state.representatives, emptyRepresentative)) {
        payload.representatives.push(emptyRepresentative);
      }

      return {
        ...state,
        representatives: payload.representatives,
        selectedDataProviderGroup: isUndefined(group[0]) ? null : group[0]
      };

    case 'UPDATE_ACCOUNT':
      console.log('UPDATE_ACCOUNT');

      return {
        ...state,
        responseStatus: payload,
        refresher: !state.refresher
      };

    case 'UPDATE_DATA_PROVIDER':
      console.log('UPDATE_DATA_PROVIDER payload');

      return {
        ...state,
        responseStatus: payload,
        refresher: !state.refresher
      };

    case 'ON_ACCOUNT_CHANGE':
      console.log('ON_ACCOUNT_CHANGE payload', payload);
      console.log('ON_ACCOUNT_CHANGE state.representatives', state.representatives);

      updatedList = state.representatives.map(representative => {
        if (representative.dataProviderId === payload.dataProviderId) {
          representative.providerAccount = payload.providerAccount;
        }
        return representative;
      });

      return {
        ...state,
        representatives: updatedList
      };

    case 'ON_PROVIDER_CHANGE':
      console.log('ON_PROVIDER_CHANGE ', payload);

      updatedList = state.representatives.map(representative => {
        if (representative.representativeId === payload.representativeId) {
          representative.dataProviderId = payload.dataProviderId;
        }
        return representative;
      });

      if (payload.representativeId !== null) {
        return {
          ...state,
          refresher: !state.refresher
        };
      } else {
        return {
          ...state,
          representatives: updatedList
        };
      }

    case 'SELECT_PROVIDERS_TYPE':
      console.log('SELECT_PROVIDERS_TYPE ', payload);
      return {
        ...state,
        selectedDataProviderGroup: payload
      };

    case 'SHOW_CONFIRM_DIALOG':
      console.log('SHOW_CONFIRM_DIALOG');
      return {
        ...state,
        isVisibleConfirmDeleteDialog: true,
        representativeIdToDelete: payload.representativeId
      };

    default:
      return state;
  }
};
