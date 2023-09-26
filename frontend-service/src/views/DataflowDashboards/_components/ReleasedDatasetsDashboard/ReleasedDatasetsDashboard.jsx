import { Fragment, useContext, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';

import styles from './ReleasedDatasetsDashboard.module.css';

import colors from 'conf/colors.json';

import { getUrl } from 'repositories/_utils/UrlUtils';

import { routes } from 'conf/routes';

import { Chart } from 'primereact/chart';
import { Spinner } from 'views/_components/Spinner';
import { StatusList } from 'views/_components/StatusList';

import { DataflowService } from 'services/DataflowService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useStatusFilter } from 'views/_components/StatusList/_hooks/useStatusFilter';

export const ReleasedDatasetsDashboard = ({ countryDatasets, dataflowId }) => {
  const resourcesContext = useContext(ResourcesContext);

  const navigate = useNavigate();

  const [isLoading, setLoading] = useState(true);
  const [maxValue, setMaxValue] = useState();
  const [releasedDashboardData, setReleasedDashboardData] = useState([]);

  const { statusDispatcher, updatedState } = useStatusFilter(releasedDashboardData);

  const refCountries = useRef();
  refCountries.current = countryDatasets;

  useEffect(() => {
    onLoadDashboard();
  }, []);

  const onLoadDashboard = async () => {
    try {
      const data = await DataflowService.getDatasetsReleasedStatus(dataflowId);
      setReleasedDashboardData(buildReleasedDashboardObject(data));
    } catch (error) {
      console.error('ReleasedDatasetsDashboard - onLoadDashboard.', error);
    } finally {
      setLoading(false);
    }
  };

  const getMaxOfArrays = (releasedNumArr, unReleasedNumArr) => {
    const maxReleased = Math.max.apply(null, releasedNumArr);
    const maxUnReleased = Math.max.apply(null, unReleasedNumArr);
    return Math.max(maxReleased, maxUnReleased);
  };

  const buildReleasedDashboardObject = data => {
    setMaxValue(getMaxOfArrays(data.releasedData, data.unReleasedData));
    return {
      labels: data.labels,
      datasets: [
        { label: resourcesContext.messages['released'], backgroundColor: colors.green400, data: data.releasedData },
        { label: resourcesContext.messages['unreleased'], backgroundColor: colors.gray25, data: data.unReleasedData }
      ]
    };
  };

  const releasedOptionsObject = {
    tooltips: {
      mode: 'point',
      intersect: true
    },
    legend: {
      display: false
    },
    responsive: true,
    datasets: { bar: { maxBarThickness: 100 } },
    scales: {
      xAxes: [
        {
          stacked: true,
          gridLines: { display: false }
        }
      ],
      yAxes: [
        {
          stacked: true,
          ticks: {
            beginAtZero: true,
            max: maxValue,
            callback: value => {
              if (Number.isInteger(value)) {
                return value;
              }
            },
            stepSize: 10
          },
          gridLines: { display: false }
        }
      ]
    },
    onClick: (event, item) => {
      if (refCountries.current.length !== 0) {
        const selectedCountry = refCountries.current.find(
          country => updatedState.dashboardData.labels[item[0]._index] === country.name
        );
        const providerId = selectedCountry.providerId;
        navigate(getUrl(routes.DATAFLOW_PROVIDER, { dataflowId, providerId }, true));
      }
    }
  };

  if (isLoading) {
    return <Spinner className={styles.positioning} />;
  }

  if (!isEmpty(releasedDashboardData.datasets) && !isEmpty(releasedDashboardData.labels)) {
    if (releasedDashboardData.datasets.length > 0 && releasedDashboardData.labels.length > 0) {
      return (
        <div className={`${styles.chart_released}`}>
          {!isEmpty(updatedState.dashboardData) ? (
            <Fragment>
              <StatusList
                filterDispatch={statusDispatcher}
                filteredStatusTypes={updatedState.filterStatus}
                statusTypes={['RELEASED', 'UNRELEASED']}
              />

              <Chart
                data={updatedState.dashboardData}
                height="25%"
                options={releasedOptionsObject}
                type="bar"
                width="100%"
              />
            </Fragment>
          ) : (
            <Chart height="25%" options={releasedOptionsObject} type="bar" width="100%" />
          )}
        </div>
      );
    }
  }

  return (
    <div>
      <h2>{resourcesContext.messages['emptyReleasedDashboard']}</h2>
    </div>
  );
};
