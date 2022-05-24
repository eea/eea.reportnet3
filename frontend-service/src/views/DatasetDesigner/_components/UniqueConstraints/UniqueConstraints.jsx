import { useContext, useEffect, useReducer } from 'react';
import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';

import styles from './UniqueConstraints.module.scss';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { UniqueConstraintService } from 'services/UniqueConstraintService';

import { filteredDataStore } from 'views/_components/Filters/_functions/Stores/filterStore';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';

import { constraintsReducer } from './_functions/Reducers/constraintsReducer';

import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';
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
  const resourcesContext = useContext(ResourcesContext);

  const filteredData = useRecoilValue(filteredDataStore('uniqueConstraints'));

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
    isDataUpdated: false,
    isDeleteDialogVisible: false,
    isLoading: true,
    isDeleting: false
  });

  const { isFiltered, setData } = useApplyFilters('uniqueConstraints');

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

  const isDataUpdated = value => constraintsDispatch({ type: 'IS_DATA_UPDATED', payload: { value } });

  const isDeleteDialogVisible = value => constraintsDispatch({ type: 'IS_DELETE_DIALOG_VISIBLE', payload: { value } });

  const isLoading = value => constraintsDispatch({ type: 'IS_LOADING', payload: { value } });

  const onDeleteConstraint = async () => {
    setConstraintManagingId(uniqueId);
    constraintsDispatch({ type: 'IS_DELETING', payload: true });
    try {
      await UniqueConstraintService.delete(dataflowId, uniqueId);
      onUpdateData();
      refreshList(true);
    } catch (error) {
      console.error('UniqueConstraints - onDeleteConstraint.', error);
      notificationContext.add({ type: 'DELETE_UNIQUE_CONSTRAINT_ERROR' }, true);
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
      const uniqueConstraintList = await UniqueConstraintService.getAll(dataflowId, datasetSchemaId);
      const uniques = UniqueConstraintsUtils.parseConstraintsList(uniqueConstraintList, datasetSchemaAllTables);

      constraintsDispatch({ type: 'INITIAL_LOAD', payload: { data: uniques } });
      setData(uniques);
      setIsDuplicatedToManageUnique(false);
      refreshList(false);
    } catch (error) {
      console.error('UniqueConstraints - onLoadConstraints.', error);
      notificationContext.add({ type: 'LOAD_UNIQUE_CONSTRAINTS_ERROR' }, true);
    } finally {
      isLoading(false);
      constraintsDispatch({ type: 'IS_DELETING', payload: false });
      setIsUniqueConstraintCreating(false);
      setIsUniqueConstraintUpdating(false);
    }
  };

  const onUpdateData = () => isDataUpdated(!constraintsState.isDataUpdated);

  const renderActionButtonsColumn = (
    <Column
      body={row => actionsTemplate(row)}
      className={styles.validationCol}
      header={resourcesContext.messages['actions']}
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
          header={resourcesContext.messages[field]}
          key={field}
          sortable={true}
        />
      ));

    fieldColumns.push(renderActionButtonsColumn);
    return fieldColumns;
  };

  const renderFieldBody = rowData => rowData.fieldData.map(field => field.name).join(', ');

  const filterOptions = [
    {
      nestedOptions: [
        { key: 'tableSchemaName', label: resourcesContext.messages['tableSchemaName'] },
        {
          key: 'fieldData',
          nestedKey: 'fieldId',
          label: resourcesContext.messages['fieldData'],
          multiSelectOptions: UniqueConstraintsUtils.getFieldsOptions(constraintsState.data)
        }
      ],
      type: 'MULTI_SELECT'
    }
  ];

  const renderContent = () => {
    if (isEmpty(filteredData)) {
      return (
        <div className={styles.emptyFilteredData}>
          {resourcesContext.messages['noConstraintsWithSelectedParameters']}
        </div>
      );
    }

    return (
      <DataTable
        autoLayout={true}
        onRowClick={event => getManageUniqueConstraint(event.data)}
        paginator={true}
        paginatorRight={
          <PaginatorRecordsCount
            dataLength={constraintsState.data.length}
            filteredDataLength={filteredData.length}
            isFiltered={isFiltered}
          />
        }
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        summary={resourcesContext.messages['uniqueConstraints']}
        totalRecords={filteredData.length}
        value={filteredData}>
        {renderColumns(filteredData)}
      </DataTable>
    );
  };

  if (constraintsState.isLoading) {
    return (
      <div className={styles.constraintsWithoutTable}>
        <div className={styles.spinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      </div>
    );
  }

  if (isEmpty(constraintsState.data)) {
    return (
      <div className={styles.constraintsWithoutTable}>
        <div className={styles.noConstraints}>{resourcesContext.messages['noConstraints']}</div>
      </div>
    );
  }

  return (
    <div className={styles.constraints}>
      <Filters className="lineItems" isStrictModeVisible={true} options={filterOptions} recoilId="uniqueConstraints" />

      {renderContent()}

      {constraintsState.isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm={'p-button-danger'}
          disabledConfirm={constraintsState.isDeleting}
          header={resourcesContext.messages['deleteUniqueConstraintHeader']}
          iconConfirm={constraintsState.isDeleting ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onDeleteConstraint}
          onHide={() => isDeleteDialogVisible(false)}
          visible={constraintsState.isDeleteDialogVisible}>
          {resourcesContext.messages['deleteUniqueConstraintConfirm']}
        </ConfirmDialog>
      )}
    </div>
  );
};
