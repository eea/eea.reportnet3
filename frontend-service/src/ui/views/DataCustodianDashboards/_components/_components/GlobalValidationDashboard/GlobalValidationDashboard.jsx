import React, { useEffect, useReducer } from 'react';

import { isEmpty } from 'lodash';

import { Chart } from 'primereact/chart';
import { FilterList } from './_components/FilterList/FilterList';

const GlobalValidationDashboard = data => {
  const [filterState, filterDispatch] = useReducer(filterReducer, initialFiltersState);
  const [isLoading, setLoading] = useState(true);

  useEffect(() => {
    onLoadDashboard();
  }, []);

  const onLoadDashboard = async () => {
    try {
      const datasetsValidationStatistics = await DataflowService.datasetsValidationStatistics(match.params.dataflowId);
      filterDispatch({ type: 'INIT_DATA', payload: buildDatasetDashboardObject(datasetsValidationStatistics) });
    } catch (error) {
      onErrorLoadingDashboard(error);
    } finally {
      setLoading(false);
    }
  };

  const onErrorLoadingDashboard = error => {
    console.error('Dashboard error: ', error);
    const errorResponse = error.response;
    console.error('Dashboard errorResponse: ', errorResponse);
    if (!isUndefined(errorResponse) && (errorResponse.status === 401 || errorResponse.status === 403)) {
      history.push(getUrl(routes.DATAFLOW, { dataflowId }));
    }
  };

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  if (!isEmpty(data)) {
    return (
      <div className="rep-row">
        <FilterList originalData={filterState.originalData} filterDispatch={filterDispatch}></FilterList>
        <Chart type="bar" data={filterState.data} options={datasetOptionsObject} width="100%" height="30%" />
      </div>
    );
  } else {
    return (
      <div>
        <h2>{resources.messages['emptyErrorsDashboard']}</h2>
      </div>
    );
  }
};

export { GlobalValidationDashboard };
