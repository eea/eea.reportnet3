import { useContext, useLayoutEffect } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { BreadCrumbContext } from 'views/_functions/Contexts/BreadCrumbContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { routes } from 'conf/routes';

export const useBreadCrumbs = ({
  countryCode,
  currentPage,
  dataflowId,
  dataflowStateData,
  history,
  isBusinessDataflow = false,
  isLoading,
  matchParams,
  metaData,
  representativeId,
  referenceDataflowId
}) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const resources = useContext(ResourcesContext);

  useLayoutEffect(() => {
    !isLoading && setBreadCrumbs();
  }, [dataflowStateData, matchParams, metaData, isBusinessDataflow, isLoading]);

  const getDataCollectionCrumb = () => {
    return { label: resources.messages['dataCollection'], icon: 'dataCollection' };
  };

  const getDataflowCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId }, true)),
      href: getUrl(routes.DATAFLOW, { dataflowId }, true),
      icon: 'clone',
      label: isBusinessDataflow ? resources.messages['businessDataflowCrumbLabel'] : resources.messages['dataflow']
    };
  };

  const getReferenceDataflowCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.REFERENCE_DATAFLOW, { referenceDataflowId }, true)),
      href: getUrl(routes.REFERENCE_DATAFLOW, { referenceDataflowId }, true),
      icon: 'clone',
      label: resources.messages['referenceDataflowCrumbLabel']
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

  const getTechnicalFeedbackCrumb = () => {
    return { label: resources.messages['technicalFeedback'], icon: 'comments' };
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

  const getReferenceDatasetDesignerCrumb = () => ({
    label: resources.messages['referenceDatasetDesigner'],
    icon: 'pencilRuler'
  });

  const getReferenceDatasetCrumb = () => ({
    label: resources.messages['referenceDataset'],
    icon: 'howTo'
  });

  const getEUDatasetCrumb = () => {
    return { label: resources.messages['euDataset'], icon: 'euDataset' };
  };

  const getHomeCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.ACCESS_POINT)),
      href: getUrl(routes.ACCESS_POINT),
      label: resources.messages['homeBreadcrumb']
    };
  };

  const getRepresentativeCrumb = () => {
    const intRepresentativeId = parseInt(representativeId);
    if (representativeId || intRepresentativeId === 0) {
      let representativeCrumbLabel;

      if (intRepresentativeId === 0) {
        representativeCrumbLabel = resources.messages['testDatasetBreadcrumbs'];
      } else {
        const representatives = dataflowStateData.datasets.map(dataset => {
          return { name: dataset.datasetSchemaName, dataProviderId: dataset.dataProviderId };
        });

        const currentRepresentative = representatives
          .filter(representative => representative.dataProviderId === intRepresentativeId)
          .map(representative => representative.name);

        representativeCrumbLabel = currentRepresentative[0];
      }

      return {
        command: () => history.push(getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true)),
        href: getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true),
        label: representativeCrumbLabel,
        icon: 'representative'
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
  const getPublicCountriesCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.PUBLIC_COUNTRIES, {}, true)),
      href: getUrl(routes.PUBLIC_COUNTRIES, {}, true),
      label: resources.messages['publicCountriesBreadcrumbs']
    };
  };
  const getPublicCountryCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.PUBLIC_COUNTY_INFORMATION, { countryCode }, true)),
      href: getUrl(routes.PUBLIC_COUNTRY_INFORMATION, { countryCode }, true),
      label: resources.messages['publicCountryBreadcrumbs']
    };
  };
  const getPublicDataflowCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId }, true)),
      href: getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId }, true),
      label: resources.messages['publicDataflowBreadcrumbs']
    };
  };
  const getPublicDataflowsCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.PUBLIC_DATAFLOWS, {}, true)),
      href: getUrl(routes.PUBLIC_DATAFLOWS, {}, true),
      label: resources.messages['publicDataflowsBreadcrumbs']
    };
  };
  const getPublicHomeCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.ACCESS_POINT, {}, true)),
      href: getUrl(routes.ACCESS_POINT, {}, true),
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

  const getTestDatasetsCrumb = () => {
    return {
      command: () => history.push(getUrl(routes.DATAFLOW, { dataflowId }, true)),
      href: getUrl(getUrl(routes.DATAFLOW, { dataflowId }, true)),
      icon: 'dataset',
      label: resources.messages['testDatasetBreadcrumbs']
    };
  };

  const setBreadCrumbs = () => {
    if (currentPage === CurrentPage.DATA_COLLECTION) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDataCollectionCrumb()]);
    }

    if (currentPage === CurrentPage.DATAFLOW_DASHBOARDS) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDataflowDashboardsCrumb()]);
    }

    if (currentPage === CurrentPage.DATAFLOW_FEEDBACK) {
      const datasetBreadCrumbs = [getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb()];

      if (breadCrumbContext.prevModel.length === 4 && !isNil(breadCrumbContext.prevModel[3].href)) {
        datasetBreadCrumbs.push(getRepresentativeCrumb());
      }

      breadCrumbContext.add([...datasetBreadCrumbs, getTechnicalFeedbackCrumb()]);
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
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getEUDatasetCrumb()]);
    }

    if (currentPage === CurrentPage.USER_SETTINGS) {
      breadCrumbContext.add([getHomeCrumb(), getSettingsCrumb()]);
    }

    if (currentPage === CurrentPage.PUBLIC_COUNTRIES) {
      breadCrumbContext.add([getPublicHomeCrumb(), getPublicCountriesCrumb()]);
    }

    if (currentPage === CurrentPage.PUBLIC_COUNTRY) {
      breadCrumbContext.add([getPublicHomeCrumb(), getPublicCountriesCrumb(), getPublicCountryCrumb()]);
    }

    if (currentPage === CurrentPage.PUBLIC_DATAFLOW) {
      breadCrumbContext.add([getPublicHomeCrumb(), getPublicDataflowsCrumb(), getPublicDataflowCrumb()]);
    }

    if (currentPage === CurrentPage.PUBLIC_DATAFLOWS) {
      breadCrumbContext.add([getPublicHomeCrumb(), getPublicDataflowsCrumb()]);
    }

    if (currentPage === CurrentPage.PUBLIC_INDEX) {
      breadCrumbContext.add([]);
    }

    if (currentPage === CurrentPage.TEST_DATASETS) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getTestDatasetsCrumb()]);
    }

    if (currentPage === CurrentPage.REFERENCE_DATAFLOW) {
      breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getReferenceDataflowCrumb()]);
    }

    if (currentPage === CurrentPage.REFERENCE_DATASET_DESIGNER) {
      breadCrumbContext.add([
        getHomeCrumb(),
        getDataflowsCrumb(),
        getReferenceDataflowCrumb(),
        getReferenceDatasetDesignerCrumb()
      ]);
    }

    if (currentPage === CurrentPage.REFERENCE_DATASET) {
      breadCrumbContext.add([
        getHomeCrumb(),
        getDataflowsCrumb(),
        getReferenceDataflowCrumb(),
        getReferenceDatasetCrumb()
      ]);
    }
  };
};
