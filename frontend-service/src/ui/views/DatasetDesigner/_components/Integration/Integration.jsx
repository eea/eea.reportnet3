import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './Integration.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Spinner } from 'ui/views/_components/Spinner';

import { IntegrationService } from 'core/services/Integration';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { integrationReducer } from './_functions/Reducers/integrationReducer';

export const Integration = ({ dataflowId, designerState }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [integrationState, integrationDispatch] = useReducer(integrationReducer, {
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
        console.log('edit the integration', integrationState.integrationId);
      }}
    />
  );

  console.log('designerState en integration', designerState);

  const integrationId = value => integrationDispatch({ type: 'ON_LOAD_INTEGRATION_ID', payload: { value } });

  const isDeleteDialogVisible = value => integrationDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => integrationDispatch({ type: 'IS_LOADING', payload: value });

  const onDeleteConstraint = async () => {
    console.log('delete the integration', integrationState.integrationId);
  };

  const onLoadIntegrations = async () => {
    try {
      const response = await IntegrationService.all();
      // const response = await IntegrationService.all(dataflowId);
      integrationDispatch({ type: 'INITIAL_LOAD', payload: { data: response, integrations: response.list } });
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

  if (integrationState.isLoading) {
    return <Spinner className={styles.positioning} />;
  } else {
    return (
      <div>
        {!isEmpty(integrationState.data) ? (
          <DataTable
            autoLayout={true}
            onRowClick={event => {
              integrationId(event.data.integrationId);
            }}
            paginator={true}
            paginatorRight={`${resources.messages['totalRecords']} ${integrationState.integrations.length}`}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            totalRecords={integrationState.integrations.length}
            value={integrationState.integrations}>
            {renderColumns(integrationState.integrations)}
          </DataTable>
        ) : (
          <div className={styles.emptyFilteredData}>No with selected parameters</div>
        )}

        {integrationState.isDeleteDialogVisible && (
          <ConfirmDialog
            classNameConfirm={'p-button-danger'}
            header={resources.messages['deleteIntegrationHeader']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={() => onDeleteConstraint(integrationState.integrationId)}
            onHide={() => isDeleteDialogVisible(false)}
            visible={integrationState.isDeleteDialogVisible}>
            {resources.messages['deleteIntegrationConfirm']}
          </ConfirmDialog>
        )}
      </div>
    );
  }
};
