import { useContext, useEffect } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

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

  const getRepresentativeCrumb = () => {
    if (representativeId) {
      const representatives = dataflowStateData.datasets.map(dataset => {
        return { name: dataset.datasetSchemaName, dataProviderId: dataset.dataProviderId };
      });

      const currentRepresentative = representatives
        .filter(representative => representative.dataProviderId === parseInt(representativeId))
        .map(representative => representative.name);

      return {
        command: () => history.push(getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true)),
        href: getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true),
        label: currentRepresentative[0],
        icon: 'clone'
      };
    }

    representativeId = parseInt(breadCrumbContext.prevModel[3].href.split('/').slice(-1)[0]);
    return {
      command: () => history.push(getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true)),
      href: getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true),
      icon: 'representative',
      label: breadCrumbContext.prevModel[3].label
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
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb()]);
    }

    if (currentPage === CurrentPage.DATAFLOW) {
      if (!isEmpty(dataflowStateData)) {
        const breadCrumbs = [getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb()];
        if (representativeId) {
          breadCrumbs.push(getRepresentativeCrumb());
        }
        breadCrumbContext.add([...breadCrumbs]);
      }
    }

    if (currentPage === CurrentPage.DATASET_DESIGNER) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDatasetDesignerCrumb()]);
    }

    if (currentPage === CurrentPage.DATASET) {
      const datasetBreadCrumbs = [getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb()];

      if (breadCrumbContext.prevModel.length === 4 && !isNil(breadCrumbContext.prevModel[3].href)) {
        datasetBreadCrumbs.push(getRepresentativeCrumb());
      }

      breadCrumbContext.add([...datasetBreadCrumbs, getDatasetCrumb()]);
    }

    if (currentPage === CurrentPage.EU_DATASET) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getEuDatasetCrumb()]);
    }

    if (currentPage === CurrentPage.USER_SETTINGS) {
      breadCrumbContext.add([getHomeCrumb(), getSettingsCrumb()]);
    }
  };

  useEffect(() => {
    setBreadCrumbs();
  }, [dataflowStateData, matchParams, metaData]);
};
