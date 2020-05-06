import { useEffect } from 'react';

const useHelpSteps = (useFilterHelpSteps, leftSideBarContext, dataflowState) => {
  const steps = useFilterHelpSteps();
  useEffect(() => {
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
