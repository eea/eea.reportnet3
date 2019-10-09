import React, { useEffect, useContext, useState, useReducer } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isUndefined } from 'lodash';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Chart } from 'primereact/chart';
import { FilterList } from './_components/FilterList/FilterList';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';
import { GlobalValidationDashboard } from 'ui/views/_components/GlobalValidationDashboard/';
import { getUrl } from 'core/infrastructure/api/getUrl';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowMetadata, setDataflowMetadata] = useState({});
  const [isLoadingReleasedData, setIsLoadingReleasedData] = useState(true);
  const [releasedDashboardData, setReleasedDashboardData] = useState([]);

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages.dataflow,
        command: () => history.push(`/dataflow/${match.params.dataflowId}`)
      },
      {
        label: resources.messages.dataCustodianDashboards
      }
    ]);
  }, []);

  useEffect(() => {
    try {
      loadDataflowMetadata();
      loadDashboards();
    } catch (error) {
      console.error(error.response);
    }
  }, []);

  const loadDataflowMetadata = async () => {
    const dataflowMetadata = await DataflowService.metadata(match.params.dataflowId);
    setDataflowMetadata(dataflowMetadata);
  };

  function buildDatasetDashboardObject(datasetsDashboardsData) {
    let datasets = [];
    if (!isUndefined(datasetsDashboardsData.tables)) {
      datasets = datasetsDashboardsData.tables
        .map(table => [
          {
            label: `CORRECT`,
            tableName: table.tableName,
            tableId: table.tableId,
            backgroundColor: 'rgba(153, 204, 51, 1)',
            data: table.tableStatisticPercentages[0],
            totalData: table.tableStatisticValues[0],
            stack: table.tableName
          },
          {
            label: `WARNINGS`,
            tableName: table.tableName,
            tableId: table.tableId,
            backgroundColor: 'rgba(255, 204, 0, 1)',
            data: table.tableStatisticPercentages[1],
            totalData: table.tableStatisticValues[1],
            stack: table.tableName
          },
          {
            label: `ERRORS`,
            tableName: table.tableName,
            tableId: table.tableId,
            backgroundColor: 'rgba(204, 51, 0, 1)',
            data: table.tableStatisticPercentages[2],
            totalData: table.tableStatisticValues[2],
            stack: table.tableName
          }
        ])
        .flat();
    }
    const labels = datasetsDashboardsData.datasetReporters.map(reporterData => reporterData.reporterName);
    const datasetDataObject = {
      labels: labels,
      datasets: datasets
    };
    return datasetDataObject;
  }

  function buildReleasedDashboardObject(releasedData) {
    return {
      labels: releasedData.map(dataset => dataset.dataSetName),
      datasets: [
        {
          label: resources.messages['released'],
          backgroundColor: 'rgba(51, 153, 0, 1)',
          data: releasedData.map(dataset => dataset.isReleased)
        },
        {
          label: resources.messages['unreleased'],
          backgroundColor: 'rgba(208, 208, 206, 1)',
          data: releasedData.map(dataset => !dataset.isReleased)
        }
      ]
    };
  }

  const loadDashboards = async () => {
    const releasedData = await DataflowService.datasetsReleasedStatus(match.params.dataflowId);
    setReleasedDashboardData(buildReleasedDashboardObject(releasedData));
    setIsLoadingReleasedData(false);
  };

  const releasedOptionsObject = {
    tooltips: {
      enabled: false
    },
    responsive: true,
    scales: {
      xAxes: [
        {
          stacked: true,
          maxBarThickness: 100
        }
      ],
      yAxes: [
        {
          stacked: true,
          display: false
        }
      ]
    }
  };

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  const releasedDashboard = () => {
    if (!isEmpty(releasedDashboardData.datasets) && isEmpty(!releasedDashboardData.labels)) {
      if (releasedDashboardData.datasets.length > 0 && releasedDashboardData.labels.length > 0) {
        return (
          <div className={`rep-row ${styles.chart_released}`}>
            <Chart type="bar" data={releasedDashboardData} options={releasedOptionsObject} width="100%" height="25%" />
          </div>
        );
      }
    }
    return (
      <div>
        <h2>{resources.messages['emptyReleasedDashboard']}</h2>
      </div>
    );
  };

  return layout(
    <>
      <div className="rep-row">
        <h1>
          {resources.messages['dataflow']}: {dataflowMetadata.name}
        </h1>
      </div>
      {/* <div> {isLoadingValidationData ? <Spinner className={styles.positioning} /> : errorsDashboard()}</div> */}
      <div>
        <GlobalValidationDashboard dataflowId={match.params.dataflowId}></GlobalValidationDashboard>
      </div>
      <div> {isLoadingReleasedData ? <Spinner className={styles.positioning} /> : releasedDashboard()}</div>
    </>
  );
});

function cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabelsArray) {
  return tablesData.map(table => ({
    ...table,
    data: table.data.filter((d, i) => !labelsPositionsInFilteredLabelsArray.includes(i)),
    totalData: table.totalData.filter((td, i) => !labelsPositionsInFilteredLabelsArray.includes(i))
  }));
}

function getLabelIndex(originalData, label) {
  return originalData.labels.indexOf(label);
}

function showArrayItem(array, item) {
  return !array.includes(item);
}
