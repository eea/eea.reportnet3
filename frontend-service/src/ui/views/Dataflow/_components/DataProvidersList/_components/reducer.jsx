import { includes } from 'lodash';

import { DataProviderService } from 'core/services/DataProvider';

export const reducer = (state, { type, payload }) => {
  const emptyField = { representativeId: null, dataProviderId: '', providerAccount: '' };

  let updatedList = [];
  switch (type) {
    case 'ADD_DATAPROVIDER':
      // await DataProviderService.add(dataflowId, providerAccount, name);
      console.log('On add dataProvider', payload.dataflowId, payload.providerAccount);
      return state;

    case 'CREATE_UNUSED_OPTIONS_LIST':
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

    case 'DELETE_DATA_PROVIDER':
      console.log('Delete Provider with representativeId :', state.representativeIdToDelete);
      /* await DataProviderService.delete(dataProviderId); */

      return {
        ...state,
        isVisibleConfirmDeleteDialog: false
      };

    case 'GET_DATA_PROVIDERS_LIST_BY_TYPE':
      // const dataResponse = await DataProviderService.allDataProviders(payload.type);
      const dataResponse = [
        { dataProviderId: '', label: 'Select...' },
        { dataProviderId: 1, label: 'Spain' },
        { dataProviderId: 2, label: 'Germany' },
        { dataProviderId: 3, label: 'United Kingdom' },
        { dataProviderId: 4, label: 'France' },
        { dataProviderId: 5, label: 'Italy' }
      ];

      return { ...state, allPossibleDataProviders: dataResponse };

    case 'GET_REPRESENTATIVES_TYPES_LIST':
      //Need get function on api for that list
      // Http requester......
      const response = [
        { label: 'Countries', dataProviderGroupId: 123456 },
        { label: 'Companies', dataProviderGroupId: 654123 }
      ];

      return { ...state, representativesTypesList: response };

    case 'HIDE_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: false,
        representativeIdToDelete: ''
      };

    case 'INITIAL_LOAD':
      if (!includes(state.representatives, emptyField)) {
        payload.representatives.push(emptyField);
      }

      return {
        ...state,
        representatives: payload.representatives,
        selectedRepresentativeType: payload.representativesOf
      };

    case 'UPDATE_EMAIL':
      console.log('payload', payload);
      console.log('updatedList', state.representatives);

      //api call to update providerAccount

      return state;

    case 'ON_EMAIL_CHANGE':
      console.log('payload', payload);
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
      updatedList = state.representatives.map(dataProvider => {
        console.log('dataProvider', dataProvider);
        if (dataProvider.representativeId === payload.representativeId) {
          dataProvider.dataProviderId = payload.dataProviderId;
        }
        return dataProvider;
      });

      return {
        ...state,
        representatives: updatedList
      };

    case 'SELECT_REPRESENTATIVE_TYPE':
      console.log('SELECT_REPRESENTATIVE_TYPE ', payload);
      return {
        ...state,
        selectedRepresentativeType: payload
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
