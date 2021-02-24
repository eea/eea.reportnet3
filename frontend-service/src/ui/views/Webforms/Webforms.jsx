import React, { Fragment } from 'react';

import { Article13 } from './Article13';
import { Article15 } from './Article15';
import { NationalSystems } from './NationalSystems';

export const Webforms = ({ dataflowId, datasetId, isReleasing, isReporting = false, state, webformType }) => {
  switch (webformType) {
    case 'MMR-ART13':
      return (
        <Article13
          dataflowId={dataflowId}
          datasetId={datasetId}
          state={state}
          isReporting={isReporting}
          isReleasing={isReleasing}
        />
      );

    case 'MMR-ART15':
      return <Article15 dataflowId={dataflowId} datasetId={datasetId} state={state} isReporting={isReporting} />;

    case 'NATIONAL-SYSTEMS':
      return <NationalSystems dataflowId={dataflowId} datasetId={datasetId} state={state} isReporting={isReporting} />;

    default:
      return <Fragment />;
  }
};
