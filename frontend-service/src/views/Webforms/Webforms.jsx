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
import { EntitiesWebform } from './EntitiesWebform';

export const Webforms = ({
  bigData,
  dataflowId,
  dataProviderId,
  datasetId,
  isEditor,
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
  const [rootPkFieldId, setRootPkFieldId] = useState();
  const [rootTableId, setRootTableId] = useState();
  const [rootTableName, setRootTableName] = useState();

  const { datasetSchema } = state;
  const datasetSchemaAllTables = datasetSchema?.tables || [];

  useEffect(() => {
    getWebformConfiguration();
  }, [webform.name]);

  const getWebformConfiguration = async () => {
    setLoadingStatus('pending');
    try {
      const selectedWebform = options.find(item => item.name === webform.name);
      const getWebformConfigData = await WebformService.getWebformConfig(selectedWebform.id);
      const rootTable = getWebformConfigData?.tables.find(table => table?.isRootTable === true);

      if (webform.type === 'ENTITIES') {
        setRootTable(rootTable.name);
      }

      setSelectedConfiguration(getWebformConfigData);
      setLoadingStatus('success');
    } catch (error) {
      console.error('Webforms - getWebformConfiguration.', error);
      setLoadingStatus('failed');
      notificationContext.add({ type: 'LOADING_WEBFORM_ERROR' }, true);
    }
  };

  const setRootTable = rootTableName => {
    const filteredRootTable = datasetSchemaAllTables.filter(table => table?.tableSchemaName === rootTableName);

    const filteredFieldId = filteredRootTable[0].records[0].fields.filter(field => field?.pk === true);

    setRootPkFieldId(filteredFieldId[0].fieldId);
    setRootTableId(filteredRootTable[0].tableSchemaId);
    setRootTableName(rootTableName);
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
    case 'ENTITIES':
      return (
        <EntitiesWebform
          bigData={bigData}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          hideEntities={selectedConfiguration?.hideEntities}
          hideTabularData={selectedConfiguration?.hideTabularData}
          isEditor={isEditor}
          isIcebergCreated={isIcebergCreated}
          isLoadingIceberg={isLoadingIceberg}
          isReleasing={isReleasing}
          isReporting={isReporting}
          overview={selectedConfiguration.overview}
          rootPkFieldId={rootPkFieldId}
          rootTableId={rootTableId}
          rootTableName={rootTableName}
          state={state}
          tables={selectedConfiguration.tables}
        />
      );
    case 'PAMS':
      return (
        <PaMsWebform
          bigData={bigData}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          isEditor={isEditor}
          isIcebergCreated={isIcebergCreated}
          isLoadingIceberg={isLoadingIceberg}
          isReleasing={isReleasing}
          isReporting={isReporting}
          overview={selectedConfiguration.overview}
          state={state}
          tables={selectedConfiguration.tables}
        />
      );

    case 'TABLES':
      return (
        <TableWebform
          bigData={bigData}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          isEditor={isEditor}
          isIcebergCreated={isIcebergCreated}
          isLoadingIceberg={isLoadingIceberg}
          isReporting={isReporting}
          state={state}
          tables={selectedConfiguration.tables}
        />
      );
    case 'QA':
      return (
        <QuestionAnswerWebform
          bigData={bigData}
          dataflowId={dataflowId}
          dataProviderId={dataProviderId}
          datasetId={datasetId}
          isEditor={isEditor}
          isIcebergCreated={isIcebergCreated}
          isLoadingIceberg={isLoadingIceberg}
          isReporting={isReporting}
          state={state}
          tables={selectedConfiguration.tables}
        />
      );
    default:
      return <div />;
  }
};
