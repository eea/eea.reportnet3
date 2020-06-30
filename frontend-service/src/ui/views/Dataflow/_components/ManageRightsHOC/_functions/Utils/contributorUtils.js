import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import { Contributor } from 'core/domain/model/Contributor/Contributor';
import { ContributorService } from 'core/services/Contributor';

const emptyContributor = new Contributor({ account: '', dataProviderId: '', writePermission: '' });

export const autofocusOnEmptyInput = formState => {
  if (!isEmpty(formState.contributors)) {
    if (
      isNil(formState.contributors[formState.contributors.length - 1].account) &&
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

const addContributor = async (formDispatcher, contributors, dataflowId, formState) => {
  const newContributor = contributors.filter(contributor => isNil(contributor.account));

  if (!isEmpty(newContributor[0].providerAccount) && !isEmpty(newContributor[0].dataProviderId)) {
    try {
      await ContributorService.add(
        dataflowId,
        newContributor[0].providerAccount,
        parseInt(newContributor[0].dataProviderId)
      );

      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on ContributorService.add', error);
      if (error.response.status === 400 || error.response.status === 404) {
        let { contributorsHaveError } = formState;
        contributorsHaveError.unshift(contributors[contributors.length - 1].account);
        formDispatcher({
          type: 'MANAGE_ERRORS',
          payload: { contributorsHaveError: uniq(contributorsHaveError) }
        });
      }
    }
  }
};

export const getInitialData = async (formDispatcher, dataflowId) => {
  try {
    const contributors = await ContributorService.all(dataflowId);

    console.log('contributors', contributors);

    contributors.push(emptyContributor);

    const contributorsByCopy = cloneDeep(contributors);

    formDispatcher({
      type: 'INITIAL_LOAD',
      payload: { contributors: contributors, contributorsByCopy }
    });
  } catch (error) {
    console.error('error on ContributorService.all', error);
  }
};

export const onAddContributor = (formDispatcher, formState, contributor, dataflowId) => {
  isNil(contributor.account)
    ? addContributor(formDispatcher, formState.contributors, dataflowId, formState)
    : updateContributor(formDispatcher, formState, contributor);
};

export const onWritePermissionChange = async (contributor, dataflowId, formDispatcher, formState, writePermission) => {
  if (!isNil(contributor.account)) {
    try {
      contributor.writePermission = writePermission;
      await ContributorService.updateWritePermission(contributor, dataflowId);
      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on ContributorService.updateDataProviderId', error);
    }
  } else {
    const { contributors } = formState;

    const [thisContributor] = contributors.filter(thisContributor => thisContributor.account === contributor.account);
    thisContributor.writePermission = writePermission;

    formDispatcher({
      type: 'ON_PERMISSIONS_CHANGE',
      payload: { contributors }
    });
  }
};

export const onDeleteConfirm = async (formDispatcher, formState) => {
  try {
    await ContributorService.deleteContributor(formState.accountToDelete);

    const updatedList = formState.contributors.filter(contributor => contributor.account !== formState.accountToDelete);

    formDispatcher({
      type: 'DELETE_CONTRIBUTOR',
      payload: { updatedList }
    });
  } catch (error) {
    console.error('error on ContributorService.deleteContributor: ', error);
  }
};

export const onKeyDown = (event, formDispatcher, formState, contributor, dataflowId) => {
  if (event.key === 'Enter') {
    onAddContributor(formDispatcher, formState, contributor, dataflowId);
  }
};

const updateContributor = async (formDispatcher, formState, updatedContributor) => {
  let isChangedAccount = false;
  const { initialContributors } = formState;

  for (let initialContributor of initialContributors) {
    if (
      initialContributor.account === updatedContributor.account &&
      initialContributor.providerAccount !== updatedContributor.providerAccount
    ) {
      isChangedAccount = true;
    } else if (
      initialContributor.account === updatedContributor.account &&
      initialContributor.providerAccount === updatedContributor.providerAccount
    ) {
      const filteredInputsWithErrors = formState.contributorsHaveError.filter(
        account => account !== updatedContributor.account
      );
      formDispatcher({
        type: 'MANAGE_ERRORS',
        payload: { contributorsHaveError: filteredInputsWithErrors }
      });
    }
  }

  if (isChangedAccount) {
    try {
      await ContributorService.updateProviderAccount(
        parseInt(updatedContributor.account),
        updatedContributor.providerAccount
      );
      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on ContributorService.updateProviderAccount', error);

      if (error.response.status === 400 || error.response.status === 404) {
        let { contributorsHaveError } = formState;
        contributorsHaveError.unshift(updatedContributor.account);
        formDispatcher({
          type: 'MANAGE_ERRORS',
          payload: { contributorsHaveError: uniq(contributorsHaveError) }
        });
      }
    }
  }
};
