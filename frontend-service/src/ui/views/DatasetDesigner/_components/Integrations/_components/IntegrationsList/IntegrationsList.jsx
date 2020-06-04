import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './IntegrationsList.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Spinner } from 'ui/views/_components/Spinner';

import { IntegrationService } from 'core/services/Integration';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { integrationsListReducer } from './_functions/Reducers/integrationsListReducer';

export const IntegrationsList = ({ dataflowId, designerState, manageDialogs }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [integrationListState, integrationListDispatch] = useReducer(integrationsListReducer, {
    integrations: [],
    data: {},
    integrationId: '',
    isDeleteDialogVisible: false,
    isLoading: true
  });

  useEffect(() => {
    onLoadIntegrations();
  }, []);

  const actionsTemplate = () => (
    <ActionsColumn
      onDeleteClick={() => isDeleteDialogVisible(true)}
      onEditClick={() => {
        console.log('edit the integration', integrationListState.integrationId);
      }}
    />
  );

  const integrationId = value => integrationListDispatch({ type: 'ON_LOAD_INTEGRATION_ID', payload: { value } });

  const isDeleteDialogVisible = value =>
    integrationListDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => integrationListDispatch({ type: 'IS_LOADING', payload: value });

  const onDeleteConstraint = async () => {
    console.log('delete the integration', integrationListState.integrationId);
  };

  const onLoadIntegrations = async () => {
    try {
      const response = await IntegrationService.all();
      // const response = await IntegrationService.all(dataflowId);
      integrationListDispatch({ type: 'INITIAL_LOAD', payload: { data: response, integrations: response.list } });
    } catch (error) {
      console.log('error', error);
    } finally {
      isLoading(false);
    }
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

  if (integrationListState.isLoading) {
    return <Spinner className={styles.positioning} />;
  } else {
    return (
      <div>
        {!isEmpty(integrationListState.data) ? (
          <DataTable
            autoLayout={true}
            onRowClick={event => {
              integrationId(event.data.integrationId);
            }}
            paginator={true}
            paginatorRight={`${resources.messages['totalRecords']} ${integrationListState.integrations.length}`}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            totalRecords={integrationListState.integrations.length}
            value={integrationListState.integrations}>
            {renderColumns(integrationListState.integrations)}
          </DataTable>
        ) : (
          <div className={styles.emptyFilteredData}>No with selected parameters</div>
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
  }
};
