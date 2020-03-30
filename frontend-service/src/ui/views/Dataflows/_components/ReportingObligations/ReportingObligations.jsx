import React, { useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Spinner } from 'ui/views/_components/Spinner';

import { ObligationService } from 'core/services/Obligation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { ReportingObligationReducer } from './_functions/Reducers/ReportingObligationReducer';

export const ReportingObligations = (dataflowId, refresh) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [ReportingObligationState, ReportingObligationDispatch] = useReducer(ReportingObligationReducer, {
    data: [],
    isLoading: false
  });

  console.log('ReportingObligationState', ReportingObligationState);

  useEffect(() => {
    if (refresh) {
      onLoadReportingObligations();
    }
  }, [refresh]);

  const onLoadingData = value => ReportingObligationDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadReportingObligations = async () => {
    onLoadingData(true);
    ReportingObligationDispatch({ type: 'INITIAL_LOAD', payload: { data: await ObligationService.opened() } });
    try {
    } catch (error) {
    } finally {
      onLoadingData(false);
    }
  };

  const renderColumns = data => {
    console.log('data', data);
    return (
      <Column
        // body={template}
        // key={field}
        columnResizeMode="expand"
        // field={field}
        // header={getHeader(field)}
        // sortable={true}
        // style={columnStyles(field)}
      />
    );
  };

  const renderData = () => (
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
  );

  if (ReportingObligationState.isLoading) return <Spinner />;

  return isEmpty(ReportingObligationState.data) ? <h3>{resources.messages['emptyValidations']}</h3> : renderData();
};
