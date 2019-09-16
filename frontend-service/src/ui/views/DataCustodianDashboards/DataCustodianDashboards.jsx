import React, { useEffect, useContext, useState, useReducer, useRef } from 'react';
import { withRouter } from 'react-router-dom';

import { isEmpty, isArray } from 'lodash';

import styles from './DataCustodianDashboards.module.scss';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Chart } from 'primereact/chart';
import { MainLayout } from 'ui/views/_components/Layout';
import { Menu } from 'primereact/menu';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DataFlowService } from 'core/services/DataFlow';
import { UserContext } from '../_components/_context/UserContext';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const userData = useContext(UserContext);
  const [loading, setLoading] = useState(true);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);

  const [dashboardsData, setDashboardsData] = useState({});
  const [datasetsDashboardData, setDatasetsDashboardData] = useState({});
  const [datasetsDashboardOptions, setDatasetsDashboardOptions] = useState({});
  const [releasedDashboardData, setReleasedDashboardData] = useState({});
  const [releasedDashboardOptions, setReleasedDashboardOptions] = useState({});

  const [dashboardStatusButtonList, setDashboardStatusButtonList] = useState([]);

  let exportMenuRef = useRef();

  const home = {
    icon: config.icons['home'],
    command: () => history.push('/')
  };

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowTask'],
        command: () => history.push('/data-flow-task')
      },
      {
        label: resources.messages.reportingDataFlow,
        command: () => history.push(`/reporting-data-flow/${match.params.dataFlowId}`)
      },
      {
        label: resources.messages.dataCustodianDashboards
      }
    ]);
    setDashboardStatusButtonList([
      {
        label: resources.messages['correct'],
        icon: config.icons.checkCircle,
        command: () => dashboardDispatch('correct')
      },
      {
        label: resources.messages['warning'],
        icon: config.icons.warningCircle,
        command: () => dashboardDispatch('warning')
      },
      {
        label: resources.messages['error'],
        icon: config.icons.errorCircle,
        command: () => dashboardDispatch('error')
      }
    ]);
  }, []);

  const loadDashboards = async () => {
    try {
      setDashboardsData(await DataFlowService.dashboards(match.params.dataFlowId));
    } catch (error) {
      console.error(error.response);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboards();
  }, []);

  let datasetDataObject;

  const tableNames = isArray(dashboardsData.tables) && dashboardsData.tables.map(table => table.tableName);

  const tablePercentages =
    isArray(dashboardsData.tables) && dashboardsData.tables.map(table => table.tableStatisticPercentages);
  const tableOnePercentages = tablePercentages[0];
  const tableTwoPercentages = tablePercentages[1];
  const tableThirdPercentages = tablePercentages[2];
  const tableFourthPercentages = tablePercentages[3];

  const tableValues =
    isArray(dashboardsData.tables) && dashboardsData.tables.map(values => values.tableStatisticValues);
  const tableOneValues = tableValues[0];
  const tableTwoValues = tableValues[1];
  const tableThirdValues = tableValues[2];
  const tableFourthValues = tableValues[3];

  const datasetDataObjectCorrect = {
    labels:
      isArray(dashboardsData.dataSetCountries) &&
      dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
    type: 'bar',
    datasets: [
      {
        data: isArray(tableOnePercentages) && tableOnePercentages[0],
        totalData: isArray(tableOneValues) && tableOneValues[0],
        label: tableNames[0],
        backgroundColor: 'rgba(255, 99, 132, 0.7)',
        borderColor: 'rgba(255, 99, 132, 1)'
      },
      {
        data: isArray(tableTwoPercentages) && tableTwoPercentages[0],
        totalData: isArray(tableTwoValues) && tableTwoValues[0],
        label: tableNames[1],
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderColor: 'rgba(54, 162, 235, 0.7)'
      },
      {
        data: isArray(tableThirdPercentages) && tableThirdPercentages[0],
        totalData: isArray(tableThirdValues) && tableThirdValues[0],
        label: tableNames[2],
        backgroundColor: 'rgba(255, 206, 86, 0.7)',
        borderColor: 'rgba(255, 206, 86, 1)'
      },
      {
        data: isArray(tableFourthPercentages) && tableFourthPercentages[0],
        totalData: isArray(tableFourthValues) && tableFourthValues[0],
        label: tableNames[3],
        backgroundColor: 'rgba(153, 102, 255, 0.5)',
        borderColor: 'rgba(153, 102, 255, 0.7)'
      }
    ]
  };

  const datasetDataObjectWarning = {
    labels:
      isArray(dashboardsData.dataSetCountries) &&
      dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
    type: 'bar',
    datasets: [
      {
        data: isArray(tableOnePercentages) && tableOnePercentages[1],
        totalData: isArray(tableOneValues) && tableOneValues[1],
        label: tableNames[0],
        backgroundColor: 'rgba(255, 99, 132, 0.7)',
        borderColor: 'rgba(255, 99, 132, 1)'
      },
      {
        data: isArray(tableTwoPercentages) && tableTwoPercentages[1],
        totalData: isArray(tableTwoValues) && tableTwoValues[1],
        label: tableNames[1],
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderColor: 'rgba(54, 162, 235, 0.7)'
      },
      {
        data: isArray(tableThirdPercentages) && tableThirdPercentages[1],
        totalData: isArray(tableThirdValues) && tableThirdValues[1],
        label: tableNames[2],
        backgroundColor: 'rgba(255, 206, 86, 0.7)',
        borderColor: 'rgba(255, 206, 86, 1)'
      },
      {
        data: isArray(tableFourthPercentages) && tableFourthPercentages[1],
        totalData: isArray(tableFourthValues) && tableFourthValues[1],
        label: tableNames[3],
        backgroundColor: 'rgba(153, 102, 255, 0.5)',
        borderColor: 'rgba(153, 102, 255, 0.7)'
      }
    ]
  };

  const datasetDataObjectError = {
    labels:
      isArray(dashboardsData.dataSetCountries) &&
      dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
    type: 'bar',
    datasets: [
      {
        data: isArray(tableOnePercentages) && tableOnePercentages[2],
        totalData: isArray(tableOneValues) && tableOneValues[2],
        label: tableNames[0],
        backgroundColor: 'rgba(255, 99, 132, 0.7)',
        borderColor: 'rgba(255, 99, 132, 1)'
      },
      {
        data: isArray(tableTwoPercentages) && tableTwoPercentages[2],
        totalData: isArray(tableTwoValues) && tableTwoValues[2],
        label: tableNames[1],
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderColor: 'rgba(54, 162, 235, 0.7)'
      },
      {
        data: isArray(tableThirdPercentages) && tableThirdPercentages[2],
        totalData: isArray(tableThirdValues) && tableThirdValues[2],
        label: tableNames[2],
        backgroundColor: 'rgba(255, 206, 86, 0.7)',
        borderColor: 'rgba(255, 206, 86, 1)'
      },
      {
        data: isArray(tableFourthPercentages) && tableFourthPercentages[2],
        totalData: isArray(tableFourthValues) && tableFourthValues[2],
        label: tableNames[3],
        backgroundColor: 'rgba(153, 102, 255, 0.5)',
        borderColor: 'rgba(153, 102, 255, 0.7)'
      }
    ]
  };

  const initialState = {
    datasetDataObject: datasetDataObjectCorrect,
    statusText: 'correct'
  };

  const dashboardReducer = (state, action) => {
    switch (action) {
      case 'correct':
        return {
          ...state,
          datasetDataObject: initialState.datasetDataObject,
          statusText: initialState.statusText
        };
      case 'warning':
        return {
          ...state,
          datasetDataObject: (datasetDataObject = datasetDataObjectWarning),
          statusText: 'warning'
        };
      case 'error':
        return {
          ...state,
          datasetDataObject: (datasetDataObject = datasetDataObjectError),
          statusText: 'error'
        };
      default:
        return datasetDataObject;
    }
  };

  datasetDataObject = datasetDataObjectCorrect;

  const [dashboardState, dashboardDispatch] = useReducer(dashboardReducer, initialState);

  function getDatasetsDashboardOptions() {
    const datasetOptionsObject = {
      tooltips: {
        mode: 'index',
        intersect: true
        // callbacks: {
        //   label: (tooltipItems, data) =>
        //     `${isArray(data.dashboardsData) &&
        //       data.dashboardsData > 0 &&
        //       data.dashboardsData[tooltipItems.datasetIndex].totalData[tooltipItems['index']][
        //         tooltipItems.datasetIndex
        //       ]} (${tooltipItems.yLabel}%)`
        // }
      },
      legend: {
        display: true,
        labels: {
          filter: (legendItem, data) => {
            return legendItem.text !== 'error table3';
          }
        }
      },
      responsive: true,
      scales: {
        yAxes: [
          {
            id: 'left-y-axis',
            type: 'linear',
            position: 'left',
            ticks: {
              callback: (value, index, values) => `${value}%`
            }
          }
        ]
      }
    };
    return datasetOptionsObject;
  }
  function getReleasedDashboardData() {
    const releasedDataObject = {
      labels: dashboardsData.dataSetCountries.map(countryData => countryData.countryName),
      datasets: [
        {
          label: 'Released',
          backgroundColor: 'rgba(50, 205, 50, 0.7)',
          borderColor: 'rgba(50, 205, 50, 1)',
          data: dashboardsData.dataSetCountries.map(released => released.isDataSetReleased)
        },
        {
          label: 'Unreleased',
          backgroundColor: 'rgba(143, 188, 143, 0.7)',
          borderColor: 'rgba(143, 188, 143, 1)',
          data: dashboardsData.dataSetCountries.map(released => !released.isDataSetReleased)
        }
      ]
    };
    return releasedDataObject;
  }
  function getReleasedDashboardOptions() {
    const releasedOptionsObject = {
      tooltips: {
        mode: 'index',
        intersect: false
      },
      responsive: true,
      scales: {
        xAxes: [
          {
            stacked: true,
            scaleLabel: {
              display: true,
              labelString: 'Datasets'
            }
          }
        ],
        yAxes: [
          {
            stacked: true,
            /*  scaleLabel: {
              display: true,
              labelString: 'Percentage'
            }, */
            ticks: {
              // Include a % sign in the ticks
              callback: (value, index, values) => `${value}`
            }
          }
        ]
      }
    };
    return releasedOptionsObject;
  }

  const onPageLoad = () => {
    setDatasetsDashboardData(datasetDataObject);
    setDatasetsDashboardOptions(getDatasetsDashboardOptions());
    setReleasedDashboardData(getReleasedDashboardData());
    setReleasedDashboardOptions(getReleasedDashboardOptions());
  };

  useEffect(() => {
    if (!isEmpty(dashboardsData)) {
      onPageLoad();
    }
  }, [dashboardsData]);

  const getDashboardButtonPosition = button => {
    const buttonLeftPosition = document.getElementById('dashboardButton').offsetLeft;
    const buttonTopPosition = button.style.top;

    const dashboardButtonMenu = document.getElementById('dashboardButtonMenu');
    dashboardButtonMenu.style.top = buttonTopPosition;
    dashboardButtonMenu.style.left = `${buttonLeftPosition}px`;
  };

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<Spinner />);
  }

  return layout(
    <>
      <div className="rep-row">
        <h1>{resources.messages['dataFlow']}</h1>
      </div>

      <Toolbar>
        <div className="p-toolbar-group-left">
          <div className={styles.toolbarText}>
            You are currently watching the{' '}
            <span data-status={dashboardState.statusText} className={styles.statusText}>
              {dashboardState.statusText}
            </span>{' '}
            data for each table
          </div>
        </div>
        <div className="p-toolbar-group-right">
          <Button
            className={`p-button-rounded p-button-secondary`}
            disabled={false}
            icon={'dashboard'}
            label={resources.messages['chooseDataStatus']}
            onClick={event => exportMenuRef.current.show(event)}
            id={'dashboardButton'}
            //iconClasses={dashboardState.statusText}
          />
          <Menu
            model={dashboardStatusButtonList}
            popup={true}
            ref={exportMenuRef}
            id="dashboardButtonMenu"
            onShow={e => {
              getDashboardButtonPosition(e.target);
            }}
          />
        </div>
      </Toolbar>
      <div className="rep-row">
        <Chart type="bar" data={datasetDataObject} options={datasetsDashboardOptions} width="100%" height="35%" />
      </div>
      <div className={`rep-row ${styles.chart_released}`}>
        <Chart type="bar" data={releasedDashboardData} options={releasedDashboardOptions} width="100%" height="35%" />
      </div>
    </>
  );
});
