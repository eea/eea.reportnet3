import { Article13 } from './Article13';
import { Article15 } from './Article15';
import { NationalSystems } from './NationalSystems';

export const Webforms = ({
  dataProviderId,
  dataflowId,
  datasetId,
  isReleasing,
  isReporting = false,
  state,
  webformType
}) => {
  switch (webformType) {
    case 'MMR-ART13':
      return (
        <Article13
          dataProviderId={dataProviderId}
          dataflowId={dataflowId}
          datasetId={datasetId}
          isReleasing={isReleasing}
          isReporting={isReporting}
          state={state}
        />
      );
    case 'MMR-ART15':
      return (
        <Article15
          dataProviderId={dataProviderId}
          dataflowId={dataflowId}
          datasetId={datasetId}
          isReporting={isReporting}
          state={state}
        />
      );
    case 'NATIONAL-SYSTEMS':
      return (
        <NationalSystems
          dataProviderId={dataProviderId}
          dataflowId={dataflowId}
          datasetId={datasetId}
          isReporting={isReporting}
          state={state}
        />
      );
    default:
      return <div />;
  }
};
