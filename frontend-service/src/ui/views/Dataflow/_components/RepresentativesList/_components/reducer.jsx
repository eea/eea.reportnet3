import { includes } from 'lodash';

export const reducer = (state, { type, payload }) => {
  // console.log('payload', payload);
  const emptyField = { representativeId: null, dataProviderId: '', providerAccount: '' };

  let updatedList = [];
  switch (type) {
    case 'ADD_DATA_PROVIDER':
      // await RepresentativeService.add( payload.dataflowId, payload.providerAccount, payload.dataProviderId);
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
      // await RepresentativeService.deleteById( state.representativeIdToDelete);
      console.log('Delete REPRESENTATIVE with representativeId :', state.representativeIdToDelete);

      return {
        ...state,
        isVisibleConfirmDeleteDialog: false
      };

    case 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID':
      console.log('GET_DATA_PROVIDERS_LIST_BY_GROUP_ID', state.selectedDataProviderGroupId);

      // const dataResponse = await RepresentativeService.allDataProviders(state.selectedGroupId);

      const dataResponse = [
        { dataProviderId: '', label: 'Select...' },
        { dataProviderId: 1, label: 'Spain' },
        { dataProviderId: 2, label: 'Germany' },
        { dataProviderId: 3, label: 'United Kingdom' },
        { dataProviderId: 4, label: 'France' },
        { dataProviderId: 5, label: 'Italy' }
      ];

      return { ...state, allPossibleDataProviders: dataResponse };

    // case 'GET_PROVIDERS_TYPES_LIST':
    //   console.log('GET_PROVIDERS_TYPES_LIST');
    //   // await RepresentativeService.getProviderTypes();

    //   // const response = [
    //   //   { label: 'Countries', dataProviderGroupId: 123456 },
    //   //   { label: 'Companies', dataProviderGroupId: 654123 }
    //   // ];

    //   console.log('payload', payload);

    //   return { ...state, dataProvidersTypesList: payload };

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
        dataProvidersTypesList: payload.dataProvidersTypesList,
        selectedDataProviderGroupId: payload.group
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
