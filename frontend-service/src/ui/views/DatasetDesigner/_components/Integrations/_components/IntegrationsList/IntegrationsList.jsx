import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './IntegrationsList.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { Integration } from 'core/domain/model/Integration/Integration';

import { IntegrationService } from 'core/services/Integration';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { integrationsListReducer } from './_functions/Reducers/integrationsListReducer';

export const IntegrationsList = ({ dataflowId, designerState, getUpdatedData, manageDialogs }) => {
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
    onLoadIntegrations();
  }, [integrationListState.isDataUpdated]);

  const actionsTemplate = row => (
    <ActionsColumn
      onDeleteClick={() => isDeleteDialogVisible(true)}
      onEditClick={() => {
        const updatedData = integrationListState.data.filter(
          integration => integration.integrationId === row.integrationId
        );
        manageDialogs('isIntegrationManageDialogVisible', true, 'isIntegrationListDialogVisible', false);
        getUpdatedData(updatedData);
      }}
    />
  );

  const integrationId = value => integrationListDispatch({ type: 'ON_LOAD_INTEGRATION_ID', payload: { value } });

  const isDataUpdated = value => integrationListDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const isDeleteDialogVisible = value =>
    integrationListDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => integrationListDispatch({ type: 'IS_LOADING', payload: value });

  const onDeleteConstraint = async () => {
    try {
      const response = await IntegrationService.deleteById(integrationListState.integrationId);
      if (response.status >= 200 && response.status <= 299) onUpdateData();
    } catch (error) {
      console.log('error', error);
      notificationContext.add({ type: 'DELETE_INTEGRATION_ERROR' });
    } finally {
      isDeleteDialogVisible(false);
    }
  };

  const onLoadFilteredData = data => integrationListDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onLoadIntegrations = async () => {
    try {
      const integration = new Integration();
      const internalParameters = { datasetSchemaId: designerState.datasetSchemaId };
      integration.internalParameters = internalParameters;
      const response = await IntegrationService.all(integration);
      integrationListDispatch({ type: 'INITIAL_LOAD', payload: { data: response } });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_INTEGRATIONS_ERROR' });
      console.log('error', error);
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
      .filter(key => key.includes('integrationName') || key.includes('operation'))
      .map(field => <Column field={field} header={resources.messages[field]} key={field} sortable={true} />);

    fieldColumns.push(renderActionButtonsColumn);
    return fieldColumns;
  };

  if (integrationListState.isLoading) return <Spinner style={{ top: 0 }} />;

  return isEmpty(integrationListState.data) ? (
    <div className={styles.noIntegrations}>{resources.messages['noIntegrations']}</div>
  ) : (
    <div className={styles.integrations}>
      <Filters
        data={integrationListState.data}
        getFilteredData={onLoadFilteredData}
        selectOptions={['integrationName', 'operation']}
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
          onConfirm={() => onDeleteConstraint(integrationListState.integrationId)}
          onHide={() => isDeleteDialogVisible(false)}
          visible={integrationListState.isDeleteDialogVisible}>
          {resources.messages['deleteIntegrationConfirm']}
        </ConfirmDialog>
      )}
    </div>
  );
};
