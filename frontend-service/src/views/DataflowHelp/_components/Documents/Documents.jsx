import { Fragment, useContext, useEffect, useState } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './Documents.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { config } from 'conf';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { DocumentFileUpload } from './_components/DocumentFileUpload';
import { DownloadFile } from 'views/_components/DownloadFile';
import { Icon } from 'views/_components/Icon';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';

import { DocumentService } from 'services/DocumentService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

const Documents = ({
  dataflowId,
  documents,
  isDeletingDocument,
  isLoading,
  isToolbarVisible,
  setIsDeletingDocument,
  setSortFieldDocuments,
  setSortOrderDocuments,
  sortFieldDocuments,
  sortOrderDocuments
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [allDocuments, setAllDocuments] = useState(documents);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [documentInitialValues, setDocumentInitialValues] = useState({
    description: '',
    lang: { label: '', value: '' },
    uploadFile: {},
    isPublic: false
  });
  const [downloadingId, setDownloadingId] = useState('');
  const [fileDeletingId, setFileDeletingId] = useState(null);
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [fileUpdatingId, setFileUpdatingId] = useState(null);
  const [isEditForm, setIsEditForm] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  const [rowDataState, setRowDataState] = useState();

  useEffect(() => {
    setAllDocuments(documents);
  }, [documents]);

  useEffect(() => {
    if (!isNil(fileToDownload)) DownloadFile(fileToDownload, fileName);
  }, [fileToDownload]);

  const createFileName = title => `${title.split(' ').join('_')}`;

  const dateColumnTemplate = rowData => <span>{dayjs(rowData.date).format(userContext.userProps.dateFormat)}</span>;

  const documentsEditButtons = rowData => (
    <div className={`${styles.documentsEditButtons} dataflowHelp-document-edit-delete-help-step`}>
      <ActionsColumn
        isDeletingDocument={isDeletingDocument}
        isUpdating={isUpdating}
        onDeleteClick={() => {
          setDocumentInitialValues(rowData);
          setDeleteDialogVisible(true);
          setRowDataState(rowData);
        }}
        onEditClick={() => {
          const langField = config.languages.filter(language => language.name === rowData.language[0]);

          rowData = { ...rowData, lang: { label: langField[0].name, value: langField[0].code } };

          setDocumentInitialValues(rowData);
          onEditDocument();
        }}
        rowDataId={rowData.id}
        rowDeletingId={fileDeletingId}
        rowUpdatingId={fileUpdatingId}
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
          <FontAwesomeIcon aria-label={resources.messages['downloadFile']} icon={AwesomeIcons(rowData.category)} />
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

  const getAllDocuments = () => {
    const inmAllDocuments = [...allDocuments];
    const filteredAllDocuments = inmAllDocuments.filter(document => document.id !== fileDeletingId);
    setAllDocuments(filteredAllDocuments);
  };

  useCheckNotifications(['DELETE_DOCUMENT_COMPLETED_EVENT'], getAllDocuments);

  useCheckNotifications(
    [
      'UPLOAD_DOCUMENT_COMPLETED_EVENT',
      'UPLOAD_DOCUMENT_FAILED_EVENT',
      'UPDATED_DOCUMENT_COMPLETED_EVENT',
      'UPDATED_DOCUMENT_FAILED_EVENT'
    ],
    setIsUpdating,
    false
  );

  const isPublicColumnTemplate = rowData => (
    <span>
      {rowData.isPublic ? (
        <FontAwesomeIcon aria-label={resources.messages['documentIsPublic']} icon={AwesomeIcons('check')} />
      ) : (
        ''
      )}
    </span>
  );

  const onCancelDialog = () => {
    setIsUploadDialogVisible(false);
  };

  const onDeleteDocument = async document => {
    setFileDeletingId(document.id);
    notificationContext.add({ type: 'DELETE_DOCUMENT_INIT_INFO' });

    try {
      await DocumentService.delete(document.id);
    } catch (error) {
      console.error('Documents - onDeleteDocument.', error);
      notificationContext.add({ type: 'DELETE_DOCUMENT_ERROR', content: {} });
      setIsDeletingDocument(false);
      setFileDeletingId('');
    } finally {
      setDeleteDialogVisible(false);
    }
  };

  const onDownloadDocument = async document => {
    try {
      setDownloadingId(document.id);
      setFileName(createFileName(document.title));
      const { data } = await DocumentService.download(document.id);
      setFileToDownload(data);
    } catch (error) {
      console.error('Documents - onDownloadDocument.', error);
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
      {isToolbarVisible && (
        <Toolbar className={styles.documentsToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary-transparent dataflowHelp-document-upload-help-step`}
              icon={'upload'}
              label={resources.messages['upload']}
              onClick={() => {
                setDocumentInitialValues({
                  description: '',
                  lang: { label: '', value: '' },
                  uploadFile: {},
                  isPublic: false
                });
                setIsEditForm(false);
                setIsUploadDialogVisible(true);
              }}
            />
          </div>
        </Toolbar>
      )}

      <DataTable
        autoLayout={true}
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
          className={styles.iconStyle}
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
        {isToolbarVisible && !isEmpty(documents) ? (
          <Column
            body={documentsEditButtons}
            className={styles.crudColumn}
            header={resources.messages['documentsActionColumns']}
            style={{ width: '5em' }}
          />
        ) : (
          <Column className={styles.emptyTableHeader} header={resources.messages['documentsActionColumns']} />
        )}
      </DataTable>

      {isLoading && isEmpty(documents) && <Spinner style={{ top: 0 }} />}

      {!isLoading && isEmpty(documents) && (
        <div className={styles.noDataWrapper}>
          <h4>{resources.messages['noDocuments']}</h4>
        </div>
      )}

      {isUploadDialogVisible && (
        <Dialog
          className={styles.dialog}
          header={isEditForm ? resources.messages['editDocument'] : resources.messages['uploadDocument']}
          onHide={onCancelDialog}
          visible={isUploadDialogVisible}>
          <DocumentFileUpload
            dataflowId={dataflowId}
            documentInitialValues={documentInitialValues}
            isEditForm={isEditForm}
            isUploadDialogVisible={isUploadDialogVisible}
            onUpload={onUploadDocument}
            setFileUpdatingId={setFileUpdatingId}
            setIsUpdating={setIsUpdating}
            setIsUploadDialogVisible={setIsUploadDialogVisible}
          />
        </Dialog>
      )}

      {deleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={isDeletingDocument}
          header={resources.messages['delete']}
          iconConfirm={isDeletingDocument ? 'spinnerAnimate' : 'check'}
          isDeleting={isDeletingDocument}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => {
            setIsDeletingDocument(true);
            onDeleteDocument(rowDataState);
          }}
          onHide={onHideDeleteDialog}
          visible={deleteDialogVisible}>
          {resources.messages['deleteDocument']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};

export { Documents };
