import React from 'react';
import { Article13 } from './_components/Article13';
import { Article15 } from './_components/Article15';

const Webforms = ({ webformType, dataflowId, datasetId, state }) => {
  switch (webformType) {
    case 'MMR-ART13':
      return <Article13 dataflowId={dataflowId} datasetId={datasetId} state={state} />;

    case 'MMR-ART15':
      return <Article15 dataflowId={dataflowId} datasetId={datasetId} state={state} />;

    default:
      return <></>;
  }
};

export { Webforms };
