import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

import { Contributor } from 'core/domain/model/Contributor/Contributor';
import { ContributorService } from 'core/services/Contributor';

const emptyContributor = new Contributor({ account: '', dataProviderId: '', writePermission: '', isNew: true });

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

const addContributor = async (formDispatcher, contributor, dataflowId, formState) => {
  if (!isEmpty(contributor.account) && !isEmpty(contributor.writePermission)) {
    try {
      await ContributorService.update(contributor, dataflowId);

      formDispatcher({
        type: 'REFRESH'
      });
    } catch (error) {
      console.error('error on ContributorService.add', error);

      let { contributorsHaveError } = formState;
      contributorsHaveError.unshift(formState.contributors[formState.contributors.length - 1].account);
      formDispatcher({
        type: 'MANAGE_ERRORS',
        payload: { contributorsHaveError: uniq(contributorsHaveError) }
      });

      /* 
      // error.response.status OF UNDEFINED - CHECK WHY
      if (error.response.status === 400 || error.response.status === 404) {
        let { contributorsHaveError } = formState;
        contributorsHaveError.unshift(formState.contributors[formState.contributors.length - 1].account);
        formDispatcher({
          type: 'MANAGE_ERRORS',
          payload: { contributorsHaveError: uniq(contributorsHaveError) }
        });
      } */
    }
  }
};

export const getInitialData = async (formDispatcher, dataflowId, dataProviderId) => {
  try {
    let contributors = await ContributorService.all(dataflowId, dataProviderId);

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
  addContributor(formDispatcher, contributor, dataflowId, formState);
};

export const onWritePermissionChange = async (
  contributor,
  dataflowId,
  dataProviderId,
  formDispatcher,
  formState,
  writePermission
) => {
  if (!isNil(contributor.account)) {
    try {
      contributor.writePermission = writePermission;
      await ContributorService.update(contributor, dataflowId, dataProviderId);
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

export const onDeleteConfirm = async (formDispatcher, formState, dataflowId, dataProviderId) => {
  try {
    console.log('account', formState.contributorToDelete.contributor.account, dataProviderId);
    await ContributorService.deleteContributor(
      formState.contributorToDelete.contributor.account,
      dataflowId,
      dataProviderId
    );

    const updatedList = formState.contributors.filter(
      contributor => contributor.account !== formState.contributorToDelete
    );

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
