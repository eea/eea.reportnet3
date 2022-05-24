import { Fragment, useContext, useEffect, useState, useRef } from 'react';

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

import { FileUtils } from 'views/_functions/Utils/FileUtils';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

export const Documents = ({
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
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [allDocuments, setAllDocuments] = useState(documents);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [downloadingId, setDownloadingId] = useState('');
  const [fileDeletingId, setFileDeletingId] = useState(null);
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [fileUpdatingId, setFileUpdatingId] = useState(null);
  const [isEditForm, setIsEditForm] = useState(false);
  const [isSubmitting, setSubmitting] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [rowDataState, setRowDataState] = useState();
  const [documentInitialValues, setDocumentInitialValues] = useState({
    description: '',
    lang: { label: '', value: '' },
    uploadFile: {},
    isPublic: false
  });

  const footerRef = useRef(null);

  useEffect(() => {
    setAllDocuments(documents);
  }, [documents]);

  useEffect(() => {
    if (!isNil(fileToDownload)) {
      DownloadFile(fileToDownload, fileName);
    }
  }, [fileToDownload]);

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

  const renderDownloadIcon = rowData => {
    if (downloadingId === rowData.id) {
      return <Icon icon="spinnerAnimate" />;
    }

    return (
      <div>
        <FontAwesomeIcon aria-label={resourcesContext.messages['downloadFile']} icon={AwesomeIcons(rowData.category)} />
      </div>
    );
  };

  const downloadColumnTemplate = rowData => (
    <span
      className={`${styles.downloadIcon} dataflowHelp-document-icon-help-step`}
      onClick={() => onDownloadDocument(rowData)}>
      {renderDownloadIcon(rowData)}
    </span>
  );

  const onConfirm = () => {
    if (footerRef.current) {
      footerRef.current.onConfirm();
    }
  };

  const dialogFooter = (
    <div className={`${styles.buttonWrap} ui-dialog-buttonpane p-clearfix`}>
      <Button
        disabled={isSubmitting || isUploading}
        icon={!isUploading ? (isEditForm ? 'check' : 'add') : 'spinnerAnimate'}
        label={isEditForm ? resourcesContext.messages['save'] : resourcesContext.messages['upload']}
        onClick={() => onConfirm()}
      />
      <Button
        className={`${styles.cancelButton} p-button-secondary button-right-aligned`}
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={() => setIsUploadDialogVisible(false)}
      />
    </div>
  );

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

  const renderPublicIcon = rowData => {
    if (rowData.isPublic) {
      return <FontAwesomeIcon aria-label={resourcesContext.messages['isPublic']} icon={AwesomeIcons('check')} />;
    }
  };

  const isPublicColumnTemplate = rowData => (
    <div className={styles.iconStyle}>
      <span>{renderPublicIcon(rowData)}</span>
    </div>
  );

  const onCancelDialog = () => {
    setIsUploadDialogVisible(false);
  };

  const onDeleteDocument = async document => {
    setFileDeletingId(document.id);
    notificationContext.add({ type: 'DELETE_DOCUMENT_INIT_INFO' });

    try {
      await DocumentService.delete(document.id, dataflowId);
    } catch (error) {
      console.error('Documents - onDeleteDocument.', error);
      notificationContext.add({ type: 'DELETE_DOCUMENT_ERROR', content: {} }, true);
      setIsDeletingDocument(false);
      setFileDeletingId('');
    } finally {
      setDeleteDialogVisible(false);
    }
  };

  const onDownloadDocument = async document => {
    try {
      setDownloadingId(document.id);
      setFileName(`${document.title.split(' ').join('_')}`);
      const { data } = await DocumentService.download(document.id, dataflowId);
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
    const formatedRowData = FileUtils.formatBytes(rowData.size);
    return (
      <Fragment>
        {formatedRowData.bytesParsed} {formatedRowData.sizeType}
      </Fragment>
    );
  };

  const titleColumnTemplate = rowData => <span onClick={() => onDownloadDocument(rowData)}>{rowData.title}</span>;

  const renderDocumentsColumns = () => {
    const columns = [
      { key: 'title', header: resourcesContext.messages['title'], template: titleColumnTemplate },
      { key: 'description', header: resourcesContext.messages['description'] },
      { key: 'category', header: resourcesContext.messages['category'] },
      { key: 'language', header: resourcesContext.messages['language'] },
      { key: 'isPublic', header: resourcesContext.messages['isPublic'], template: isPublicColumnTemplate },
      { key: 'date', header: resourcesContext.messages['documentUploadDate'], template: dateColumnTemplate },
      { key: 'size', header: resourcesContext.messages['documentSize'], template: sizeColumnTemplate },
      { key: 'url', header: resourcesContext.messages['file'], template: downloadColumnTemplate }
    ];

    if (isToolbarVisible) {
      columns.push({
        key: 'buttonsUniqueId',
        header: resourcesContext.messages['actions'],
        template: documentsEditButtons,
        className: styles.crudColumn
      });
    }

    return columns.map(column => (
      <Column
        body={column.template}
        className={column.className ? column.className : ''}
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={!isEmpty(documents) && column.key !== 'buttonsUniqueId'}
      />
    ));
  };

  const renderSpinner = () => {
    if (isLoading && isEmpty(documents)) {
      return <Spinner style={{ top: 0 }} />;
    }
  };

  const renderNoDocuments = () => {
    if (!isLoading && isEmpty(documents)) {
      return (
        <div className={styles.noDataWrapper}>
          <h4>{resourcesContext.messages['noDocuments']}</h4>
        </div>
      );
    }
  };

  return (
    <Fragment>
      {isToolbarVisible && (
        <Toolbar className={styles.documentsToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded dataflowHelp-document-upload-help-step`}
              icon="upload"
              label={resourcesContext.messages['upload']}
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
        summary={resourcesContext.messages['documents']}
        value={allDocuments}>
        {renderDocumentsColumns()}
      </DataTable>
      {renderSpinner()}
      {renderNoDocuments()}
      {isUploadDialogVisible && (
        <Dialog
          className={styles.dialog}
          footer={dialogFooter}
          header={isEditForm ? resourcesContext.messages['editDocument'] : resourcesContext.messages['uploadDocument']}
          onHide={onCancelDialog}
          visible={isUploadDialogVisible}>
          <DocumentFileUpload
            dataflowId={dataflowId}
            documentInitialValues={documentInitialValues}
            footerRef={footerRef}
            isEditForm={isEditForm}
            isUploadDialogVisible={isUploadDialogVisible}
            onUpload={onUploadDocument}
            setFileUpdatingId={setFileUpdatingId}
            setIsUpdating={setIsUpdating}
            setIsUploading={setIsUploading}
            setSubmitting={setSubmitting}
          />
        </Dialog>
      )}
      {deleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={isDeletingDocument}
          header={resourcesContext.messages['delete']}
          iconConfirm={isDeletingDocument ? 'spinnerAnimate' : 'check'}
          isDeleting={isDeletingDocument}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => {
            setIsDeletingDocument(true);
            onDeleteDocument(rowDataState);
          }}
          onHide={onHideDeleteDialog}
          visible={deleteDialogVisible}>
          {resourcesContext.messages['deleteDocument']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
