import { includes } from 'lodash';

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

      if (!includes(state.representatives, emptyRepresentative)) {
        payload.representatives.push(emptyRepresentative);
      }

      return {
        ...state,
        representatives: payload.representatives,
        selectedDataProviderGroup: payload.group
      };

    case 'UPDATE_ACCOUNT':
      console.log('UPDATE_ACCOUNT payload', payload);
      console.log('updatedList', state.representatives);

      //api call to update providerAccount

      return state;

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
      console.log('ON_PROVIDER_CHANGE payload', payload.representativeId);

      updatedList = state.representatives.map(representative => {
        if (representative.representativeId === payload.representativeId) {
          representative.dataProviderId = payload.dataProviderId;
        }
        return representative;
      });

      return {
        ...state,
        representatives: updatedList,
        refresher: !state.refresher
      };

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
