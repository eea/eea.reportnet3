import React, { useEffect, useContext } from 'react';

import isNil from 'lodash/isNil';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const useHelpSteps = (leftSideBarContext, dataflowState) => {
  const resources = useContext(ResourcesContext);

  const filterHelpSteps = () => {
    const dataflowSteps = [
      {
        content: <h2>{resources.messages['dataflowHelp']}</h2>,
        locale: { skip: <strong aria-label="skip">{resources.messages['skipHelp']}</strong> },
        placement: 'center',
        target: 'body'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep1']}</h2>,
        target: '.dataflow-new-item-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep2']}</h2>,
        target: '.dataflow-documents-weblinks-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep3']}</h2>,
        target: '.dataflow-schema-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep4']}</h2>,
        target: '.dataflow-dataset-container-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep5']}</h2>,
        target: '.dataflow-dataset-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep6']}</h2>,
        target: '.dataflow-datacollection-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep7']}</h2>,
        target: '.dataflow-dashboards-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep8']}</h2>,
        target: '.dataflow-edit-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep9']}</h2>,
        target: '.dataflow-manage-roles-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep10']}</h2>,
        target: '.dataflow-properties-help-step'
      },
      {
        content: <h2>{resources.messages['dataflowHelpStep11']}</h2>,
        target: '.dataflow-properties-provider-help-step'
      }
    ];

    const loadedClassesSteps = [...dataflowSteps].filter(
      dataflowStep =>
        !isNil(document.getElementsByClassName(dataflowStep.target.substring(1, dataflowStep.target.length))[0]) ||
        dataflowStep.target === 'body'
    );
    return loadedClassesSteps;
  };

  useEffect(() => {
    const steps = filterHelpSteps();
    leftSideBarContext.addHelpSteps('dataflowHelp', steps);
  }, [
    dataflowState.data,
    dataflowState.designDatasetSchemas,
    dataflowState.formHasRepresentatives,
    dataflowState.isCustodian,
    dataflowState.isDataSchemaCorrect,
    dataflowState.status,
    dataflowState.id
  ]);
};

export { useHelpSteps };
