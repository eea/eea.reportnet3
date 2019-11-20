import React, { useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined, isEmpty } from 'lodash';

import styles from './DataCustodianDashboards.module.css';

import { config } from 'conf';
import { routes } from 'ui/routes';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DataflowService } from 'core/services/DataFlow';
import { GlobalReleasedDashboard } from 'ui/views/_components/GlobalReleasedDashboard/';
import { GlobalValidationDashboard } from 'ui/views/_components/GlobalValidationDashboard/';
import { getUrl } from 'core/infrastructure/api/getUrl';

export const DataCustodianDashboards = withRouter(({ match, history }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataflowName, setDataflowName] = useState('');
  const [dataSchema, setDataSchema] = useState();
  const [dashboardInitialValues, setDashboardInitialValues] = useState({});

  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages.dataflow,
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
        label: resources.messages.dashboards
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
      setDataSchema(dataflow.designDatasets);
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
      case 'TOOGLE_SCHEMA_CHART':
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
            className={`p-button-rounded p-button-secondary ${styles.dashboardsButton}`}
            iconClasses={chartState[schema.datasetSchemaId] ? styles.show : styles.hide}
            key={schema.datasetSchemaId}
            label={schema.datasetSchemaName}
            icon={chartState[schema.datasetSchemaId] ? 'eye-slash' : 'eye'}
            onClick={() => chartDispatch({ type: 'TOOGLE_SCHEMA_CHART', payload: schema.datasetSchemaId })}
          />
        );
      })
    : null;

  const onLoadCharts = !isUndefined(dataSchema)
    ? dataSchema.map(schema => {
        return (
          <GlobalValidationDashboard
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
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  return layout(
    <>
      <Title title={`${resources.messages['dataflow']}: ${dataflowName}`} icon="barChart" />
      <div className={styles.validationChartWrap}>
        <h2>{resources.messages['validationDashboards']}</h2>
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
        <h2>{resources.messages['releaseDashboard']}</h2>
        <GlobalReleasedDashboard dataflowId={match.params.dataflowId} />
      </div>
    </>
  );
});
