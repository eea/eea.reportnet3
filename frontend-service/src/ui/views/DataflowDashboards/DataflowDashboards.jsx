import { Fragment, useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { isUndefined } from 'lodash';

import styles from './DataflowDashboards.module.css';

import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { DatasetValidationDashboard } from './_components/DatasetValidationDashboard';
import { MainLayout } from 'ui/views/_components/Layout';
import { ReleasedDatasetsDashboard } from './_components/ReleasedDatasetsDashboard';
import { Title } from 'ui/views/_components/Title';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { DataflowService } from 'core/services/Dataflow';

import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const DataflowDashboards = withRouter(
  ({
    match: {
      params: { dataflowId }
    },
    history
  }) => {
    const leftSideBarContext = useContext(LeftSideBarContext);
    const resources = useContext(ResourcesContext);

    const [dashboardInitialValues, setDashboardInitialValues] = useState({});
    const [dataflowName, setDataflowName] = useState('');
    const [dataSchema, setDataSchema] = useState();

    useBreadCrumbs({ currentPage: CurrentPage.DATAFLOW_DASHBOARDS, dataflowId, history });

    useEffect(() => {
      leftSideBarContext.removeModels();
      try {
        getDataflowName();
        onLoadDataSchemas();
      } catch (error) {
        console.error(error.response);
      }
    }, []);

    const getDataflowName = async () => {
      const { data } = await DataflowService.dataflowDetails(dataflowId);
      setDataflowName(data.name);
    };

    const onLoadDataSchemas = async () => {
      try {
        const { data } = await DataflowService.reporting(dataflowId);
        setDataSchema(data.designDatasets);
        setDashboardInitialValues(
          data.designDatasets.forEach(schema => (dashboardInitialValues[schema.datasetSchemaId] = true))
        );
      } catch (error) {
        if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
          history.push(getUrl(routes.DATAFLOWS));
        }
      }
    };

    const chartReducer = (state, { type, payload }) => {
      switch (type) {
        case 'TOGGLE_SCHEMA_CHART':
          return { ...state, [payload]: !state[payload] };

        default:
          return { ...state };
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

    const onLoadCharts =
      !isUndefined(dataSchema) &&
      dataSchema.map(schema => (
        <DatasetValidationDashboard
          dataflowId={dataflowId}
          datasetSchemaId={schema.datasetSchemaId}
          datasetSchemaName={schema.datasetSchemaName}
          isVisible={chartState[schema.datasetSchemaId]}
          key={schema.datasetSchemaId}
        />
      ));

    const layout = children => (
      <MainLayout history={history}>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );

    return layout(
      <Fragment>
        <Title title={resources.messages['dashboards']} subtitle={dataflowName} icon="barChart" iconSize="4.5rem" />
        <div className={styles.validationChartWrap}>
          <h2 className={styles.dashboardType}>
            {resources.messages['validationDashboards']}{' '}
            <span
              className={styles.dashboardWarning}
              dangerouslySetInnerHTML={{
                __html: resources.messages['dashboardWarning']
              }}></span>{' '}
          </h2>
          <Toolbar className={styles.chartToolbar}>
            <div className="p-toolbar-group-left">{onLoadButtons}</div>
          </Toolbar>

          {!Object.values(chartState).includes(true) && (
            <div className={styles.informationText}>{resources.messages['noDashboardSelected']}</div>
          )}

          {onLoadCharts}
        </div>

        <div className={styles.releasedChartWrap}>
          <h2 className={styles.dashboardType}>{resources.messages['releaseDashboard']}</h2>
          <ReleasedDatasetsDashboard dataflowId={dataflowId} />
        </div>
      </Fragment>
    );
  }
);
