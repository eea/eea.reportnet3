import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './DataflowManagement.module.scss';

import { config } from 'conf';
import { routes } from 'conf/routes';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataflowManagementForm } from './_components/DataflowManagementForm';
import { Dialog } from 'views/_components/Dialog';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'views/_components/InputText';
import ReactTooltip from 'react-tooltip';

import { DataflowService } from 'services/Dataflow';

import { LoadingContext } from 'views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { dataflowManagementReducer } from './_functions/Reducers/dataflowManagementReducer';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'views/_functions/Utils';

export const DataflowManagement = ({
  dataflowId,
  history,
  isEditForm,
  manageDialogs,
  obligation,
  onConfirmDeleteDataflow,
  onCreateDataflow,
  onEditDataflow,
  resetObligations,
  setCheckedObligation,
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
    obligation,
    pinDataflow: false,
    isReleasable: state.isReleasable
  };

  const [dataflowManagementState, dataflowManagementDispatch] = useReducer(
    dataflowManagementReducer,
    dataflowManagementInitialState
  );

  useEffect(() => {
    if (isEditForm) {
      onLoadObligation({ id: state.obligations.obligationId, title: state.obligations.title });
      setCheckedObligation({ id: state.obligations.obligationId, title: state.obligations.title });
    }
  }, [state]);

  useEffect(() => {
    onLoadObligation(obligation);
  }, [obligation]);

  useEffect(() => {
    if (!isNil(deleteInputRef.current) && state.isDeleteDialogVisible) deleteInputRef.current.element.focus();
  }, [state.isDeleteDialogVisible]);

  const secondaryDialog = isEditForm ? 'isEditDialogVisible' : 'isAddDialogVisible';

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
    onResetData();
    resetObligations();
    manageDialogs(secondaryDialog, false);
  };

  const onLoadData = ({ name, description }) =>
    dataflowManagementDispatch({ type: 'ON_LOAD_DATA', payload: { name, description } });

  const onLoadObligation = ({ id, title }) =>
    dataflowManagementDispatch({ type: 'ON_LOAD_OBLIGATION', payload: { id, title } });

  const onResetData = () =>
    dataflowManagementDispatch({ type: 'RESET_STATE', payload: { resetData: dataflowManagementInitialState } });

  const onSave = () => {
    if (formRef.current) formRef.current.handleSubmit(dataflowManagementState.pinDataflow);
    manageDialogs(secondaryDialog, false);
  };

  const renderCancelButton = action => (
    <Button
      className={`p-button-secondary button-right-aligned p-button-animated-blink ${styles.cancelButton}`}
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
        {!isEditForm && (
          <div className={styles.checkboxWrapper}>
            <Checkbox
              ariaLabel={resources.messages['pinDataflow']}
              checked={dataflowManagementState.pinDataflow}
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              onChange={() =>
                dataflowManagementDispatch({ type: 'TOGGLE_PIN', payload: !dataflowManagementState.pinDataflow })
              }
              role="checkbox"
            />
            <label>
              <span
                onClick={() =>
                  dataflowManagementDispatch({ type: 'TOGGLE_PIN', payload: !dataflowManagementState.pinDataflow })
                }>
                {resources.messages['pinDataflow']}
              </span>
            </label>
            <FontAwesomeIcon
              aria-hidden={false}
              className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
              data-for="pinDataflow"
              data-tip
              icon={AwesomeIcons('infoCircle')}
            />
            <ReactTooltip border={true} className={styles.tooltip} effect="solid" id="pinDataflow" place="top">
              <span>{resources.messages['pinDataflowMessage']}</span>
            </ReactTooltip>
          </div>
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

  return (
    <Fragment>
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
            obligation={dataflowManagementState.obligation}
            onCreate={onCreateDataflow}
            onEdit={onEditDataflow}
            onResetData={onResetData}
            onSearch={() => manageDialogs('isReportingObligationsDialogVisible', true)}
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
              name={resources.messages['deleteDataflowButton']}
              onChange={event => onConfirmDeleteDataflow(event)}
              ref={deleteInputRef}
              value={state.deleteInput}
            />
          </p>
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
