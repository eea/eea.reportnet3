import React, { Fragment, useContext, useReducer } from 'react';

import styles from './ManageManuelAcceptanceDataset.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { RadioButton } from 'ui/views/_components/RadioButton';

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
      datasetFeedbackStatus: dataset.feedbackStatus,
      updateButtonEnabled: false
    }
  );

  const { datasetId, datasetName } = dataset;

  const onBlurMessage = message => {
    if (message !== manageManualAcceptanceDatasetState.initialDatasetMessage) {
      return manageManualAcceptanceDatasetDispatch({ type: 'UPDATE_BUTTON_DISABLED', payload: { value: true } });
    }
  };

  const onChangeMessage = message => {
    if (message !== manageManualAcceptanceDatasetState.initialDatasetMessage) {
      return manageManualAcceptanceDatasetDispatch({
        type: 'ON_UPDATE_MESSAGE',
        payload: { message: message, value: true }
      });
    }
  };

  const onChangeStatus = value =>
    manageManualAcceptanceDatasetDispatch({ type: 'ON_CHANGE_STATUS', payload: { value } });

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      manageManualAcceptanceDatasetDispatch({
        type: 'ON_UPDATE_MESSAGE',
        payload: { value: manageManualAcceptanceDatasetState.initialDatasetMessage }
      });
    } else if (event.key === 'Enter') {
      event.preventDefault();
      const value = event.target.value;
      manageManualAcceptanceDatasetDispatch({ type: 'ON_UPDATE_MESSAGE', payload: { value } });
      onBlurMessage(event.target.value);
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
        manageDialogs(false);
      }
    } catch (error) {
      notificationContext.add({ type: 'UPDATE_DATASET_FEEDBACK_STATUS_ERROR' });
    }
  };

  const renderDialogFooter = (
    <Fragment>
      <Button
        className="p-button-primary p-button-animated-blink"
        disabled={!manageManualAcceptanceDatasetState.updateButtonEnabled}
        icon={'check'}
        label={resources.messages['update']}
        onClick={() => onUpdateDataset()}
      />
      <Button
        className="p-button-secondary p-button-animated-blink"
        icon={'cancel'}
        label={resources.messages['close']}
        onClick={() => manageDialogs(false)}
      />
    </Fragment>
  );
  const renderDialogLayout = children => (
    <Fragment>
      {isManageDatasetDialogVisible && (
        <Dialog
          footer={renderDialogFooter}
          header={`${resources.messages['editStatusDataset']} ${datasetName}`}
          onHide={() => manageDialogs(false)}
          style={{ width: '60%' }}
          visible={isManageDatasetDialogVisible}>
          {children}
        </Dialog>
      )}
    </Fragment>
  );

  const renderInputTextLayout = (options = []) => {
    return options.map((option, index) => (
      <div key={index}>
        <label htmlFor="datasetMessage">{resources.messages[option]}</label>
        <InputTextarea
          className={`${styles.datasetMessage} datasetSchema-metadata-help-step`}
          collapsedHeight={55}
          expandableOnClick={true}
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
          // value={manageManualAcceptanceDatasetState.datasetMessage}
        />
      </div>
    ));
  };

  const renderRadioButtons = () =>
    ['correctionRequested', 'technicallyAccept'].map(feedBackStatus => {
      return (
        <div className={styles.radioButton} key={feedBackStatus}>
          <label className={styles.label} htmlFor={feedBackStatus}>
            {resources.messages[feedBackStatus]}
          </label>
          <RadioButton
            className={styles.button}
            checked={manageManualAcceptanceDatasetState.datasetFeedbackStatus === feedBackStatus}
            inputId={feedBackStatus}
            onChange={event => {
              onChangeStatus(event.target.value);
            }}
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
        <div className={styles.group}>{renderInputTextLayout(['message'])}</div>
      </div>
    </Fragment>
  );
};
