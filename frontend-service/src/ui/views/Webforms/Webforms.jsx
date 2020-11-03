import React, { Fragment } from 'react';

import { Article13 } from './Article13';
import { Article15 } from './Article15';

export const Webforms = ({ dataflowId, datasetId, state, webformType }) => {
  switch (webformType) {
    case 'MMR-ART13':
      return <Article13 dataflowId={dataflowId} datasetId={datasetId} state={state} />;

    case 'MMR-ART15':
      return <Article15 dataflowId={dataflowId} datasetId={datasetId} state={state} />;

    default:
      return <Fragment />;
  }
};
