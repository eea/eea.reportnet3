import React, { useState, useEffect, useContext, useRef } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { isUndefined } from 'lodash';

import styles from './Documents.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { DocumentFileUpload } from './_components/DocumentFileUpload';
import { Growl } from 'primereact/growl';

import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Icon } from 'ui/views/_components/Icon';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DocumentService } from 'core/services/Document';

const Documents = ({ documents, isCustodian, match, onLoadDocuments }) => {
  const resources = useContext(ResourcesContext);

  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [documentInitialValues, setDocumentInitialValues] = useState({});
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [isDownloading, setIsDownloading] = useState('');
  const [isFormReset, setIsFormReset] = useState(true);
  const [isEditForm, setIsEditForm] = useState(false);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  const [rowDataState, setRowDataState] = useState();

  const growlRef = useRef();

  useEffect(() => {
    if (!isUndefined(fileToDownload)) {
      DownloadFile(fileToDownload, fileName);
    }
  }, [fileToDownload]);

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  const onUploadDocument = () => {
    setIsUploadDialogVisible(false);
  };

  const onCancelDialog = () => {
    setIsUploadDialogVisible(false);
    setIsFormReset(false);
  };

  const onDeleteDocument = async documentData => {
    setDeleteDialogVisible(false);
    try {
      const response = await DocumentService.deleteDocument(documentData.id);
      if (response >= 200 && response <= 299) {
        documents.filter(document => document.id !== documentData.id);
      }
    } catch (error) {
      console.error(error.response);
    }
  };

  const onHideDeleteDialog = () => {
    setDeleteDialogVisible(false);
  };

  const onEditDocument = () => {
    setIsEditForm(true);
    setIsUploadDialogVisible(true);
  };

  const documentsEditButtons = rowData => {
    return (
      <div className={styles.documentsEditButtons}>
        <Button
          type="button"
          icon="edit"
          className={`${`p-button-rounded p-button-secondary ${styles.editRowButton}`}`}
          onClick={e => {
            onEditDocument();
          }}
        />
        <Button
          type="button"
          icon="trash"
          className={`${`p-button-rounded p-button-secondary ${styles.deleteRowButton}`}`}
          onClick={() => {
            setDeleteDialogVisible(true);
            setRowDataState(rowData);
          }}
        />
      </div>
    );
  };

  const onDownloadDocument = async rowData => {
    try {
      setIsDownloading(rowData.id);
      setFileName(createFileName(rowData.title));
      setFileToDownload(await DocumentService.downloadDocumentById(rowData.id));
    } catch (error) {
      console.error(error.response);
    } finally {
      setIsDownloading('');
    }
  };

  const createFileName = title => {
    return `${title.split(' ').join('_')}`;
  };

  const downloadTemplate = rowData => {
    switch (rowData.category) {
    }
    return (
      <span className={styles.downloadIcon} onClick={() => onDownloadDocument(rowData)}>
        {isDownloading === rowData.id ? (
          <Icon icon="spinnerAnimate" />
        ) : (
          <FontAwesomeIcon icon={AwesomeIcons(rowData.category)} />
        )}
      </span>
    );
  };

  return (
    <>
      <Growl ref={growlRef} />
      {isCustodian ? (
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={'export'}
              label={resources.messages['upload']}
              onClick={() => {
                setIsEditForm(false);
                setIsUploadDialogVisible(true);
              }}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={false}
              icon={'refresh'}
              label={resources.messages['refresh']}
              onClick={() => onLoadDocuments()}
            />
          </div>
        </Toolbar>
      ) : (
        <></>
      )}

      <DataTable
        value={documents}
        autoLayout={true}
        paginator={false}
        selectionMode="single"
        onRowSelect={e => {
          setDocumentInitialValues(Object.assign({}, e.data));
        }}>
        {isCustodian ? (
          <Column className={styles.crudColumn} body={documentsEditButtons} style={{ width: '5em' }} />
        ) : (
          <Column className={styles.hideColumn} />
        )}
        <Column
          columnResizeMode="expand"
          field="title"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['title']}
          sortable={true}
        />
        <Column
          field="description"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['description']}
          sortable={true}
        />
        <Column
          field="category"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['category']}
          sortable={true}
        />
        <Column
          field="language"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['language']}
          sortable={true}
        />
        <Column
          field="isPublic"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['documentIsPublic']}
          sortable={true}
        />
        <Column
          field="date"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['documentUploadDate']}
          sortable={true}
        />
        <Column
          field="size"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['documentSize']}
          sortable={true}
        />
        <Column
          body={downloadTemplate}
          field="url"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['file']}
          style={{ textAlign: 'center', width: '8em' }}
        />
      </DataTable>

      <Dialog
        header={isEditForm ? resources.messages['edit'] : resources.messages['upload']}
        className={styles.dialog}
        visible={isUploadDialogVisible}
        dismissableMask={false}
        onHide={onCancelDialog}>
        <DocumentFileUpload
          dataflowId={match.params.dataflowId}
          onUpload={onUploadDocument}
          onGrowlAlert={onGrowlAlert}
          isEditForm={isEditForm}
          isFormReset={isFormReset}
          documentInitialValues={documentInitialValues}
          setIsUploadDialogVisible={setIsUploadDialogVisible}
        />
      </Dialog>

      <ConfirmDialog
        header={resources.messages['delete']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={() => onDeleteDocument(rowDataState)}
        onHide={onHideDeleteDialog}
        visible={deleteDialogVisible}>
        {resources.messages['deleteDocument']}
      </ConfirmDialog>
    </>
  );
};

export { Documents };
