import { includes } from 'lodash';

import { DataProviderService } from 'core/services/DataProvider';

export const reducer = (state, { type, payload }) => {
  const emptyField = { dataProviderId: '', email: '', id: '' };

  let updatedList = [];
  switch (type) {
    case 'ADD_DATAPROVIDER':
      // await DataProviderService.add(dataflowId, email, name);
      console.log('On add dataProvider', payload.dataflowId, payload.email, payload.id);
      return state;

    case 'GET_DATA_PROVIDERS_LIST_BY_TYPE':
      // const dataResponse = await DataProviderService.allRepresentativesOf(payload.type);
      const dataResponse = [
        { id: '', label: 'Select...' },
        { id: 1, label: 'Spain' },
        { id: 2, label: 'Germany' },
        { id: 3, label: 'United Kingdom' },
        { id: 4, label: 'France' },
        { id: 5, label: 'Italy' }
      ];

      return { ...state, allPossibleDataProviders: dataResponse };

    case 'GET_REPRESENTATIVES_TYPES_LIST':
      //Need get function on api for that list
      // Http requester......
      const response = [
        { nameLabel: 'Countries', name: 'countries' },
        { nameLabel: 'Companies', name: 'companies' }
      ];

      return { ...state, representativesTypesList: response };

    case 'DELETE_DATA_PROVIDER':
      console.log('Delete Provider with id :', state.dataProviderIdToDelete);
      /* await DataProviderService.delete(dataProviderId); */

      return {
        ...state,
        isVisibleConfirmDeleteDialog: false
      };

    case 'CREATE_UNUSED_OPTIONS_LIST':
      let unusedDataProvidersOptions = state.allPossibleDataProviders.filter(possibleProvider => {
        let result = true;

        for (let index = 0; index < state.dataProviders.length; index++) {
          if (state.dataProviders[index].id === possibleProvider.id) {
            result = false;
          }
        }

        return result;
      });
      return { ...state, unusedDataProvidersOptions };

    case 'HIDE_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: false,
        dataProviderIdToDelete: ''
      };

    case 'INITIAL_LOAD':
      if (!includes(state.dataProviders, emptyField)) {
        payload.dataProviders.push(emptyField);
      }

      return {
        ...state,
        dataProviders: payload.dataProviders,
        selectedRepresentativeType: payload.representativesOf
      };

    case 'ON_EMAIL_CHANGE':
      console.log('payload', payload);
      updatedList = state.dataProviders.map(dataProvider => {
        if (dataProvider.dataProviderId === payload.dataProviderId) {
          dataProvider.email = payload.input;
        }
        return dataProvider;
      });

      return {
        ...state,
        dataProviders: updatedList
      };
    case 'UPDATE_EMAIL':
      console.log('payload', payload);
      console.log('updatedList', state.dataProviders);

      //api call to update email

      return state;

    case 'ON_PROVIDER_CHANGE':
      console.log('ON_PROVIDER_CHANGE', payload.dataProviderId);
      updatedList = state.dataProviders.map(dataProvider => {
        if (dataProvider.dataProviderId === payload.dataProviderId) {
          dataProvider.id = payload.id;
        }
        return dataProvider;
      });

      return {
        ...state,
        dataProviders: updatedList
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
        dataProviderIdToDelete: payload.dataProviderId
      };

    default:
      return state;
  }
};
