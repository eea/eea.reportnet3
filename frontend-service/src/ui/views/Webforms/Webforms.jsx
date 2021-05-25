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
          isReleasing={isReleasing}
          isReporting={isReporting}
          state={state}
        />
      );

    case 'MMR-ART15':
      return <Article15 dataflowId={dataflowId} datasetId={datasetId} isReporting={isReporting} state={state} />;

    case 'NATIONAL-SYSTEMS':
      return <NationalSystems dataflowId={dataflowId} datasetId={datasetId} isReporting={isReporting} state={state} />;

    default:
      return <div />;
  }
};
