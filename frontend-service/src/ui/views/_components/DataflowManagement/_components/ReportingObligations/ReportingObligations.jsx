import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ReportingObligations.module.scss';

import { CardsView } from 'ui/views/_components/CardsView';
import { Filters } from 'ui/views/_components/Filters';
import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { Spinner } from 'ui/views/_components/Spinner';
import { TableView } from './_components/TableView';

import { ObligationService } from 'core/services/Obligation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { reportingObligationReducer } from './_functions/Reducers/reportingObligationReducer';

import { ReportingObligationUtils } from './_functions/Utils/ReportingObligationUtils';
import { RodUrl } from 'core/infrastructure/RodUrl';

export const ReportingObligations = ({ getObligation, oblChecked }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [reportingObligationState, reportingObligationDispatch] = useReducer(reportingObligationReducer, {
    countries: [],
    data: [],
    filterBy: { expirationDate: [], countries: {}, issues: {}, organizations: {} },
    filteredData: [],
    filteredSearched: false,
    isFiltered: false,
    isLoading: false,
    isSearched: false,
    issues: [],
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

  useEffect(() => {
    if (!isNil(reportingObligationState.filterBy)) {
      if (
        !isEmpty(reportingObligationState.filterBy.countries) ||
        !isEmpty(reportingObligationState.filterBy.issues) ||
        !isEmpty(reportingObligationState.filterBy.organizations) ||
        reportingObligationState.filterBy.expirationDate.length !== 0
      ) {
        reportingObligationDispatch({ type: 'IS_SEARCHED', payload: { value: true } });
      } else {
        reportingObligationDispatch({ type: 'IS_SEARCHED', payload: { value: false } });
      }
    }
  }, [reportingObligationState.filterBy]);

  useEffect(() => {
    if (reportingObligationState.isSearched || reportingObligationState.isFiltered) {
      getFilteredSeearched(true);
    } else {
      getFilteredSeearched(false);
    }
  }, [reportingObligationState.isSearched, reportingObligationState.isFiltered]);

  const getFiltered = value => reportingObligationDispatch({ type: 'IS_FILTERED', payload: { value } });

  const getFilteredSeearched = value =>
    reportingObligationDispatch({ type: 'IS_FILTERED_SEARCHED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {reportingObligationState.filteredSearched &&
      reportingObligationState.searchedData.length !== reportingObligationState.filteredData.length
        ? `${resources.messages['filtered']}: ${reportingObligationState.searchedData.length} | `
        : ''}
      {resources.messages['totalRecords']} {reportingObligationState.data.length}{' '}
      {resources.messages['records'].toLowerCase()}
      {reportingObligationState.filteredSearched &&
      reportingObligationState.searchedData.length === reportingObligationState.filteredData.length
        ? ` (${resources.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const isLoading = value => reportingObligationDispatch({ type: 'IS_LOADING', payload: { value } });

  const onChangePagination = pagination => {
    reportingObligationDispatch({ type: 'ON_PAGINATE', payload: { pagination } });
  };

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
    isLoading(true);
    try {
      const response = await ObligationService.opened(filterData);
      reportingObligationDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          data: ReportingObligationUtils.initialValues(response, userContext.userProps.dateFormat),
          filteredData: ReportingObligationUtils.filteredInitialValues(
            response,
            oblChecked.id,
            userContext.userProps.dateFormat
          ),
          oblChoosed: oblChecked,
          filterBy: filterData,
          pagination: { first: 0, rows: 10, page: 0 }
        }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_OPENED_OBLIGATION_ERROR' });
    } finally {
      isLoading(false);
    }
  };

  const onLoadSearchedData = data => reportingObligationDispatch({ type: 'SEARCHED_DATA', payload: { data } });

  const onOpenObligation = id => window.open(`${RodUrl.obligations}${id}`);

  const onSelectObl = rowData => {
    const oblChoosed = { id: rowData.id, title: rowData.title };
    reportingObligationDispatch({ type: 'ON_SELECT_OBL', payload: { oblChoosed } });
  };

  const filterOptions = [
    {
      type: 'dropdown',
      properties: [{ name: 'countries' }, { name: 'issues' }, { name: 'organizations' }]
    },

    { type: 'date', properties: [{ name: 'expirationDate' }] }
  ];

  const parsedFilterList = {
    countries: reportingObligationState.countries,
    issues: reportingObligationState.issues,
    organizations: reportingObligationState.organizations
  };

  const renderData = () =>
    userContext.userProps.listView ? (
      <TableView
        checkedObligation={reportingObligationState.oblChoosed}
        data={reportingObligationState.searchedData}
        onChangePagination={onChangePagination}
        onSelectObl={onSelectObl}
        pagination={reportingObligationState.pagination}
        paginatorRightText={getPaginatorRecordsCount()}
      />
    ) : (
      <CardsView
        checkedCard={reportingObligationState.oblChoosed}
        contentType={'Obligations'}
        data={reportingObligationState.searchedData}
        handleRedirect={onOpenObligation}
        onChangePagination={onChangePagination}
        onSelectCard={onSelectObl}
        pagination={reportingObligationState.pagination}
        paginatorRightText={getPaginatorRecordsCount()}
      />
    );

  return (
    <div
      className={styles.reportingObligation}
      style={{
        justifyContent:
          reportingObligationState.isLoading ||
          isEmpty(reportingObligationState.data) ||
          isEmpty(reportingObligationState.searchedData)
            ? 'flex-start'
            : 'space-between'
      }}>
      <div className={styles.repOblTools}>
        <Filters
          data={reportingObligationState.filteredData}
          getFilteredData={onLoadSearchedData}
          getFilteredSearched={getFiltered}
          searchAll
        />
        <div className={styles.switchDiv}>
          <label className={styles.switchTextInput}>{resources.messages['magazineView']}</label>
          <InputSwitch checked={userContext.userProps.listView} onChange={e => userContext.onToggleTypeView(e.value)} />
          <label className={styles.switchTextInput}>{resources.messages['listView']}</label>
        </div>
      </div>

      <div className={styles.filters}>
        <Filters
          data={reportingObligationState.data}
          dropDownList={parsedFilterList}
          filterByList={reportingObligationState.filterBy}
          options={filterOptions}
          sendData={onLoadReportingObligations}
        />
      </div>

      {reportingObligationState.isLoading ? (
        <Spinner className={styles.spinner} />
      ) : isEmpty(reportingObligationState.data) ? (
        reportingObligationState.filteredSearched ? (
          <h3 className={styles.noObligations}>{resources.messages['noObligationsWithSelectedParameters']}</h3>
        ) : (
          <h3 className={styles.noObligations}>{resources.messages['emptyObligationList']}</h3>
        )
      ) : (
        renderData()
      )}

      {!reportingObligationState.isLoading && (
        <span
          className={`${styles.selectedObligation} ${
            isEmpty(reportingObligationState.data) || isEmpty(reportingObligationState.searchedData)
              ? styles.filteredSelected
              : ''
          }`}>
          <span>{`${resources.messages['selectedObligation']}: `}</span>
          {`${
            !isEmpty(reportingObligationState.oblChoosed.title) && !isEmpty(reportingObligationState.oblChoosed)
              ? reportingObligationState.oblChoosed.title
              : '-'
          }`}
        </span>
      )}
    </div>
  );
};
