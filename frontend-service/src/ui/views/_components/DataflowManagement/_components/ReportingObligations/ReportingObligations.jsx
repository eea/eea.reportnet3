import React, { useContext, useEffect, useReducer, Fragment } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ReportingObligations.module.scss';

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

export const ReportingObligations = ({ getObligation, oblChecked }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [reportingObligationState, reportingObligationDispatch] = useReducer(reportingObligationReducer, {
    countries: [],
    data: [],
    filterBy: { expirationDate: [], countries: {}, issues: {}, organizations: {} },
    filteredData: [],
    isLoading: false,
    issues: [],
    isTableView: false,
    oblChoosed: {},
    organizations: [],
    pagination: { first: 0, rows: 10, page: 0 },
    searchedData: []
  });

  useEffect(() => {
    onLoadCountries();
    onLoadIssues();
    onLoadOrganizations();
    onLoadReportingObligations();
  }, []);

  useEffect(() => {
    if (getObligation) getObligation(reportingObligationState.oblChoosed);
  }, [reportingObligationState.oblChoosed]);

  const isLoading = value => reportingObligationDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadCountries = async () => {
    try {
      const countries = await ObligationService.getCountries();
      reportingObligationDispatch({ type: 'ON_LOAD_COUNTRIES', payload: { countries } });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_COUNTRIES_ERROR' });
    }
  };

  const onLoadIssues = async () => {
    try {
      const issues = await ObligationService.getIssues();
      reportingObligationDispatch({ type: 'ON_LOAD_ISSUES', payload: { issues } });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_ISSUES_ERROR' });
    }
  };

  const onLoadOrganizations = async () => {
    try {
      const organizations = await ObligationService.getOrganizations();
      reportingObligationDispatch({ type: 'ON_LOAD_ORGANIZATIONS', payload: { organizations } });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_ORGANIZATIONS_ERROR' });
    }
  };

  const onLoadReportingObligations = async filterData => {
    console.log('filterData', filterData);
    isLoading(true);
    try {
      const response = await ObligationService.opened(filterData);
      reportingObligationDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          data: response,
          filteredData: ReportingObligationUtils.filteredInitialValues(response, oblChecked.id),
          oblChoosed: oblChecked,
          filterBy: filterData
        }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_OPENED_OBLIGATION_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onLoadSearchedData = data => reportingObligationDispatch({ type: 'SEARCHED_DATA', payload: { data } });

  const onChangePagination = pagination =>
    reportingObligationDispatch({ type: 'ON_PAGINATE', payload: { pagination } });

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

  const renderData = () => (
    <Fragment>
      {reportingObligationState.isTableView ? (
        <TableView
          checkedObligation={reportingObligationState.oblChoosed}
          data={reportingObligationState.searchedData}
          onChangePagination={onChangePagination}
          onSelectObl={onSelectObl}
          pagination={reportingObligationState.pagination}
        />
      ) : (
        <CardsView
          checkedObligation={reportingObligationState.oblChoosed}
          data={reportingObligationState.searchedData}
          onChangePagination={onChangePagination}
          onSelectObl={onSelectObl}
          pagination={reportingObligationState.pagination}
        />
      )}

      <span className={styles.selectedObligation}>
        <span>{`${resources.messages['selectedObligation']}: `}</span>
        {`${
          !isEmpty(reportingObligationState.oblChoosed.title) && !isEmpty(reportingObligationState.oblChoosed)
            ? reportingObligationState.oblChoosed.title
            : '-'
        }`}
      </span>
    </Fragment>
  );

  return (
    <div className={styles.reportingObligation}>
      <div className={styles.filters}>
        <Filters
          data={reportingObligationState.data}
          dateOptions={ObligationConf.filterItems['date']}
          dropDownList={parsedFilterList}
          dropdownOptions={ObligationConf.filterItems['dropdown']}
          filterByList={reportingObligationState.filterBy}
          sendData={onLoadReportingObligations}
        />
      </div>
      <div className={styles.repOblTools}>
        <SearchAll data={reportingObligationState.filteredData} getValues={onLoadSearchedData} />
        <div className={styles.switchDiv}>
          <label className={styles.switchTextInput}>{resources.messages['magazineView']}</label>
          <InputSwitch checked={reportingObligationState.isTableView} onChange={() => onToggleView()} />
          <label className={styles.switchTextInput}>{resources.messages['listView']}</label>
        </div>
      </div>
      {reportingObligationState.isLoading ? (
        <Spinner style={{ top: 0, left: 0 }} />
      ) : isEmpty(reportingObligationState.data) ? (
        <h3 className={styles.noObligations}>{resources.messages['emptyObligationList']}</h3>
      ) : (
        renderData()
      )}
    </div>
  );
};
