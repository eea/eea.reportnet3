import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import { Representative } from 'core/domain/model/Representative/Representative';
import { RepresentativeService } from 'core/services/Representative';

const emptyRepresentative = new Representative({ dataProviderId: '', permission: '', providerAccount: '' });

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

const addRepresentative = async (formDispatcher, formState, dataflowId) => {
  const { representatives } = formState;
  const [newRepresentative] = representatives.filter(representative => isNil(representative.representativeId));

  if (!isEmpty(newRepresentative.providerAccount) && !isEmpty(newRepresentative.permission)) {
    try {
      await RepresentativeService.add(dataflowId, newRepresentative.providerAccount, newRepresentative.permission);

      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on RepresentativeService.add', error);
      if (error.response.status === 400 || error.response.status === 404) {
        let { representativeHasError } = formState;
        representativeHasError.unshift(representatives[representatives.length - 1].representativeId);
        formDispatcher({
          type: 'MANAGE_ERRORS',
          payload: { representativeHasError: uniq(representativeHasError) }
        });
      }
    }
  }
};

export const getInitialData = async (formDispatcher, dataflowId, formState) => {
  try {
    const response = await RepresentativeService.allRepresentatives(dataflowId);

    response.representatives.push(emptyRepresentative);

    const representativesByCopy = cloneDeep(response.representatives);

    formDispatcher({
      type: 'INITIAL_LOAD',
      payload: { representatives: response.representatives, representativesByCopy }
    });
  } catch (error) {
    console.error('error on RepresentativeService.allRepresentatives', error);
  }
};

export const onAddRepresentative = (formDispatcher, formState, representative, dataflowId) => {
  isNil(representative.representativeId)
    ? addRepresentative(formDispatcher, formState, dataflowId)
    : updateRepresentative(formDispatcher, formState, representative);
};

export const onPermissionsChange = async (formDispatcher, newPermission, representative, formState) => {
  if (!isNil(representative.representativeId)) {
    try {
      await RepresentativeService.updatePermission(parseInt(representative.representativeId), newPermission);
      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on RepresentativeService.updatePermission', error);
    }
  } else {
    const { representatives } = formState;

    const [thisRepresentative] = representatives.filter(
      thisRepresentative => thisRepresentative.representativeId === representative.representativeId
    );
    thisRepresentative.permission = newPermission;

    formDispatcher({
      type: 'ON_PERMISSIONS_CHANGE',
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
    onAddRepresentative(formDispatcher, formState, representative, dataflowId);
  }
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
      const filteredInputsWithErrors = formState.representativeHasError.filter(
        representativeId => representativeId !== updatedRepresentative.representativeId
      );
      formDispatcher({
        type: 'MANAGE_ERRORS',
        payload: { representativeHasError: filteredInputsWithErrors }
      });
    }
  }

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
        let { representativeHasError } = formState;
        representativeHasError.unshift(updatedRepresentative.representativeId);
        formDispatcher({
          type: 'MANAGE_ERRORS',
          payload: { representativeHasError: uniq(representativeHasError) }
        });
      }
    }
  }
};
