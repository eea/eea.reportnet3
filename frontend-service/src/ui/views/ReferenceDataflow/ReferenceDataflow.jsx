/* eslint-disable jsx-a11y/anchor-is-valid */
import { useContext, useEffect, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';

import styles from './ReferenceDataflow.module.scss';

import { config } from 'conf';

import { BigButtonListReference } from './_components/BigButtonListReference';
import { Button } from 'ui/views/_components/Button';
import { MainLayout } from 'ui/views/_components/Layout';
import { ReferencingDataflows } from './_components/ReferencingDataflows';
import { routes } from 'ui/routes';
import { Spinner } from 'ui/views/_components/Spinner';
import { Title } from 'ui/views/_components/Title';

import { DatasetService } from 'core/services/Dataset';
import { ReferenceDataflowService } from 'core/services/ReferenceDataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { dataflowReducer } from './_functions/Reducers/dataflowReducer';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';
import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';
import { useLeftSideBar } from './_functions/Hooks/useLeftSideBar';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { Dialog } from '../_components/Dialog/Dialog';
import { getUrl } from 'core/infrastructure/CoreUtils';

const ReferenceDataflow = withRouter(({ history, match }) => {
  const {
    params: { referenceDataflowId }
  } = match;

  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const dataflowInitialState = {
    requestStatus: 'idle',
    error: null,
    data: {},
    description: '',
    designDatasetSchemas: [],
    isDataUpdated: false,
    name: '',
    status: '',
    updatedDatasetSchema: [],
    isReferencingDataflowsDialogVisible: false
  };

  const [dataflowState, dataflowDispatch] = useReducer(dataflowReducer, dataflowInitialState);

  const setUpdatedDatasetSchema = updatedData =>
    dataflowDispatch({ type: 'SET_UPDATED_DATASET_SCHEMA', payload: { updatedData } });

  useEffect(() => {
    dataflowDispatch({ type: 'LOADING_STARTED' });
    onLoadReportingDataflow();
  }, [dataflowState.isDataUpdated]);

  useBreadCrumbs({
    currentPage: CurrentPage.REFERENCE_DATAFLOW,
    referenceDataflowId,
    history,
    matchParams: match.params
  });

  useLeftSideBar(dataflowState, getLeftSidebarButtonsVisibility, manageDialogs);

  function manageDialogs(dialog, value, secondDialog, secondValue) {
    dataflowDispatch({
      type: 'MANAGE_DIALOGS',
      payload: { dialog, value, secondDialog, secondValue, deleteInput: '' }
    });
  }

  const onSaveDatasetName = async (value, index) => {
    try {
      await DatasetService.updateSchemaNameById(
        dataflowState.designDatasetSchemas[index].datasetId,
        encodeURIComponent(value)
      );
      const updatedTitles = [...dataflowState.updatedDatasetSchema];
      updatedTitles[index].schemaName = value;
      setUpdatedDatasetSchema(updatedTitles);
    } catch (error) {
      if (error?.response?.status === 400) {
        notificationContext.add({
          type: 'DATASET_SCHEMA_CREATION_ERROR_INVALID_NAME',
          content: { schemaName: value }
        });
      }
    }
  };

  const onLoadReportingDataflow = async () => {
    let referenceDataflowResponse;
    try {
      referenceDataflowResponse = await ReferenceDataflowService.referenceDataflow(referenceDataflowId);
      const referenceDataflow = referenceDataflowResponse.data;

      dataflowDispatch({
        type: 'LOADING_SUCCESS',
        payload: {
          data: referenceDataflow,
          description: referenceDataflow.description,
          name: referenceDataflow.name,
          status: referenceDataflow.status
        }
      });

      if (!isEmpty(referenceDataflow.designDatasets)) {
        referenceDataflow.designDatasets.forEach((schema, idx) => {
          schema.index = idx;
        });

        dataflowDispatch({
          type: 'SET_DESIGN_DATASET_SCHEMAS',
          payload: { designDatasets: referenceDataflow.designDatasets }
        });

        const datasetSchemaInfo = [];
        referenceDataflow.designDatasets.forEach(schema => {
          datasetSchemaInfo.push({ schemaName: schema.datasetSchemaName, schemaIndex: schema.index });
        });

        setUpdatedDatasetSchema(datasetSchemaInfo);
      } else {
        dataflowDispatch({ type: 'SET_DESIGN_DATASET_SCHEMAS', payload: { designDatasets: [] } });
      }
    } catch (error) {
      notificationContext.add({ type: 'LOADING_ERROR', error });
      history.push(getUrl(routes.DATAFLOWS));
    }
  };

  const setIsDataUpdated = () => dataflowDispatch({ type: 'SET_IS_DATA_UPDATED' });


  function getLeftSidebarButtonsVisibility() {
    return {
      apiKeyBtn: true,
      editBtn: true,
      manageRequestersBtn: dataflowState.status === config.dataflowStatus.DESIGN,
      propertiesBtn: true,
      reportingDataflows: dataflowState.status === config.dataflowStatus.OPEN
    };
  }

  const layout = children => (
    <MainLayout leftSideBarConfig={{ isCustodian: dataflowState.isCustodian, buttons: [] }}>
      <div className="rep-container">{children}</div>
    </MainLayout>
  );

  if (dataflowState.requestStatus === 'pending') return layout(<Spinner />);

  return layout(
    <div className="rep-row">
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-12`}>
        <Title
          icon="clone"
          iconSize="4rem"
          subtitle={resources.messages['referenceDataflowCrumbLabel']}
          title={dataflowState.name}
        />
      </div>
      <BigButtonListReference
        className="dataflow-big-buttons-help-step"
        dataflowId={referenceDataflowId}
        dataflowState={dataflowState}
        onSaveName={onSaveDatasetName}
        onUpdateData={setIsDataUpdated}
      />
      {dataflowState.isReferencingDataflowsDialogVisible && (
        <Dialog
          // footer={dataflowUsersListFooter}
          header={resources.messages['referencingDataflowsDialogHeader']}
          onHide={() => manageDialogs('isReferencingDataflowsDialogVisible', false)}
          visible={dataflowState.isReferencingDataflowsDialogVisible}>
          <ReferencingDataflows referenceDataflowId={referenceDataflowId} />
        </Dialog>
      )}
    </div>
  );
});

export { ReferenceDataflow };
