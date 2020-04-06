import React, { Fragment, useContext, useEffect, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ReportingObligations.module.scss';

import { CardsView } from './_components/CardsView';
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
    data: [],
    filteredData: [],
    isLoading: false,
    isTableView: false,
    oblChoosed: {},
    pagination: { first: 0, rows: 10, page: 0 },
    searchedData: []
  });

  useEffect(() => {
    onLoadReportingObligations();
  }, []);

  useEffect(() => {
    if (getObligation) getObligation(reportingObligationState.oblChoosed);
  }, [reportingObligationState.oblChoosed]);

  const onLoadingData = value => reportingObligationDispatch({ type: 'IS_LOADING', payload: { value } });

  const onLoadReportingObligations = async () => {
    onLoadingData(true);
    try {
      const response = await ObligationService.opened();
      reportingObligationDispatch({
        type: 'INITIAL_LOAD',
        payload: {
          data: response,
          filteredData: ReportingObligationUtils.filteredInitialValues(response, oblChecked.id),
          oblChoosed: oblChecked
        }
      });
    } catch (error) {
      notificationContext.add({ type: 'LOAD_OPENED_OBLIGATION_ERROR' });
    } finally {
      onLoadingData(false);
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

  const renderData = () =>
    reportingObligationState.isTableView ? (
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
    );

  if (reportingObligationState.isLoading) return <Spinner style={{ top: 0 }} />;

  return (
    <div className={styles.reportingObligation}>
      <div className={styles.repOblTools}>
        <SearchAll data={reportingObligationState.filteredData} getValues={onLoadSearchedData} />
        <div className={styles.switchDiv}>
          <label className={styles.switchTextInput}>{resources.messages['magazineView']}</label>
          <InputSwitch checked={reportingObligationState.isTableView} onChange={() => onToggleView()} />
          <label className={styles.switchTextInput}>{resources.messages['listView']}</label>
        </div>
      </div>

      {isEmpty(reportingObligationState.data) ? (
        <h3 className={styles.noObligations}>{resources.messages['emptyObligationList']}</h3>
      ) : (
        renderData()
      )}

      <span className={styles.selectedObligation}>
        <span>{`${resources.messages['selectedObligation']}:`}</span>{' '}
        {`${
          !isEmpty(reportingObligationState.oblChoosed.title) && !isEmpty(reportingObligationState.oblChoosed)
            ? reportingObligationState.oblChoosed.title
            : '-'
        }`}
      </span>
    </div>
  );
};
