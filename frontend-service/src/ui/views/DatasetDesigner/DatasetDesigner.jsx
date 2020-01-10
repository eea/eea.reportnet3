import React, { useState, useEffect, useContext, useRef } from 'react';
import { withRouter } from 'react-router-dom';
import { isUndefined } from 'lodash';

import styles from './DatasetDesigner.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { CodelistsManager } from 'ui/views/_components/CodelistsManager';
import { Dialog } from 'ui/views/_components/Dialog';
import { Growl } from 'primereact/growl';
import { InputTextarea } from 'ui/views/_components/InputTextarea';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Snapshots } from 'ui/views/_components/Snapshots';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsDesigner } from './_components/TabsDesigner';
import { Toolbar } from 'ui/views/_components/Toolbar';
import { useDatasetDesigner } from 'ui/views/_components/Snapshots/_hooks/useDatasetDesigner';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/Dataflow';
import { DatasetService } from 'core/services/Dataset';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

export const DatasetDesigner = withRouter(({ match, history }) => {
  const {
    params: { datasetId }
  } = match;
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowName, setDataflowName] = useState('');
  const [datasetDescription, setDatasetDescription] = useState('');
  const [datasetSchemaName, setDatasetSchemaName] = useState('');
  const [datasetSchemaId, setDatasetSchemaId] = useState('');
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const [initialDatasetDescription, setInitialDatasetDescription] = useState();
  const [isCodelistManagerVisible, setIsCodelistManagerVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

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
  } = useDatasetDesigner(match.params.dataflowId, datasetId, datasetSchemaId, growlRef);

  useEffect(() => {
    try {
      setIsLoading(true);
      const getDatasetSchemaId = async () => {
        const dataset = await DatasetService.schemaById(datasetId);
        setDatasetDescription(dataset.datasetSchemaDescription);
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

  const codelistDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button label={resources.messages['cancel']} icon="cancel" onClick={() => setIsCodelistManagerVisible(false)} />
    </div>
  );

  const getDataflowName = async () => {
    const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowData.name);
  };

  const onBlurDescription = description => {
    if (description !== initialDatasetDescription) {
      console.log({ description });
      onUpdateDescription(description);
    }
  };

  const onCodelistSelected = () => {
    setIsCodelistManagerVisible(true);
  };

  const onKeyChange = event => {
    if (event.key === 'Escape') {
      setDatasetDescription(initialDatasetDescription);
    } else if (event.key == 'Enter') {
      event.preventDefault();
      onBlurDescription(event.target.value);
    }
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

  const onUpdateDescription = async description => {
    try {
      const response = await DatasetService.updateDatasetDescriptionDesign(datasetId, description);
      if (response.status < 200 || response.status > 299) {
        console.error('Error during datasetSchema Description update');
      }
    } catch (error) {
      console.error('Error during datasetSchema Description update: ', error);
    } finally {
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
      <h4 className={styles.descriptionLabel}>{resources.messages['newDatasetSchemaDescriptionPlaceHolder']}</h4>
      <div className={styles.ButtonsBar}>
        <InputTextarea
          className={styles.datasetDescription}
          collapsedHeight={40}
          expandableOnClick={true}
          key="datasetDescription"
          onBlur={e => onBlurDescription(e.target.value)}
          onChange={e => setDatasetDescription(e.target.value)}
          onFocus={e => {
            setInitialDatasetDescription(e.target.value);
          }}
          onKeyDown={e => onKeyChange(e)}
          placeholder={resources.messages['newDatasetSchemaDescriptionPlaceHolder']}
          value={datasetDescription}
        />
        <Toolbar>
          <div className="p-toolbar-group-right">
            {/* <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'clock'}
              label={resources.messages['events']}
              onClick={null}
            /> */}
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
      <TabsDesigner editable={true} onCodelistSelected={onCodelistSelected} />
      <Dialog
        blockScroll={false}
        contentStyle={{ overflow: 'auto' }}
        closeOnEscape={false}
        footer={codelistDialogFooter}
        header={resources.messages['codelistsManager']}
        modal={true}
        onHide={() => setIsCodelistManagerVisible(false)}
        style={{ width: '80%' }}
        visible={isCodelistManagerVisible}>
        <div className="p-grid p-fluid">{<CodelistsManager setIsLoading={setIsLoading} isInDesign={true} />}</div>
      </Dialog>
      <Snapshots
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        isSnapshotDialogVisible={isSnapshotDialogVisible}
        setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
        snapshotListData={snapshotListData}
      />
    </SnapshotContext.Provider>
  );
});
