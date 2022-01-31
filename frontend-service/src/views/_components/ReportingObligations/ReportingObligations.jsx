import { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ReportingObligations.module.scss';

import { CardsView } from 'views/_components/CardsView';
import { MyFilters } from 'views/_components/MyFilters';
import { InputSwitch } from 'views/_components/InputSwitch';
import { Spinner } from 'views/_components/Spinner';
import { TableView } from './_components/TableView';

import { ObligationService } from 'services/ObligationService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useFilters } from 'views/_functions/Hooks/useFilters';

import { reportingObligationReducer } from './_functions/Reducers/reportingObligationReducer';

import { ReportingObligationUtils } from './_functions/Utils/ReportingObligationUtils';
import { RodUrl } from 'repositories/config/RodUrl';

export const ReportingObligations = ({ obligationChecked, setCheckedObligation }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const { filterBy, filteredData, isFiltered } = useFilters('reportingObligations');

  const [reportingObligationState, reportingObligationDispatch] = useReducer(reportingObligationReducer, {
    countries: [],
    data: [],
    filterBy: { expirationDate: [], countries: {}, issues: {}, organizations: {} },
    filteredSearched: false,
    isFiltered: false,
    isLoading: false,
    isSearched: false,
    issues: [],
    organizations: [],
    pagination: { first: 0, rows: 10, page: 0 },
    searchedData: [],
    selectedObligation: {}
  });

  useEffect(() => {
    onLoadCountries();
    onLoadIssues();
    onLoadOrganizations();
    onLoadReportingObligations();
  }, []);

  useEffect(() => {
    setCheckedObligation(reportingObligationState.selectedObligation);
  }, [reportingObligationState.selectedObligation]);

  useEffect(() => {
    if (!isNil(reportingObligationState.filterBy)) {
      if (
        !isEmpty(reportingObligationState.filterBy.countries) ||
        !isEmpty(reportingObligationState.filterBy.issues) ||
        !isEmpty(reportingObligationState.filterBy.organizations) ||
        reportingObligationState.filterBy.expirationDate?.length !== 0
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

  const getFilteredSeearched = value =>
    reportingObligationDispatch({ type: 'IS_FILTERED_SEARCHED', payload: { value } });

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {reportingObligationState.filteredSearched && reportingObligationState.searchedData.length !== filteredData.length
        ? `${resourcesContext.messages['filtered']}: ${reportingObligationState.searchedData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {reportingObligationState.data.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {reportingObligationState.filteredSearched && reportingObligationState.searchedData.length === filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
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
      console.error('ReportingObligations - onLoadCountries.', error);
      notificationContext.add({ type: 'LOAD_COUNTRIES_ERROR' }, true);
    }
  };

  const onLoadIssues = async () => {
    try {
      const issues = await ObligationService.getIssues();
      reportingObligationDispatch({ type: 'ON_LOAD_ISSUES', payload: { issues } });
    } catch (error) {
      console.error('ReportingObligations - onLoadIssues.', error);
      notificationContext.add({ type: 'LOAD_ISSUES_ERROR' }, true);
    }
  };

  const onLoadOrganizations = async () => {
    try {
      const organizations = await ObligationService.getOrganizations();
      reportingObligationDispatch({ type: 'ON_LOAD_ORGANIZATIONS', payload: { organizations } });
    } catch (error) {
      console.error('ReportingObligations - onLoadOrganizations.', error);
      notificationContext.add({ type: 'LOAD_ORGANIZATIONS_ERROR' }, true);
    }
  };

  const onLoadReportingObligations = async () => {
    isLoading(true);
    try {
      const response = await ObligationService.getOpen(filterBy);
      reportingObligationDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          data: ReportingObligationUtils.initialValues(response, userContext.userProps.dateFormat),
          filteredData: ReportingObligationUtils.filteredInitialValues(
            response,
            obligationChecked.id,
            userContext.userProps.dateFormat
          ),
          selectedObligation: obligationChecked,
          filterBy,
          pagination: { first: 0, rows: 10, page: 0 }
        }
      });
    } catch (error) {
      console.error('ReportingObligations - onLoadReportingObligations.', error);
      notificationContext.add({ type: 'LOAD_OPENED_OBLIGATION_ERROR' }, true);
    } finally {
      isLoading(false);
    }
  };

  const onOpenObligation = id => window.open(`${RodUrl.obligations}${id}`);

  const onSelectObl = rowData => {
    const selectedObligation = { id: rowData.id, title: rowData.title };
    reportingObligationDispatch({ type: 'ON_SELECT_OBL', payload: { selectedObligation } });
  };

  const parseDropdownOptions = (options = []) =>
    options.map(dropdown => ({ label: dropdown.name, value: dropdown.id }));

  const filterOptions = [
    {
      key: 'search',
      label: resourcesContext.messages['search'],
      searchBy: ['title', 'legalInstrument', 'dueDate'],
      type: 'SEARCH'
    },
    {
      nestedOptions: [
        {
          key: 'countries',
          label: resourcesContext.messages['countries'],
          isSortable: true,
          dropdownOptions: parseDropdownOptions(reportingObligationState.countries)
        },
        {
          key: 'issues',
          label: resourcesContext.messages['issues'],
          isSortable: true,
          dropdownOptions: parseDropdownOptions(reportingObligationState.issues)
        },
        {
          key: 'organizations',
          label: resourcesContext.messages['organizations'],
          isSortable: true,
          dropdownOptions: parseDropdownOptions(reportingObligationState.organizations)
        }
      ],
      type: 'DROPDOWN'
    },
    { key: 'expirationDate', label: resourcesContext.messages['expirationDate'], type: 'DATE' }
  ];

  const renderData = () => {
    if (reportingObligationState.isLoading) {
      return <Spinner className={styles.spinner} />;
    }

    if (isEmpty(reportingObligationState.data)) {
      if (isFiltered) {
        <h3 className={styles.noObligations}>{resourcesContext.messages['noObligationsWithSelectedParameters']}</h3>;
      } else {
        <h3 className={styles.noObligations}>{resourcesContext.messages['emptyObligationList']}</h3>;
      }
    }

    return userContext.userProps.listView ? (
      <TableView
        checkedObligation={reportingObligationState.selectedObligation}
        data={reportingObligationState.searchedData}
        onChangePagination={onChangePagination}
        onSelectObl={onSelectObl}
        pagination={reportingObligationState.pagination}
        paginatorRightText={getPaginatorRecordsCount()}
      />
    ) : (
      <CardsView
        checkedCard={reportingObligationState.selectedObligation}
        contentType={'Obligations'}
        data={reportingObligationState.searchedData}
        handleRedirect={onOpenObligation}
        onChangePagination={onChangePagination}
        onSelectCard={onSelectObl}
        pagination={reportingObligationState.pagination}
        paginatorRightText={getPaginatorRecordsCount()}
      />
    );
  };

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
        <div className={styles.switchDiv}>
          <label className={styles.switchTextInput}>{resourcesContext.messages['magazineView']}</label>
          <InputSwitch checked={userContext.userProps.listView} onChange={e => userContext.onToggleTypeView(e.value)} />
          <label className={styles.switchTextInput}>{resourcesContext.messages['listView']}</label>
        </div>
      </div>

      <div className={styles.filters}>
        <MyFilters
          className="reportingObligations"
          data={filteredData}
          onFilter={onLoadReportingObligations}
          options={filterOptions}
          viewType="reportingObligations"
        />
      </div>
      {renderData()}
      {!reportingObligationState.isLoading && (
        <span
          className={`${styles.selectedObligation} ${
            isEmpty(reportingObligationState.data) || isEmpty(reportingObligationState.searchedData)
              ? styles.filteredSelected
              : ''
          }`}>
          <span>{`${resourcesContext.messages['selectedObligation']}: `}</span>
          {`${
            !isEmpty(reportingObligationState.selectedObligation.title) &&
            !isEmpty(reportingObligationState.selectedObligation)
              ? reportingObligationState.selectedObligation.title
              : '-'
          }`}
        </span>
      )}
    </div>
  );
};
