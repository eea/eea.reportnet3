import { Fragment, useContext, useEffect, useReducer, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isUndefined from 'lodash/isUndefined';

import styles from './DataflowDashboards.module.css';

import { config } from 'conf';

import { routes } from 'conf/routes';

import { Button } from 'views/_components/Button';
import { DatasetValidationDashboard } from './_components/DatasetValidationDashboard';
import { MainLayout } from 'views/_components/Layout';
import { ReleasedDatasetsDashboard } from './_components/ReleasedDatasetsDashboard';
import { Title } from 'views/_components/Title';
import { Toolbar } from 'views/_components/Toolbar';

import { DataflowService } from 'services/DataflowService';

import { LeftSideBarContext } from 'views/_functions/Contexts/LeftSideBarContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';

export const DataflowDashboards = withRouter(
  ({
    match: {
      params: { dataflowId }
    },
    history
  }) => {
    const leftSideBarContext = useContext(LeftSideBarContext);
    const resourcesContext = useContext(ResourcesContext);

    const [dashboardInitialValues, setDashboardInitialValues] = useState({});
    const [dataflowName, setDataflowName] = useState('');
    const [dataflowType, setDataflowType] = useState('');
    const [dataSchema, setDataSchema] = useState();
    const [isLoading, setIsLoading] = useState(true);

    useBreadCrumbs({
      currentPage: CurrentPage.DATAFLOW_DASHBOARDS,
      dataflowId,
      dataflowType,
      history,
      isLoading
    });

    useEffect(() => {
      leftSideBarContext.removeModels();
      getDataflowDetails();
      onLoadDataSchemas();
    }, []);

    const getDataflowDetails = async () => {
      try {
        const data = await DataflowService.getDetails(dataflowId);
        setDataflowType(data.type);
        setDataflowName(data.name);
        setIsLoading(false);
      } catch (error) {
        console.error('DataflowDashboards - getDataflowDetails.', error);
      }
    };

    const onLoadDataSchemas = async () => {
      try {
        const data = await DataflowService.get(dataflowId);
        setDataSchema(data.designDatasets);
        setDashboardInitialValues(
          data.designDatasets.forEach(schema => (dashboardInitialValues[schema.datasetSchemaId] = true))
        );
      } catch (error) {
        console.error('DataflowDashboards - onLoadDataSchemas.', error);
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
              icon={chartState[schema.datasetSchemaId] ? 'eye' : 'eye-slash'}
              key={schema.datasetSchemaId}
              label={schema.datasetSchemaName}
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
      <MainLayout>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );

    return layout(
      <Fragment>
        <Title
          icon="barChart"
          iconSize="4.5rem"
          subtitle={dataflowName}
          title={resourcesContext.messages['dashboards']}
        />

        <div className={styles.validationChartWrap}>
          <h2 className={styles.dashboardType}>
            {resourcesContext.messages['validationDashboards']}{' '}
            <span
              className={styles.dashboardWarning}
              dangerouslySetInnerHTML={{
                __html: resourcesContext.messages['dashboardWarning']
              }}></span>{' '}
          </h2>
          <Toolbar className={styles.chartToolbar}>
            <div className="p-toolbar-group-left">{onLoadButtons}</div>
          </Toolbar>

          {!Object.values(chartState).includes(true) && (
            <div className={styles.informationText}>{resourcesContext.messages['noDashboardSelected']}</div>
          )}

          {onLoadCharts}
        </div>

        <div className={styles.releasedChartWrap}>
          <h2 className={styles.dashboardType}>{resourcesContext.messages['releaseDashboard']}</h2>
          <ReleasedDatasetsDashboard dataflowId={dataflowId} />
        </div>
      </Fragment>
    );
  }
);
