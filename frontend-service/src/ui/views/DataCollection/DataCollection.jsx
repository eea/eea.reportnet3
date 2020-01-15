import React, { useState, useEffect, useContext, useRef } from 'react';

import { withRouter } from 'react-router-dom';
import { isUndefined } from 'lodash';

import styles from './DataCollection.module.css';

import { config } from 'conf';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { Growl } from 'primereact/growl';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserService } from 'core/services/User';

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

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowName, setDataflowName] = useState('');
  const [datasetName, setDatasetName] = useState('');
  const [datasetSchemaName, setDatasetSchemaName] = useState();
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [isValidationSelected, setIsValidationSelected] = useState(false);
  const [loading, setLoading] = useState(true);

  let growlRef = useRef();

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(user, [config.permissions.PROVIDER], `${config.permissions.DATASET}${datasetId}`)
      );
    }
  }, [user]);

  useEffect(() => {
    setBreadCrumbItems([
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
      const dataCollection = dataflowData.dataCollections.filter(datasets => datasets.dataCollectionId == datasetId);
      console.log('dataCollection', dataCollection);
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
      console.log('datasetSchema', datasetSchema);
    } catch (error) {
      console.log('datasetSchema error', error);
    }
  };

  const layout = children => {
    return (
      <MainLayout>
        <Growl ref={growlRef} />
        <BreadCrumb model={breadCrumbItems} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <SnapshotContext.Provider value={{}}>
      <Title
        title={datasetName}
        subtitle={`${dataflowName} - ${datasetSchemaName}`}
        icon="dataCollection"
        iconSize="3.5rem"
      />
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
          onValidationsVisible: () => {}
        }}></DatasetContext.Provider>
    </SnapshotContext.Provider>
  );
});
