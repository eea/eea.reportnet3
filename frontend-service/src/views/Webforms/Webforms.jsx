import { useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import { Article13 } from './Article13';
import { Article15 } from './Article15';
import { NationalSystems } from './NationalSystems';
import { Spinner } from 'views/_components/Spinner';

import { WebformService } from 'services/WebformService';

export const Webforms = ({
  dataflowId,
  dataProviderId,
  datasetId,
  isReleasing,
  isReporting = false,
  options,
  state,
  webformType
}) => {
  const [selectedConfiguration, setSelectedConfiguration] = useState({ tables: {} });

  useEffect(() => {
    getWebformConfiguration();
  }, []);

  const getWebformConfiguration = async () => {
    try {
      const selectedWebform = options.find(item => item.value === webformType);
      setSelectedConfiguration(await WebformService.getWebformConfig(selectedWebform.id));
    } catch (error) {
      console.log('error :>> ', error);
    }
  };

  if (isEmpty(selectedConfiguration.tables)) return <Spinner style={{ top: 0, margin: '1rem' }} />;

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
          tables={selectedConfiguration.tables}
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
          tables={selectedConfiguration.tables}
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
          tables={selectedConfiguration.tables}
        />
      );
    default:
      return <div />;
  }
};
