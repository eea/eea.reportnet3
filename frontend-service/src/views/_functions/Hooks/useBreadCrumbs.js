import { useContext, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';

import { routes } from 'conf/routes';

import { BreadCrumbContext } from 'views/_functions/Contexts/BreadCrumbContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const useBreadCrumbs = ({
  countryCode,
  currentPage,
  dataflowId,
  dataflowStateData,
  dataflowType,
  dataProviderId,
  dataProviderName,
  isLoading,
  metaData,
  referenceDataflowId,
  representativeId
}) => {
  const navigate = useNavigate();
  const params = useParams();

  const breadCrumbContext = useContext(BreadCrumbContext);
  const resourcesContext = useContext(ResourcesContext);

  useEffect(() => {
    !isLoading && setBreadCrumbs();
  }, [
    dataflowStateData,
    dataflowType,
    dataProviderId,
    dataProviderName,
    isLoading,
    params,
    metaData,
    representativeId
  ]);

  const getDataCollectionCrumb = () => ({ label: resourcesContext.messages['dataCollection'], icon: 'dataCollection' });

  const getDataflowCrumb = () => ({
    command: () => navigate(getUrl(routes.DATAFLOW, { dataflowId }, true)),
    href: getUrl(routes.DATAFLOW, { dataflowId }, true),
    icon: 'clone',
    label: TextByDataflowTypeUtils.getLabelByDataflowType(resourcesContext.messages, dataflowType, 'breadCrumbs')
  });

  const getReferenceDataflowCrumb = () => ({
    command: () => navigate(getUrl(routes.REFERENCE_DATAFLOW, { referenceDataflowId }, true)),
    href: getUrl(routes.REFERENCE_DATAFLOW, { referenceDataflowId }, true),
    icon: 'clone',
    label: resourcesContext.messages['referenceDataflowCrumbLabel']
  });

  const getDataflowsCrumb = () => ({
    command: () => navigate(getUrl(routes.DATAFLOWS)),
    href: getUrl(routes.DATAFLOWS),
    icon: 'home',
    label: resourcesContext.messages['dataflows']
  });

  const getDataflowDashboardsCrumb = () => ({ label: resourcesContext.messages['dashboards'], icon: 'barChart' });

  const getTechnicalFeedbackCrumb = () => ({ label: resourcesContext.messages['technicalFeedback'], icon: 'comments' });

  const getDataflowHelpCrumb = () => ({ label: resourcesContext.messages['dataflowHelp'], icon: 'info' });

  const getDatasetCrumb = () => ({ label: resourcesContext.messages['dataset'], icon: 'dataset' });

  const getDatasetDesignerCrumb = () => ({ label: resourcesContext.messages['datasetDesigner'], icon: 'pencilRuler' });

  const getReferenceDatasetDesignerCrumb = () => ({
    label: resourcesContext.messages['referenceDatasetDesigner'],
    icon: 'pencilRuler'
  });

  const getReferenceDatasetCrumb = () => ({ label: resourcesContext.messages['referenceDataset'], icon: 'howTo' });

  const getEUDatasetCrumb = () => ({ label: resourcesContext.messages['euDataset'], icon: 'euDataset' });

  const getHomeCrumb = () => ({
    command: () => navigate(getUrl(routes.ACCESS_POINT)),
    href: getUrl(routes.ACCESS_POINT),
    label: resourcesContext.messages['homeBreadcrumb']
  });

  const getRepresentativeCrumb = () => {
    const intRepresentativeId = parseInt(representativeId);
    if (representativeId || intRepresentativeId === 0) {
      let representativeCrumbLabel;

      if (intRepresentativeId === 0) {
        representativeCrumbLabel = resourcesContext.messages['testDatasetBreadcrumbs'];
      } else {
        const representatives = dataflowStateData?.datasets.map(dataset => ({
          name: dataset.datasetSchemaName,
          dataProviderId: dataset.dataProviderId
        }));

        const currentRepresentative = representatives
          .filter(representative => representative.dataProviderId === intRepresentativeId)
          .map(representative => representative.name);

        representativeCrumbLabel = currentRepresentative[0];
      }

      return {
        command: () => navigate(getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true)),
        href: getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true),
        label: representativeCrumbLabel,
        icon: 'representative'
      };
    }

    return {
      command: () =>
        navigate(getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId: dataProviderId }, true)),
      href: getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId }, true),
      icon: 'representative',
      label: dataProviderName
    };
  };

  const getPublicCountriesCrumb = () => ({
    command: () => navigate(getUrl(routes.PUBLIC_COUNTRIES, {}, true)),
    href: getUrl(routes.PUBLIC_COUNTRIES, {}, true),
    label: resourcesContext.messages['publicCountriesBreadcrumbs']
  });

  const getPublicCountryCrumb = () => ({
    command: () => navigate(getUrl(routes.PUBLIC_COUNTY_INFORMATION, { countryCode }, true)),
    href: getUrl(routes.PUBLIC_COUNTRY_INFORMATION, { countryCode }, true),
    label: resourcesContext.messages['publicCountryBreadcrumbs']
  });

  const getPublicDataflowCrumb = () => ({
    command: () => navigate(getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId }, true)),
    href: getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId }, true),
    label: resourcesContext.messages['publicDataflowBreadcrumbs']
  });

  const getPublicDataflowsCrumb = () => ({
    command: () => navigate(getUrl(routes.PUBLIC_DATAFLOWS, {}, true)),
    href: getUrl(routes.PUBLIC_DATAFLOWS, {}, true),
    label: resourcesContext.messages['publicDataflowsBreadcrumbs']
  });

  const getPublicHomeCrumb = () => ({
    command: () => navigate(getUrl(routes.ACCESS_POINT, {}, true)),
    href: getUrl(routes.ACCESS_POINT, {}, true),
    label: resourcesContext.messages['homeBreadcrumb']
  });

  const getSettingsCrumb = () => ({
    command: () => navigate(getUrl(routes.SETTINGS)),
    href: getUrl(routes.SETTINGS),
    icon: 'user-profile',
    label: resourcesContext.messages['userSettingsBreadcrumbs']
  });

  const getTestDatasetsCrumb = () => ({
    command: () =>
      navigate(getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId: dataProviderId }, true)),
    href: getUrl(getUrl(routes.DATAFLOW_REPRESENTATIVE, { dataflowId, representativeId: dataProviderId }, true)),
    icon: 'representative',
    label: resourcesContext.messages['testDatasetBreadcrumbs']
  });

  const setBreadCrumbs = () => {
    switch (currentPage) {
      case CurrentPage.DATA_COLLECTION:
        breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDataCollectionCrumb()]);
        break;

      case CurrentPage.DATAFLOW_DASHBOARDS:
        breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDataflowDashboardsCrumb()]);
        break;

      case CurrentPage.DATAFLOW_FEEDBACK:
        const feedbackBreadCrumbs = [getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb()];

        if (!representativeId) {
          breadCrumbContext.add([...feedbackBreadCrumbs, getTechnicalFeedbackCrumb()]);
        } else if (!isEmpty(dataflowStateData)) {
          breadCrumbContext.add([...feedbackBreadCrumbs, getRepresentativeCrumb(), getTechnicalFeedbackCrumb()]);
        }
        break;

      case CurrentPage.DATAFLOW_HELP:
        breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDataflowHelpCrumb()]);
        break;

      case CurrentPage.DATAFLOWS:
        breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb()]);
        break;

      case CurrentPage.DATAFLOW:
        if (!isEmpty(dataflowStateData)) {
          const breadCrumbs = [getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb()];
          if (representativeId) {
            breadCrumbs.push(getRepresentativeCrumb());
          }
          breadCrumbContext.add([...breadCrumbs]);
        }
        break;

      case CurrentPage.DATASET_DESIGNER:
        breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getDatasetDesignerCrumb()]);
        break;

      case CurrentPage.DATASET:
        const datasetBreadCrumbs = [getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb()];

        if (dataProviderId) {
          datasetBreadCrumbs.push(getRepresentativeCrumb());
        }

        breadCrumbContext.add([...datasetBreadCrumbs, getDatasetCrumb()]);

        break;

      case CurrentPage.EU_DATASET:
        breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb(), getEUDatasetCrumb()]);
        break;

      case CurrentPage.USER_SETTINGS:
        breadCrumbContext.add([getHomeCrumb(), getSettingsCrumb()]);
        break;

      case CurrentPage.PUBLIC_COUNTRIES:
        breadCrumbContext.add([getPublicHomeCrumb(), getPublicCountriesCrumb()]);
        break;

      case CurrentPage.PUBLIC_COUNTRY:
        breadCrumbContext.add([getPublicHomeCrumb(), getPublicCountriesCrumb(), getPublicCountryCrumb()]);
        break;

      case CurrentPage.PUBLIC_DATAFLOW:
        breadCrumbContext.add([getPublicHomeCrumb(), getPublicDataflowsCrumb(), getPublicDataflowCrumb()]);
        break;

      case CurrentPage.PUBLIC_DATAFLOWS:
        breadCrumbContext.add([getPublicHomeCrumb(), getPublicDataflowsCrumb()]);
        break;

      case CurrentPage.PUBLIC_INDEX:
        breadCrumbContext.add([]);
        break;

      case CurrentPage.TEST_DATASETS:
        breadCrumbContext.add([
          getHomeCrumb(),
          getDataflowsCrumb(),
          getDataflowCrumb(),
          getTestDatasetsCrumb(),
          getDatasetCrumb()
        ]);
        break;

      case CurrentPage.REFERENCE_DATAFLOW:
        breadCrumbContext.add([getHomeCrumb(), getDataflowsCrumb(), getReferenceDataflowCrumb()]);
        break;

      case CurrentPage.REFERENCE_DATASET_DESIGNER:
        breadCrumbContext.add([
          getHomeCrumb(),
          getDataflowsCrumb(),
          getReferenceDataflowCrumb(),
          getReferenceDatasetDesignerCrumb()
        ]);
        break;

      case CurrentPage.REFERENCE_DATASET:
        breadCrumbContext.add([
          getHomeCrumb(),
          getDataflowsCrumb(),
          getReferenceDataflowCrumb(),
          getReferenceDatasetCrumb()
        ]);
        break;

      case CurrentPage.DATAFLOW_REFERENCE_DATASET:
        const referenceDatasetBreadCrumbs = [getHomeCrumb(), getDataflowsCrumb(), getDataflowCrumb()];

        if (dataProviderId) {
          referenceDatasetBreadCrumbs.push(getRepresentativeCrumb());
        }

        breadCrumbContext.add([...referenceDatasetBreadCrumbs, getReferenceDatasetCrumb()]);
        break;

      default:
        break;
    }
  };
};
