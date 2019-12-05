import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';
import { isUndefined } from 'lodash';

import styles from './DatasetDesigner.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { Growl } from 'primereact/growl';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { SnapshotContext } from 'ui/views/_components/_context/SnapshotContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsDesigner } from './_components/TabsDesigner';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { useDatasetDesigner } from 'ui/views/_components/Snapshots/_hooks/useDatasetDesigner';

import { getUrl } from 'core/infrastructure/api/getUrl';
import { routes } from 'ui/routes';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';

export const DatasetDesigner = withRouter(({ match, history }) => {
  const {
    params: { datasetId }
  } = match;
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowName, setDataflowName] = useState('');
  const [datasetSchemaName, setDatasetSchemaName] = useState('');
  const [datasetSchemaId, setDatasetSchemaId] = useState('');
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [isLoading, setIsLoading] = useState(false);

  let growlRef = useRef();

  const {
    isLoadingSnapshotListData,
    isSnapshotsBarVisible,
    setIsSnapshotsBarVisible,
    isSnapshotDialogVisible,
    setIsSnapshotDialogVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  } = useDatasetDesigner(datasetId, datasetSchemaId, growlRef);

  useEffect(() => {
    try {
      setIsLoading(true);
      const getDatasetSchemaId = async () => {
        const dataset = await DatasetService.schemaById(datasetId);

        setDatasetSchemaId(dataset.datasetSchemaId);
      };
      getDatasetSchemaId();
    } catch (error) {
      console.error(`Error while loading schema: ${error}`);
    } finally {
      setIsLoading(false);
    }
  }, []);

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
      { label: resources.messages['datasetDesigner'], icon: 'pencilRuler' }
    ]);
    getDataflowName();
    onLoadDatasetSchemaName();
  }, []);

  const getDataflowName = async () => {
    const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowData.name);
  };

  const onLoadDatasetSchemaName = async () => {
    setIsLoading(true);
    try {
      const dataset = await DatasetService.getMetaData(datasetId);
      setDatasetSchemaName(dataset.datasetSchemaName);
    } catch (error) {
      console.error(`Error while getting datasetSchemaName: ${error}`);
    } finally {
      setIsLoading(false);
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

  if (isLoading) {
    return layout(<Spinner />);
  }

  return layout(
    <SnapshotContext.Provider
      value={{
        snapshotState: snapshotState,
        snapshotDispatch: snapshotDispatch,
        isSnapshotsBarVisible: isSnapshotsBarVisible,
        setIsSnapshotsBarVisible: setIsSnapshotsBarVisible
      }}>
      <Title
        title={`${resources.messages['datasetSchema']}: ${datasetSchemaName}`}
        subtitle={dataflowName}
        icon="pencilRuler"
        iconSize="3.4rem"
      />
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'clock'}
              label={resources.messages['events']}
              onClick={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'validate'}
              label={resources.messages['validate']}
              onClick={() => null}
              ownButtonClasses={null}
              iconClasses={null}
            />

            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={hasWritePermissions}
              icon={'camera'}
              label={resources.messages['snapshots']}
              onClick={() => setIsSnapshotsBarVisible(!isSnapshotsBarVisible)}
            />
          </div>
        </Toolbar>
      </div>
      <TabsDesigner editable={true} />
      <Snapshots
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        isSnapshotDialogVisible={isSnapshotDialogVisible}
        setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
        snapshotListData={snapshotListData}
      />
    </SnapshotContext.Provider>
  );
});
