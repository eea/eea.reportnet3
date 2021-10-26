import { useContext, useEffect, useState } from 'react';

import styles from './Webforms.module.scss';

import { Article13 } from './Article13';
import { Article15 } from './Article15';
import { Button } from 'views/_components/Button';
import { NationalSystems } from './NationalSystems';
import { Spinner } from 'views/_components/Spinner';

import { WebformService } from 'services/WebformService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const Webforms = ({
  dataflowId,
  dataProviderId,
  datasetId,
  isReleasing,
  isReporting = false,
  options = [],
  state,
  webformType
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const [selectedConfiguration, setSelectedConfiguration] = useState({ tables: {} });
  const [loadingStatus, setLoadingStatus] = useState('idle');

  useEffect(() => {
    getWebformConfiguration();
  }, []);

  const getWebformConfiguration = async () => {
    setLoadingStatus('pending');
    try {
      const selectedWebform = options.find(item => item.value === webformType);
      setSelectedConfiguration(await WebformService.getWebformConfig(selectedWebform.id));
      setLoadingStatus('success');
    } catch (error) {
      setLoadingStatus('failed');
    }
  };

  if (loadingStatus === 'pending') return <Spinner style={{ top: 0, margin: '1rem' }} />;

  if (loadingStatus === 'failed') {
    return (
      <div className={styles.somethingWentWrong}>
        {resourcesContext.messages['somethingWentWrong']}
        <Button icon="refresh" label={'Refresh'} onClick={getWebformConfiguration} />
      </div>
    );
  }

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
