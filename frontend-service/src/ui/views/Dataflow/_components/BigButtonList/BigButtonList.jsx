import React, { useContext, useState } from 'react';

import { isUndefined, remove, uniq } from 'lodash';
import { PDFDownloadLink } from '@react-pdf/renderer';

import styles from './BigButtonList.module.css';

import { BigButton } from './_components/BigButton';
import { Button } from 'ui/views/_components/Button';
import { Calendar } from 'ui/views/_components/Calendar/Calendar';
import { ConfirmationReceipt } from '../ConfirmationReceipt';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { NewDatasetSchemaForm } from './_components/NewDatasetSchemaForm';

import { DatasetService } from 'core/services/Dataset';
import { DataCollectionService } from 'core/services/DataCollection';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';

import { MetadataUtils } from 'ui/views/_functions/Utils';

export const BigButtonList = ({
  dataflowData,
  dataflowId,
  dataflowStatus,
  designDatasetSchemas,
  handleRedirect,
  hasRepresentatives,
  hasWritePermissions,
  isCustodian,
  isDataSchemaCorrect,
  onUpdateData,
  onSaveName,
  showReleaseSnapshotDialog,
  updatedDatasetSchema,
  setUpdatedDatasetSchema
}) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [dataCollectionDialog, setDataCollectionDialog] = useState(false);
  const [dataCollectionDueDate, setDataCollectionDueDate] = useState();
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [deleteSchemaIndex, setDeleteSchemaIndex] = useState();
  const [errorDialogVisible, setErrorDialogVisible] = useState(false);
  const [isCreateButtonActive, setIsCreateButtonActive] = useState(true);
  const [isDuplicated, setIsDuplicated] = useState(false);
  const [isFormReset, setIsFormReset] = useState(true);
  const [newDatasetDialog, setNewDatasetDialog] = useState(false);

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['ok']}
        icon="check"
        onClick={() => {
          setErrorDialogVisible(false);
          setIsDuplicated(false);
        }}
      />
    </div>
  );

  const getDeleteSchemaIndex = index => {
    setDeleteSchemaIndex(index);
    setDeleteDialogVisible(true);
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      notificationContext.add({
        type: 'GET_METADATA_ERROR',
        content: {
          dataflowId
        }
      });
    }
  };

  const onCreateDatasetSchema = () => {
    setNewDatasetDialog(false);
  };

  const onCreateDataCollection = async date => {
    setDataCollectionDialog(false);
    notificationContext.add({
      type: 'CREATE_DATA_COLLECTION_INIT',
      content: {}
    });
    setIsCreateButtonActive(false);
    try {
      return await DataCollectionService.create(dataflowId, date);
    } catch (error) {
      const {
        dataflow: { name: dataflowName }
      } = await getMetadata({ dataflowId });
      notificationContext.add({
        type: 'CREATE_DATA_COLLECTION_ERROR',
        content: {
          dataflowId,
          dataflowName
        }
      });
      setIsCreateButtonActive(true);
    }
  };

  const onDatasetSchemaNameError = () => {
    setErrorDialogVisible(true);
  };

  const onDeleteDatasetSchema = async index => {
    setDeleteDialogVisible(false);
    showLoading();
    try {
      const response = await DatasetService.deleteSchemaById(designDatasetSchemas[index].datasetId);
      if (response >= 200 && response <= 299) {
        onUpdateData();
        setUpdatedDatasetSchema(remove(updatedDatasetSchema, event => event.schemaIndex != index));
      }
    } catch (error) {
      console.error(error.response);
    } finally {
      hideLoading();
    }
  };

  const onDuplicateName = () => {
    setIsDuplicated(true);
  };

  const onHideErrorDialog = () => {
    setErrorDialogVisible(false);
    setIsDuplicated(false);
  };

  const onShowNewSchemaDialog = () => {
    setNewDatasetDialog(true);
    setIsFormReset(true);
  };

  const onShowDataCollectionModal = () => {
    setDataCollectionDialog(true);
  };

  const onUpdatedButtonList = () => {
    const receiptButton = (
      <PDFDownloadLink
        document={<ConfirmationReceipt dataflowData={dataflowData} />}
        fileName={`${dataflowData.name}_${Date.now()}.pdf`}>
        {({ blob, url, loading, error }) => {
          const { datasets } = dataflowData;
          const representatives = datasets.map(dataset => {
            return dataset.datasetSchemaName;
          });
          const isReleased = datasets.map(dataset => {
            return dataset.isReleased;
          });
          if (!isCustodian && uniq(representatives).length === 1 && !isReleased.includes(false)) {
            return (
              <BigButton
                layout="defaultBigButton"
                buttonClass="schemaDataset"
                buttonIcon={loading ? 'spinner' : 'fileDownload'}
                buttonIconClass={loading ? 'spinner' : ''}
                caption={resources.messages['confirmationReceipt']}
              />
            );
          }
        }}
      </PDFDownloadLink>
    );
    return [...bigButtonList, receiptButton];
  };

  const bigButtonList = useBigButtonList({
    dataflowData: dataflowData,
    dataflowId: dataflowId,
    dataflowStatus: dataflowStatus,
    getDeleteSchemaIndex: getDeleteSchemaIndex,
    handleRedirect: handleRedirect,
    hasRepresentatives: hasRepresentatives,
    hasWritePermissions: hasWritePermissions,
    isCreateButtonActive: isCreateButtonActive,
    isCustodian: isCustodian,
    isDataSchemaCorrect: isDataSchemaCorrect,
    onDatasetSchemaNameError: onDatasetSchemaNameError,
    onDuplicateName: onDuplicateName,
    onSaveName: onSaveName,
    onShowDataCollectionModal: onShowDataCollectionModal,
    onShowNewSchemaDialog: onShowNewSchemaDialog,
    showReleaseSnapshotDialog: showReleaseSnapshotDialog,
    updatedDatasetSchema: updatedDatasetSchema
  })
    .filter(button => button.visibility)
    .map((button, i) => <BigButton key={i} {...button} />);

  return (
    <>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>{onUpdatedButtonList()}</div>
        </div>
      </div>

      <Dialog
        header={resources.messages['newDatasetSchema']}
        visible={newDatasetDialog}
        className={styles.dialog}
        dismissableMask={false}
        onHide={() => {
          setNewDatasetDialog(false);
          setIsFormReset(false);
        }}>
        <NewDatasetSchemaForm
          dataflowId={dataflowId}
          datasetSchemaInfo={updatedDatasetSchema}
          isFormReset={isFormReset}
          onCreate={onCreateDatasetSchema}
          onUpdateData={onUpdateData}
          setNewDatasetDialog={setNewDatasetDialog}
        />
      </Dialog>

      <Dialog
        footer={errorDialogFooter}
        header={resources.messages['error'].toUpperCase()}
        onHide={onHideErrorDialog}
        visible={isDuplicated}>
        <div className="p-grid p-fluid">{resources.messages['duplicateSchemaError']}</div>
      </Dialog>

      <Dialog
        footer={errorDialogFooter}
        header={resources.messages['error'].toUpperCase()}
        onHide={onHideErrorDialog}
        visible={errorDialogVisible}>
        <div className="p-grid p-fluid">{resources.messages['emptyDatasetSchema']}</div>
      </Dialog>

      <ConfirmDialog
        header={resources.messages['delete'].toUpperCase()}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        onConfirm={() => onDeleteDatasetSchema(deleteSchemaIndex)}
        onHide={() => setDeleteDialogVisible(false)}
        visible={deleteDialogVisible}>
        {resources.messages['deleteDatasetSchema']}
      </ConfirmDialog>

      <ConfirmDialog
        header={resources.messages['createDataCollection']}
        disabledConfirm={isUndefined(dataCollectionDueDate)}
        labelCancel={resources.messages['close']}
        labelConfirm={resources.messages['create']}
        onConfirm={() => onCreateDataCollection(new Date(dataCollectionDueDate).getTime() / 1000)}
        onHide={() => setDataCollectionDialog(false)}
        visible={dataCollectionDialog}>
        <p>{`${resources.messages['chooseExpirationDate']}: `}</p>
        <Calendar
          className={styles.calendar}
          inline={true}
          monthNavigator={true}
          minDate={new Date()}
          onChange={event => setDataCollectionDueDate(event.target.value)}
          showWeek={true}
          value={dataCollectionDueDate}
          yearNavigator={true}
          yearRange="2020:2030"
        />
      </ConfirmDialog>
    </>
  );
};
