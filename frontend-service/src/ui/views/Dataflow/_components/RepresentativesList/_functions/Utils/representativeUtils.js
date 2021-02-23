import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { RepresentativeService } from 'core/services/Representative';

const addRepresentative = async (formDispatcher, representatives, dataflowId, formState) => {
  const newRepresentative = representatives.filter(representative => isNil(representative.representativeId));
  if (!isEmpty(newRepresentative[0].dataProviderId)) {
    formDispatcher({
      type: 'SET_IS_LOADING',
      payload: { isLoading: true }
    });
    try {
      await RepresentativeService.add(
        dataflowId,
        formState.selectedDataProviderGroup.dataProviderGroupId,
        parseInt(newRepresentative[0].dataProviderId)
      );

      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on RepresentativeService.add', error);
      //TODO Add notification if representative add fails?
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

const parseInsideLeadReporters = (leadReporters = []) => {
  const reporters = {};
  for (let index = 0; index < leadReporters.length; index++) {
    const leadReporter = leadReporters[index];

    reporters[leadReporter.id] = leadReporter;
    reporters['empty'] = '';
  }
  return reporters;
};

const parseLeadReporters = (representatives = []) => {
  const filteredRepresentatives = representatives.filter(re => !isNil(re.dataProviderId));

  const dataProvidersLeadReporters = {};

  filteredRepresentatives.forEach(representative => {
    if (isNil(representative.leadReporters)) return {};

    dataProvidersLeadReporters[representative.dataProviderId] = parseInsideLeadReporters(representative.leadReporters);
  });

  return dataProvidersLeadReporters;
};

const getAllRepresentatives = async (dataflowId, formDispatcher) => {
  try {
    let responseAllRepresentatives = await RepresentativeService.allRepresentatives(dataflowId);

    const parsedLeadReporters = parseLeadReporters(responseAllRepresentatives.representatives);

    formDispatcher({
      type: 'INITIAL_LOAD',
      payload: { response: responseAllRepresentatives, parsedLeadReporters }
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

export const onAddRepresentative = (formDispatcher, formState, dataflowId) => {
  addRepresentative(formDispatcher, formState.representatives, dataflowId, formState);
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

export const isValidEmail = email => {
  const expression = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

  return expression.test(email);
};
