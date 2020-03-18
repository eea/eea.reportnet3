import React, { useContext, useEffect, useState } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { isUndefined, isEmpty } from 'lodash';
import moment from 'moment';

import styles from './Documents.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { DocumentFileUpload } from './_components/DocumentFileUpload';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Icon } from 'ui/views/_components/Icon';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DocumentService } from 'core/services/Document';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const Documents = ({
  documents,
  isCustodian,
  isDeletingDocument,
  dataflowId,
  onLoadDocuments,
  setIsDeletingDocument,
  setSortFieldDocuments,
  setSortOrderDocuments,
  sortFieldDocuments,
  sortOrderDocuments
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [documentInitialValues, setDocumentInitialValues] = useState({});
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [isDownloading, setIsDownloading] = useState('');
  const [isEditForm, setIsEditForm] = useState(false);
  const [isFormReset, setIsFormReset] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  const [rowDataState, setRowDataState] = useState();

  useEffect(() => {
    if (!isUndefined(fileToDownload)) {
      DownloadFile(fileToDownload, fileName);
    }
  }, [fileToDownload]);

  const createFileName = title => {
    return `${title.split(' ').join('_')}`;
  };

  const dateColumnTemplate = rowData => {
    return <span>{moment(rowData.date).format('YYYY-MM-DD')}</span>;
  };

  const documentsEditButtons = rowData => {
    return (
      <div className={`${styles.documentsEditButtons} dataflowHelp-document-edit-delete-help-step`}>
        <ActionsColumn
          onDeleteClick={() => {
            setDeleteDialogVisible(true);
            setRowDataState(rowData);
          }}
          onEditClick={() => onEditDocument()}
        />
      </div>
    );
  };

  const downloadColumnTemplate = rowData => {
    return (
      <span
        className={`${styles.downloadIcon} dataflowHelp-document-icon-help-step`}
        onClick={() => onDownloadDocument(rowData)}>
        {isDownloading === rowData.id ? (
          <Icon icon="spinnerAnimate" />
        ) : (
          <div>
            <FontAwesomeIcon icon={AwesomeIcons(rowData.category)} />
          </div>
        )}
      </span>
    );
  };

  const formatBytes = bytes => {
    if (bytes === 0) return '0 B';

    const k = 1024;
    const sizeTypes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];

    const i = Math.floor(Math.log(bytes) / Math.log(k));
    const decimals = i !== 0 ? 2 : 0;

    const bytesParsed = parseFloat(bytes / k ** i).toFixed(decimals);

    const result = { bytesParsed, sizeType: sizeTypes[i] };

    return result;
  };

  const isPublicColumnTemplate = rowData => {
    return <span>{rowData.isPublic ? resources.messages['yes'] : resources.messages['no']}</span>;
  };

  const onCancelDialog = () => {
    setIsFormReset(false);
    setIsUploadDialogVisible(false);
  };

  const onDeleteDocument = async documentData => {
    setDeleteDialogVisible(false);
    notificationContext.add({
      type: 'DELETE_DOCUMENT_INIT_INFO',
      content: {}
    });
    try {
      await DocumentService.deleteDocument(documentData.id);
    } catch (error) {
      notificationContext.add({
        type: 'DELETE_DOCUMENT_ERROR',
        content: {}
      });
      setIsDeletingDocument(false);
    }
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

  const onEditDocument = () => {
    setIsEditForm(true);
    setIsUploadDialogVisible(true);
  };

  const onHideDeleteDialog = () => {
    setDeleteDialogVisible(false);
  };

  const onUploadDocument = () => {
    setIsUploadDialogVisible(false);
  };

  const sizeColumnTemplate = rowData => {
    const formatedRowData = formatBytes(rowData.size);
    return (
      <>
        {formatedRowData.bytesParsed} {formatedRowData.sizeType}
      </>
    );
  };

  const titleColumnTemplate = rowData => {
    return <span onClick={() => onDownloadDocument(rowData)}>{rowData.title}</span>;
  };

  return (
    <>
      {isCustodian ? (
        <Toolbar className={styles.documentsToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-upload dataflowHelp-document-upload-help-step`}
              icon={'upload'}
              label={resources.messages['upload']}
              onClick={() => {
                setIsEditForm(false);
                setIsUploadDialogVisible(true);
              }}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-spin dataflowHelp-document-refresh-help-step`}
              icon={'refresh'}
              label={resources.messages['refresh']}
              onClick={async () => {
                setIsLoading(true);
                await onLoadDocuments();
                setIsLoading(false);
              }}
            />
          </div>
        </Toolbar>
      ) : (
        <></>
      )}

      <DataTable
        autoLayout={true}
        loading={isLoading}
        onRowSelect={e => {
          setDocumentInitialValues(Object.assign({}, e.data));
        }}
        onSort={e => {
          setSortFieldDocuments(e.sortField);
          setSortOrderDocuments(e.sortOrder);
        }}
        paginator={false}
        selectionMode="single"
        sortField={sortFieldDocuments}
        sortOrder={sortOrderDocuments}
        value={documents}>
        <Column
          body={titleColumnTemplate}
          columnResizeMode="expand"
          field="title"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['title']}
          sortable={!isEmpty(documents)}
        />
        <Column
          field="description"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['description']}
          sortable={!isEmpty(documents)}
        />
        <Column
          field="category"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['category']}
          sortable={!isEmpty(documents)}
        />
        <Column
          field="language"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['language']}
          sortable={!isEmpty(documents)}
        />
        <Column
          body={isPublicColumnTemplate}
          field="isPublic"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['documentIsPublic']}
          sortable={!isEmpty(documents)}
        />
        <Column
          body={dateColumnTemplate}
          field="date"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['documentUploadDate']}
          sortable={!isEmpty(documents)}
        />
        <Column
          body={sizeColumnTemplate}
          field="size"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['documentSize']}
          sortable={!isEmpty(documents)}
        />
        <Column
          body={downloadColumnTemplate}
          field="url"
          filter={false}
          filterMatchMode="contains"
          header={resources.messages['file']}
          style={{ textAlign: 'center', width: '8em' }}
        />
        {isCustodian && !isEmpty(documents) ? (
          <Column
            className={styles.crudColumn}
            body={documentsEditButtons}
            header={resources.messages['documentsActionColumns']}
            style={{ width: '5em' }}
          />
        ) : (
          <Column className={styles.hideColumn} />
        )}
      </DataTable>

      <Dialog
        className={styles.dialog}
        dismissableMask={false}
        header={isEditForm ? resources.messages['editDocument'] : resources.messages['uploadDocument']}
        onHide={onCancelDialog}
        visible={isUploadDialogVisible}>
        <DocumentFileUpload
          dataflowId={dataflowId}
          documentInitialValues={documentInitialValues}
          isEditForm={isEditForm}
          isFormReset={isFormReset}
          isUploadDialogVisible={isUploadDialogVisible}
          onUpload={onUploadDocument}
          setIsUploadDialogVisible={setIsUploadDialogVisible}
        />
      </Dialog>

      <ConfirmDialog
        classNameConfirm={'p-button-danger'}
        header={resources.messages['delete']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={() => {
          onDeleteDocument(rowDataState);
          setIsDeletingDocument(true);
        }}
        onHide={onHideDeleteDialog}
        visible={deleteDialogVisible}>
        {resources.messages['deleteDocument']}
      </ConfirmDialog>
    </>
  );
};

export { Documents };
