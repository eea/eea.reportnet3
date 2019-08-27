/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './DocumentationDataSet.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
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

export const DocumentationDataSet = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  const [webLinks, setWebLinks] = useState([]);

  const home = {
    icon: config.icons['home'],
    command: () => history.push('/')
  };

  useEffect(() => {
    onLoadDocumentsAndWebLinks();
  }, []);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowTask'],
        command: () => history.push('/data-flow-task')
      },
      {
        label: resources.messages['reportingDataFlow'],
        command: () => history.push(`/reporting-data-flow/${match.params.dataFlowId}`)
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
    setFileName(createFileName(rowData.title));
    setFileToDownload(await DocumentService.downloadDocumentById(rowData.id));
  };

  const onHide = () => {
    setIsUploadDialogVisible(false);
    onLoadDocumentsAndWebLinks();
  };

  const onCancelDialog = () => {
    setIsUploadDialogVisible(false);
  };

  const onLoadDocumentsAndWebLinks = async () => {
    setIsLoading(true);
    setWebLinks(await WebLinkService.all(`${match.params.dataFlowId}`));
    setDocuments(await DocumentService.all(`${match.params.dataFlowId}`));
    setIsLoading(false);
  };

  const createFileName = title => {
    return `${title.split(' ').join('_')}`;
  };

  const actionTemplate = (rowData, column) => {
    return (
      <span className={styles.downloadIcon} onClick={() => onDownloadDocument(rowData)}>
        {' '}
        <Icon icon="archive" />
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
            <DocumentFileUpload dataFlowId={match.params.dataFlowId} onUpload={onHide} onGrowlAlert={onGrowlAlert} />
          </Dialog>
          {
            <DataTable value={documents} autoLayout={true} paginator={true} rowsPerPageOptions={[5, 10, 100]} rows={10}>
              <Column
                columnResizeMode="expand"
                field="title"
                filter={false}
                filterMatchMode="contains"
                header={resources.messages['title']}
              />
              <Column
                field="description"
                filter={false}
                filterMatchMode="contains"
                header={resources.messages['description']}
              />
              <Column
                field="category"
                filter={false}
                filterMatchMode="contains"
                header={resources.messages['category']}
              />
              <Column
                field="language"
                filter={false}
                filterMatchMode="contains"
                header={resources.messages['language']}
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
            <DataTable value={webLinks} autoLayout={true} paginator={true} rowsPerPageOptions={[5, 10, 100]} rows={10}>
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
    );
  } else {
    return <></>;
  }
});
