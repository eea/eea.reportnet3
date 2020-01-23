import React, { useState, useEffect, useContext, useRef } from 'react';

import { withRouter } from 'react-router-dom';
import { capitalize, isEmpty, isUndefined } from 'lodash';

import styles from './DataCollection.module.css';

import { DatasetConfig } from 'conf/domain/model/Dataset';
import { config } from 'conf';
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
import { UserService } from 'core/services/User';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { DatasetContext } from 'ui/views/_functions/Contexts/DatasetContext';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { MetadataUtils } from 'ui/views/_functions/Utils';

export const DataCollection = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const breadCrumbContext = useContext(BreadCrumbContext);
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [dataflowName, setDataflowName] = useState('');
  const [dataCollectionName, setDataCollectionName] = useState();
  const [datasetHasData, setDatasetHasData] = useState(false);
  const [datasetSchemaName, setDatasetSchemaName] = useState();
  const [dataViewerOptions, setDataViewerOptions] = useState({
    recordPositionId: -1,
    selectedRecordErrorId: -1,
    activeIndex: null
  });
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [isValidationSelected, setIsValidationSelected] = useState(false);
  const [levelErrorTypes, setLevelErrorTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tableSchema, setTableSchema] = useState();
  const [tableSchemaColumns, setTableSchemaColumns] = useState();
  const [tableSchemaNames, setTableSchemaNames] = useState([]);

  let growlRef = useRef();

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(user, [config.permissions.PROVIDER], `${config.permissions.DATASET}${datasetId}`)
      );
    }
  }, [user]);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: resources.messages['dataflowList'],
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        icon: 'archive',
        href: getUrl(
          routes.DATAFLOW,
          {
            dataflowId: match.params.dataflowId
          },
          true
        ),
        command: () =>
          history.push(
            getUrl(
              routes.DATAFLOW,
              {
                dataflowId: match.params.dataflowId
              },
              true
            )
          )
      },
      { label: resources.messages['dataCollection'], icon: 'dataCollection' }
    ]);
  }, []);

  useEffect(() => {
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
      const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
      setDataflowName(dataflowData.name);
    } catch (error) {
      notificationContext.add({
        type: 'DATAFLOW_DETAILS_ERROR',
        content: {
          dataflowId,
          datasetId
        }
      });
    }
  };

  const getMetadata = async ids => {
    try {
      return await MetadataUtils.getMetadata(ids);
    } catch (error) {
      console.log('METADATA error', error);
      notificationContext.add({
        type: 'GET_METADATA_ERROR',
        content: {
          dataflowId,
          datasetId
        }
      });
    }
  };

  const onLoadDataflowData = async () => {
    try {
      const dataflowData = await DataflowService.reporting(match.params.dataflowId);
      const dataCollection = dataflowData
        ? dataflowData.dataCollections.filter(datasets => datasets.dataCollectionId == datasetId)
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
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName
        }
      });
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
      setLoading(false);
    }
  };

  const onLoadDatasetSchema = async () => {
    try {
      const datasetSchema = await DatasetService.schemaById(datasetId);
      setDatasetSchemaName(datasetSchema.dataCollectionName);
      setLevelErrorTypes(datasetSchema.levelErrorTypes);
      const tableSchemaNamesList = [];
      setTableSchema(
        datasetSchema.tables.map(tableSchema => {
          tableSchemaNamesList.push(tableSchema.tableSchemaName);
          return {
            id: tableSchema['tableSchemaId'],
            name: tableSchema['tableSchemaName']
          };
        })
      );
      setTableSchemaNames(tableSchemaNamesList);
      setTableSchemaColumns(
        datasetSchema.tables.map(table => {
          return table.records[0].fields.map(field => {
            return {
              table: table['tableSchemaName'],
              field: field['fieldId'],
              header: `${capitalize(field['name'])}`,
              type: field['type'],
              recordId: field['recordId']
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
      const datasetError = {
        type: '',
        content: {
          dataflowId,
          datasetId,
          dataflowName,
          datasetName
        }
      };
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

  const onLoadTableData = hasData => {
    setDatasetHasData(hasData);
  };

  const onRenderTabsSchema = (
    <TabsSchema
      activeIndex={dataViewerOptions.activeIndex}
      hasWritePermissions={hasWritePermissions}
      isDataCollection={true}
      isWebFormMMR={false}
      levelErrorTypes={levelErrorTypes}
      onLoadTableData={onLoadTableData}
      onTabChange={tableSchemaId => onTabChange(tableSchemaId)}
      recordPositionId={dataViewerOptions.recordPositionId}
      selectedRecordErrorId={dataViewerOptions.selectedRecordErrorId}
      tables={tableSchema}
      tableSchemaColumns={tableSchemaColumns}
    />
  );

  const onTabChange = tableSchemaId => {
    setDataViewerOptions({ ...dataViewerOptions, activeIndex: tableSchemaId.index });
  };

  const layout = children => {
    return (
      <MainLayout
        leftSideBarConfig={{
          buttons: []
        }}>
        <Growl ref={growlRef} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <SnapshotContext.Provider value={{}}>
      <Title title={dataCollectionName} subtitle={dataflowName} icon="dataCollection" iconSize="3.5rem" />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'import'}
              label={resources.messages['export']}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'trash'}
              label={resources.messages['deleteDatasetData']}
            />
          </div>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'validate'}
              iconClasses={null}
              label={resources.messages['validate']}
              ownButtonClasses={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'warning'}
              iconClasses={''}
              label={resources.messages['showValidations']}
              ownButtonClasses={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'camera'}
              label={resources.messages['snapshots']}
            />
          </div>
        </Toolbar>
      </div>
      <DatasetContext.Provider
        value={{
          isValidationSelected: isValidationSelected,
          setIsValidationSelected: setIsValidationSelected,
          onSelectValidation: (tableSchemaId, posIdRecord, selectedRecordErrorId) => {
            setDataViewerOptions({
              recordPositionId: posIdRecord,
              selectedRecordErrorId: selectedRecordErrorId,
              activeIndex: tableSchemaId
            });
          },
          onValidationsVisible: () => {}
        }}>
        {onRenderTabsSchema}
      </DatasetContext.Provider>
    </SnapshotContext.Provider>
  );
});
