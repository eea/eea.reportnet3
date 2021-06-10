import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './DataflowManagement.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataflowManagementForm } from './_components/DataflowManagementForm';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { ReportingObligations } from './_components/ReportingObligations';

import { DataflowService } from 'core/services/Dataflow';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { dataflowManagementReducer } from './_functions/Reducers/dataflowManagementReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { TextUtils } from 'ui/views/_functions/Utils';

export const DataflowManagement = ({
  dataflowId,
  history,
  isEditForm,
  onCreateDataflow,
  onEditDataflow,
  onConfirmDeleteDataflow,
  manageDialogs,
  state
}) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const deleteInputRef = useRef(null);
  const formRef = useRef(null);

  const dataflowManagementInitialState = {
    description: isEditForm ? state.description : '',
    isSubmitting: false,
    name: isEditForm ? state.name : '',
    obligation:
      isEditForm && state.obligations
        ? { id: state.obligations.obligationId, title: state.obligations.title }
        : { id: null, title: '' },
    obligationPrevState:
      isEditForm && state.obligations
        ? { id: state.obligations.obligationId, title: state.obligations.title }
        : { id: null, title: '' },
    isReleasable: state.isReleasable
  };

  const [dataflowManagementState, dataflowManagementDispatch] = useReducer(
    dataflowManagementReducer,
    dataflowManagementInitialState
  );

  useEffect(() => {
    if (!isNil(deleteInputRef.current) && state.isDeleteDialogVisible) deleteInputRef.current.element.focus();
  }, [state.isDeleteDialogVisible]);

  const secondaryDialog = isEditForm ? 'isEditDialogVisible' : 'isAddDialogVisible';

  const getPrevState = data =>
    dataflowManagementDispatch({ type: 'PREV_STATE', payload: { id: data.id, title: data.title } });

  const onSubmit = value => dataflowManagementDispatch({ type: 'ON_SUBMIT', payload: { submit: value } });

  const onDeleteDataflow = async () => {
    manageDialogs('isDeleteDialogVisible', false);
    showLoading();
    try {
      const response = await DataflowService.deleteById(dataflowId);
      if (response.status >= 200 && response.status <= 299) {
        history.push(getUrl(routes.DATAFLOWS));
        notificationContext.add({ type: 'DATAFLOW_DELETE_SUCCESS' });
      } else {
        throw new Error(`Delete dataflow error with this status: ', ${response.status}`);
      }
    } catch (error) {
      notificationContext.add({ type: 'DATAFLOW_DELETE_BY_ID_ERROR', content: { dataflowId } });
    } finally {
      hideLoading();
    }
  };

  const onHideDataflowDialog = () => {
    manageDialogs(secondaryDialog, false);
    onResetData();
  };

  const onHideObligationDialog = () => {
    manageDialogs('isRepObDialogVisible', false);
    onResetObl();
  };

  const onLoadData = ({ name, description }) =>
    dataflowManagementDispatch({ type: 'ON_LOAD_DATA', payload: { name, description } });

  const onLoadObligation = ({ id, title }) =>
    dataflowManagementDispatch({ type: 'ON_LOAD_OBLIGATION', payload: { id, title } });

  const onResetData = () =>
    dataflowManagementDispatch({ type: 'RESET_STATE', payload: { resetData: dataflowManagementInitialState } });

  const onResetObl = () =>
    dataflowManagementDispatch({ type: 'ON_LOAD_OBLIGATION', payload: dataflowManagementState.obligationPrevState });

  const onSave = () => {
    if (formRef.current) formRef.current.handleSubmit();
  };

  const renderCancelButton = action => (
    <Button
      className="p-button-secondary button-right-aligned p-button-animated-blink"
      icon="cancel"
      label={isEditForm ? resources.messages['cancel'] : resources.messages['close']}
      onClick={() => action()}
    />
  );

  const renderDataflowDialog = () => (
    <Fragment>
      <div className="p-toolbar-group-left">
        {isEditForm && state.isCustodian && state.status === config.dataflowStatus.DESIGN && (
          <Button
            className="p-button-danger p-button-animated-blink"
            icon="trash"
            label={resources.messages['deleteDataflowButton']}
            onClick={() => manageDialogs('isDeleteDialogVisible', true)}
          />
        )}
      </div>
      <Button
        className={`p-button-primary ${
          !isEmpty(dataflowManagementState.name) &&
          !isEmpty(dataflowManagementState.description) &&
          !isNil(dataflowManagementState.obligation?.id) &&
          !dataflowManagementState.isSubmitting
            ? 'p-button-animated-blink'
            : ''
        }`}
        disabled={
          isEmpty(dataflowManagementState.name) ||
          isEmpty(dataflowManagementState.description) ||
          isNil(dataflowManagementState.obligation?.id) ||
          dataflowManagementState.isSubmitting
        }
        icon={dataflowManagementState.isSubmitting ? 'spinnerAnimate' : isEditForm ? 'check' : 'add'}
        label={isEditForm ? resources.messages['save'] : resources.messages['create']}
        onClick={() => (dataflowManagementState.isSubmitting ? {} : onSave())}
      />
      {renderCancelButton(onHideDataflowDialog)}
    </Fragment>
  );

  const renderOblFooter = () => (
    <Fragment>
      <Button
        icon="check"
        label={resources.messages['ok']}
        onClick={() => {
          manageDialogs('isRepObDialogVisible', false);
          getPrevState(dataflowManagementState.obligation);
        }}
      />
      {renderCancelButton(onHideObligationDialog)}
    </Fragment>
  );

  return (
    <Fragment>
      {state.isRepObDialogVisible && (
        <Dialog
          footer={renderOblFooter()}
          header={resources.messages['reportingObligations']}
          onHide={() => onHideObligationDialog()}
          style={{ width: '95%' }}
          visible={state.isRepObDialogVisible}>
          <ReportingObligations getObligation={onLoadObligation} oblChecked={dataflowManagementState.obligation} />
        </Dialog>
      )}

      {(state.isAddDialogVisible || state.isEditDialogVisible) && (
        <Dialog
          className={styles.dialog}
          footer={renderDataflowDialog()}
          header={resources.messages[isEditForm ? 'updateDataflow' : 'createNewDataflow']}
          onHide={() => onHideDataflowDialog()}
          visible={state.isAddDialogVisible || state.isEditDialogVisible}>
          <DataflowManagementForm
            data={dataflowManagementState}
            dataflowId={dataflowId}
            getData={onLoadData}
            isEditForm={isEditForm}
            onCreate={onCreateDataflow}
            onEdit={onEditDataflow}
            onResetData={onResetData}
            onSearch={() => manageDialogs('isRepObDialogVisible', true)}
            onSubmit={onSubmit}
            ref={formRef}
            refresh={isEditForm ? state.isEditDialogVisible : state.isAddDialogVisible}
          />
        </Dialog>
      )}

      {state.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={!TextUtils.areEquals(state.deleteInput, state.name)}
          header={resources.messages['delete'].toUpperCase()}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteDataflow()}
          onHide={() => manageDialogs('isDeleteDialogVisible', false)}
          visible={state.isDeleteDialogVisible}>
          <p>{resources.messages['deleteDataflow']}</p>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resources.messages['deleteDataflowConfirm'], {
                dataflowName: state.name
              })
            }}></p>
          <p>
            <InputText
              autoFocus={true}
              className={`${styles.inputText}`}
              id={'deleteDataflow'}
              maxLength={255}
              onChange={event => onConfirmDeleteDataflow(event)}
              ref={deleteInputRef}
              value={state.deleteInput}
            />
            <label className="srOnly" htmlFor="deleteDataflow">
              {resources.messages['deleteDataflowButton']}
            </label>
          </p>
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
