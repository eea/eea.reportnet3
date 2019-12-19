import { includes } from 'lodash';

import { RepresentativeService } from 'core/services/Representative';

export const reducer = (state, { type, payload }) => {
  const emptyField = { representativeId: null, dataProviderId: '', providerAccount: '' };

  let updatedList = [];
  switch (type) {
    case 'ADD_DATAPROVIDER':
      // await RepresentativeService.add(dataflowId, providerAccount, dataProviderId);
      console.log('On add dataProvider', payload.dataflowId, payload.providerAccount, payload.dataProviderId);
      return state;

    case 'CREATE_UNUSED_OPTIONS_LIST':
      console.log('CREATE_UNUSED_OPTIONS_LIST');
      let unusedDataProvidersOptions = state.allPossibleDataProviders.filter(possibleProvider => {
        let result = true;

        for (let index = 0; index < state.representatives.length; index++) {
          if (state.representatives[index].dataProviderId === possibleProvider.dataProviderId) {
            result = false;
          }
        }

        return result;
      });
      return { ...state, unusedDataProvidersOptions };

    case 'DELETE_REPRESENTATIVE':
      console.log('Delete REPRESTNTATIVE with representativeId :', state.representativeIdToDelete);
      /* await RepresentativeService.delete(dataProviderId); */

      return {
        ...state,
        isVisibleConfirmDeleteDialog: false
      };

    case 'GET_REPRESENTATIVES_LIST_BY_TYPE':
      console.log('GET_REPRESENTATIVES_LIST_BY_TYPE');

      // const dataResponse = await RepresentativeService.allDataProviders(payload.type);
      const dataResponse = [
        { dataProviderId: '', label: 'Select...' },
        { dataProviderId: 1, label: 'Spain' },
        { dataProviderId: 2, label: 'Germany' },
        { dataProviderId: 3, label: 'United Kingdom' },
        { dataProviderId: 4, label: 'France' },
        { dataProviderId: 5, label: 'Italy' }
      ];

      return { ...state, allPossibleDataProviders: dataResponse };

    case 'GET_PROVIDERS_TYPES_LIST':
      //Need get function on api for that list
      // Http requester......
      const response = [
        { label: 'Countries', dataProviderGroupId: 123456 },
        { label: 'Companies', dataProviderGroupId: 654123 }
      ];

      return { ...state, dataProvidersTypesList: response };

    case 'HIDE_CONFIRM_DIALOG':
      console.log('HIDE_CONFIRM_DIALOG');
      return {
        ...state,
        isVisibleConfirmDeleteDialog: false,
        representativeIdToDelete: ''
      };

    case 'INITIAL_LOAD':
      console.log('INITIAL_LOAD');
      if (!includes(state.representatives, emptyField)) {
        payload.representatives.push(emptyField);
      }

      return {
        ...state,
        representatives: payload.representatives,
        selectedDataProviderType: payload.group
      };

    case 'UPDATE_EMAIL':
      console.log('UPDATE_EMAIL payload', payload);
      console.log('updatedList', state.representatives);

      //api call to update providerAccount

      return state;

    case 'ON_EMAIL_CHANGE':
      console.log('ON_EMAIL_CHANGE payload', payload);
      updatedList = state.representatives.map(dataProvider => {
        if (dataProvider.dataProviderId === payload.dataProviderId) {
          dataProvider.providerAccount = payload.input;
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
        selectedDataProviderType: payload
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
