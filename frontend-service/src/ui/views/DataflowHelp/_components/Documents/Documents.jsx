import React, { Fragment, useContext, useEffect, useState } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
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
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const Documents = ({
  documents,
  isCustodian,
  isDeletingDocument,
  dataflowId,
  setIsDeletingDocument,
  setSortFieldDocuments,
  setSortOrderDocuments,
  sortFieldDocuments,
  sortOrderDocuments
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [allDocuments, setAllDocuments] = useState(documents);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [documentInitialValues, setDocumentInitialValues] = useState({});
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [downloadingId, setDownloadingId] = useState('');
  const [isEditForm, setIsEditForm] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  const [rowDataState, setRowDataState] = useState();

  useEffect(() => {
    setAllDocuments(documents);
  }, [documents]);

  useEffect(() => {
    if (!isNil(fileToDownload)) DownloadFile(fileToDownload, fileName);
  }, [fileToDownload]);

  const createFileName = title => `${title.split(' ').join('_')}`;

  const dateColumnTemplate = rowData => <span>{moment(rowData.date).format(user.userProps.dateFormat)}</span>;

  const documentsEditButtons = rowData => (
    <div className={`${styles.documentsEditButtons} dataflowHelp-document-edit-delete-help-step`}>
      <ActionsColumn
        isDeletingDocument={isDeletingDocument}
        onDeleteClick={() => {
          setDeleteDialogVisible(true);
          setRowDataState(rowData);
        }}
        onEditClick={() => onEditDocument()}
      />
    </div>
  );

  const downloadColumnTemplate = rowData => (
    <span
      className={`${styles.downloadIcon} dataflowHelp-document-icon-help-step`}
      onClick={() => onDownloadDocument(rowData)}>
      {downloadingId === rowData.id ? (
        <Icon icon="spinnerAnimate" />
      ) : (
        <div>
          <FontAwesomeIcon icon={AwesomeIcons(rowData.category)} />
        </div>
      )}
    </span>
  );

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

  const isPublicColumnTemplate = rowData => (
    <span>{rowData.isPublic ? resources.messages['yes'] : resources.messages['no']}</span>
  );

  const onCancelDialog = () => {
    setIsUploadDialogVisible(false);
  };

  const onDeleteDocument = async documentData => {
    setDeleteDialogVisible(false);
    notificationContext.add({ type: 'DELETE_DOCUMENT_INIT_INFO' });

    try {
      await DocumentService.deleteDocument(documentData.id);
      const inmAllDocuments = [...allDocuments];
      const filteredAllDocuments = inmAllDocuments.filter(document => document.id !== documentData.id);
      setAllDocuments(filteredAllDocuments);
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
      setDownloadingId(rowData.id);
      setFileName(createFileName(rowData.title));
      setFileToDownload(await DocumentService.downloadDocumentById(rowData.id));
    } catch (error) {
      console.error(error.response);
    } finally {
      setDownloadingId('');
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
      <Fragment>
        {formatedRowData.bytesParsed} {formatedRowData.sizeType}
      </Fragment>
    );
  };

  const titleColumnTemplate = rowData => <span onClick={() => onDownloadDocument(rowData)}>{rowData.title}</span>;

  return (
    <Fragment>
      {isCustodian ? (
        <Toolbar className={styles.documentsToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary-transparent dataflowHelp-document-upload-help-step`}
              icon={'upload'}
              label={resources.messages['upload']}
              onClick={() => {
                setIsEditForm(false);
                setIsUploadDialogVisible(true);
              }}
            />
          </div>
        </Toolbar>
      ) : (
        <Fragment></Fragment>
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
        value={allDocuments}>
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
      {documents.length === 0 && (
        <div className={styles.noDataWrapper}>
          <h4>{resources.messages['noDocuments']}</h4>
        </div>
      )}

      {isUploadDialogVisible && (
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
            isUploadDialogVisible={isUploadDialogVisible}
            onUpload={onUploadDocument}
            setIsUploadDialogVisible={setIsUploadDialogVisible}
          />
        </Dialog>
      )}

      <ConfirmDialog
        classNameConfirm={'p-button-danger'}
        header={resources.messages['delete']}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={() => {
          setIsDeletingDocument(true);
          onDeleteDocument(rowDataState);
        }}
        onHide={onHideDeleteDialog}
        visible={deleteDialogVisible}>
        {resources.messages['deleteDocument']}
      </ConfirmDialog>
    </Fragment>
  );
};

export { Documents };
