import { Fragment, useContext, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import styles from './DataCollection.module.scss';

import { config } from 'conf';
import { DatasetConfig } from 'repositories/config/DatasetConfig';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';

import { Button } from 'views/_components/Button';
import { DatasetsInfo } from 'views/_components/DatasetsInfo';
import { Dialog } from 'views/_components/Dialog';
import { Growl } from 'views/_components/Growl';
import { MainLayout } from 'views/_components/Layout';
import { Menu } from 'views/_components/Menu';
import { Spinner } from 'views/_components/Spinner';
import { TabsSchema } from 'views/_components/TabsSchema';
import { Title } from 'views/_components/Title';
import { Toolbar } from 'views/_components/Toolbar';

import { DataflowService } from 'services/DataflowService';
import { DatasetService } from 'services/DatasetService';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';
import { useFilters } from 'views/_functions/Hooks/useFilters';

import { CurrentPage } from 'views/_functions/Utils';
import { MetadataUtils } from 'views/_functions/Utils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { isNil } from 'lodash';

export const DataCollection = () => {
  const navigate = useNavigate();
  const { dataflowId, datasetId } = useParams();

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dataCollectionName, setDataCollectionName] = useState();
  const [dataflowName, setDataflowName] = useState('');
  const [dataflowType, setDataflowType] = useState('');
  const [datasetSchemaId, setDatasetSchemaId] = useState(null);
  const [dataViewerOptions, setDataViewerOptions] = useState({ activeIndex: null });
  const [exportButtonsList, setExportButtonsList] = useState([]);
  const [isDatasetsInfoDialogVisible, setIsDatasetsInfoDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingFile, setIsLoadingFile] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [metadata, setMetadata] = useState(undefined);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();

  const { resetFiltersState: resetDatasetInfoFiltersState } = useFilters('datasetInfo');
  const { resetFiltersState: resetUserListFiltersState } = useFilters('userList');

  let exportMenuRef = useRef();
  let growlRef = useRef();
  let bigDataRef = useRef();

  bigDataRef.current = metadata?.dataflow.bigData;

  useBreadCrumbs({ currentPage: CurrentPage.DATA_COLLECTION, dataflowId, dataflowType, isLoading });

  useCheckNotifications(
    [
      'CALL_FME_PROCESS_FAILED_EVENT',
      'DOWNLOAD_EXPORT_DATASET_FILE_ERROR',
      'EXPORT_DATA_BY_ID_ERROR',
      'EXPORT_DATASET_FILE_AUTOMATICALLY_DOWNLOAD',
      'EXPORT_TABLE_DATA_FILE_AUTOMATICALLY_DOWNLOAD',
      'DOWNLOAD_EXPORT_TABLE_DATA_FILE_ERROR'
    ],
    setIsLoadingFile,
    false
  );

  useEffect(() => {
    leftSideBarContext.removeModels();
    getMetadata();
  }, []);

  useEffect(() => {
    getExtensionsList();
  }, [metadata?.dataflow.bigData]);

  useEffect(() => {
    const isAdmin = userContext.hasPermission([config.permissions.roles.ADMIN.key]);
    const isDataCustodian = userContext.hasPermission([config.permissions.roles.CUSTODIAN.key]);

    leftSideBarContext.removeModels();

    if (isAdmin || isDataCustodian) {
      leftSideBarContext.addModels([
        {
          className: 'dataflow-help-datasets-info-step',
          icon: 'listClipboard',
          isVisible: true,
          label: 'datasetsInfo',
          onClick: () => setIsDatasetsInfoDialogVisible(true),
          title: 'datasetsInfo'
        }
      ]);
    }
  }, [userContext]);

  useEffect(() => {
    if (!isUndefined(metadata)) {
      onLoadDataflowData();
      onLoadDatasetSchema();
    }
  }, [metadata]);

  useEffect(() => {
    if (notificationContext.hidden.some(notification => notification.key === 'EXPORT_DATASET_FAILED_EVENT')) {
      setIsLoadingFile(false);
    }
  }, [notificationContext.hidden]);

  const getMetadata = async () => {
    try {
      const metadata = await MetadataUtils.getMetadata({ datasetId, dataflowId });
      setMetadata(metadata);
      setDataflowName(metadata.dataflow.name);
      setDatasetSchemaId(metadata.dataset.datasetSchemaId);
    } catch (error) {
      console.error('DataCollection - getMetadata.', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } }, true);
    }
  };

  const getExtensionsList = () => {
    const internalExtensionsList = config.exportTypes.exportDatasetTypes
      .map(type => {
        const extensionsTypes = !isNil(type.code) && type.code.split('+');
        if (bigDataRef.current) {
          if (extensionsTypes?.includes('zip') && extensionsTypes?.includes('csv')) {
            return {
              command: () => onExportDataInternalExtension(type.code),
              icon: extensionsTypes[0],
              label: resourcesContext.messages[type.key]
            };
          } else {
            return null;
          }
        } else {
          return {
            command: () => onExportDataInternalExtension(type.code),
            icon: extensionsTypes[0],
            label: resourcesContext.messages[type.key]
          };
        }
      })
      .filter(item => item !== null);

    setExportButtonsList(internalExtensionsList);
  };

  const onExportDataInternalExtension = async fileType => {
    setIsLoadingFile(true);
    notificationContext.add({ type: 'EXPORT_DATASET_DATA' });

    try {
      if (bigDataRef.current) {
        await DatasetService.exportDatasetDataDL(datasetId, fileType);
      } else {
        await DatasetService.exportDatasetData(datasetId, fileType);
      }
    } catch (error) {
      console.error('DataCollection - onExportDataInternalExtension.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = metadata;

      notificationContext.add(
        {
          type: 'EXPORT_DATA_BY_ID_ERROR',
          content: { dataflowName: dataflowName, datasetName: datasetName }
        },
        true
      );
    }
  };

  const onLoadDataflowData = async () => {
    try {
      const data = await DataflowService.get(dataflowId);
      const dataCollection = data
        ? data.dataCollections.filter(dataset => dataset.dataCollectionId.toString() === datasetId)
        : [];
      const [firstDataCollection] = dataCollection;
      if (!isEmpty(firstDataCollection)) {
        setDataCollectionName(firstDataCollection.dataCollectionName);
      }

      setDataflowType(data.type);

      setIsLoading(false);
    } catch (error) {
      console.error('DataCollection - onLoadDataflowData.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = metadata;
      notificationContext.add(
        {
          type: 'REPORTING_ERROR',
          content: { dataflowId, datasetId, dataflowName, datasetName }
        },
        true
      );
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        navigate(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  const onLoadDatasetSchema = async () => {
    try {
      setLoading(true);
      const datasetSchema = await DatasetService.getSchema(dataflowId, datasetId);
      setLevelErrorTypes(datasetSchema.levelErrorTypes);
      const tableSchemaNamesList = [];
      setTableSchema(
        datasetSchema.tables.map(tableSchema => {
          tableSchemaNamesList.push(tableSchema.tableSchemaName);
          return {
            dataAreManuallyEditable: tableSchema['dataAreManuallyEditable'],
            description: tableSchema['tableSchemaDescription'],
            fixedNumber: tableSchema['tableSchemaFixedNumber'],
            hasInfoTooltip: true,
            id: tableSchema['tableSchemaId'],
            name: tableSchema['tableSchemaName'],
            notEmpty: tableSchema['tableSchemaNotEmpty'],
            numberOfFields: tableSchema.records ? tableSchema.records[0].fields?.length : 0,
            readOnly: tableSchema['tableSchemaReadOnly'],
            toPrefill: tableSchema['tableSchemaToPrefill']
          };
        })
      );
      setTableSchemaColumns(
        datasetSchema.tables.map(table => {
          return table.records[0].fields.map(field => {
            return {
              codelistItems: field['codelistItems'],
              description: field['description'],
              field: field['fieldId'],
              header: field['name'],
              pk: field['pk'],
              maxSize: field['maxSize'],
              pkHasMultipleValues: field['pkHasMultipleValues'],
              readOnly: field['readOnly'],
              recordId: field['recordId'],
              referencedField: field['referencedField'],
              table: table['tableSchemaName'],
              type: field['type'],
              validExtensions: field['validExtensions']
            };
          });
        })
      );
    } catch (error) {
      console.error('DataCollection - onLoadDatasetSchema.', error);
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = metadata;
      const {
        response,
        response: {
          data: { path }
        }
      } = error;
      const datasetError = { type: '', content: { dataflowId, datasetId, dataflowName, datasetName } };
      if (!isUndefined(path) && path.includes(getUrl(DatasetConfig.getSchema, { datasetId }))) {
        datasetError.type = 'SCHEMA_BY_ID_ERROR';
      } else {
        datasetError.type = 'ERROR_STATISTICS_BY_ID_ERROR';
      }
      notificationContext.add(datasetError, true);
      if (!isUndefined(response) && (response.status === 401 || response.status === 403)) {
        navigate(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    }
    setLoading(false);
  };

  const onRenderTabsSchema = (
    <TabsSchema
      bigData={metadata?.dataflow.bigData}
      dataflowType={dataflowType}
      datasetSchemaId={datasetSchemaId}
      hasCountryCode={true}
      hasWritePermissions={false}
      isExportable={false}
      isFilterable={false}
      levelErrorTypes={levelErrorTypes}
      onTabChange={table => onTabChange(table)}
      showWriteButtons={false}
      tables={tableSchema}
      tableSchemaColumns={tableSchemaColumns}
      tableSchemaId={dataViewerOptions.tableSchemaId}
    />
  );

  const onTabChange = table => setDataViewerOptions({ ...dataViewerOptions, tableSchemaId: table.tableSchemaId });

  const renderDialogFooterCloseBtn = () => (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => {
        setIsDatasetsInfoDialogVisible(false);
        resetDatasetInfoFiltersState();
        resetUserListFiltersState();
      }}
    />
  );

  const layout = children => {
    return (
      <MainLayout bigData={metadata?.dataflow.bigData} leftSideBarConfig={{ buttons: [] }}>
        <Growl ref={growlRef} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <Fragment>
      <Title
        icon="dataCollection"
        iconSize="3.5rem"
        subtitle={
          metadata?.dataflow.bigData ? (
            <p
              dangerouslySetInnerHTML={{
                __html: TextUtils.parseText(resourcesContext.messages['bigDataDataflowNamed'], {
                  name: dataflowName
                })
              }}></p>
          ) : (
            dataflowName
          )
        }
        title={dataCollectionName}
      />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className="p-button-rounded p-button-secondary-transparent p-button-animated-blink"
              icon={isLoadingFile ? 'spinnerAnimate' : 'export'}
              id="buttonExportDataset"
              label={resourcesContext.messages['exportDataset']}
              onClick={event => exportMenuRef.current.show(event)}
            />
            <Menu
              className={styles.exportSubmenu}
              id="exportDataSetMenu"
              model={exportButtonsList}
              popup={true}
              ref={exportMenuRef}
            />
          </div>
        </Toolbar>
        {isDatasetsInfoDialogVisible && (
          <Dialog
            footer={renderDialogFooterCloseBtn()}
            header={`${resourcesContext.messages['datasetsInfo']} - ${resourcesContext.messages['dataflowId']}: ${dataflowId}`}
            onHide={() => {
              setIsDatasetsInfoDialogVisible(false);
              resetDatasetInfoFiltersState();
            }}
            visible={isDatasetsInfoDialogVisible}>
            <DatasetsInfo dataflowId={dataflowId} dataflowType={dataflowType} datasetId={datasetId} />
          </Dialog>
        )}
      </div>
      {onRenderTabsSchema}
    </Fragment>
  );
};
