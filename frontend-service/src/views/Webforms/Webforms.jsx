import { useContext, useEffect, useState } from 'react';

import styles from './Webforms.module.scss';

import { PaMsWebform } from './PaMsWebform';
import { TableWebform } from './TableWebform';
import { Button } from 'views/_components/Button';
import { QuestionAnswerWebform } from './QuestionAnswerWebform';
import { Spinner } from 'views/_components/Spinner';

import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const Webforms = ({
  bigData,
  dataflowId,
  dataProviderId,
  datasetId,
  isIcebergCreated,
  isLoadingIceberg,
  isReleasing,
  isReporting = false,
  options = [],
  state,
  webform
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [selectedConfiguration, setSelectedConfiguration] = useState({ tables: [] });
  const [loadingStatus, setLoadingStatus] = useState('idle');

  useEffect(() => {
    getWebformConfiguration();
  }, [webform.name]);

  const getWebformConfiguration = async () => {
    setLoadingStatus('pending');
    try {
      const selectedWebform = options.find(item => item.name === webform.name);
      setSelectedConfiguration(await WebformService.getWebformConfig(selectedWebform.id));
      setLoadingStatus('success');
    } catch (error) {
      console.error('Webforms - getWebformConfiguration.', error);
      setLoadingStatus('failed');
      notificationContext.add({ type: 'LOADING_WEBFORM_ERROR' }, true);
    }
  };

  if (loadingStatus === 'pending') {
    return <Spinner style={{ top: 0, margin: '1rem' }} />;
  }

  if (loadingStatus === 'failed') {
    return (
      <div className={styles.somethingWentWrong}>
        {resourcesContext.messages['somethingWentWrongWebform']}
        <Button icon="refresh" label={'Refresh'} onClick={getWebformConfiguration} />
      </div>
    );
  }

  switch (webform.type) {
    case 'PAMS':
      return (
        <PaMsWebform
          bigData={bigData}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          isReleasing={isReleasing}
          isReporting={isReporting}
          overview={selectedConfiguration.overview}
          state={state}
          tables={selectedConfiguration.tables}
          isLoadingIceberg={isLoadingIceberg}
        />
      );
    case 'TABLES':
      return (
        <TableWebform
          bigData={bigData}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          isReporting={isReporting}
          state={state}
          tables={selectedConfiguration.tables}
          isIcebergCreated={isIcebergCreated}
          isLoadingIceberg={isLoadingIceberg}
        />
      );
    case 'QA':
      return (
        <QuestionAnswerWebform
          bigData={bigData}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          isReporting={isReporting}
          state={state}
          tables={selectedConfiguration.tables}
          isLoadingIceberg={isLoadingIceberg}
        />
      );
    default:
      return <div />;
  }
};
