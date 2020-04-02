import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined, isNull, pick } from 'lodash';

import styles from './DatasetSchemas.module.css';

import { Button } from 'ui/views/_components/Button';
import { DatasetSchema } from './_components/DatasetSchema';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { ValidationService } from 'core/services/Validation';

const DatasetSchemas = ({ datasetsSchemas, isCustodian, onLoadDatasetsSchemas }) => {
  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isLoading, setIsLoading] = useState(false);
  const [validationList, setValidationList] = useState();

  useEffect(() => {
    if (!isEmpty(datasetsSchemas)) {
      getValidationList(datasetsSchemas);
    }
  }, [datasetsSchemas]);

  useEffect(() => {
    renderDatasetSchemas();
  }, [validationList]);

  const getValidationList = async datasetsSchemas => {
    try {
      const datasetValidations = datasetsSchemas.map(async datasetSchema => {
        return await ValidationService.getAll(datasetSchema.datasetSchemaId);
      });
      Promise.all(datasetValidations).then(allValidations => {
        setValidationList(
          !isUndefined(allValidations[0])
            ? allValidations[0].validations.map(validation =>
                pick(
                  validation,
                  'shortCode',
                  'name',
                  'description',
                  'entityType',
                  'levelError',
                  'message',
                  'automatic',
                  'enabled'
                )
              )
            : []
        );
      });

      // setValidationList([
      //   {
      //     automatic: 'true',
      //     datasetSchemaId: '5e450733a268b2000140ad7c',
      //     date: '2018-01-01',
      //     enabled: 'true',
      //     entityType: 'FIELD',
      //     id: 0,
      //     levelError: 'ERROR',
      //     message: 'This is an error message',
      //     ruleName: 'Test rule'
      //   },
      //   {
      //     automatic: 'false',
      //     datasetSchemaId: '5e450733a268b2000140ad7c',
      //     date: '2018-01-01',
      //     enabled: 'true',
      //     entityType: 'TABLE',
      //     id: 0,
      //     levelError: 'WARNING',
      //     message: 'This is a warning message 2',
      //     ruleName: 'Test rule 2'
      //   }
      // ]);
    } catch (error) {
      const schemaError = {
        type: error.message
      };
      notificationContext.add(schemaError);
    } finally {
      setIsLoading(false);
    }
  };

  const renderDatasetSchemas = () => {
    return !isUndefined(datasetsSchemas) && !isNull(datasetsSchemas) && datasetsSchemas.length > 0 ? (
      datasetsSchemas.map((designDataset, i) => {
        return <DatasetSchema designDataset={designDataset} key={i} index={i} validationList={validationList} />;
      })
    ) : (
      <h3>{`${resources.messages['noDesignSchemasCreated']}`}</h3>
    );
  };

  const renderToolbar = () => {
    return isCustodian ? (
      <Toolbar className={styles.datasetSchemasToolbar}>
        <div className="p-toolbar-group-right">
          <Button
            className={`p-button-rounded p-button-secondary-transparent  p-button-animated-blink ${
              isLoading ? 'p-button-animated-spin' : ''
            }`}
            disabled={false}
            icon={'refresh'}
            label={resources.messages['refresh']}
            onClick={async () => {
              setIsLoading(true);
              await onLoadDatasetsSchemas();
              setIsLoading(false);
            }}
          />
        </div>
      </Toolbar>
    ) : (
      <></>
    );
  };

  return (
    <>
      {renderToolbar()}
      {isLoading ? <Spinner className={styles.positioning} /> : renderDatasetSchemas()}
    </>
  );
};

export { DatasetSchemas };
