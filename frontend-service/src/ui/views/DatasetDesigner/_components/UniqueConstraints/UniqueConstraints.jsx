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

import { constraintsReducer } from './_functions/Reducers/constraintsReducer';

import { UniqueConstraintsUtils } from './_functions/Utils/UniqueConstraintsUtils';

export const UniqueConstraints = ({ designerState, getManageUniqueConstraint, manageDialogs }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const {
    datasetSchemaId,
    datasetSchemaAllTables,
    manageUniqueConstraintData: { uniqueId }
  } = designerState;

  const [constraintsState, constraintsDispatch] = useReducer(constraintsReducer, {
    data: {},
    filteredData: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isLoading: true
  });

  useEffect(() => {
    onLoadConstraints();
  }, [constraintsState.isDataUpdated]);

  const actionsTemplate = () => (
    <ActionsColumn
      onDeleteClick={() => isDeleteDialogVisible(true)}
      onEditClick={() => {
        manageDialogs('isUniqueConstraintsListDialogVisible', false, 'isManageUniqueConstraintDialogVisible', true);
      }}
    />
  );

  const isDataUpdated = value => constraintsDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const isDeleteDialogVisible = value => constraintsDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => constraintsDispatch({ type: 'IS_LOADING', payload: value });

  const onDeleteConstraint = async () => {
    try {
      const response = await UniqueConstraintsService.deleteById(uniqueId);
      if (response.status >= 200 && response.status <= 299) onUpdateData();
    } catch (error) {
      notificationContext.add({ type: 'DELETE_UNIQUE_CONSTRAINT_ERROR' });
    } finally {
      isDeleteDialogVisible(false);
      getManageUniqueConstraint({ tableSchemaId: null, tableSchemaName: '', fieldData: [], uniqueId: null });
    }
  };

  const onLoadConstraints = async () => {
    try {
      const response = await UniqueConstraintsService.all(datasetSchemaId);
      constraintsDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: UniqueConstraintsUtils.parseConstraintsList(response, datasetSchemaAllTables) }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_UNIQUE_CONSTRAINTS_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => constraintsDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onUpdateData = () => isDataUpdated(!constraintsState.isDataUpdated);

  const renderActionButtonsColumn = (
    <Column
      body={row => actionsTemplate(row)}
      className={styles.validationCol}
      header={resources.messages['actions']}
      key="actions"
      style={{ width: '100px' }}
    />
  );

  const renderColumns = constraints => {
    const fieldColumns = Object.keys(constraints[0])
      .filter(key => !key.includes('Id') && !key.includes('filter'))
      .map(field => (
        <Column
          body={field === 'fieldData' ? renderFieldBody : null}
          field={field}
          header={resources.messages[field]}
          key={field}
          sortable={true}
        />
      ));

    fieldColumns.push(renderActionButtonsColumn);
    return fieldColumns;
  };

  const renderFieldBody = rowData => rowData.fieldData.map(field => field.name).join(', ');

  if (constraintsState.isLoading) return <Spinner style={{ top: 0 }} />;

  return isEmpty(constraintsState.data) ? (
    <div className={styles.noConstraints}>{resources.messages['noConstraints']}</div>
  ) : (
    <Fragment>
      <Filters
        className={'uniqueConstraint'}
        data={constraintsState.data}
        getFilteredData={onLoadFilteredData}
        inputOptions={['filterFieldsNames']}
        selectOptions={['tableSchemaName']}
      />

      {!isEmpty(constraintsState.filteredData) ? (
        <DataTable
          autoLayout={true}
          onRowClick={event => getManageUniqueConstraint(event.data)}
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
          onHide={() => isDeleteDialogVisible(false)}
          visible={constraintsState.isDeleteDialogVisible}>
          {resources.messages['deleteUniqueConstraintConfirm']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
