import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './DataflowDashboards.module.css';

import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DataflowService } from 'core/services/Dataflow';
import { ReleasedDatasetsDashboard } from './_components/ReleasedDatasetsDashboard';
import { DatasetValidationDashboard } from './_components/DatasetValidationDashboard';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const DataflowDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dashboardInitialValues, setDashboardInitialValues] = useState({});
  const [dataflowName, setDataflowName] = useState('');
  const [dataSchema, setDataSchema] = useState();

  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        icon: 'home',
        href: routes.DATAFLOWS,
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        icon: 'archive',
        href: getUrl(
          routes.DATAFLOW,
          {
            dataflowId: match.params.dataflowId
          },
          true
        ),
        command: () =>
          history.push(
            getUrl(
              routes.DATAFLOW,
              {
                dataflowId: match.params.dataflowId
              },
              true
            )
          )
      },
      {
        label: resources.messages.dashboards,
        icon: 'barChart'
      }
    ]);
  }, []);

  useEffect(() => {
    try {
      getDataflowName();
      onLoadDataSchemas();
    } catch (error) {
      console.error(error.response);
    }
  }, []);

  const getDataflowName = async () => {
    const dataflowData = await DataflowService.dataflowDetails(match.params.dataflowId);
    setDataflowName(dataflowData.name);
  };

  const onLoadDataSchemas = async () => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      setDataSchema(dataflow.datasets);
      setDashboardInitialValues(
        dataflow.designDatasets.forEach(schema => {
          dashboardInitialValues[schema.datasetSchemaId] = true;
        })
      );
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS));
      }
    } finally {
    }
  };

  const chartReducer = (state, { type, payload }) => {
    switch (type) {
      case 'TOGGLE_SCHEMA_CHART':
        return {
          ...state,
          [payload]: !state[payload]
        };

      default:
        return {
          ...state
        };
    }
  };

  const [chartState, chartDispatch] = useReducer(chartReducer, dashboardInitialValues);

  const onLoadButtons = !isUndefined(dataSchema)
    ? dataSchema.map(schema => {
        return (
          <Button
            className={`p-button-rounded ${
              chartState[schema.datasetSchemaId] ? 'p-button-primary' : 'p-button-secondary'
            } ${styles.dashboardsButton}`}
            key={schema.datasetSchemaId}
            label={schema.datasetSchemaName}
            icon={chartState[schema.datasetSchemaId] ? 'eye' : 'eye-slash'}
            onClick={() => chartDispatch({ type: 'TOGGLE_SCHEMA_CHART', payload: schema.datasetSchemaId })}
          />
        );
      })
    : null;

  const onLoadCharts = !isUndefined(dataSchema)
    ? dataSchema.map(schema => {
        return (
          <DatasetValidationDashboard
            key={schema.datasetSchemaId}
            datasetSchemaId={schema.datasetSchemaId}
            isVisible={chartState[schema.datasetSchemaId]}
            datasetSchemaName={schema.datasetSchemaName}
          />
        );
      })
    : null;

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  return layout(
    <>
      <Title title={resources.messages['dashboards']} subtitle={dataflowName} icon="barChart" iconSize="4.5rem" />
      <div className={styles.validationChartWrap}>
        <h2 className={styles.dashboardType}>{resources.messages['validationDashboards']}</h2>
        <Toolbar className={styles.chartToolbar}>
          <div className="p-toolbar-group-left">{onLoadButtons}</div>
        </Toolbar>
        {Object.values(chartState).includes(true) ? (
          <></>
        ) : (
          <div className={styles.informationText}>{resources.messages['noDashboardSelected']}</div>
        )}
        {onLoadCharts}
      </div>
      <div className={styles.releasedChartWrap}>
        <h2 className={styles.dashboardType}>{resources.messages['releaseDashboard']}</h2>
        <ReleasedDatasetsDashboard dataflowId={match.params.dataflowId} />
      </div>
    </>
  );
});
