import React, { useContext, useEffect, useReducer, useState, Fragment } from 'react';

import isEmpty from 'lodash/isEmpty';

import ObligationConf from 'conf/obligation.config.json';

import { CardsView } from './_components/CardsView';
import { Filters } from 'ui/views/_components/Filters';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { SearchAll } from './_components/SearchAll';
import { Spinner } from 'ui/views/_components/Spinner';
import { TableView } from './_components/TableView';

import { ObligationService } from 'core/services/Obligation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { reportingObligationReducer } from './_functions/Reducers/reportingObligationReducer';

import { ReportingObligationUtils } from './_functions/Utils/ReportingObligationUtils';

export const ReportingObligations = (dataflowId, refresh) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [reportingObligationState, reportingObligationDispatch] = useReducer(reportingObligationReducer, {
    countries: [],
    data: [],
    filteredData: [],
    isLoading: false,
    issues: [],
    isTableView: true,
    oblChoosed: {},
    organizations: []
  });

  useEffect(() => {
    if (refresh) onLoadReportingObligations();
  }, [refresh]);

  const onLoadingData = value => reportingObligationDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadReportingObligations = async () => {
    onLoadingData(true);
    try {
      const data = await ObligationService.opened();
      const countries = await ObligationService.getCountries();
      const issues = await ObligationService.getIssues();
      const organizations = await ObligationService.getOrganizations();

      reportingObligationDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          countries,
          data,
          filteredData: ReportingObligationUtils.filteredInitialValues(data),
          issues,
          organizations
        }
      });
    } catch (error) {
    } finally {
      onLoadingData(false);
    }
  };

  const onSendFilters = data => console.log('data', data);

  const onSelectObl = rowData => {
    const oblChoosed = { id: rowData.id, title: rowData.title };
    reportingObligationDispatch({ type: 'ON_SELECT_OBL', payload: { oblChoosed } });
  };

  const onToggleView = () =>
    reportingObligationDispatch({ type: 'ON_TOGGLE_VIEW', payload: { view: !reportingObligationState.isTableView } });

  const parsedFilterList = {
    countries: reportingObligationState.countries,
    issues: reportingObligationState.issues,
    organizations: reportingObligationState.organizations
  };

  const renderData = () =>
    reportingObligationState.isTableView ? (
      <TableView
        checkedRow={reportingObligationState.oblChoosed}
        data={reportingObligationState.filteredData}
        onSelectObl={onSelectObl}
      />
    ) : (
      <CardsView />
    );

  if (reportingObligationState.isLoading) return <Spinner />;

  return (
    <Fragment>
      <Filters
        data={reportingObligationState.data}
        dateOptions={ObligationConf.filterItems['date']}
        dropDownList={parsedFilterList}
        dropdownOptions={ObligationConf.filterItems['dropdown']}
        sendData={onSendFilters}
      />
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
