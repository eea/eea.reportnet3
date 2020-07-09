import React, { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uuid from 'uuid';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { InputText } from 'ui/views/_components/InputText';
import { Spinner } from 'ui/views/_components/Spinner';

import { Contributor } from 'core/domain/model/Contributor/Contributor';
import { ContributorService } from 'core/services/Contributor';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { shareRightsReducer } from './_functions/Reducers/shareRightsReducer';

import { useInputTextFocus } from 'ui/views/_functions/Hooks/useInputTextFocus';

export const ShareRights = ({ dataflowId, dataflowState, representativeId }) => {
  const { dataProviderId, isCustodian, isShareRightsDialogVisible } = dataflowState;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const inputRef = useRef(null);

  const [shareRightsState, shareRightsDispatch] = useReducer(shareRightsReducer, {
    accountHasError: false,
    contributorAccountToDelete: '',
    contributors: [],
    isContributorDeleting: false,
    isDataUpdated: false,
    isDeleteDialogVisible: false
  });

  const deleteConfirmMessage =
    resources.messages[`${isCustodian ? 'editors' : 'reporters'}RightsDialogConfirmDeleteQuestion`];

  const deleteConfirmHeader = isCustodian
    ? resources.messages['editorsRightsDialogConfirmDeleteHeader']
    : resources.messages['reportersRightsDialogConfirmDeleteHeader'];

  // useInputTextFocus(isShareRightsDialogVisible, inputRef);

  useEffect(() => {
    getAllContributors();
  }, [shareRightsState.isDataUpdated]);

  const getAllContributors = async () => {
    const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;

    try {
      const contributors = await ContributorService.all(dataflowId, dataProvider);
      const emptyContributor = new Contributor({ account: '', dataProviderId: '', isNew: true, writePermission: '' });

      shareRightsDispatch({
        type: 'GET_ALL_CONTRIBUTORS',
        payload: { contributors: [...contributors, emptyContributor] }
      });
    } catch (error) {}
  };

  const isValidEmail = email => {
    if (isNil(email)) {
      return true;
    }

    const expression = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

    return email.match(expression);
  };

  const isExistingAccount = account => {
    const sameAccounts = shareRightsState.contributors.filter(contributor => contributor.account === account);

    return sameAccounts.length > 1;
  };

  const updateContributor = contributor => {
    shareRightsDispatch({
      type: 'SET_ACCOUNT_HAS_ERROR',
      payload: { accountHasError: !isValidEmail(contributor.account) || isExistingAccount(contributor.account) }
    });

    if (contributor.writePermission !== '' && !shareRightsState.accountHasError) {
      onUpdateContributor(contributor);
    }
  };

  const onDeleteContributor = async () => {
    onToggleDeletingContributor(true);
    const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;
    try {
      const response = await ContributorService.deleteContributor(
        shareRightsState.contributorAccountToDelete,
        dataflowId,
        dataProvider
      );
      if (response.status >= 200 && response.status <= 299) {
        onDataChange();
      }
    } catch (error) {
      const notificationKey = isCustodian ? 'DELETE_EDITOR_ERROR' : 'DELETE_REPORTER_ERROR';

      notificationContext.add({ type: notificationKey });
    } finally {
      onToggleDeletingContributor(false);
      shareRightsDispatch({ type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG', payload: { isDeleteDialogVisible: false } });
    }
  };

  const onDataChange = () => {
    shareRightsDispatch({ type: 'ON_DATA_CHANGE', payload: { isDataUpdated: !shareRightsState.isDataUpdated } });
  };

  const onUpdateContributor = async contributor => {
    if (contributor.writePermission !== '') {
      const dataProvider = isNil(representativeId) ? dataProviderId : representativeId;
      try {
        const response = await ContributorService.update(contributor, dataflowId, dataProvider);
        if (response.status >= 200 && response.status <= 299) {
          onDataChange();
        }
      } catch (error) {
        notificationContext.add({ type: 'UPDATE_CONTRIBUTOR_ERROR' });
        if (error.status >= 400 && error.status <= 404) {
          shareRightsDispatch({ type: 'SET_ACCOUNT_HAS_ERROR', payload: { accountHasError: true } });
        }
      }
    }
  };

  const onWritePermissionChange = async (contributor, newWritePermission) => {
    const { contributors } = shareRightsState;
    const [thisContributor] = contributors.filter(thisContributor => thisContributor.account === contributor.account);
    thisContributor.writePermission = newWritePermission;

    if (!shareRightsState.accountHasError) {
      shareRightsDispatch({ type: 'ON_WRITE_PERMISSION_CHANGE', payload: { contributors } });
    }

    if (isValidEmail(contributor.account) && !shareRightsState.accountHasError) {
      onUpdateContributor(thisContributor);
    }
  };

  const onSetAccount = inputValue => {
    const { contributors } = shareRightsState;
    const [newContributor] = contributors.filter(contributor => contributor.isNew);
    newContributor.account = inputValue;

    shareRightsDispatch({
      type: 'ON_SET_ACCOUNT',
      payload: { contributors, accountHasError: !isValidEmail(inputValue) || isExistingAccount(inputValue) }
    });
  };

  const onToggleDeletingContributor = value => {
    shareRightsDispatch({ type: 'TOGGLE_DELETING_CONTRIBUTOR', payload: { isDeleting: value } });
  };

  const renderDeleteColumnTemplate = contributor =>
    contributor.isNew ? (
      <Fragment />
    ) : (
      <ActionsColumn
        onDeleteClick={() =>
          shareRightsDispatch({
            type: 'ON_DELETE_CONTRIBUTOR',
            payload: { isDeleteDialogVisible: true, contributorAccountToDelete: contributor.account }
          })
        }
      />
    );

  const renderWritePermissionsColumnTemplate = contributor => {
    const writePermissionsOptions = contributor.isNew
      ? [
          { label: resources.messages['selectPermission'], writePermission: '' },
          { label: resources.messages['readPermission'], writePermission: false },
          { label: resources.messages['readAndWritePermission'], writePermission: true }
        ]
      : [
          { label: resources.messages['readPermission'], writePermission: false },
          { label: resources.messages['readAndWritePermission'], writePermission: true }
        ];

    return (
      <select
        // onKeyDown={event => onKeyDown(event, formDispatcher, formState, contributor, dataflowId)}
        onBlur={() => updateContributor(contributor)}
        onChange={event => onWritePermissionChange(contributor, event.target.value)}
        value={contributor.writePermission}>
        {writePermissionsOptions.map(option => {
          return (
            <option key={uuid.v4()} className="p-dropdown-item" value={option.writePermission}>
              {option.label}
            </option>
          );
        })}
      </select>
    );
  };

  const renderAccountTemplate = contributor => {
    const hasError =
      !isEmpty(contributor.account) &&
      contributor.isNew &&
      (!isValidEmail(contributor.account) || isExistingAccount(contributor.account));
    return (
      <div className={`formField ${hasError && 'error'}`} style={{ marginBottom: '0rem' }}>
        <input
          autoFocus={contributor.isNew}
          disabled={!contributor.isNew}
          id={isEmpty(contributor.account) ? 'emptyInput' : contributor.account}
          onBlur={() => updateContributor(contributor)}
          onChange={event => onSetAccount(event.target.value)}
          placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
          value={contributor.account}
        />
      </div>
    );
  };

  return (
    <Fragment>
      <div>
        {isEmpty(shareRightsState.contributors) ? (
          <Spinner style={{ top: 0 }} />
        ) : (
          <DataTable value={shareRightsState.contributors}>
            <Column
              body={renderAccountTemplate}
              header={
                dataflowState.isCustodian
                  ? resources.messages['editorsAccountColumn']
                  : resources.messages['reportersAccountColumn']
              }
            />
            <Column body={renderWritePermissionsColumnTemplate} header={resources.messages['writePermissionsColumn']} />
            <Column body={renderDeleteColumnTemplate} style={{ width: '60px' }} />
          </DataTable>
        )}
      </div>

      {shareRightsState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={shareRightsState.isContributorDeleting}
          header={deleteConfirmHeader}
          iconConfirm={shareRightsState.isContributorDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteContributor()}
          onHide={() =>
            shareRightsDispatch({
              type: 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG',
              payload: { isDeleteDialogVisible: false }
            })
          }
          visible={shareRightsState.isDeleteDialogVisible}>
          {deleteConfirmMessage}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
