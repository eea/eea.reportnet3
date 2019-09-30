/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import isUndefined from 'lodash/isUndefined';

import styles from './DocumentationDataSet.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { DocumentFileUpload } from './_components/DocumentFileUpload';
import { DownloadFile } from 'ui/views/_components/DownloadFile';
import { Icon } from 'ui/views/_components/Icon';
import { Growl } from 'primereact/growl';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView, TabPanel } from 'primereact/tabview';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DocumentService } from 'core/services/Document';
import { WebLinkService } from 'core/services/WebLink';
import { getUrl } from 'core/infrastructure/api/getUrl';

export const DocumentationDataSet = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [deleteDialogVisible, setDeleteDialogVisible] = useState(false);
  const [documents, setDocuments] = useState([]);
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [isFormReset, setIsFormReset] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isDownloading, setIsDownloading] = useState('');
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  const [rowDataState, setRowDataState] = useState();
  const [webLinks, setWebLinks] = useState([]);

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(config.DATAFLOWS.url))
  };

  useEffect(() => {
    onLoadDocumentsAndWebLinks();
  }, []);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowList'],
        command: () => history.push(getUrl(config.DATAFLOWS.url))
      },
      {
        label: resources.messages['dataFlow'],
        command: () => history.push(`/dataflow/${match.params.dataFlowId}`)
      },
      { label: resources.messages['documents'] }
    ]);
  }, [history, match.params.dataFlowId, resources.messages]);

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

  const onDeleteDocument = async documentData => {
    setDeleteDialogVisible(false);
    try {
      const response = await DocumentService.deleteDocument(documentData.id);
      if (response >= 200 && response <= 299) {
        setDocuments(documents.filter(document => document.id !== documentData.id));
      }
    } catch (error) {
      console.error(error.response);
    }
  };

  const onUploadDocument = () => {
    setIsUploadDialogVisible(false);
  };

  const onHideDeleteDialog = () => {
    setDeleteDialogVisible(false);
  };

  const onCancelDialog = () => {
    setIsUploadDialogVisible(false);
    setIsFormReset(false);
  };

  const onLoadDocumentsAndWebLinks = async () => {
    setIsLoading(true);
    try {
      setWebLinks(await WebLinkService.all(`${match.params.dataFlowId}`));
      setDocuments(await DocumentService.all(`${match.params.dataFlowId}`));
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(config.DATAFLOWS.url));
      }
    } finally {
      setIsLoading(false);
    }
  };

  const createFileName = title => {
    return `${title.split(' ').join('_')}`;
  };

  const crudTemplate = (rowData, column) => {
    return (
      <>
        <span
          className={styles.delete}
          onClick={() => {
            setDeleteDialogVisible(true);
            setRowDataState(rowData);
          }}>
          <FontAwesomeIcon icon={AwesomeIcons('delete')} />
        </span>
      </>
    );
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

  const actionWeblink = (rowData, column) => {
    return (
      <a href={rowData.url} target="_blank" rel="noopener noreferrer">
        {' '}
        {rowData.url}
      </a>
    );
  };

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  let growlRef = useRef();

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
        <Growl ref={growlRef} />
      </MainLayout>
    );
  };

  if (isLoading) {
    return layout(<Spinner />);
  }

  if (documents) {
    return layout(
      <>
        <TabView>
          <TabPanel header={resources.messages['documents']}>
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
                dataFlowId={match.params.dataFlowId}
                onUpload={onUploadDocument}
                onGrowlAlert={onGrowlAlert}
                isFormReset={isFormReset}
              />
            </Dialog>
            {
              <DataTable
                value={documents}
                autoLayout={true}
                paginator={true}
                rowsPerPageOptions={[5, 10, 100]}
                rows={10}>
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
          </TabPanel>

          <TabPanel header={resources.messages['webLinks']}>
            {
              <DataTable
                value={webLinks}
                autoLayout={true}
                paginator={true}
                rowsPerPageOptions={[5, 10, 100]}
                rows={10}>
                <Column
                  columnResizeMode="expand"
                  field="description"
                  header={resources.messages['description']}
                  filter={false}
                  filterMatchMode="contains"
                />
                <Column
                  body={actionWeblink}
                  field="url"
                  header={resources.messages['url']}
                  filter={false}
                  filterMatchMode="contains"
                />
              </DataTable>
            }
          </TabPanel>
        </TabView>
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
  } else {
    return <></>;
  }
});
