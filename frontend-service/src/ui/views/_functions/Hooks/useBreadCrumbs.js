import { useContext, useEffect } from 'react';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

export const useBreadCrumbs = ({
  currentPage,
  dataflowId,
  dataflowStateData,
  history,
  matchParams,
  metaData,
  representativeId
}) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const resources = useContext(ResourcesContext);

  const getDataCollectionCrumb = () => {
    return { label: resources.messages['dataCollection'], icon: 'dataCollection' };
  };

  const getDataflowCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId }, true)),
      href: getUrl(routes.DATAFLOW, { dataflowId }, true),
      icon: 'clone',
      label: resources.messages['dataflow']
    };
  };

  const getDataflowsCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.DATAFLOWS)),
      href: getUrl(routes.DATAFLOWS),
      icon: 'home',
      label: resources.messages['dataflows']
    };
  };

  const getDataflowDashboardsCrumb = () => {
    return { label: resources.messages['dashboards'], icon: 'barChart' };
  };

  const getDataflowHelpCrumb = () => {
    return { label: resources.messages['dataflowHelp'], icon: 'info' };
  };

  const getDatasetCrumb = () => {
    return { label: resources.messages['dataset'], icon: 'dataset' };
  };

  const getDatasetDesignerCrumb = () => {
    return { label: resources.messages['datasetDesigner'], icon: 'pencilRuler' };
  };

  const getEuDatasetCrumb = () => {
    return { label: resources.messages['euDataset'], icon: 'euDataset' };
  };

  const getHomeCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.DATAFLOWS)),
      href: getUrl(routes.DATAFLOWS),
      label: resources.messages['homeBreadcrumb']
    };
  };

  const getSettingsCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.SETTINGS)),
      href: getUrl(routes.SETTINGS),
      icon: 'user-profile',
      label: resources.messages['userSettingsBreadcrumbs']
    };
  };

  const setBreadCrumbs = () => {
    if (currentPage === CurrentPage.DATA_COLLECTION) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDataCollectionCrumb()]);
    }

    if (currentPage === CurrentPage.DATAFLOW_DASHBOARDS) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDataflowDashboardsCrumb()]);
    }

    if (currentPage === CurrentPage.DATAFLOW_HELP) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDataflowHelpCrumb()]);
    }

    if (currentPage === CurrentPage.DATAFLOWS) {
      breadCrumbContext.add([getHomeCrumb(), { label: resources.messages['dataflows'], icon: 'home' }]);
    }

    if (currentPage === CurrentPage.DATAFLOW) {
      if (!isEmpty(dataflowStateData)) {
        let representatives = dataflowStateData.datasets.map(dataset => {
          return { name: dataset.datasetSchemaName, dataProviderId: dataset.dataProviderId };
        });

        if (representativeId) {
          const currentRepresentative = representatives
            .filter(representative => representative.dataProviderId === parseInt(representativeId))
            .map(representative => representative.name);

          breadCrumbContext.add([
            getHomeCrumb(),
            getDataflowsCrumb(),
            {
              command: () => history.goBack(),
              href: getUrl(routes.DATAFLOW),
              icon: 'clone',
              label: resources.messages['dataflow']
            },
            {
              label: currentRepresentative[0],
              icon: 'clone'
            }
          ]);
        } else {
          breadCrumbContext.add([
            getHomeCrumb(),
            getDataflowsCrumb(),
            {
              icon: 'clone',
              label: resources.messages['dataflow']
            }
          ]);
        }
      }
    }

    if (currentPage === CurrentPage.DATASET_DESIGNER) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDatasetDesignerCrumb()]);
    }

    if (currentPage === CurrentPage.DATASET) {
      if (!isUndefined(metaData.dataset)) {
        const datasetBreadCrumbs = [
          getHomeCrumb(),
          getDataflowsCrumb(),
          {
            className: 'datasetSchema-breadcrumb-back-help-step',
            command: () => {
              history.goBack();
            },
            href: getUrl(routes.DATAFLOW, { dataflowId }, true),
            icon: 'clone',
            label: resources.messages['dataflow']
          }
        ];

        if (breadCrumbContext.model.find(model => model.icon === 'representative')) {
          datasetBreadCrumbs.push({
            command: () =>
              history.push(getUrl(routes.REPRESENTATIVE, { dataflowId, representative: metaData.dataset.name }, true)),
            href: getUrl(routes.REPRESENTATIVE, { dataflowId, representative: metaData.dataset.name }, true),
            icon: 'representative',
            label: !isUndefined(metaData.dataset) ? metaData.dataset.name : resources.messages['representative']
          });
        }

        breadCrumbContext.add([...datasetBreadCrumbs, getDatasetCrumb()]);
      }
    }

    if (currentPage === CurrentPage.EU_DATASET) {
      if (!isUndefined(metaData.dataset)) {
        breadCrumbContext.add(
          getDataflowsCrumb(),
          {
            command: () => history.goBack(),
            href: getUrl(routes.DATAFLOW, { dataflowId }, true),
            icon: 'clone',
            label: resources.messages['dataflow']
          },
          getEuDatasetCrumb()
        );
      }
    }

    if (currentPage === CurrentPage.USER_SETTINGS) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getSettingsCrumb()]);
    }
  };

  useEffect(() => {
    setBreadCrumbs();
  }, [dataflowStateData, matchParams, metaData]);
};
