import { Fragment, useContext, useEffect, useReducer, useRef } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'conf/routes';

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

import { LoadingContext } from 'views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { reportingDataflowReducer } from './_functions/Reducers/reportingDataflowReducer';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

export const ManageReportingDataflow = ({
  dataflowId,
  history,
  isEditForm,
  manageDialogs,
  obligation,
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

  const manageReportingDataflowInitialState = {
    deleteInput: '',
    description: isEditForm ? state.description : '',
    isSubmitting: false,
    name: isEditForm ? state.name : '',
    obligation,
    pinDataflow: false,
    isReleasable: state.isReleasable
  };

  const [reportingDataflowState, manageReportingDataflowDispatch] = useReducer(
    reportingDataflowReducer,
    manageReportingDataflowInitialState
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

  const onSubmit = value => manageReportingDataflowDispatch({ type: 'ON_SUBMIT', payload: { submit: value } });

  const onDeleteDataflow = async () => {
    manageDialogs('isDeleteDialogVisible', false);
    showLoading();
    try {
      await DataflowService.delete(dataflowId);
      history.push(getUrl(routes.DATAFLOWS));
      notificationContext.add({ type: 'DATAFLOW_DELETE_SUCCESS' });
    } catch (error) {
      console.error('ManageReportingDataflow - onDeleteDataflow.', error);
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
    manageReportingDataflowDispatch({ type: 'ON_LOAD_DATA', payload: { name, description } });

  const onLoadObligation = ({ id, title }) =>
    manageReportingDataflowDispatch({ type: 'ON_LOAD_OBLIGATION', payload: { id, title } });

  const onResetData = () =>
    manageReportingDataflowDispatch({
      type: 'RESET_STATE',
      payload: { resetData: manageReportingDataflowInitialState }
    });

  const onDeleteInputChange = value =>
    manageReportingDataflowDispatch({ type: 'ON_DELETE_INPUT_CHANGE', payload: { deleteInput: value } });

  const onSave = () => {
    if (formRef.current) formRef.current.handleSubmit(reportingDataflowState.pinDataflow);
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
              checked={reportingDataflowState.pinDataflow}
              id="replaceCheckbox"
              inputId="replaceCheckbox"
              onChange={() =>
                manageReportingDataflowDispatch({
                  type: 'TOGGLE_PIN',
                  payload: !reportingDataflowState.pinDataflow
                })
              }
              role="checkbox"
            />
            <label>
              <span
                onClick={() =>
                  manageReportingDataflowDispatch({
                    type: 'TOGGLE_PIN',
                    payload: !reportingDataflowState.pinDataflow
                  })
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
        label={isEditForm ? resources.messages['save'] : resources.messages['create']}
        onClick={() => (reportingDataflowState.isSubmitting ? {} : onSave())}
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
              hasMaxCharCounter={true}
              id={'deleteDataflow'}
              maxLength={config.INPUT_MAX_LENGTH}
              name={resources.messages['deleteDataflowButton']}
              onChange={event => onDeleteInputChange(event.target.value)}
              ref={deleteInputRef}
              value={reportingDataflowState.deleteInput}
            />
          </p>
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
