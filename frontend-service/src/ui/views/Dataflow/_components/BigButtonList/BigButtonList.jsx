import React, { useContext, useState } from 'react';

import styles from './BigButtonList.module.css';

import { BigButton } from './_components/BigButton';
import { Button } from 'ui/views/_components/Button';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { NewDatasetSchemaForm } from './_components/NewDatasetSchemaForm';

import { DatasetService } from 'core/services/Dataset';

import { LoadingContext } from 'ui/views/_functions/Contexts/LoadingContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBigButtonList } from './_functions/Hooks/useBigButtonList';

export const BigButtonList = ({
  dataflowData,
  dataflowId,
  dataflowStatus,
  designDatasetSchemas,
  handleRedirect,
  hasWritePermissions,
  isCustodian,
  onUpdateData,
  onSaveName,
  showReleaseSnapshotDialog,
  updatedDatasetSchema
}) => {
  const { showLoading, hideLoading } = useContext(LoadingContext);
  const resources = useContext(ResourcesContext);

  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [deleteSchemaIndex, setDeleteSchemaIndex] = useState();
  const [errorDialogVisible, setErrorDialogVisible] = useState(false);
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

  const onCreateDatasetSchema = () => {
    setNewDatasetDialog(false);
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

  return (
    <>
      <div className={styles.buttonsWrapper}>
        <div className={styles.splitButtonWrapper}>
          <div className={styles.datasetItem}>
            {useBigButtonList({
              dataflowData: dataflowData,
              dataflowId: dataflowId,
              dataflowStatus: dataflowStatus,
              getDeleteSchemaIndex: getDeleteSchemaIndex,
              handleRedirect: handleRedirect,
              hasWritePermissions: hasWritePermissions,
              isCustodian: isCustodian,
              onDatasetSchemaNameError: onDatasetSchemaNameError,
              onDuplicateName: onDuplicateName,
              onSaveName: onSaveName,
              onShowNewSchemaDialog: onShowNewSchemaDialog,
              showReleaseSnapshotDialog: showReleaseSnapshotDialog,
              updatedDatasetSchema: updatedDatasetSchema
            }).map(button => (button.visibility ? <BigButton {...button} /> : <></>))}
          </div>

          <div className={`${styles.datasetItem}`}>
            <BigButton layout="dataCollection" caption="Create data collection" />
          </div>
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
    </>
  );
};
