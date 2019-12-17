import { includes } from 'lodash';

import { DataProviderService } from 'core/services/DataProvider';

export const reducer = (state, { type, payload }) => {
  const emptyField = { dataProviderId: '', email: '', name: '' };

  let updatedList = [];
  switch (type) {
    case 'ADD_DATAPROVIDER':
      // await DataProviderService.add(dataflowId, email, name);
      console.log('On add dataProvider', payload.dataflowId, payload.email, payload.name);
      return state;

    case 'GET_DATA_PROVIDERS_LIST_OF_SELECTED_TYPE':
      // const dataResponse = await DataProviderService.allRepresentativesOf(payload.type);
      const dataResponse = [
        { nameLabel: 'Select...', name: '' },
        { nameLabel: 'Spain', name: 'Es' },
        { nameLabel: 'Germany', name: 'De' },
        { nameLabel: 'Uk', name: 'UK' },
        { nameLabel: 'France', name: 'Fr' },
        { nameLabel: 'Italy', name: 'It' }
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
    /* 
    case 'SET_CHOSEN_OPTIONS':
      console.log('FILTER :', payload.name);
      let chosenOption = state.allPossibleDataProviders.filter(dataProvider => dataProvider.name === payload.name);
      let chosenOptionsList = state.chosenDataProviders;
      chosenOptionsList.push(chosenOption[0]);
      let updatedChosenOptionsList = Array.from(new Set(chosenOptionsList));
      console.log('listWithUniqueValues :', updatedChosenOptionsList);
      // return { ...state, chosenDataProviders: updatedChosenOptionsList };
      return state; */
    case 'FILTER_CHOSEN_OPTIONS':
      console.log('FILTER_CHOSEN_OPTIONS state', state);
      return state;

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

    case 'ON_PROVIDER_CHANGE':
      updatedList = state.dataProviders.map(dataProvider => {
        if (dataProvider.dataProviderId === payload.dataProviderId) {
          dataProvider.name = payload.name;
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
