import React, { useContext, useEffect, useReducer, useState, Fragment } from 'react';

import isEmpty from 'lodash/isEmpty';

import ObligationConf from 'conf/obligation.config.json';

import { Filters } from 'ui/views/Dataflows/_components/DataflowsList/_components/Filters';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { MagazineView } from './_components/MagazineView';
import { SearchAll } from './_components/SearchAll';
import { Spinner } from 'ui/views/_components/Spinner';
import { TableView } from './_components/TableView';

import { ObligationService } from 'core/services/Obligation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { reportingObligationReducer } from './_functions/Reducers/ReportingObligationReducer';

import { on } from 'events';

// import { reportingObligationReducer } from './_functions/Reducers/reportingObligationReducer';

export const ReportingObligations = (dataflowId, refresh) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [filteredData, setFilteredData] = useState([]);

  const onLoadFiltredData = data => {
    setFilteredData(data);
  };

  const [reportingObligationState, reportingObligationDispatch] = useReducer(reportingObligationReducer, {
    data: [],
    isLoading: false,
    oblChoosed: {},
    isTableView: true
  });

  useEffect(() => {
    if (refresh) onLoadReportingObligations();
  }, [refresh]);

  const onLoadingData = value => reportingObligationDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadReportingObligations = async () => {
    onLoadingData(true);
    reportingObligationDispatch({ type: 'INITIAL_LOAD', payload: { data: await ObligationService.opened() } });
    onLoadFiltredData(reportingObligationState.data);

    try {
    } catch (error) {
    } finally {
      onLoadingData(false);
    }
  };

  const onSelectObl = rowData => {
    const oblChoosed = { id: rowData.obligationId, title: rowData.oblTitle };
    reportingObligationDispatch({ type: 'ON_SELECT_OBL', payload: { oblChoosed } });
  };

  const onToggleView = () =>
    reportingObligationDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view: !reportingObligationState.isTableView } });

  const renderData = () =>
    reportingObligationState.isTableView ? (
      <>
        <Filters
          data={reportingObligationState.data}
          dateOptions={ObligationConf.filterItems['date']}
          getFiltredData={onLoadFiltredData}
          inputOptions={ObligationConf.filterItems['input']}
          selectOptions={ObligationConf.filterItems['select']}
        />
        <TableView data={filteredData} onSelectObl={onSelectObl} />
      </>
    ) : (
      <MagazineView />
    );

  if (reportingObligationState.isLoading) return <Spinner />;

  return (
    <Fragment>
      {/* <div style={{ display: 'flex' }}>
        <SearchAll />
        <InputSwitch
          checked={reportingObligationState.isTableView}
          onChange={() => onToggleView()}
          style={{ marginRight: '1rem' }}
        />
      </div> */}
      {isEmpty(reportingObligationState.data) ? <h3>{resources.messages['emptyValidations']}</h3> : renderData()}
    </Fragment>
  );
};
