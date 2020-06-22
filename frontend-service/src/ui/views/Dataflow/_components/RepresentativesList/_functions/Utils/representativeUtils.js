import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { RepresentativeService } from 'core/services/Representative';

export const autofocusOnEmptyInput = formState => {
  if (!isEmpty(formState.representatives)) {
    if (
      isNil(formState.representatives[formState.representatives.length - 1].representativeId) &&
      !isNil(document.getElementById('emptyInput'))
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
  const newRepresentative = representatives.filter(representative => isNil(representative.representativeId));
  if (!isEmpty(newRepresentative[0].providerAccount) && !isEmpty(newRepresentative[0].dataProviderId)) {
    try {
      await RepresentativeService.add(
        dataflowId,
        newRepresentative[0].providerAccount,
        parseInt(newRepresentative[0].dataProviderId)
      );

      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on RepresentativeService.add', error);
      if (error.response.status === 400 || error.response.status === 404) {
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
    console.error('error on RepresentativeService.allDataProviders', error);
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
    console.error('error on RepresentativeService.allRepresentatives', error);
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
    console.error('error on  RepresentativeService.getProviderTypes', error);
  }
};

export const getInitialData = async (formDispatcher, dataflowId, formState) => {
  await getProviderTypes(formDispatcher);
  await getAllRepresentatives(dataflowId, formDispatcher, formState);
  if (!isEmpty(formState.representatives)) {
    await getAllDataProviders(formState.selectedDataProviderGroup, formDispatcher);
    createUnusedOptionsList(formDispatcher);
  }
};

export const onAddProvider = (formDispatcher, formState, representative, dataflowId) => {
  isNil(representative.representativeId)
    ? addRepresentative(formDispatcher, formState.representatives, dataflowId)
    : updateRepresentative(formDispatcher, formState, representative);
};

export const onDataProviderIdChange = async (formDispatcher, newDataProviderId, representative, formState) => {
  if (!isNil(representative.representativeId)) {
    try {
      await RepresentativeService.updateDataProviderId(
        parseInt(representative.representativeId),
        parseInt(newDataProviderId)
      );
      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on RepresentativeService.updateDataProviderId', error);
    }
  } else {
    const { representatives } = formState;

    const [thisRepresentative] = representatives.filter(
      thisRepresentative => thisRepresentative.representativeId === representative.representativeId
    );
    thisRepresentative.dataProviderId = newDataProviderId;

    formDispatcher({
      type: 'ON_PROVIDER_CHANGE',
      payload: { representatives }
    });
  }
};

export const onDeleteConfirm = async (formDispatcher, formState) => {
  try {
    await RepresentativeService.deleteById(formState.representativeIdToDelete);

    const updatedList = formState.representatives.filter(
      representative => representative.representativeId !== formState.representativeIdToDelete
    );

    formDispatcher({
      type: 'DELETE_REPRESENTATIVE',
      payload: { updatedList }
    });
  } catch (error) {
    console.error('error on RepresentativeService.deleteById: ', error);
  }
};

export const onKeyDown = (event, formDispatcher, formState, representative, dataflowId) => {
  if (event.key === 'Enter') {
    onAddProvider(formDispatcher, formState, representative, dataflowId);
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
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on RepresentativeService.updateProviderAccount', error);

      if (error.response.status === 400 || error.response.status === 404) {
        formDispatcher({
          type: 'REPRESENTATIVE_HAS_ERROR',
          payload: { representativeIdThatHasError: updatedRepresentative.representativeId }
        });
      }
    }
  }
};
