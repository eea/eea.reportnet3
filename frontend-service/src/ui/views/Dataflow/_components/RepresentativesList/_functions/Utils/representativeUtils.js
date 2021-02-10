import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import { DownloadFile } from 'ui/views/_components/DownloadFile';

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

const addRepresentative = async (formDispatcher, representatives, dataflowId, formState) => {
  const newRepresentative = representatives.filter(representative => isNil(representative.representativeId));
  if (
    !isEmpty(newRepresentative[0].providerAccount) &&
    isValidEmail(newRepresentative[0].providerAccount) &&
    !isEmpty(newRepresentative[0].dataProviderId)
  ) {
    formDispatcher({
      type: 'SET_IS_LOADING',
      payload: { isLoading: true }
    });
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
        let { representativesHaveError } = formState;
        representativesHaveError.unshift(representatives[representatives.length - 1].representativeId);
        formDispatcher({
          type: 'MANAGE_ERRORS',
          payload: { representativesHaveError: uniq(representativesHaveError) }
        });
      }
    } finally {
      formDispatcher({
        type: 'SET_IS_LOADING',
        payload: { isLoading: false }
      });
    }
  }
};

export const createUnusedOptionsList = formDispatcher => {
  formDispatcher({
    type: 'CREATE_UNUSED_OPTIONS_LIST'
  });
};

export const getAllDataProviders = async (selectedDataProviderGroup, representatives, formDispatcher) => {
  try {
    const responseAllDataProviders = await RepresentativeService.allDataProviders(selectedDataProviderGroup);

    const providersNoSelect = [...responseAllDataProviders];
    if (representatives.length <= responseAllDataProviders.length) {
      responseAllDataProviders.unshift({ dataProviderId: '', label: ' Select...' });
    }

    formDispatcher({
      type: 'GET_DATA_PROVIDERS_LIST_BY_GROUP_ID',
      payload: { responseAllDataProviders, providersNoSelect }
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
    await getAllDataProviders(formState.selectedDataProviderGroup, formState.representatives, formDispatcher);
    createUnusedOptionsList(formDispatcher);
  }
};

export const onAddProvider = (formDispatcher, formState, representative, dataflowId) => {
  isNil(representative.representativeId)
    ? addRepresentative(formDispatcher, formState.representatives, dataflowId, formState)
    : updateRepresentative(formDispatcher, formState, representative);
};

export const onDataProviderIdChange = async (formDispatcher, newDataProviderId, representative, formState) => {
  if (!isNil(representative.representativeId)) {
    formDispatcher({
      type: 'SET_IS_LOADING',
      payload: { isLoading: true }
    });
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
    } finally {
      formDispatcher({
        type: 'SET_IS_LOADING',
        payload: { isLoading: false }
      });
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

export const onExportLeadReportersTemplate = async selectedDataProviderGroup => {
  const response = await RepresentativeService.downloadTemplateById(selectedDataProviderGroup.dataProviderGroupId);
  if (!isNil(response)) {
    DownloadFile(response, `GroupId_${selectedDataProviderGroup.dataProviderGroupId}_Template.csv`);
  }
};

export const onKeyDown = (event, formDispatcher, formState, representative, dataflowId) => {
  if (event.key === 'Enter') {
    onAddProvider(formDispatcher, formState, representative, dataflowId);
  }
};

export const isValidEmail = email => {
  if (isNil(email)) {
    return true;
  }

  const expression = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

  return email.match(expression);
};

const updateRepresentative = async (formDispatcher, formState, updatedRepresentative) => {
  let isChangedAccount = false;
  const { initialRepresentatives } = formState;

  for (let initialRepresentative of initialRepresentatives) {
    if (
      initialRepresentative.representativeId === updatedRepresentative.representativeId &&
      initialRepresentative.providerAccount !== updatedRepresentative.providerAccount
    ) {
      isChangedAccount = true;
    } else if (
      initialRepresentative.representativeId === updatedRepresentative.representativeId &&
      initialRepresentative.providerAccount === updatedRepresentative.providerAccount
    ) {
      const filteredInputsWithErrors = formState.representativesHaveError.filter(
        representativeId => representativeId !== updatedRepresentative.representativeId
      );

      formDispatcher({
        type: 'MANAGE_ERRORS',
        payload: { representativesHaveError: filteredInputsWithErrors }
      });
    }
  }

  if (isChangedAccount && isValidEmail(updatedRepresentative.providerAccount)) {
    formDispatcher({
      type: 'SET_IS_LOADING',
      payload: { isLoading: true }
    });
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

      if (error.response.status >= 400 || error.response.status <= 404) {
        let { representativesHaveError } = formState;
        representativesHaveError.unshift(updatedRepresentative.representativeId);

        formDispatcher({
          type: 'MANAGE_ERRORS',
          payload: { representativesHaveError: uniq(representativesHaveError) }
        });
      }
    } finally {
      formDispatcher({
        type: 'SET_IS_LOADING',
        payload: { isLoading: false }
      });
    }
  }
};
