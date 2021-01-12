import React, { Fragment } from 'react';

import { Article13 } from './Article13';
import { Article15 } from './Article15';
import { NationalSystems } from './NationalSystems';

export const Webforms = ({ dataflowId, datasetId, isReporting = false, state, webformType }) => {
  console.log('designerState', state);
  switch (webformType) {
    case 'MMR-ART13':
      return <Article13 dataflowId={dataflowId} datasetId={datasetId} state={state} isReporting={isReporting} />;

    case 'MMR-ART15':
      return <Article15 dataflowId={dataflowId} datasetId={datasetId} state={state} isReporting={isReporting} />;

    case 'NATIONAL-SYSTEMS':
      return <NationalSystems dataflowId={dataflowId} datasetId={datasetId} state={state} isReporting={isReporting} />;

    default:
      return <Fragment />;
  }
};
