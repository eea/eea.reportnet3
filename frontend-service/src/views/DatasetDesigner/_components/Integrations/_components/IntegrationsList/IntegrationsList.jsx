import { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './IntegrationsList.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { IntegrationService } from 'services/IntegrationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { integrationsListReducer } from './_functions/Reducers/integrationsListReducer';
import { TooltipButton } from 'views/_components/TooltipButton';

export const IntegrationsList = ({
  dataflowId,
  designerState,
  getUpdatedData,
  integrationsList,
  isCreating,
  isUpdating,
  manageDialogs,
  needsRefresh,
  onUpdateDesignData,
  refreshList,
  setIsCreating,
  setIsUpdating
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [integrationListState, integrationListDispatch] = useReducer(integrationsListReducer, {
    data: [],
    filtered: false,
    filteredData: [],
    integrationId: '',
    integrationToDeleteId: '',
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isDeleting: false,
    isLoading: true
  });

  useEffect(() => {
    if (!designerState.isIntegrationManageDialogVisible && needsRefresh) {
      onLoadIntegrations();
    }
  }, [integrationListState.isDataUpdated, designerState.isIntegrationManageDialogVisible, needsRefresh]);

  const actionsTemplate = row => (
    <ActionsColumn
      isDeletingDocument={integrationListState.isDeleting}
      isUpdating={isUpdating}
      onDeleteClick={row.operation === 'EXPORT_EU_DATASET' ? null : () => isDeleteDialogVisible(true)}
      onEditClick={() => {
        const filteredData = integrationListState.data.filter(
          integration => integration.integrationId === row.integrationId
        );
        manageDialogs('isIntegrationManageDialogVisible', true);
        if (!isEmpty(filteredData)) getUpdatedData(filteredData[0]);
      }}
      rowDataId={row.integrationId}
      rowDeletingId={integrationListState.integrationToDeleteId}
      rowUpdatingId={integrationListState.integrationId}
    />
  );

  const getFilteredSearched = value => integrationListDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {integrationListState.filtered && integrationListState.data.length !== integrationListState.filteredData.length
        ? `${resources.messages['filtered']} : ${integrationListState.filteredData.length} | `
        : ''}
      {resources.messages['totalRecords']} {integrationListState.data.length}{' '}
      {resources.messages['records'].toLowerCase()}
      {integrationListState.filtered && integrationListState.data.length === integrationListState.filteredData.length
        ? ` (${resources.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const integrationId = value => integrationListDispatch({ type: 'ON_LOAD_INTEGRATION_ID', payload: { value } });

  const isDataUpdated = value => integrationListDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const isDeleteDialogVisible = value =>
    integrationListDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => integrationListDispatch({ type: 'IS_LOADING', payload: { value } });

  const onDeleteIntegration = async () => {
    try {
      integrationListDispatch({
        type: 'SET_INTEGRATION_ID_TO_DELETE',
        payload: { data: integrationListState.integrationId }
      });
      integrationListDispatch({ type: 'IS_DELETING', payload: true });
      await IntegrationService.delete(dataflowId, integrationListState.integrationId);
      onUpdateData();
      onUpdateDesignData();
      refreshList(true);
    } catch (error) {
      console.error('IntegrationsList - onDeleteIntegration.', error);
      notificationContext.add({ type: 'DELETE_INTEGRATION_ERROR' });
      integrationListDispatch({ type: 'IS_DELETING', payload: false });
    } finally {
      isDeleteDialogVisible(false);
    }
  };

  const onLoadFilteredData = data => integrationListDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onLoadIntegrations = async () => {
    try {
      if (isCreating || isUpdating || integrationsList.isDeleting) {
        isLoading(false);
      }
      const integrations = await IntegrationService.getAll(dataflowId, designerState.datasetSchemaId);
      integrationListDispatch({ type: 'INITIAL_LOAD', payload: { data: integrations, filteredData: integrations } });
      integrationsList(integrations);
      refreshList(false);
    } catch (error) {
      console.error('IntegrationsList - onLoadIntegrations.', error);
      notificationContext.add({ type: 'LOAD_INTEGRATIONS_ERROR' });
    } finally {
      isLoading(false);
      setIsUpdating(false);
      setIsCreating(false);
      integrationListDispatch({ type: 'IS_DELETING', payload: false });
    }
  };

  const onUpdateData = () => {
    isDataUpdated(!integrationListState.isDataUpdated);
  };

  const filterOptions = [
    { type: 'input', properties: [{ name: 'integrationName' }] },
    { type: 'multiselect', properties: [{ name: 'operationName' }] }
  ];

  if (integrationListState.isLoading) {
    return (
      <div className={styles.integrationsWithoutTable}>
        <div className={styles.spinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      </div>
    );
  }

  const integrationNameTemplate = row => (
    <Fragment>
      {row.integrationName}
      <TooltipButton message={`${resources.messages['integrationId']}: ${row.integrationId}`}></TooltipButton>
    </Fragment>
  );

  return isEmpty(integrationListState.data) ? (
    <div className={styles.integrationsWithoutTable}>
      <div className={styles.noIntegrations}>{resources.messages['noIntegrations']}</div>
    </div>
  ) : (
    <div className={styles.integrations}>
      <Filters
        data={integrationListState.data}
        getFilteredData={onLoadFilteredData}
        getFilteredSearched={getFilteredSearched}
        options={filterOptions}
      />

      {!isEmpty(integrationListState.filteredData) ? (
        <DataTable
          autoLayout={true}
          onRowClick={event => integrationId(event.data.integrationId)}
          paginator={true}
          paginatorRight={getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          summary={resources.messages['externalIntegrations']}
          totalRecords={integrationListState.filteredData.length}
          value={integrationListState.filteredData}>
          <Column
            body={integrationNameTemplate}
            field="integrationName"
            header={resources.messages['integrationName']}
            key="integrationName"
            sortable={true}
          />
          <Column
            field="operationName"
            header={resources.messages['operationName']}
            key="operationName"
            sortable={true}
          />
          <Column
            body={row => actionsTemplate(row)}
            className={styles.validationCol}
            header={resources.messages['actions']}
            key="actions"
            style={{ width: '100px' }}
          />
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resources.messages['noIntegrationsWithSelectedParameters']}</div>
      )}

      {integrationListState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={integrationListState.isDeleting}
          header={resources.messages['deleteIntegrationHeader']}
          iconConfirm={integrationListState.isDeleting ? 'spinnerAnimate' : 'check'}
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
