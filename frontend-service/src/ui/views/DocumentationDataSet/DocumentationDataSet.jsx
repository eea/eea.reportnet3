import React, { useState, useEffect, useContext } from 'react';

import styles from './DocumentationDataSet.module.scss';

import { config } from 'assets/conf';

import { BreadCrumb } from 'primereact/breadcrumb';
import { ButtonsBar } from 'ui/views/_components/ButtonsBar';
import { Column } from 'primereact/column';
import { CustomFileUpload } from 'ui/views/_components/CustomFileUpload';
import { DataTable } from 'primereact/datatable';
import { Dialog } from 'primereact/dialog';
import { IconComponent } from 'ui/views/_components/IconComponent';
import { MainLayout } from 'ui/views/_components/Layout';
import { ProgressSpinner } from 'primereact/progressspinner';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { TabView, TabPanel } from 'primereact/tabview';

import { DocumentService } from 'core/services/Document';
import { WebLinkService } from 'core/services/WebLink';

import { getUrl } from 'core/infrastructure/getUrl';

export const DocumentationDataSet = ({ match, history }) => {
  const resources = useContext(ResourcesContext);

  // const [documentsAndWebLinksData, setDocumentsAndWebLinksData] = useState();
  const [documents, setDocuments] = useState([]);
  const [documentToDownload, setDocumentToDownload] = useState();
  const [webLinks, setWebLinks] = useState([]);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [customButtons, setCustomButtons] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isUploadDialogVisible, setIsUploadDialogVisible] = useState(false);

  const home = {
    icon: resources.icons['home'],
    command: () => history.push('/')
  };

  const setDocumentsAndWebLinks = async () => {
    setDocuments(await DocumentService.all(`${config.loadDatasetsByDataflowID.url}${match.params.dataFlowId}`));
    setWebLinks(await WebLinkService.all(`${config.loadDatasetsByDataflowID.url}${match.params.dataFlowId}`));
  };

  useEffect(() => {
    setIsLoading(true);
    setDocumentsAndWebLinks();
    setIsLoading(false);
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

  //Data Fetching
  useEffect(() => {
    //#region Button initialization
    setCustomButtons([
      {
        label: resources.messages['upload'],
        icon: '0',
        group: 'left',
        disabled: false,
        clickHandler: () => setIsUploadDialogVisible(true)
      },
      {
        label: resources.messages['visibility'],
        icon: '6',
        group: 'left',
        disabled: true,
        clickHandler: null
      },
      {
        label: resources.messages['filter'],
        icon: '7',
        group: 'left',
        disabled: true,
        clickHandler: null
      },
      {
        label: resources.messages['export'],
        icon: '1',
        group: 'left',
        disabled: true,
        clickHandler: null
      }
    ]);
    //#end region Button initialization
  }, []);

  const onHideHandler = () => {
    setIsUploadDialogVisible(false);
  };

  const getDocumentByID = async documentId => {
    await DocumentService.getByDocumentId(documentId);
  };

  const downloadDocument = documentId => {
    getDocumentByID(documentId);
  };

  const actionTemplate = (rowData, column) => {
    return (
      <a onClick={() => downloadDocument(rowData.id)}>
        {' '}
        <IconComponent icon="pi pi-file" />
      </a>
    );
  };

  const actionWeblink = (rowData, column) => {
    return (
      <a href={rowData.url} target="_blank">
        {' '}
        {rowData.url}
      </a>
    );
  };

  const layout = children => {
    return (
      <MainLayout>
        <div className="titleDiv">
          <BreadCrumb model={breadCrumbItems} home={home} />
        </div>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (isLoading) {
    return layout(<ProgressSpinner />);
  }

  if (documents) {
    return layout(
      <TabView>
        <TabPanel header={resources.messages['documents']}>
          <ButtonsBar buttonsList={customButtons} />
          <Dialog
            header={resources.messages['upload']}
            visible={isUploadDialogVisible}
            className={styles.Dialog}
            dismissableMask={false}
            onHide={onHideHandler}>
            <CustomFileUpload
              mode="advanced"
              name="file"
              /* url={`${window.env.REACT_APP_BACKEND}/dataset/${dataSetId}/loadTableData/${props.id}`} */
              /* onUpload={onUploadHandler} */
              multiple={false}
              chooseLabel={resources.messages['selectFile']} //allowTypes="/(\.|\/)(csv|doc)$/"
              fileLimit={1}
              className={styles.FileUpload}
              maxFileSize={1024}
            />
          </Dialog>
          {
            <DataTable value={documents} autoLayout={true}>
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
            <DataTable value={webLinks} autoLayout={true}>
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
