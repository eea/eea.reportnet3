import { useContext, useEffect } from 'react';

import isEmpty from 'lodash/isEmpty';

import { routes } from 'ui/routes';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { CurrentPage } from 'ui/views/_functions/Utils';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const useBreadCrumbs = (
  history,
  currentPage,
  dataflowId = null,
  matchParams = null,
  dataflowStateData = [],
  representativeId = null
) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const resources = useContext(ResourcesContext);

  const homeCrumb = {
    label: resources.messages['homeBreadcrumb'],
    href: getUrl(routes.DATAFLOWS),
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  const dataflowsCrumb = {
    label: resources.messages['dataflows'],
    icon: 'home',
    href: getUrl(routes.DATAFLOWS),
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  const dataflowCrumb = {
    command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId }, true)),
    href: getUrl(routes.DATAFLOW, { dataflowId }, true),
    icon: 'clone',
    label: resources.messages['dataflow']
  };

  const settingsCrumb = {
    label: resources.messages['userSettingsBreadcrumbs'],
    icon: 'user-profile',
    href: getUrl(routes.SETTINGS),
    command: () => history.push(getUrl(routes.SETTINGS))
  };

  const dataflowHelpCrumb = { label: resources.messages['dataflowHelp'], icon: 'info' };

  const datasetDesignerCrumb = { label: resources.messages['datasetDesigner'], icon: 'pencilRuler' };

  const dataflowDashboardsCrumb = {
    label: resources.messages['dashboards'],
    icon: 'barChart'
  };

  const setBreadCrumbs = () => {
    if (currentPage === CurrentPage.DATAFLOWS) {
      breadCrumbContext.add([
        {
          label: resources.messages['homeBreadcrumb'],
          href: getUrl(routes.DATAFLOWS),
          command: () => history.push(getUrl(routes.DATAFLOWS))
        },
        { label: resources.messages['dataflows'], icon: 'home' }
      ]);
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
              label: resources.messages['dataflow'],
              icon: 'clone',
              href: getUrl(routes.DATAFLOW),
              command: () => history.goBack()
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
              label: resources.messages['dataflow'],
              icon: 'clone'
            }
          ]);
        }
      }
    }
  };

  useEffect(() => {
    setBreadCrumbs();
  }, [matchParams, dataflowStateData]);
};
