import React, { Fragment, useContext, useReducer } from 'react';

import styles from './ManageManualAcceptanceDataset.module.scss';

import camelCase from 'lodash/camelCase';
import isEmpty from 'lodash/isEmpty';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { RadioButton } from 'ui/views/_components/RadioButton';
import ReactTooltip from 'react-tooltip';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DatasetService } from 'core/services/Dataset';

import { manageManualAcceptanceDatasetReducer } from './_functions/Reducers/manageManualAcceptanceDatasetReducer';

export const ManageManualAcceptanceDataset = ({
  dataflowId,
  dataset,
  isManageDatasetDialogVisible,
  manageDialogs,
  onUpdatedData
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [manageManualAcceptanceDatasetState, manageManualAcceptanceDatasetDispatch] = useReducer(
    manageManualAcceptanceDatasetReducer,
    {
      initialDatasetMessage: '',
      datasetMessage: '',
      datasetFeedbackStatus: dataset.feedbackStatus
    }
  );

  const { datasetId, datasetName } = dataset;

  const onChangeMessage = message => {
    if (message !== manageManualAcceptanceDatasetState.initialDatasetMessage) {
      return manageManualAcceptanceDatasetDispatch({
        type: 'ON_UPDATE_MESSAGE',
        payload: { message }
      });
    }
  };

  const onChangeStatus = value =>
    manageManualAcceptanceDatasetDispatch({ type: 'ON_CHANGE_STATUS', payload: { value } });

  const onKeyChange = event => {
    if (event.key === 'Enter') {
      event.preventDefault();
      const value = event.target.value;
      manageManualAcceptanceDatasetDispatch({ type: 'ON_UPDATE_MESSAGE', payload: { value } });
      !isEmpty(manageManualAcceptanceDatasetState.datasetMessage) &&
        !isEmpty(manageManualAcceptanceDatasetState.datasetFeedbackStatus) &&
        onUpdateDataset();
    }
  };

  const onUpdateDataset = async () => {
    try {
      const response = await DatasetService.updateDatasetFeedbackStatus(
        dataflowId,
        datasetId,
        manageManualAcceptanceDatasetState.datasetMessage,
        manageManualAcceptanceDatasetState.datasetFeedbackStatus
      );

      if (response.status >= 200 && response.status <= 299) {
        onUpdatedData(true);
      }
    } catch (error) {
      notificationContext.add({ type: 'UPDATE_DATASET_FEEDBACK_STATUS_ERROR' });
    } finally {
      manageDialogs(false);
    }
  };

  const renderDialogFooter = (
    <Fragment>
      <span data-tip data-for="createTooltip">
        <Button
          className="p-button-primary p-button-animated-blink"
          disabled={
            isEmpty(manageManualAcceptanceDatasetState.datasetMessage) ||
            isEmpty(manageManualAcceptanceDatasetState.datasetFeedbackStatus)
          }
          icon={'check'}
          label={resources.messages['update']}
          onClick={() => onUpdateDataset()}
        />
      </span>
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs(false)}
      />
      {(isEmpty(manageManualAcceptanceDatasetState.datasetMessage) ||
        isEmpty(manageManualAcceptanceDatasetState.datasetFeedbackStatus)) && (
        <ReactTooltip className={styles.tooltipClass} effect="solid" id="createTooltip" place="top">
          <span>{resources.messages['fcSubmitButtonDisabled']}</span>
        </ReactTooltip>
      )}
    </Fragment>
  );
  const renderDialogLayout = children => (
    <Fragment>
      {isManageDatasetDialogVisible && (
        <Dialog
          footer={renderDialogFooter}
          header={`${resources.messages['editStatusDataset']} ${datasetName}`}
          onHide={() => manageDialogs(false)}
          style={{ width: '50%' }}
          visible={isManageDatasetDialogVisible}>
          {children}
        </Dialog>
      )}
    </Fragment>
  );

  const renderInputTextLayout = option => (
    <span>
      <label htmlFor="datasetMessage">{resources.messages[option]}</label>
      <InputTextarea
        className={`${styles.datasetMessage} datasetSchema-metadata-help-step`}
        collapsedHeight={85}
        // expandableOnClick={true}
        id="datasetMessage"
        key="datasetMessage"
        onChange={e => onChangeMessage(e.target.value)}
        onFocus={e =>
          manageManualAcceptanceDatasetDispatch({
            type: 'INITIAL_DATASET_MESSAGE',
            payload: { value: e.target.value }
          })
        }
        onKeyDown={e => onKeyChange(e)}
        placeholder={resources.messages['message']}
      />
    </span>
  );

  const renderRadioButtons = () =>
    ['Technically accept', 'Correction requested'].map(feedBackStatus => {
      return (
        <div className={styles.radioButtonWrapper} key={feedBackStatus}>
          <label className={styles.label} htmlFor={feedBackStatus}>
            {resources.messages[camelCase(feedBackStatus)]}
          </label>
          <RadioButton
            className={styles.radioButton}
            checked={manageManualAcceptanceDatasetState.datasetFeedbackStatus === feedBackStatus}
            inputId={feedBackStatus}
            onChange={event => onChangeStatus(event.target.value)}
            value={feedBackStatus}
          />
        </div>
      );
    });

  return renderDialogLayout(
    <Fragment>
      <div className={styles.content}>
        <div className={styles.group}>
          <div>{resources.messages['feedbackStatus']}</div>
          {renderRadioButtons()}
        </div>
        <div className={styles.group}>{renderInputTextLayout('message')}</div>
      </div>
    </Fragment>
  );
};
