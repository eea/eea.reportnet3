import { Fragment, useContext, useEffect, useReducer } from 'react';

import cloneDeep from 'lodash/cloneDeep';
import isEmpty from 'lodash/isEmpty';

import styles from './IntegrationsList.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { MyFilters } from 'views/_components/MyFilters';
import { Spinner } from 'views/_components/Spinner';

import { IntegrationService } from 'services/IntegrationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { integrationsListReducer } from './_functions/Reducers/integrationsListReducer';
import { TooltipButton } from 'views/_components/TooltipButton';

import { useFilters } from 'views/_functions/Hooks/useFilters';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const IntegrationsList = ({
  dataflowId,
  designerState,
  getClonedData,
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
  const resourcesContext = useContext(ResourcesContext);

  const [integrationListState, integrationListDispatch] = useReducer(integrationsListReducer, {
    data: [],
    integrationId: '',
    integrationToDeleteId: '',
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isDeleting: false,
    isLoading: true
  });

  const { filteredData, isFiltered } = useFilters('integrationsList');

  useEffect(() => {
    if (!designerState.isIntegrationManageDialogVisible && needsRefresh) {
      onLoadIntegrations();
    }
  }, [integrationListState.isDataUpdated, designerState.isIntegrationManageDialogVisible, needsRefresh]);

  const actionsTemplate = row => (
    <ActionsColumn
      hideDeletion={TextUtils.areEquals(row.operation, 'EXPORT_EU_DATASET')}
      isDeletingDocument={integrationListState.isDeleting}
      isUpdating={isUpdating}
      onCloneClick={() => {
        const filteredData = cloneDeep(
          integrationListState.data.find(integration =>
            TextUtils.areEquals(integration.integrationId, row.integrationId)
          )
        );
        manageDialogs('isIntegrationManageDialogVisible', true);
        if (!isEmpty(filteredData)) {
          filteredData.integrationName = `${filteredData.integrationName}_DUPLICATED`;
          filteredData.integrationId = null;
          getClonedData(filteredData);
        }
      }}
      onDeleteClick={TextUtils.areEquals(row.operation, 'EXPORT_EU_DATASET') ? null : () => isDeleteDialogVisible(true)}
      onEditClick={() => {
        const filteredData = cloneDeep(
          integrationListState.data.find(integration =>
            TextUtils.areEquals(integration.integrationId, row.integrationId)
          )
        );
        manageDialogs('isIntegrationManageDialogVisible', true);
        if (!isEmpty(filteredData)) getUpdatedData(filteredData);
      }}
      rowDataId={row.integrationId}
      rowDeletingId={integrationListState.integrationToDeleteId}
      rowUpdatingId={integrationListState.integrationId}
    />
  );

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isFiltered && integrationListState.data.length !== filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {integrationListState.data.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {isFiltered && integrationListState.data.length === filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
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
      notificationContext.add({ type: 'DELETE_INTEGRATION_ERROR' }, true);
      integrationListDispatch({ type: 'IS_DELETING', payload: false });
    } finally {
      isDeleteDialogVisible(false);
    }
  };

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
      notificationContext.add({ type: 'LOAD_INTEGRATIONS_ERROR' }, true);
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
    { type: 'INPUT', key: 'integrationName', label: resourcesContext.messages['integrationName'] },
    {
      type: 'MULTI_SELECT',
      nestedOptions: [{ key: 'operationName', label: resourcesContext.messages['operationName'] }]
    }
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
      <TooltipButton
        message={`${resourcesContext.messages['integrationId']}: ${row.integrationId}`}
        uniqueIdentifier={row.integrationId}></TooltipButton>
    </Fragment>
  );

  return isEmpty(integrationListState.data) ? (
    <div className={styles.integrationsWithoutTable}>
      <div className={styles.noIntegrations}>{resourcesContext.messages['noIntegrations']}</div>
    </div>
  ) : (
    <div className={styles.integrations}>
      <MyFilters
        className="integrationsList"
        data={integrationListState.data}
        options={filterOptions}
        viewType="integrationsList"
      />

      {!isEmpty(filteredData) ? (
        <DataTable
          autoLayout={true}
          onRowClick={event => integrationId(event.data.integrationId)}
          paginator={true}
          paginatorRight={getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          summary={resourcesContext.messages['externalIntegrations']}
          totalRecords={filteredData.length}
          value={filteredData}>
          <Column
            body={integrationNameTemplate}
            field="integrationName"
            header={resourcesContext.messages['integrationName']}
            key="integrationName"
            sortable={true}
          />
          <Column
            field="operationName"
            header={resourcesContext.messages['operationName']}
            key="operationName"
            sortable={true}
          />
          <Column
            body={row => actionsTemplate(row)}
            className={styles.validationCol}
            header={resourcesContext.messages['actions']}
            key="actions"
            style={{ width: '100px' }}
          />
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>
          {resourcesContext.messages['noIntegrationsWithSelectedParameters']}
        </div>
      )}

      {integrationListState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={integrationListState.isDeleting}
          header={resourcesContext.messages['deleteIntegrationHeader']}
          iconConfirm={integrationListState.isDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => onDeleteIntegration(integrationListState.integrationId)}
          onHide={() => isDeleteDialogVisible(false)}
          visible={integrationListState.isDeleteDialogVisible}>
          {resourcesContext.messages['deleteIntegrationConfirm']}
        </ConfirmDialog>
      )}
    </div>
  );
};
