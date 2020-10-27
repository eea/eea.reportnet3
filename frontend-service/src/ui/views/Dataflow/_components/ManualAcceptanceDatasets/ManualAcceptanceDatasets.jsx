import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';
import uniq from 'lodash/uniq';

import styles from './ManualAcceptanceDatasets.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { ManageManualAcceptanceDataset } from './_components/ManageManualAcceptanceDataset/ManageManualAcceptanceDataset';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/Dataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { manualAcceptanceDatasetsReducer } from './_functions/Reducers/manualAcceptanceDatasetsReducer';

export const ManualAcceptanceDatasets = ({ dataflowId, dataflowData, isManualTechnicalAcceptance }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [manualAcceptanceDatasetsState, manualAcceptanceDatasetsDispatch] = useReducer(
    manualAcceptanceDatasetsReducer,
    {
      allStatus: [],
      data: [],
      datasetToEdit: {},
      filtered: false,
      filteredData: [],
      isLoading: true,
      isManageDatasetDialogVisible: false,
      isUpdatedData: false
    }
  );

  useEffect(() => {
    onLoadManualAcceptanceDatasets();
  }, [manualAcceptanceDatasetsState.isUpdatedData]);

  const getFiltered = value => manualAcceptanceDatasetsDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {manualAcceptanceDatasetsState.filtered &&
      manualAcceptanceDatasetsState.data.length !== manualAcceptanceDatasetsState.filteredData.length
        ? `${resources.messages['filtered']} : ${manualAcceptanceDatasetsState.filteredData.length} | `
        : ''}
      {resources.messages['totalRecords']} {manualAcceptanceDatasetsState.data.length}{' '}
      {resources.messages['records'].toLowerCase()}
      {manualAcceptanceDatasetsState.filtered &&
      manualAcceptanceDatasetsState.data.length === manualAcceptanceDatasetsState.filteredData.length
        ? ` (${resources.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const handleManageAcceptanceDatasetDialog = value =>
    manualAcceptanceDatasetsDispatch({ type: 'ON_CLOSE_EDIT_DIALOG', payload: { value } });

  const isLoading = value => manualAcceptanceDatasetsDispatch({ type: 'IS_LOADING', payload: { value } });

  const response = [
    { id: 1, datasetSchemaName: 'Austria', status: 'technicallyAccept', name: 'ds1', isReleased: true },
    { id: 2, datasetSchemaName: 'Belgium', status: 'correctionRequested', name: 'ds2', isReleased: false },
    { id: 3, datasetSchemaName: 'Austria', status: 'technicallyAccept', name: 'ds1', isReleased: false },
    { id: 4, datasetSchemaName: 'Belgium', status: 'correctionRequested', name: 'ds2', isReleased: true }
  ];

  const onLoadManualAcceptanceDatasets = async () => {
    try {
      isLoading(true);
      const allStatus = uniq(Object.values(response.map(dataset => dataset.status)));
      manualAcceptanceDatasetsDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: response, filteredData: response, filtered: false, allStatus: allStatus }
      });

      // const response = await DataflowService.reporting(dataflowId);
      // console.log('response', response.datasets);
      // const allStatus = uniq(Object.values(response.map(dataset => dataset.status)));
      // manualAcceptanceDatasetsDispatch({
      //   type: 'INITIAL_LOAD',
      //   payload: { data: response.datasets, filteredData: response.datasets, filtered: false, allStatus: allStatus }
      // });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_DATASETS_RELEASES_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => manualAcceptanceDatasetsDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const onUpdatedData = value => manualAcceptanceDatasetsDispatch({ type: 'ON_UPDATED_DATA', payload: { value } });

  const getOrderedValidations = datasets => {
    const datasetsWithPriority = [
      { id: 'name', index: 0 },
      { id: 'datasetSchemaName', index: 1 },
      { id: 'status', index: 2 },
      { id: 'isReleased', index: 3 }
    ];

    return datasets
      .map(error => datasetsWithPriority.filter(e => error === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedError => orderedError.id);
  };

  const actionsTemplate = row => (
    <ActionsColumn
      onEditClick={() => {
        const [datasetToEdit] = manualAcceptanceDatasetsState.data.filter(dataset => dataset.id === row.id && row);
        manualAcceptanceDatasetsDispatch({ type: 'ON_EDIT_DATASET', payload: { datasetToEdit, value: true } });
      }}
    />
  );

  const isReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isReleased ? <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} /> : null}
    </div>
  );

  const renderActionButtonsColumn = (
    <Column
      body={row => row.isReleased && actionsTemplate(row)}
      className={styles.validationCol}
      header={resources.messages['changeStatus']}
      key="actions"
      style={{ width: '100px' }}
    />
  );

  const renderColumns = datasets => {
    const fieldColumns = getOrderedValidations(Object.keys(datasets[0])).map(field => {
      let template = null;
      if (field === 'isReleased') template = isReleasedTemplate;
      return (
        <Column
          body={template}
          columnResizeMode="expand"
          field={field}
          header={resources.messages[field]}
          key={field}
          sortable={true}
        />
      );
    });
    fieldColumns.push(renderActionButtonsColumn);
    return fieldColumns;
  };

  if (manualAcceptanceDatasetsState.isLoading)
    return (
      <div className={styles.manualAcceptanceDatasetsWithoutTable}>
        <div className={styles.spinner}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      </div>
    );

  return isEmpty(manualAcceptanceDatasetsState.data) ? (
    <div className={styles.historicReleasesWithoutTable}>
      <div className={styles.noManualAcceptanceDatasets}>{resources.messages['noDatasets']}</div>
    </div>
  ) : (
    <div className={styles.manualAcceptanceDatasets}>
      <Filters
        checkboxOptions={['isReleased']}
        data={manualAcceptanceDatasetsState.data}
        getFilteredData={onLoadFilteredData}
        getFilteredSearched={getFiltered}
        inputOptions={['name']}
        selectOptions={['datasetSchemaName', 'status']}
      />

      {!isEmpty(manualAcceptanceDatasetsState.filteredData) ? (
        <DataTable
          autoLayout={true}
          paginator={true}
          paginatorRight={getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={manualAcceptanceDatasetsState.filteredData.length}
          value={manualAcceptanceDatasetsState.filteredData}>
          {renderColumns(manualAcceptanceDatasetsState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resources.messages['noDatasetsWithSelectedParameters']}</div>
      )}

      {manualAcceptanceDatasetsState.isManageDatasetDialogVisible && (
        <ManageManualAcceptanceDataset
          allStatus={manualAcceptanceDatasetsState.allStatus}
          dataset={manualAcceptanceDatasetsState.datasetToEdit}
          isManageDatasetDialogVisible={manualAcceptanceDatasetsState.isManageDatasetDialogVisible}
          manageDialogs={handleManageAcceptanceDatasetDialog}
          onUpdatedData={onUpdatedData}
        />
      )}
    </div>
  );
};
