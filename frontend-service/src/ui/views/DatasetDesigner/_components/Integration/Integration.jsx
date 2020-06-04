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

import { IntegrationReducer } from './_functions/Reducers/IntegrationReducer';

export const Integration = (designerState, manageDialogs) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [IntegrationState, IntegrationDispatch] = useReducer(IntegrationReducer, {
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
        console.log('edit the integration', IntegrationState.integrationId);
      }}
    />
  );

  const integrationId = value => IntegrationDispatch({ type: 'ON_LOAD_INTEGRATION_ID', payload: { value } });

  const isDeleteDialogVisible = value => IntegrationDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => IntegrationDispatch({ type: 'IS_LOADING', payload: value });

  const onDeleteConstraint = async () => {
    console.log('delete the integration', IntegrationState.integrationId);
  };

  const onLoadIntegrations = async () => {
    try {
      const response = await IntegrationService.all();
      IntegrationDispatch({ type: 'INITIAL_LOAD', payload: { data: response, integrations: response.list } });
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

  if (IntegrationState.isLoading) {
    return <Spinner className={styles.positioning} />;
  } else {
    return (
      <div>
        {!isEmpty(IntegrationState.data) ? (
          <DataTable
            autoLayout={true}
            onRowClick={event => {
              integrationId(event.data.integrationId);
            }}
            paginator={true}
            paginatorRight={`${resources.messages['totalRecords']} ${IntegrationState.integrations.length}`}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            totalRecords={IntegrationState.integrations.length}
            value={IntegrationState.integrations}>
            {renderColumns(IntegrationState.integrations)}
          </DataTable>
        ) : (
          <div className={styles.emptyFilteredData}>No with selected parameters</div>
        )}

        {IntegrationState.isDeleteDialogVisible && (
          <ConfirmDialog
            classNameConfirm={'p-button-danger'}
            header={resources.messages['deleteIntegrationHeader']}
            labelCancel={resources.messages['no']}
            labelConfirm={resources.messages['yes']}
            onConfirm={() => onDeleteConstraint(IntegrationState.integrationId)}
            onHide={() => isDeleteDialogVisible(false)}
            visible={IntegrationState.isDeleteDialogVisible}>
            {resources.messages['deleteIntegrationConfirm']}
          </ConfirmDialog>
        )}
      </div>
    );
  }
};
