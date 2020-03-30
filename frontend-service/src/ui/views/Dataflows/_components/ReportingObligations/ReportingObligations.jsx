import React, { useContext, useEffect, useReducer, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import ObligationConf from 'conf/obligation.config.json';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Checkbox } from 'ui/views/_components/Checkbox';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/Dataflows/_components/DataflowsList/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { ObligationService } from 'core/services/Obligation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { ReportingObligationReducer } from './_functions/Reducers/ReportingObligationReducer';
import { on } from 'events';

export const ReportingObligations = (dataflowId, refresh) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [filteredData, setFilteredData] = useState([]);

  const onLoadFiltredData = data => {
    setFilteredData(data);
  };

  const [ReportingObligationState, ReportingObligationDispatch] = useReducer(ReportingObligationReducer, {
    data: [],
    isLoading: false,
    oblChoosed: {}
  });

  useEffect(() => {
    if (refresh) onLoadReportingObligations();
  }, [refresh]);

  const onLoadingData = value => ReportingObligationDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadReportingObligations = async () => {
    onLoadingData(true);
    ReportingObligationDispatch({ type: 'INITIAL_LOAD', payload: { data: await ObligationService.opened() } });
    onLoadFiltredData(ReportingObligationState.data);
    try {
    } catch (error) {
    } finally {
      onLoadingData(false);
    }
  };

  const onLoadCheckButton = row => <Checkbox checked={true} onChange={() => onSelectObl(row)} role="checkbox" />;

  const onSelectObl = rowData => {
    const oblChoosed = { id: rowData.obligationId, title: rowData.oblTitle };
    ReportingObligationDispatch({ type: 'ON_SELECT_OBL', payload: { oblChoosed } });
  };

  const renderCheckColum = <Column key="checkId" body={row => onLoadCheckButton(row)} />;

  const renderColumns = data => {
    const repOblCols = [];
    const repOblKeys = !isEmpty(data) ? Object.keys(data[0]) : [];
    repOblCols.push(
      repOblKeys.map(obligation => (
        <Column
          key={obligation}
          field={obligation}
          // body={template}
          // key={field}
          columnResizeMode="expand"
          // field={field}
          header={obligation}
          // sortable={true}
          // style={columnStyles(field)}
        />
      ))
    );
    return [renderCheckColum, ...repOblCols];
  };

  console.log('filteredData', filteredData);

  const renderData = data => (
    <>
      <Filters
        data={ReportingObligationState.data}
        dateOptions={ObligationConf.filterItems['date']}
        getFiltredData={onLoadFiltredData}
        inputOptions={ObligationConf.filterItems['input']}
        selectOptions={ObligationConf.filterItems['select']}
      />

      <DataTable
        autoLayout={true}
        // onRowClick={event => setValidationId(event.data.id)}
        paginator={true}
        rows={10}
        rowsPerPageOptions={[5, 10, 15]}
        totalRecords={ReportingObligationState.data.length}
        value={ReportingObligationState.data}>
        {renderColumns(ReportingObligationState.data)}
      </DataTable>
    </>
  );

  if (ReportingObligationState.isLoading) return <Spinner />;

  return isEmpty(ReportingObligationState.data) ? <h3>{resources.messages['emptyValidations']}</h3> : renderData();
};
