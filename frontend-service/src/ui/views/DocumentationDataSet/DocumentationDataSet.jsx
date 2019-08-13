/* eslint-disable react-hooks/exhaustive-deps */
import React, { useState, useEffect, useContext } from 'react';

import * as fileDownload from 'js-file-download';
import isUndefined from 'lodash/isUndefined';

import styles from './DocumentationDataSet.module.scss';

import { config } from 'assets/conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Dialog } from 'ui/views/_components/Dialog';
import { DocumentFileUpload } from './_components/DocumentFileUpload';
import { IconComponent } from 'ui/views/_components/IconComponent';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView, TabPanel } from 'primereact/tabview';

import { DocumentService } from 'core/services/Document';
import { WebLinkService } from 'core/services/WebLink';

export const DocumentationDataSet = ({ match, history }) => {
  const resources = useContext(ResourcesContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [buttons, setButtons] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [fileName, setFileName] = useState('');
  const [fileToDownload, setFileToDownload] = useState(undefined);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);
  const [webLinks, setWebLinks] = useState([]);

  const home = {
    icon: resources.icons['home'],
    command: () => history.push('/')
  };

  useEffect(() => {
    onLoadDocumentsAndWebLinks();
  }, []);

  useEffect(() => {
    if (!isUndefined(fileToDownload)) {
      fileDownload(fileToDownload, fileName);
    }
  }, [fileToDownload]);

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

  //Data Fetching
  useEffect(() => {
    //#region Button initialization
    setButtons([
      {
        label: resources.messages['upload'],
        icon: 'export',
        group: 'left',
        disabled: false,
        onClick: () => setIsUploadDialogVisible(true)
      },
      {
        label: resources.messages['visibility'],
        icon: 'eye',
        group: 'left',
        disabled: true,
        onClick: null
      },
      {
        label: resources.messages['filter'],
        icon: 'filter',
        group: 'left',
        disabled: true,
        onClick: null
      },
      {
        label: resources.messages['export'],
        icon: 'import',
        group: 'left',
        disabled: true,
        onClick: null
      },
      {
        label: resources.messages['refresh'],
        icon: 'refresh',
        group: 'right',
        disabled: false,
        clickHandler: () => onLoadDocumentsAndWebLinks()
      }
    ]);
    //#end region Button initialization
  }, []);

  useEffect(() => {
    if (!isUndefined(fileToDownload)) {
      fileDownload(fileToDownload, fileName);
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
        <IconComponent icon={config.icons.archive} />
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

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
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
          <ButtonsBar buttonsList={buttons} />
          <Dialog
            header={resources.messages['upload']}
            visible={isUploadDialogVisible}
            className={styles.Dialog}
            dismissableMask={false}
            onHide={onHide}>
            <DocumentFileUpload dataFlowId={match.params.dataFlowId} onUpload={onHide} />
          </Dialog>
          {
            <DataTable value={documents} autoLayout={true} paginator={true} rowsPerPageOptions={[5, 10, 100]} rows={10}>
              <Column
                columnResizeMode="expand"
                field="title"
                header={resources.messages['title']}
                filter={false}
                filterMatchMode="contains"
              />
              <Column
                field="description"
                header={resources.messages['description']}
                filter={false}
                filterMatchMode="contains"
              />
              <Column
                field="category"
                header={resources.messages['category']}
                filter={false}
                filterMatchMode="contains"
              />
              <Column
                field="language"
                header={resources.messages['language']}
                filter={false}
                filterMatchMode="contains"
              />
              <Column
                body={actionTemplate}
                style={{ textAlign: 'center', width: '8em' }}
                field="url"
                header={resources.messages['file']}
                filter={false}
                filterMatchMode="contains"
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
};
