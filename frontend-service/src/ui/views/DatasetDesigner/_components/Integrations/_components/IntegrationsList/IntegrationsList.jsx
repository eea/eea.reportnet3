import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './IntegrationsList.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { IntegrationService } from 'core/services/Integration';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { integrationsListReducer } from './_functions/Reducers/integrationsListReducer';

export const IntegrationsList = ({
  dataflowId,
  designerState,
  getUpdatedData,
  integrationsList,
  manageDialogs,
  needsRefresh,
  onUpdateDesignData,
  refreshList,
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [integrationListState, integrationListDispatch] = useReducer(integrationsListReducer, {
    data: [],
    filteredData: [],
    integrationId: '',
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isLoading: true
  });

  useEffect(() => {
    if (!designerState.isIntegrationManageDialogVisible && needsRefresh) {
      onLoadIntegrations();
    }
  }, [integrationListState.isDataUpdated, designerState.isIntegrationManageDialogVisible, needsRefresh]);

  const actionsTemplate = row => (
    <ActionsColumn
      onDeleteClick={row.operation === 'EXPORT_EU_DATASET' ? null : () => isDeleteDialogVisible(true)}
      onEditClick={() => {
        const filteredData = integrationListState.data.filter(
          integration => integration.integrationId === row.integrationId
        );
        manageDialogs('isIntegrationManageDialogVisible', true);
        if (!isEmpty(filteredData)) getUpdatedData(filteredData[0]);
      }}
    />
  );

  const integrationId = value => integrationListDispatch({ type: 'ON_LOAD_INTEGRATION_ID', payload: { value } });

  const isDataUpdated = value => integrationListDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const isDeleteDialogVisible = value =>
    integrationListDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => integrationListDispatch({ type: 'IS_LOADING', payload: { value } });

  const onDeleteIntegration = async () => {
    try {
      const response = await IntegrationService.deleteById(dataflowId, integrationListState.integrationId);
      if (response.status >= 200 && response.status <= 299) {
        onUpdateData();
        onUpdateDesignData();
      }
    } catch (error) {
      notificationContext.add({ type: 'DELETE_INTEGRATION_ERROR' });
    } finally {
      isDeleteDialogVisible(false);
    }
  };

  const onLoadFilteredData = data => integrationListDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onLoadIntegrations = async () => {
    try {
      isLoading(true);
      const response = await IntegrationService.all(dataflowId, designerState.datasetSchemaId);
      integrationListDispatch({ type: 'INITIAL_LOAD', payload: { data: response, filteredData: response } });
      integrationsList(response);
      refreshList(false);
    } catch (error) {
      notificationContext.add({ type: 'LOAD_INTEGRATIONS_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onUpdateData = () => {
    isDataUpdated(!integrationListState.isDataUpdated);
  };

  const renderActionButtonsColumn = (
    <Column
      body={row => actionsTemplate(row)}
      className={styles.validationCol}
      header={resources.messages['actions']}
      key="actions"
      style={{ width: '100px' }}
    />
  );

  const renderColumns = integrations => {
    const fieldColumns = Object.keys(integrations[0])
      .filter(key => key.includes('integrationName') || key.includes('operationName'))
      .map(field => <Column field={field} header={resources.messages[field]} key={field} sortable={true} />);

    fieldColumns.push(renderActionButtonsColumn);
    return fieldColumns;
  };

  if (integrationListState.isLoading) {
    return (
    <div className={styles.integrationsWithoutTable}>
      <div className={styles.spinner}><Spinner style={{ top: 0, left: 0 }} /></div>
    </div>);
  }

  return isEmpty(integrationListState.data) ? (
    <div className={styles.integrationsWithoutTable}>
      <div className={styles.noIntegrations}>
        {resources.messages['noIntegrations']}
      </div>
    </div>
  ) : (
    <div className={styles.integrations}>
      <Filters
        data={integrationListState.data}
        getFilteredData={onLoadFilteredData}
        inputOptions={['integrationName']}
        selectOptions={['operationName']}
      />

      {!isEmpty(integrationListState.filteredData) ? (
        <DataTable
          autoLayout={true}
          onRowClick={event => integrationId(event.data.integrationId)}
          paginator={true}
          paginatorRight={`${resources.messages['totalRecords']} ${integrationListState.filteredData.length}`}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={integrationListState.filteredData.length}
          value={integrationListState.filteredData}>
          {renderColumns(integrationListState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resources.messages['noIntegrationsWithSelectedParameters']}</div>
      )}

      {integrationListState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteIntegrationHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteIntegration(integrationListState.integrationId)}
          onHide={() => isDeleteDialogVisible(false)}
          visible={integrationListState.isDeleteDialogVisible}>
          {resources.messages['deleteIntegrationConfirm']}
        </ConfirmDialog>
      )}
    </div>
  );
};
