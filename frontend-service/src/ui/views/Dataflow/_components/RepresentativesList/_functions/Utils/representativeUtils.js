import { isEmpty, isNull, isUndefined, cloneDeep } from 'lodash';
import { RepresentativeService } from 'core/services/Representative';

export const autofocusOnEmptyInput = formState => {
  if (!isEmpty(formState.representatives)) {
    if (
      isNull(formState.representatives[formState.representatives.length - 1].representativeId) &&
      !isNull(document.getElementById('emptyInput'))
    ) {
      const activeElement = document.activeElement;

      if (activeElement.tagName === 'INPUT' || activeElement.tagName === 'SELECT') {
        return;
      } else {
        document.getElementById('emptyInput').focus();
      }
    }
  }
};

const addRepresentative = async (formDispatcher, representatives, dataflowId) => {
  const newRepresentative = representatives.filter(representative => isNull(representative.representativeId));
  if (!isEmpty(newRepresentative[0].providerAccount) && !isEmpty(newRepresentative[0].dataProviderId)) {
    try {
      await RepresentativeService.add(
        dataflowId,
        newRepresentative[0].providerAccount,
        parseInt(newRepresentative[0].dataProviderId)
      );
      formDispatcher({
        type: 'ADD_REPRESENTATIVE'
      });
    } catch (error) {
      console.log('error on RepresentativeService.add', error);
      if (error.response.status === 404) {
        formDispatcher({
          type: 'REPRESENTATIVE_HAS_ERROR',
          payload: { representativeIdThatHasError: representatives[representatives.length - 1].representativeId }
        });
      }
    }
  }
};

export const createUnusedOptionsList = formDispatcher => {
  formDispatcher({
    type: 'CREATE_UNUSED_OPTIONS_LIST'
  });
};

export const getAllDataProviders = async (selectedDataProviderGroup, formDispatcher) => {
  try {
    const responseAllDataProviders = await RepresentativeService.allDataProviders(selectedDataProviderGroup);
    formDispatcher({
      type: 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID',
      payload: { responseAllDataProviders }
    });
  } catch (error) {
    console.log('error on RepresentativeService.allDataProviders', error);
  }
};

const getAllRepresentatives = async (dataflowId, formDispatcher) => {
  try {
    const responseAllRepresentatives = await RepresentativeService.allRepresentatives(dataflowId);
    const representativesByCopy = cloneDeep(responseAllRepresentatives.representatives);
    formDispatcher({
      type: 'INITIAL_LOAD',
      payload: { response: responseAllRepresentatives, representativesByCopy }
    });
  } catch (error) {
    console.log('error on RepresentativeService.allRepresentatives', error);
  }
};

const getProviderTypes = async formDispatcher => {
  try {
    const providerTypes = await RepresentativeService.getProviderTypes();
    formDispatcher({
      type: 'GET_PROVIDERS_TYPES_LIST',
      payload: { providerTypes }
    });
  } catch (error) {
    console.log('error on  RepresentativeService.getProviderTypes', error);
  }
};

export const getInitialData = async (formDispatcher, dataflowId, formState) => {
  await getProviderTypes(formDispatcher);
  await getAllRepresentatives(dataflowId, formDispatcher);
  if (!isEmpty(formState.representatives)) {
    await getAllDataProviders(formState.selectedDataProviderGroup, formDispatcher);
    createUnusedOptionsList(formDispatcher);
  }
};

export const onAddProvider = (formDispatcher, formState, representative, dataflowId) => {
  isNull(representative.representativeId)
    ? addRepresentative(formDispatcher, formState.representatives, dataflowId)
    : updateRepresentative(formDispatcher, formState, representative);
};

export const onDataProviderIdChange = (formDispatcher, newDataProviderId, representative) => {
  if (!isNull(representative.representativeId) && !isUndefined(representative.representativeId)) {
    updateProviderId(formDispatcher, representative.representativeId, newDataProviderId);
  } else {
    formDispatcher({
      type: 'ON_PROVIDER_CHANGE',
      payload: { dataProviderId: newDataProviderId, representativeId: representative.representativeId }
    });
  }
};

export const onDeleteConfirm = async (formDispatcher, formState) => {
  try {
    await RepresentativeService.deleteById(formState.representativeIdToDelete);

    formDispatcher({
      type: 'DELETE_REPRESENTATIVE',
      payload: { representativeIdToDelete: formState.representativeIdToDelete }
    });
  } catch (error) {
    console.log('error on RepresentativeService.deleteById: ', error);
  }
};

export const onKeyDown = (event, formDispatcher, formState, representative, dataflowId) => {
  if (event.key === 'Enter') {
    onAddProvider(formDispatcher, formState, representative, dataflowId);
  }
};

const updateProviderId = async (formDispatcher, representativeId, newDataProviderId) => {
  try {
    await RepresentativeService.updateDataProviderId(parseInt(representativeId), parseInt(newDataProviderId));
    formDispatcher({
      type: 'ON_PROVIDER_CHANGE',
      payload: { dataProviderId: newDataProviderId, representativeId }
    });
  } catch (error) {
    console.log('error on RepresentativeService.updateDataProviderId', error);
  }
};

const updateRepresentative = async (formDispatcher, formState, updatedRepresentative) => {
  let isChangedAccount = false;

  formState.initialRepresentatives.forEach(initialRepresentative => {
    if (
      initialRepresentative.representativeId === updatedRepresentative.representativeId &&
      initialRepresentative.providerAccount !== updatedRepresentative.providerAccount
    ) {
      isChangedAccount = true;
    } else if (
      initialRepresentative.representativeId === updatedRepresentative.representativeId &&
      initialRepresentative.providerAccount === updatedRepresentative.providerAccount
    ) {
      formDispatcher({
        type: 'REPRESENTATIVE_HAS_NO_ERROR',
        payload: { representativeId: updatedRepresentative.representativeId }
      });
    }
  });

  if (isChangedAccount) {
    try {
      await RepresentativeService.updateProviderAccount(
        parseInt(updatedRepresentative.representativeId),
        updatedRepresentative.providerAccount
      );
      formDispatcher({
        type: 'UPDATE_ACCOUNT'
      });
    } catch (error) {
      console.log('error on RepresentativeService.updateProviderAccount', error);

      if (error.response.status === 404) {
        formDispatcher({
          type: 'REPRESENTATIVE_HAS_ERROR',
          payload: { representativeIdThatHasError: updatedRepresentative.representativeId }
        });
      }
    }
  }
};
