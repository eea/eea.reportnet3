import { Fragment, useContext, useEffect, useReducer } from 'react';
import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';

import styles from './ReportingObligations.module.scss';

import { CardsView } from 'views/_components/CardsView';
import { InputSwitch } from 'views/_components/InputSwitch';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';
import { TableView } from './_components/TableView';

import { ObligationService } from 'services/ObligationService';

import { filteredDataStore } from '../Filters/_functions/Stores/filterStore';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';

import { reportingObligationReducer } from './_functions/Reducers/reportingObligationReducer';

import { ReportingObligationUtils } from './_functions/Utils/ReportingObligationUtils';
import { RodUrl } from 'repositories/config/RodUrl';

export const ReportingObligations = ({ obligationChecked, setCheckedObligation }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const { getFilterBy, isFiltered, setData } = useApplyFilters('reportingObligations');

  const data = useRecoilValue(filteredDataStore('reportingObligations'));

  const [reportingObligationState, reportingObligationDispatch] = useReducer(reportingObligationReducer, {
    countries: [],
    data: [],
    issues: [],
    isLoading: true,
    organizations: [],
    pagination: { first: 0, rows: 10, page: 0 },
    selectedObligation: obligationChecked
  });

  const { countries, issues, isLoading, organizations, pagination, selectedObligation } = reportingObligationState;

  useEffect(() => {
    onLoadCountries();
    onLoadIssues();
    onLoadOrganizations();
    onLoadReportingObligations();
  }, []);

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
    setLoading(true);

    try {
      const filterBy = await getFilterBy();
      const response = await ObligationService.getOpen(filterBy);
      const data = ReportingObligationUtils.initialValues(response, userContext.userProps.dateFormat);
      console.log('data in :>> ', data);

      reportingObligationDispatch({ type: 'ON_LOAD_DATA', payload: { data } });
      setData(data);
    } catch (error) {
      console.error('ReportingObligations - onLoadReportingObligations.', error);
      notificationContext.add({ type: 'LOAD_OPENED_OBLIGATION_ERROR' }, true);
    } finally {
      setLoading(false);
    }
  };

  const onPaginate = pagination => reportingObligationDispatch({ type: 'ON_PAGINATE', payload: { pagination } });

  const onOpenObligation = id => window.open(`${RodUrl.obligations}${id}`);

  const onSelectObligation = rowData => {
    const selectedObligation = { id: rowData.id, title: rowData.title };

    setCheckedObligation(selectedObligation);
    reportingObligationDispatch({ type: 'ON_SELECT_OBLIGATION', payload: { selectedObligation } });
  };

  const parseDropdownOptions = (options = []) => options.map(({ name, id }) => ({ label: name, value: id }));

  const setLoading = status => reportingObligationDispatch({ type: 'SET_LOADING', payload: { status } });

  console.log('data :>> ', data);

  const renderData = () => {
    if (isLoading) {
      return <Spinner className={styles.spinner} />;
    }

    if (isEmpty(data)) {
      if (isFiltered) {
        <h3 className={styles.noObligations}>{resourcesContext.messages['noObligationsWithSelectedParameters']}</h3>;
      } else {
        <h3 className={styles.noObligations}>{resourcesContext.messages['emptyObligationList']}</h3>;
      }
    }

    const renderView = () => {
      if (userContext.userProps.listView) {
        return (
          <TableView
            checkedObligation={selectedObligation}
            data={data}
            onChangePagination={onPaginate}
            onSelectObl={onSelectObligation}
            pagination={pagination}
            paginatorRightText={renderPaginationCount()}
          />
        );
      } else {
        return (
          <CardsView
            checkedCard={selectedObligation}
            contentType="Obligations"
            data={data}
            handleRedirect={onOpenObligation}
            onChangePagination={onPaginate}
            onSelectCard={onSelectObligation}
            pagination={pagination}
            paginatorRightText={renderPaginationCount()}
          />
        );
      }
    };

    return (
      <Fragment>
        {renderView()}
        <span className={`${styles.selectedObligation} ${isEmpty(data) ? styles.selected : ''}`}>
          <span>{`${resourcesContext.messages['selectedObligation']}: `}</span>
          {selectedObligation.title || '-'}
        </span>
      </Fragment>
    );
  };

  const renderPaginationCount = () => (
    <Fragment>
      {resourcesContext.messages['totalRecords']} {reportingObligationState.data.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {isFiltered ? ` (${resourcesContext.messages['filtered'].toLowerCase()})` : ''}
    </Fragment>
  );

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
          dropdownOptions: parseDropdownOptions(countries)
        },
        {
          key: 'issues',
          label: resourcesContext.messages['issues'],
          dropdownOptions: parseDropdownOptions(issues)
        },
        {
          key: 'organizations',
          label: resourcesContext.messages['organizations'],
          dropdownOptions: parseDropdownOptions(organizations)
        }
      ],
      type: 'DROPDOWN'
    },
    { key: 'expirationDate', label: resourcesContext.messages['expirationDate'], type: 'DATE' }
  ];

  return (
    <div className={styles.reportingObligation}>
      <div className={styles.switch}>
        <label className={styles.text}>{resourcesContext.messages['magazineView']}</label>
        <InputSwitch checked={userContext.userProps.listView} onChange={e => userContext.onToggleTypeView(e.value)} />
        <label className={styles.text}>{resourcesContext.messages['listView']}</label>
      </div>

      <Filters
        className="reportingObligations"
        onFilter={onLoadReportingObligations}
        onReset={onLoadReportingObligations}
        options={filterOptions}
        recoilId="reportingObligations"
      />
      {renderData()}
    </div>
  );
};
