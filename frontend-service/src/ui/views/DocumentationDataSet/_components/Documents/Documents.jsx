import React, { useState, useEffect, useContext, useRef } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { isUndefined } from 'lodash';

import styles from './Documents.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { DocumentFileUpload } from '../DocumentFileUpload';
import { Growl } from 'primereact/growl';

import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Icon } from 'ui/views/_components/Icon';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DocumentService } from 'core/services/Document';

const Documents = ({ onLoadDocumentsAndWebLinks, match, documents, isCustodian }) => {
  const resources = useContext(ResourcesContext);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [isDownloading, setIsDownloading] = useState('');
  const [isFormReset, setIsFormReset] = useState(true);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  /*   const [rowDataState, setRowDataState] = useState(); */

  useEffect(() => {
    if (!isUndefined(fileToDownload)) {
      DownloadFile(fileToDownload, fileName);
    }
  }, [fileToDownload]);

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

  const onUploadDocument = () => {
    setIsUploadDialogVisible(false);
  };

  const actionTemplate = (rowData, column) => {
    switch (rowData.category) {
    }
    return (
      <span className={styles.downloadIcon} onClick={() => onDownloadDocument(rowData)}>
        {' '}
        {isDownloading === rowData.id ? (
          <Icon icon="spinnerAnimate" />
        ) : (
          <FontAwesomeIcon icon={AwesomeIcons(rowData.category)} />
        )}
      </span>
    );
  };
  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  let growlRef = useRef();

  const onCancelDialog = () => {
    setIsUploadDialogVisible(false);
    setIsFormReset(false);
  };

  const crudTemplate = (rowData, column) => {
    return (
      <>
        <span
          className={styles.delete}
          onClick={() => {
            setDeleteDialogVisible(true);
            /*  setRowDataState(rowData); */
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('delete')} />
        </span>
      </>
    );
  };

  return (
    <>
      <Growl ref={growlRef} />
      <Toolbar>
        <div className="p-toolbar-group-left">
          <Button
            className={`p-button-rounded p-button-secondary`}
            disabled={false}
            icon={'export'}
            label={resources.messages['upload']}
            onClick={() => setIsUploadDialogVisible(true)}
          />
          <Button
            className={`p-button-rounded p-button-secondary`}
            disabled={true}
            icon={'eye'}
            label={resources.messages['visibility']}
            onClick={null}
          />
          <Button
            className={`p-button-rounded p-button-secondary`}
            disabled={true}
            icon={'filter'}
            label={resources.messages['filter']}
            onClick={null}
          />
          <Button
            className={`p-button-rounded p-button-secondary`}
            disabled={true}
            icon={'import'}
            label={resources.messages['export']}
            onClick={null}
          />
        </div>
        <div className="p-toolbar-group-right">
          <Button
            className={`p-button-rounded p-button-secondary`}
            disabled={false}
            icon={'refresh'}
            label={resources.messages['refresh']}
            onClick={() => onLoadDocumentsAndWebLinks()}
          />
        </div>
      </Toolbar>
      <Dialog
        header={resources.messages['upload']}
        visible={isUploadDialogVisible}
        className={styles.Dialog}
        dismissableMask={false}
        onHide={onCancelDialog}>
        <DocumentFileUpload
          dataflowId={match.params.dataflowId}
          onUpload={onUploadDocument}
          onGrowlAlert={onGrowlAlert}
          isFormReset={isFormReset}
        />
      </Dialog>

      {
        <DataTable value={documents} autoLayout={true} paginator={true} rowsPerPageOptions={[5, 10, 100]} rows={10}>
          <Column className={styles.crudColumn} body={crudTemplate} />
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
            body={actionTemplate}
            field="url"
            filter={false}
            filterMatchMode="contains"
            header={resources.messages['file']}
            style={{ textAlign: 'center', width: '8em' }}
          />
        </DataTable>
      }
    </>
  );
};

export { Documents };
