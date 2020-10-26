import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ManualAcceptanceDatasets.module.scss';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { manualAcceptanceDatasetsReducer } from './_functions/Reducers/manualAcceptanceDatasetsReducer';

export const ManualAcceptanceDatasets = ({ dataflowId, dataflowData }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [manualAcceptanceDatasetsState, manualAcceptanceDatasetsDispatch] = useReducer(
    manualAcceptanceDatasetsReducer,
    {
      data: [],
      filtered: false,
      filteredData: [],
      isLoading: true
    }
  );

  useEffect(() => {
    onLoadManualAcceptanceDatasets();
  }, []);

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

  const isLoading = value => manualAcceptanceDatasetsDispatch({ type: 'IS_LOADING', payload: { value } });

  const response = [
    { id: 1, countryCode: 'Austria', status: 'technicallyAccept', datasetName: 'ds1' },
    { id: 2, countryCode: 'Belgium', status: 'correctionRequested', datasetName: 'ds2' }
  ];

  const onLoadManualAcceptanceDatasets = async () => {
    try {
      isLoading(true);
      manualAcceptanceDatasetsDispatch({
        type: 'INITIAL_LOAD',
        payload: { data: response, filteredData: response, filtered: false }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_HISTORIC_RELEASES_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => manualAcceptanceDatasetsDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const getOrderedValidations = datasets => {
    const datasetsWithPriority = [
      { id: 'countryCode', index: 0 },
      { id: 'datasetName', index: 1 },
      { id: 'status', index: 2 }
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
        const datasetEdit = manualAcceptanceDatasetsState.data.filter(dataset => dataset.id === row.id && row);
        const datasetIdEdit = datasetEdit[0].id;
        console.log('datasetIdEdit', datasetIdEdit);
      }}
    />
  );

  const renderActionButtonsColumn = (
    <Column
      body={row => actionsTemplate(row)}
      className={styles.validationCol}
      header={resources.messages['actions']}
      key="actions"
      style={{ width: '100px' }}
    />
  );

  const renderColumns = datasets => {
    const fieldColumns = getOrderedValidations(Object.keys(datasets[0]))
      // .filter(key => key.includes('datasetName') || key.includes('releasedDate'))
      .map(field => {
        let template = null;
        // if (field === 'releasedDate') template = releasedDateTemplate;
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

  return isEmpty(manualAcceptanceDatasetsState.data) ? (
    <div className={styles.historicReleasesWithoutTable}>
      <div className={styles.noHistoricReleases}>{resources.messages['noHistoricReleases']}</div>
    </div>
  ) : (
    <div className={styles.historicReleases}>
      <Filters
        data={manualAcceptanceDatasetsState.data}
        getFilteredData={onLoadFilteredData}
        getFilteredSearched={getFiltered}
        selectOptions={['countryCode', 'datasetName', 'status']}
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
        <div className={styles.emptyFilteredData}>{resources.messages['noHistoricReleasesWithSelectedParameters']}</div>
      )}
    </div>
  );
};
