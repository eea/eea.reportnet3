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

export const UniqueConstraints = ({
  dataflowId,
  designerState,
  getManageUniqueConstraint,
  getUniques,
  manageDialogs,
  needsRefresh = true,
  refreshList,
  setIsDuplicatedToManageUnique,
  setConstraintManagingId,
  setIsUniqueConstraintCreating,
  setIsUniqueConstraintUpdating
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const {
    datasetSchemaId,
    datasetSchemaAllTables,
    constraintManagingId,
    isUniqueConstraintCreating,
    isUniqueConstraintUpdating,
    manageUniqueConstraintData: { uniqueId }
  } = designerState;

  const [constraintsState, constraintsDispatch] = useReducer(constraintsReducer, {
    data: [],
    filtered: false,
    filteredData: [],
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isLoading: true,
    isDeleting: false
  });

  useEffect(() => {
    if (!designerState.isManageUniqueConstraintDialogVisible && needsRefresh) {
      onLoadConstraints();
    }
  }, [constraintsState.isDataUpdated, designerState.isManageUniqueConstraintDialogVisible, needsRefresh]);

  useEffect(() => {
    if (getUniques) getUniques(constraintsState.data);
  }, [constraintsState.data]);

  const actionsTemplate = row => (
    <ActionsColumn
      isDeletingDocument={constraintsState.isDeleting}
      isUpdating={isUniqueConstraintUpdating}
      onDeleteClick={() => isDeleteDialogVisible(true)}
      onEditClick={() => manageDialogs('isManageUniqueConstraintDialogVisible', true)}
      rowDataId={row.uniqueId}
      rowDeletingId={constraintManagingId}
      rowUpdatingId={constraintManagingId}
    />
  );

  const getFilteredState = value => constraintsDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {constraintsState.filtered && constraintsState.data.length !== constraintsState.filteredData.length
        ? `${resources.messages['filtered']} : ${constraintsState.filteredData.length} | `
        : ''}
      {resources.messages['totalRecords']} {constraintsState.data.length} {resources.messages['records'].toLowerCase()}
      {constraintsState.filtered && constraintsState.data.length === constraintsState.filteredData.length
        ? ` (${resources.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const isDataUpdated = value => constraintsDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const isDeleteDialogVisible = value => constraintsDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => constraintsDispatch({ type: 'IS_LOADING', payload: { value } });

  const onDeleteConstraint = async () => {
    setConstraintManagingId(uniqueId);
    constraintsDispatch({ type: 'IS_DELETING', payload: true });
    try {
      const response = await UniqueConstraintsService.deleteById(dataflowId, uniqueId);
      if (response.status >= 200 && response.status <= 299) {
        onUpdateData();
        refreshList(true);
      }
    } catch (error) {
      notificationContext.add({ type: 'DELETE_UNIQUE_CONSTRAINT_ERROR' });
      constraintsDispatch({ type: 'IS_DELETING', payload: false });
    } finally {
      isDeleteDialogVisible(false);
      getManageUniqueConstraint({ tableSchemaId: null, tableSchemaName: '', fieldData: [], uniqueId: null });
    }
  };

  const onLoadConstraints = async () => {
    try {
      if (isUniqueConstraintCreating || isUniqueConstraintUpdating || constraintsState.isDeleting) {
        isLoading(false);
      }
      const response = await UniqueConstraintsService.all(dataflowId, datasetSchemaId);
      const uniques = UniqueConstraintsUtils.parseConstraintsList(response.data, datasetSchemaAllTables);
      constraintsDispatch({ type: 'INITIAL_LOAD', payload: { data: uniques, filteredData: uniques } });
      setIsDuplicatedToManageUnique(false);
      refreshList(false);
    } catch (error) {
      notificationContext.add({ type: 'LOAD_UNIQUE_CONSTRAINTS_ERROR' });
    } finally {
      isLoading(false);
      constraintsDispatch({ type: 'IS_DELETING', payload: false });
      setIsUniqueConstraintCreating(false);
      setIsUniqueConstraintUpdating(false);
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

  if (constraintsState.isLoading)
    return (
      <div className={styles.constraintsWithoutTable}>
        <div className={styles.spinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      </div>
    );

  return isEmpty(constraintsState.data) ? (
    <div className={styles.constraintsWithoutTable}>
      <div className={styles.noConstraints}>{resources.messages['noConstraints']}</div>
    </div>
  ) : (
    <div className={styles.constraints}>
      <Filters
        className={'uniqueConstraints'}
        data={constraintsState.data}
        getFilteredData={onLoadFilteredData}
        getFilteredSearched={getFilteredState}
        matchMode={true}
        selectList={{ fieldData: UniqueConstraintsUtils.getFieldsOptions(constraintsState.data) }}
        selectOptions={['tableSchemaName', 'fieldData']}
      />

      {!isEmpty(constraintsState.filteredData) ? (
        <DataTable
          autoLayout={true}
          onRowClick={event => getManageUniqueConstraint(event.data)}
          paginator={true}
          paginatorRight={getPaginatorRecordsCount()}
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
          disabledConfirm={constraintsState.isDeleting}
          header={resources.messages['deleteUniqueConstraintHeader']}
          iconConfirm={constraintsState.isDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          onConfirm={() => onDeleteConstraint()}
          onHide={() => isDeleteDialogVisible(false)}
          visible={constraintsState.isDeleteDialogVisible}>
          {resources.messages['deleteUniqueConstraintConfirm']}
        </ConfirmDialog>
      )}
    </div>
  );
};
