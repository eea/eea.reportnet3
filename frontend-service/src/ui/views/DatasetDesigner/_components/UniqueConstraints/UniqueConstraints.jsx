import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './UniqueConstraints.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { UniqueConstraintsService } from 'core/services/UniqueConstraints';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { constraintsReducer } from './_functions/Reducers/constraintsReducer';

import { UniqueConstraintsUtils } from './_functions/Utils/UniqueConstraintsUtils';

export const UniqueConstraints = ({ datasetSchemaId }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [constraintsState, constraintsDispatch] = useReducer(constraintsReducer, {
    data: {},
    fieldId: '',
    filteredData: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isLoading: true
  });

  useEffect(() => {
    onLoadConstraints();
  }, [constraintsState.isDataUpdated]);

  const actionButtonsColumn = (
    <Column
      body={row => actionsTemplate(row)}
      className={styles.validationCol}
      header={resources.messages['actions']}
      key="actions"
      sortable={false}
      style={{ width: '100px' }}
    />
  );

  const actionsTemplate = row => (
    <ActionsColumn
      onDeleteClick={() => onManageDialog(true)}
      onEditClick={() => console.log('editar constraint', row)}
    />
  );

  const fieldId = value => constraintsDispatch({ type: 'ON_LOAD_CONSTRAINT_ID', payload: { value } });

  const isDeleteDialogVisible = value => constraintsDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isDataUpdated = value => constraintsDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const onDeleteConstraint = async () => {
    try {
      onUpdateData();
      const response = await UniqueConstraintsService.deleteById(datasetSchemaId, constraintsState.fieldId);
      if (response.status >= 200 && response.status <= 299) onUpdateData();
    } catch (error) {
      notificationContext.add({ type: 'DELETE_UNIQUE_CONSTRAINT_ERROR' });
      console.log('error', error);
    } finally {
      onManageDialog(false);
    }
  };

  const isLoading = value => constraintsDispatch({ type: 'IS_LOADING', payload: value });

  const onLoadConstraints = async () => {
    try {
      constraintsDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: await UniqueConstraintsService.all(datasetSchemaId) }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_UNIQUE_CONSTRAINTS_ERROR' });
      console.log('error', error);
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => constraintsDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onManageDialog = value => {
    isDeleteDialogVisible(value);
  };

  const onUpdateData = () => {
    isDataUpdated(!constraintsState.isDataUpdated);
  };

  const renderColumns = constraints => {
    const fieldColumns = Object.keys(constraints[0])
      .filter(item => !item.includes('Id'))
      .map(field => (
        <Column columnResizeMode="expand" field={field} header={field.constraintsName} key={field} sortable={true} />
      ));

    fieldColumns.push(actionButtonsColumn);
    return fieldColumns;
  };

  if (constraintsState.isLoading) return <Spinner style={{ top: 0 }} />;

  return isEmpty(constraintsState.data) ? (
    <Fragment>{resources.messages['noConstraints']}</Fragment>
  ) : (
    <Fragment>
      <Filters
        data={constraintsState.data}
        getFilteredData={onLoadFilteredData}
        inputOptions={['name', 'description']}
        selectOptions={['pkMustBeUsed', 'pkReferenced', 'unique']}
      />

      {!isEmpty(constraintsState.filteredData) ? (
        <DataTable
          autoLayout={true}
          onRowClick={event => fieldId(event.data.fieldId)}
          paginator={true}
          paginatorRight={`${resources.messages['totalUniqueConstraints']} ${constraintsState.filteredData.length}`}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={constraintsState.filteredData.length}
          value={constraintsState.filteredData}>
          {renderColumns(constraintsState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resources.messages['noConstraintsWithSelectedParameters']}</div>
      )}

      {constraintsState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          header={resources.messages['deleteUniqueConstraintHeader']}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteConstraint()}
          onHide={() => onManageDialog(false)}
          visible={constraintsState.isDeleteDialogVisible}
          maximizable={false}>
          {resources.messages['deleteUniqueConstraintConfirm']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
