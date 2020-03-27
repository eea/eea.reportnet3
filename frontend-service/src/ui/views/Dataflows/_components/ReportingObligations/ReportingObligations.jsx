import React, { Fragment, useEffect, useReducer } from 'react';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { DataTable } from 'ui/views/_components/DataTable';
import { Spinner } from 'ui/views/_components/Spinner';

import { ReportingObligationReducer } from './_functions/Reducers/ReportingObligationReducer';

export const ReportingObligations = (dataflowId, refresh) => {
  const [ReportingObligationState, ReportingObligationDispatch] = useReducer(ReportingObligationReducer, {});

  useEffect(() => {
    if (refresh) {
      onLoadReportingObligations();
    }
  }, [refresh, dataflowId]);

  const onLoadingData = value => ReportingObligationDispatch({ type: 'LOADING_DATA', payload: { value } });

  const onLoadReportingObligations = () => {
    onLoadingData(true);
    try {
    } catch (error) {
    } finally {
      onLoadingData(false);
    }
  };

  if (ReportingObligationState.isLoading) {
    return <Spinner />;
  }

  return (
    <Fragment>
      <DataTable />
    </Fragment>
  );
};
