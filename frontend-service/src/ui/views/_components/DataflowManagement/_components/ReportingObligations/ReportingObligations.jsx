import React, { useContext, useEffect, useReducer, Fragment } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ReportingObligations.module.scss';

import { InputSwitch } from 'ui/views/_components/InputSwitch';
import { CardsView } from './_components/CardsView';
import { SearchAll } from './_components/SearchAll';
import { Spinner } from 'ui/views/_components/Spinner';
import { TableView } from './_components/TableView';

import { ObligationService } from 'core/services/Obligation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { reportingObligationReducer } from './_functions/Reducers/reportingObligationReducer';

import { ReportingObligationUtils } from './_functions/Utils/ReportingObligationUtils';

export const ReportingObligations = ({ oblChecked, getObligation, refresh }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [reportingObligationState, reportingObligationDispatch] = useReducer(reportingObligationReducer, {
    data: [],
    filteredData: [],
    isLoading: false,
    isTableView: true,
    oblChoosed: {},
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
    const data = await ObligationService.opened();
    reportingObligationDispatch({
      type: 'INITIAL_LOAD',
      payload: {
        data,
        filteredData: ReportingObligationUtils.filteredInitialValues(data, oblChecked.id),
        oblChoosed: oblChecked
      }
    });
    try {
    } catch (error) {
    } finally {
      onLoadingData(false);
    }
  };

  const onLoadSearchedData = data => reportingObligationDispatch({ type: 'SEARCHED_DATA', payload: { data } });

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
        onSelectObl={onSelectObl}
      />
    ) : (
      <CardsView
        data={reportingObligationState.searchedData}
        checkedObligation={reportingObligationState.oblChoosed}
        onSelectObl={onSelectObl}
      />
    );

  if (reportingObligationState.isLoading) return <Spinner style={{ top: 0 }} />;

  return (
    <Fragment>
      <div className={styles.repOblTools}>
        <SearchAll data={reportingObligationState.filteredData} getValues={onLoadSearchedData} />
        {/* <InputSwitch
          checked={reportingObligationState.isTableView}
          onChange={() => onToggleView()}
          style={{ marginRight: '1rem' }}
        /> */}
      </div>
      {isEmpty(reportingObligationState.data) ? <h3>{resources.messages['emptyValidations']}</h3> : renderData()}
    </Fragment>
  );
};
