import React, { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
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

export const ShareRights = ({ dataflowId, dataflowState }) => {
  const { dataProviderId, isCustodian, isShareRightsDialogVisible } = dataflowState;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const inputRef = useRef(null);

  const [shareRightsState, shareRightsDispatch] = useReducer(shareRightsReducer, {
    account: '',
    contributors: [],
    isDeleteDialogVisible: false,
    writePermission: false
  });

  // const deleteConfirmMessage =
  //   resources.messages[`{${isCustodian ? 'editors' : 'reporters'}RightsDialogConfirmDeleteQuestion}`];

  const deleteConfirmMessage = isCustodian
    ? resources.messages['editorsRightsDialogConfirmDeleteQuestion']
    : resources.messages['reportersRightsDialogConfirmDeleteQuestion'];

  const deleteConfirmHeader = isCustodian
    ? resources.messages['editorsRightsDialogConfirmDeleteHeader']
    : resources.messages['reportersRightsDialogConfirmDeleteHeader'];

  useInputTextFocus(isShareRightsDialogVisible, inputRef);

  useEffect(() => {
    getAllContributors();
  }, []);

  const getAllContributors = async () => {
    try {
      const contributors = await ContributorService.all(dataflowId, dataProviderId);

      const emptyContributor = new Contributor({
        account: '',
        dataProviderId: '',
        isNew: true,
        writePermission: ''
      });

      shareRightsDispatch({
        type: 'GET_ALL_CONTRIBUTORS',
        payload: { contributors: [...contributors, emptyContributor] }
      });
    } catch (error) {}
  };

  const onAddContributor = account => {
    shareRightsDispatch({ type: 'ADD_CONTRIBUTOR', payload: { email: account } });
  };

  const onDeleteContributor = async account => {
    try {
      await ContributorService.deleteContributor(account, dataflowId, dataProviderId);

      // const updatedList = formState.contributors.filter(
      //   contributor => contributor.account !== formState.contributorToDelete
      // );

      shareRightsDispatch({ type: 'DELETE_CONTRIBUTOR', payload: {} });
    } catch (error) {
      console.error('error on ContributorService.deleteContributor: ', error);
    }
  };

  const renderInput = () => (
    <input autoFocus={true} ref={inputRef} onChange={event => onAddContributor(event.target.value)} value="" />
  );

  const renderDeleteColumnTemplate = contributor =>
    contributor.isNew ? (
      <Fragment />
    ) : (
      <ActionsColumn
        onDeleteClick={() =>
          shareRightsDispatch({
            type: 'TOGGLE_DELETE_CONFIRM_DIALOG',
            payload: { isVisible: true, contributorAccountToDelete: contributor.account }
          })
        }
      />
    );

  const renderWritePermissionsColumnTemplate = contributor => {
    const writePermissionsOptions = [
      ...(contributor.isNew && { label: resources.messages['selectPermission'], writePermission: '' }),
      { label: resources.messages['readPermission'], writePermission: 'false' },
      { label: resources.messages['readAndWritePermission'], writePermission: 'true' }
    ];

    return (
      <>
        <select
          /*  onBlur={() => onAddContributor(formDispatcher, formState, contributor, dataflowId)}
          onChange={event => {
            onWritePermissionChange(
              contributor,
              dataflowId,
              dataProviderId,
              formDispatcher,
              formState,
              event.target.value
            );
          }}
          onKeyDown={event => onKeyDown(event, formDispatcher, formState, contributor, dataflowId)} */
          value={contributor.writePermission}>
          {writePermissionsOptions.map(option => {
            return (
              <option key={uuid.v4()} className="p-dropdown-item" value={option.writePermission}>
                {option.label}
              </option>
            );
          })}
        </select>
      </>
    );
  };

  const renderAccountTemplate = contributor => {
    const hasError = false;

    console.log('contributor', contributor);

    return (
      <div className={`formField ${hasError && 'error'}`} style={{ marginBottom: '0rem' }}>
        {/* <input
          autoFocus={contributor.isNew}
          disabled={!contributor.isNew}
          id={isEmpty(contributor.account) ? 'emptyInput' : undefined}
          placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
          value={contributor.account}
        /> */}
        <InputText
          autoFocus={contributor.isNew}
          disabled={!contributor.isNew}
          id={isEmpty(contributor.account) ? 'emptyInput' : undefined}
          placeholder={resources.messages['manageRolesDialogInputPlaceholder']}
          ref={inputRef}
          value={contributor.account}
        />
      </div>
    );
  };

  return (
    <Fragment>
      <div>
        {isEmpty(shareRightsState.contributors) ? (
          <div>No data</div>
        ) : (
          <DataTable value={shareRightsState.contributors}>
            <Column body={renderAccountTemplate} header={shareRightsState.accountInputHeader} />
            <Column body={renderWritePermissionsColumnTemplate} header={resources.messages['writePermissionsColumn']} />
            <Column body={renderDeleteColumnTemplate} style={{ width: '60px' }} />
          </DataTable>
        )}
      </div>

      {shareRightsState.isDeleteDialogVisible && (
        <ConfirmDialog
          // onConfirm={() => onDeleteConfirm(formDispatcher, formState, dataflowId, dataProviderId)}
          onHide={() => shareRightsDispatch({ type: 'TOGGLE_DELETE_CONFIRM_DIALOG', payload: { isVisible: false } })}
          classNameConfirm={'p-button-danger'}
          header={deleteConfirmHeader}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          visible={shareRightsState.isDeleteDialogVisible}>
          {deleteConfirmMessage}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
