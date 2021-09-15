import { Fragment, useContext, useEffect, useReducer } from 'react';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import isEmpty from 'lodash/isEmpty';

import styles from './ManualAcceptanceDatasets.module.scss';

import { config } from 'conf';

import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { DataflowService } from 'services/DataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { manualAcceptanceDatasetsReducer } from './_functions/Reducers/manualAcceptanceDatasetsReducer';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const ManualAcceptanceDatasets = ({
  dataflowId,
  dataflowType,
  getManageAcceptanceDataset,
  isUpdatedManualAcceptanceDatasets,
  manageDialogs,
  refreshManualAcceptanceDatasets
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [manualAcceptanceDatasetsState, manualAcceptanceDatasetsDispatch] = useReducer(
    manualAcceptanceDatasetsReducer,
    { data: [], filtered: false, filteredData: [], isLoading: true }
  );

  useEffect(() => {
    onLoadManualAcceptanceDatasets();
  }, [isUpdatedManualAcceptanceDatasets]);

  const getFiltered = value => manualAcceptanceDatasetsDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {manualAcceptanceDatasetsState.filtered &&
      manualAcceptanceDatasetsState.data.length !== manualAcceptanceDatasetsState.filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${manualAcceptanceDatasetsState.filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {manualAcceptanceDatasetsState.data.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {manualAcceptanceDatasetsState.filtered &&
      manualAcceptanceDatasetsState.data.length === manualAcceptanceDatasetsState.filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const isLoading = value => manualAcceptanceDatasetsDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadManualAcceptanceDatasets = async () => {
    try {
      isLoading(true);
      const data = await DataflowService.getDatasetsFinalFeedback(dataflowId);
      manualAcceptanceDatasetsDispatch({
        type: 'INITIAL_LOAD',
        payload: { data, filteredData: data, filtered: false }
      });
      refreshManualAcceptanceDatasets(false);
    } catch (error) {
      console.error('ManualAcceptanceDatasets - onLoadManualAcceptanceDatasets.', error);
      notificationContext.add({ type: 'LOAD_DATASETS_RELEASES_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onLoadFilteredData = data => manualAcceptanceDatasetsDispatch({ type: 'FILTERED_DATA', payload: { data } });

  const getOrderedValidations = datasets => {
    const datasetsWithPriority = [
      { id: 'datasetName', index: 0 },
      { id: 'dataProviderName', index: 1 },
      { id: 'feedbackStatus', index: 2 },
      { id: 'isReleased', index: 3 }
    ];

    return datasets
      .map(error => datasetsWithPriority.filter(e => error === e.id))
      .flat()
      .sort((a, b) => a.index - b.index)
      .map(orderedError => orderedError.id);
  };

  const actionsTemplate = () => (
    <ActionsColumn
      onEditClick={() => {
        manageDialogs(true);
      }}
    />
  );

  const isReleasedTemplate = rowData => (
    <div className={styles.checkedValueColumn}>
      {rowData.isReleased ? (
        <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons('check')} role="presentation" />
      ) : null}
    </div>
  );

  const renderActionButtonsColumn = (
    <Column
      body={row => row.isReleased && actionsTemplate(row)}
      className={styles.validationCol}
      header={resourcesContext.messages['changeStatus']}
      key="actions"
      style={{ width: '100px' }}
    />
  );

  const getCodeLabelByDataflowType = () => {
    switch (dataflowType) {
      case config.dataflowType.BUSINESS.value:
        return resourcesContext.messages['company'];

      case config.dataflowType.CITIZEN_SCIENCE.value:
        return resourcesContext.messages['organization'];

      default:
        return resourcesContext.messages['dataProviderName'];
    }
  };

  const filterOptions = [
    { type: 'input', properties: [{ name: 'datasetName' }] },
    {
      type: 'multiselect',
      properties: [
        {
          name: 'dataProviderName',
          label: getCodeLabelByDataflowType()
        },
        { name: 'feedbackStatus' }
      ]
    },
    {
      type: 'checkbox',
      properties: [{ name: 'isReleased', label: resourcesContext.messages['onlyReleasedCheckboxLabel'] }]
    }
  ];

  const renderColumns = datasets => {
    const fieldColumns = getOrderedValidations(Object.keys(datasets[0])).map(field => {
      let template = null;
      if (field === 'isReleased') template = isReleasedTemplate;
      return (
        <Column
          body={template}
          columnResizeMode="expand"
          field={field}
          header={
            TextUtils.areEquals(field, 'dataProviderName')
              ? getCodeLabelByDataflowType()
              : resourcesContext.messages[field]
          }
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
      <div className={styles.noManualAcceptanceDatasets}>{resourcesContext.messages['noDatasets']}</div>
    </div>
  ) : (
    <div className={styles.manualAcceptanceDatasets}>
      <Filters
        data={manualAcceptanceDatasetsState.data}
        getFilteredData={onLoadFilteredData}
        getFilteredSearched={getFiltered}
        options={filterOptions}
      />
      {!isEmpty(manualAcceptanceDatasetsState.filteredData) ? (
        <DataTable
          autoLayout={true}
          onRowClick={event => getManageAcceptanceDataset(event.data)}
          paginator={true}
          paginatorRight={getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          summary="manualAcceptance"
          totalRecords={manualAcceptanceDatasetsState.filteredData.length}
          value={manualAcceptanceDatasetsState.filteredData}>
          {renderColumns(manualAcceptanceDatasetsState.filteredData)}
        </DataTable>
      ) : (
        <div className={styles.emptyFilteredData}>{resourcesContext.messages['noDatasetsWithSelectedParameters']}</div>
      )}
    </div>
  );
};
