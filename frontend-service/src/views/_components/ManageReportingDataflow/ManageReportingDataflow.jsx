import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import styles from './ManageReportingDataflow.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { Button } from 'views/_components/Button';
import { Checkbox } from 'views/_components/Checkbox';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { ManageReportingDataflowForm } from './_components/ManageReportingDataflowForm';
import { Dialog } from 'views/_components/Dialog';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { InputText } from 'views/_components/InputText';
import ReactTooltip from 'react-tooltip';

import { DataflowService } from 'services/DataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { reportingDataflowReducer } from './_functions/Reducers/reportingDataflowReducer';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const ManageReportingDataflow = ({
  dataflowId,
  isEditForm = false,
  isVisible,
  manageDialogs,
  obligation,
  onCreateDataflow,
  onEditDataflow,
  resetObligations,
  setCheckedObligation,
  state
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const deleteInputRef = useRef(null);
  const formRef = useRef(null);

  const reportingDataflowInitialState = {
    deleteInput: '',
    description: isEditForm ? state.description : '',
    isDeleting: false,
    isSubmitting: false,
    name: isEditForm ? state.name : '',
    obligation,
    pinDataflow: false,
    isReleasable: state.isReleasable
  };

  const setIsDeleting = isDeleting => reportingDataflowDispatch({ type: 'SET_IS_DELETING', payload: { isDeleting } });

  useCheckNotifications(['DELETE_DATAFLOW_FAILED_EVENT'], setIsDeleting, false);

  const [reportingDataflowState, reportingDataflowDispatch] = useReducer(
    reportingDataflowReducer,
    reportingDataflowInitialState
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

  const onSubmit = value => reportingDataflowDispatch({ type: 'ON_SUBMIT', payload: { submit: value } });

  const onHideDataflowDialog = () => {
    onResetData();
    resetObligations();
    manageDialogs('isReportingDataflowDialogVisible', false);
  };

  const onDeleteDataflow = async () => {
    setIsDeleting(true);
    try {
      await DataflowService.delete(dataflowId);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('ManageReportingDataflow - onDeleteDataflow.', error);
        notificationContext.add({ type: 'DATAFLOW_DELETE_BY_ID_ERROR', content: { dataflowId } });
      }
      setIsDeleting(false);
    }
  };

  const onLoadData = ({ name, description }) =>
    reportingDataflowDispatch({ type: 'ON_LOAD_DATA', payload: { name, description } });

  const onLoadObligation = ({ id, title }) =>
    reportingDataflowDispatch({ type: 'ON_LOAD_OBLIGATION', payload: { id, title } });

  const onResetData = () =>
    reportingDataflowDispatch({
      type: 'RESET_STATE',
      payload: { resetData: reportingDataflowInitialState }
    });

  const onDeleteInputChange = value =>
    reportingDataflowDispatch({ type: 'ON_DELETE_INPUT_CHANGE', payload: { deleteInput: value } });

  const onSave = () => {
    if (formRef.current) formRef.current.handleSubmit(reportingDataflowState.pinDataflow);
  };

  const renderCancelButton = action => (
    <Button
      className={`p-button-secondary button-right-aligned p-button-animated-blink ${styles.cancelButton}`}
      icon="cancel"
      label={isEditForm ? resourcesContext.messages['cancel'] : resourcesContext.messages['close']}
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
            label={resourcesContext.messages['deleteDataflowButton']}
            onClick={() => manageDialogs('isDeleteDialogVisible', true)}
          />
        )}
        {!isEditForm && (
          <div className={styles.checkboxWrapper}>
            <Checkbox
              ariaLabel={resourcesContext.messages['pinDataflow']}
              checked={reportingDataflowState.pinDataflow}
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              onChange={() =>
                reportingDataflowDispatch({
                  type: 'TOGGLE_PIN',
                  payload: !reportingDataflowState.pinDataflow
                })
              }
              role="checkbox"
            />
            <label>
              <span
                onClick={() =>
                  reportingDataflowDispatch({
                    type: 'TOGGLE_PIN',
                    payload: !reportingDataflowState.pinDataflow
                  })
                }>
                {resourcesContext.messages['pinDataflow']}
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
              <span>{resourcesContext.messages['pinDataflowMessage']}</span>
            </ReactTooltip>
          </div>
        )}
      </div>
      <Button
        className={`p-button-primary ${
          !isEmpty(reportingDataflowState.name) &&
          !isEmpty(reportingDataflowState.description) &&
          !isNil(reportingDataflowState.obligation?.id) &&
          !reportingDataflowState.isSubmitting
            ? 'p-button-animated-blink'
            : ''
        }`}
        disabled={
          isEmpty(reportingDataflowState.name) ||
          isEmpty(reportingDataflowState.description) ||
          isNil(reportingDataflowState.obligation?.id) ||
          reportingDataflowState.isSubmitting
        }
        icon={reportingDataflowState.isSubmitting ? 'spinnerAnimate' : isEditForm ? 'check' : 'add'}
        label={isEditForm ? resourcesContext.messages['save'] : resourcesContext.messages['create']}
        onClick={() => (reportingDataflowState.isSubmitting ? {} : onSave())}
      />
      {renderCancelButton(onHideDataflowDialog)}
    </Fragment>
  );

  return (
    <Fragment>
      {isVisible && (
        <Dialog
          className={styles.dialog}
          footer={renderDataflowDialog()}
          header={resourcesContext.messages[isEditForm ? 'updateDataflow' : 'createNewDataflow']}
          onHide={() => onHideDataflowDialog()}
          visible={isVisible}>
          <ManageReportingDataflowForm
            data={reportingDataflowState}
            dataflowId={dataflowId}
            getData={onLoadData}
            isEditForm={isEditForm}
            obligation={reportingDataflowState.obligation}
            onCreate={onCreateDataflow}
            onEdit={onEditDataflow}
            onResetData={onResetData}
            onSearch={() => manageDialogs('isReportingObligationsDialogVisible', true)}
            onSubmit={onSubmit}
            ref={formRef}
            refresh={state.isReportingDataflowDialogVisible}
          />
        </Dialog>
      )}

      {state.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={
            !TextUtils.areEquals(reportingDataflowState.deleteInput, state.name) || reportingDataflowState.isDeleting
          }
          header={resourcesContext.messages['delete'].toUpperCase()}
          iconConfirm={reportingDataflowState.isDeleting && 'spinnerAnimate'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onDeleteDataflow}
          onHide={() => manageDialogs('isDeleteDialogVisible', false)}
          visible={state.isDeleteDialogVisible}>
          <p>{resourcesContext.messages['deleteDataflow']}</p>
          <p
            dangerouslySetInnerHTML={{
              __html: TextUtils.parseText(resourcesContext.messages['deleteDataflowConfirm'], {
                dataflowName: state.name
              })
            }}></p>
          <InputText
            autoFocus={true}
            className={styles.inputText}
            id={'deleteDataflow'}
            maxLength={config.INPUT_MAX_LENGTH}
            name={resourcesContext.messages['deleteDataflowButton']}
            onChange={event => onDeleteInputChange(event.target.value)}
            ref={deleteInputRef}
            value={reportingDataflowState.deleteInput}
          />
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
