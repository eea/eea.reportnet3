import { useContext, useEffect } from 'react';

import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

export const useBreadCrumbs = (
  history,
  currentPage,
  dataflowId,
  metaData,
  matchParams,
  dataflowStateData,
  representativeId
) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const resources = useContext(ResourcesContext);

  const dataCollectionCrumb = { label: resources.messages['dataCollection'], icon: 'dataCollection' };

  const dataflowCrumb = {
    command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId }, true)),
    href: getUrl(routes.DATAFLOW, { dataflowId }, true),
    icon: 'clone',
    label: resources.messages['dataflow']
  };

  const dataflowsCrumb = {
    command: () => history.push(getUrl(routes.DATAFLOWS)),
    href: getUrl(routes.DATAFLOWS),
    icon: 'home',
    label: resources.messages['dataflows']
  };

  const dataflowDashboardsCrumb = { label: resources.messages['dashboards'], icon: 'barChart' };

  const dataflowHelpCrumb = { label: resources.messages['dataflowHelp'], icon: 'info' };

  const datasetCrumb = { label: resources.messages['dataset'], icon: 'dataset' };

  const datasetDesignerCrumb = { label: resources.messages['datasetDesigner'], icon: 'pencilRuler' };

  const euDatasetCrumb = { label: resources.messages['euDataset'], icon: 'euDataset' };

  const homeCrumb = {
    command: () => history.push(getUrl(routes.DATAFLOWS)),
    href: getUrl(routes.DATAFLOWS),
    label: resources.messages['homeBreadcrumb']
  };

  const settingsCrumb = {
    command: () => history.push(getUrl(routes.SETTINGS)),
    href: getUrl(routes.SETTINGS),
    icon: 'user-profile',
    label: resources.messages['userSettingsBreadcrumbs']
  };

  const setBreadCrumbs = () => {
    if (currentPage === CurrentPage.DATA_COLLECTION) {
      breadCrumbContext.add([homeCrumb, dataflowsCrumb, dataflowCrumb, dataCollectionCrumb]);
    }

    if (currentPage === CurrentPage.DATAFLOW_DASHBOARDS) {
      breadCrumbContext.add([homeCrumb, dataflowsCrumb, dataflowCrumb, dataflowDashboardsCrumb]);
    }

    if (currentPage === CurrentPage.DATAFLOW_HELP) {
      breadCrumbContext.add([homeCrumb, dataflowsCrumb, dataflowCrumb, dataflowHelpCrumb]);
    }

    if (currentPage === CurrentPage.DATAFLOWS) {
      breadCrumbContext.add([homeCrumb, { label: resources.messages['dataflows'], icon: 'home' }]);
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
            homeCrumb,
            dataflowsCrumb,
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
            homeCrumb,
            dataflowsCrumb,
            {
              icon: 'clone',
              label: resources.messages['dataflow']
            }
          ]);
        }
      }
    }

    if (currentPage === CurrentPage.DATASET_DESIGNER) {
      breadCrumbContext.add([homeCrumb, dataflowsCrumb, dataflowCrumb, datasetDesignerCrumb]);
    }

    if (currentPage === CurrentPage.DATASET) {
      if (!isUndefined(metaData.dataset)) {
        const datasetBreadCrumbs = [
          homeCrumb,
          dataflowsCrumb,
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

        breadCrumbContext.add([...datasetBreadCrumbs, datasetCrumb]);
      }
    }

    if (currentPage === CurrentPage.EU_DATASET) {
      if (!isUndefined(metaData.dataset)) {
        breadCrumbContext.add(
          dataflowsCrumb,
          {
            command: () => history.goBack(),
            href: getUrl(routes.DATAFLOW, { dataflowId }, true),
            icon: 'clone',
            label: resources.messages['dataflow']
          },
          euDatasetCrumb
        );
      }
    }

    if (currentPage === CurrentPage.USER_SETTINGS) {
      breadCrumbContext.add([homeCrumb, dataflowsCrumb, settingsCrumb]);
    }
  };

  useEffect(() => {
    setBreadCrumbs();
  }, [dataflowStateData, matchParams, metaData]);
};
