import { includes } from 'lodash';

import { RepresentativeService } from 'core/services/Representative';
import { Representative } from 'core/domain/model/Representative/Representative';

export const reducer = (state, { type, payload }) => {
  const emptyRepresentative = new Representative(null, '', '');

  let updatedList = [];
  switch (type) {
    case 'ADD_DATA_PROVIDER':
      // RepresentativeService.add( payload.dataflowId, payload.providerAccount, payload.dataProviderId);
      RepresentativeService.add(payload.dataflowId, payload.providerAccount, payload.dataProviderId);
      console.log('ADD_DATA_PROVIDER', payload.dataflowId, payload.providerAccount, payload.dataProviderId);
      return state;

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
      console.log('Delete REPRESENTATIVE with representativeId :', state.representativeIdToDelete);
      RepresentativeService.deleteById(state.representativeIdToDelete);

      return {
        ...state,
        isVisibleConfirmDeleteDialog: false
      };

    case 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID':
      console.log('GET_DATA_PROVIDERS_LIST_BY_GROUP_ID', payload);

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
        selectedDataProviderGroupId: payload.group
      };

    case 'UPDATE_ACCOUNT':
      console.log('UPDATE_ACCOUNT payload', payload);
      console.log('updatedList', state.representatives);

      //api call to update providerAccount

      return state;

    case 'ON_ACCOUNT_CHANGE':
      console.log('ON_ACCOUNT_CHANGE payload', payload);
      updatedList = state.representatives.map(dataProvider => {
        if (dataProvider.dataProviderId === payload.dataProviderId) {
          dataProvider.providerAccount = payload.providerAccount;
        }
        return dataProvider;
      });

      return {
        ...state,
        representatives: updatedList
      };

    case 'ON_PROVIDER_CHANGE':
      console.log('ON_PROVIDER_CHANGE payload', payload.representativeId);

      updatedList = state.representatives.map(representative => {
        console.log('representative', representative);
        if (representative.representativeId === payload.representativeId) {
          representative.dataProviderId = payload.dataProviderId;
        }
        return representative;
      });

      return {
        ...state,
        representatives: updatedList
      };

    case 'SELECT_PROVIDERS_TYPE':
      console.log('SELECT_PROVIDERS_TYPE ', payload);
      return {
        ...state,
        selectedDataProviderGroupId: payload
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
