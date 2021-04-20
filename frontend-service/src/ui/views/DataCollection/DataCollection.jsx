import React, { Fragment, useContext, useEffect, useRef, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import { DatasetConfig } from 'conf/domain/model/Dataset';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { Growl } from 'primereact/growl';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsSchema } from 'ui/views/_components/TabsSchema';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { MetadataUtils } from 'ui/views/_functions/Utils';

export const DataCollection = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const leftSideBarContext = useContext(LeftSideBarContext);
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [dataCollectionName, setDataCollectionName] = useState();
  const [dataflowName, setDataflowName] = useState('');
  const [dataViewerOptions, setDataViewerOptions] = useState({ activeIndex: null });
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();

  let growlRef = useRef();

  useBreadCrumbs({ currentPage: CurrentPage.DATA_COLLECTION, dataflowId, history });

  useEffect(() => {
    leftSideBarContext.removeModels();
    onLoadDatasetSchema();
  }, []);

  useEffect(() => {
    try {
      getDataflowName();
      onLoadDataflowData();
    } catch (error) {
      console.error(error.response);
    }
  }, []);

  const getDataflowName = async () => {
    try {
      const { data } = await DataflowService.dataflowDetails(match.params.dataflowId);
      setDataflowName(data.name);
    } catch (error) {
      notificationContext.add({ type: 'DATAFLOW_DETAILS_ERROR', content: {} });
    }
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      console.error('METADATA error', error);
      notificationContext.add({ type: 'GET_METADATA_ERROR', content: { dataflowId, datasetId } });
    }
  };

  const onLoadDataflowData = async () => {
    try {
      const { data } = await DataflowService.reporting(match.params.dataflowId);
      const dataCollection = data
        ? data.dataCollections.filter(dataset => dataset.dataCollectionId.toString() === datasetId)
        : [];
      const [firstDataCollection] = dataCollection;
      if (!isEmpty(firstDataCollection)) {
        setDataCollectionName(firstDataCollection.dataCollectionName);
      }
    } catch (error) {
      const {
        dataflow: { name: dataflowName },
        dataset: { name: datasetName }
      } = await getMetadata({ dataflowId, datasetId });
      notificationContext.add({
        type: 'REPORTING_ERROR',
        content: { dataflowId, datasetId, dataflowName, datasetName }
      });
      if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  const onLoadDatasetSchema = async () => {
    try {
      setLoading(true);
      const datasetSchema = await DatasetService.schemaById(datasetId);
      setLevelErrorTypes(datasetSchema.data.levelErrorTypes);
      const tableSchemaNamesList = [];
      setTableSchema(
        datasetSchema.data.tables.map(tableSchema => {
          tableSchemaNamesList.push(tableSchema.tableSchemaName);
          return {
            id: tableSchema['tableSchemaId'],
            name: tableSchema['tableSchemaName'],
            readOnly: tableSchema['tableSchemaReadOnly']
          };
        })
      );
      setTableSchemaColumns(
        datasetSchema.data.tables.map(table => {
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
      const metadata = await getMetadata({ dataflowId, datasetId });
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
      if (!isUndefined(path) && path.includes(getUrl(DatasetConfig.dataSchema, { datasetId }))) {
        datasetError.type = 'SCHEMA_BY_ID_ERROR';
      } else {
        datasetError.type = 'ERROR_STATISTICS_BY_ID_ERROR';
      }
      notificationContext.add(datasetError);
      if (!isUndefined(response) && (response.status === 401 || response.status === 403)) {
        history.push(getUrl(routes.DATAFLOW, { dataflowId }));
      }
    }
    setLoading(false);
  };

  const onRenderTabsSchema = (
    <TabsSchema
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

  const layout = children => {
    return (
      <MainLayout leftSideBarConfig={{ buttons: [] }}>
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
      <Title title={dataCollectionName} subtitle={dataflowName} icon="dataCollection" iconSize="3.5rem" />
      <Toolbar>
        <div className="p-toolbar-group-left">
          <Button
            className="p-button-rounded p-button-secondary-transparent p-button-animated-blink"
            icon={'export'}
            id="buttonExportDataset"
            label={resourcesContext.messages['exportDataset']}
          />
        </div>
      </Toolbar>
      {onRenderTabsSchema}
    </Fragment>
  );
});
