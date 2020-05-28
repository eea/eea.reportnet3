import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined, isNull, pick } from 'lodash';

import styles from './DatasetSchemas.module.css';

import { Button } from 'ui/views/_components/Button';
import { DatasetSchema } from './_components/DatasetSchema';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { UniqueConstraintsService } from 'core/services/UniqueConstraints';
import { ValidationService } from 'core/services/Validation';

const DatasetSchemas = ({ datasetsSchemas, isCustodian, onLoadDatasetsSchemas }) => {
  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isLoading, setIsLoading] = useState(false);
  const [uniqueList, setUniqueList] = useState();
  const [validationList, setValidationList] = useState();

  useEffect(() => {
    if (!isEmpty(datasetsSchemas)) {
      getValidationList(datasetsSchemas);
      getUniqueList(datasetsSchemas);
    }
  }, [datasetsSchemas]);

  useEffect(() => {
    renderDatasetSchemas();
  }, [validationList]);

  const getFieldName = (referenceId, datasetSchemaId, datasets) => {
    const fieldObj = {};
    const dataset = datasets.filter(datasetSchema => datasetSchema.datasetSchemaId === datasetSchemaId);
    if (dataset.length > 0) {
      if (!isUndefined(dataset[0].tables)) {
        dataset[0].tables.forEach(table =>
          table.records.filter(record => {
            record.fields.forEach(field => {
              if (field.fieldId === referenceId) {
                fieldObj.tableName = table.tableSchemaName;
                fieldObj.fieldName = field.name;
              }
            });
          })
        );
        return fieldObj;
      }
    }
  };

  const getUniqueList = async datasetsSchemas => {
    try {
      const datasetUniques = datasetsSchemas.map(async datasetSchema => {
        return await UniqueConstraintsService.all(datasetSchema.datasetSchemaId);
      });

      Promise.all(datasetUniques).then(allUniques => {
        // console.log({ allUniques });

        const parseUniques = uniques => {
          const parsedUniques = [];
          uniques.forEach(unique => {
            unique.fieldSchemaIds.forEach(fieldSchema => {
              parsedUniques.push({
                referenceId: fieldSchema,
                datasetSchemaId: unique.datasetSchemaId,
                tableSchemaId: unique.tableSchemaId
              });
            });
          });

          return parsedUniques;
        };

        const parsedUniques = parseUniques(allUniques.flat());
        // console.log({ parsedUniques });
        setUniqueList(
          !isUndefined(parsedUniques)
            ? parsedUniques.map(unique => {
                if (!isEmpty(unique)) {
                  // console.log({ unique });
                  // debugger;
                  const uniqueTableAndField = getFieldName(unique.referenceId, unique.datasetSchemaId, datasetsSchemas);
                  // console.log({ uniqueTableAndField });
                  if (!isUndefined(uniqueTableAndField)) {
                    unique.tableName = uniqueTableAndField.tableName;
                    unique.fieldName = uniqueTableAndField.fieldName;
                  }
                  return pick(unique, 'tableName', 'fieldName');
                }
              })
            : []
        );
      });
    } catch (error) {
      const schemaError = {
        type: error.message
      };
      notificationContext.add(schemaError);
    } finally {
      setIsLoading(false);
    }
  };

  const getValidationList = async datasetsSchemas => {
    try {
      const datasetValidations = datasetsSchemas.map(async datasetSchema => {
        return await ValidationService.getAll(datasetSchema.datasetSchemaId);
      });
      Promise.all(datasetValidations).then(allValidations => {
        if (!isCustodian) {
          allValidations[0].validations = allValidations[0].validations.filter(
            validation => validation.enabled !== false
          );
        }
        // console.log(allValidations[0]);
        setValidationList(
          !isUndefined(allValidations[0])
            ? allValidations[0].validations.map(validation => {
                // debugger;
                const validationTableAndField = getFieldName(
                  validation.referenceId,
                  //validation.idDatasetSchema,
                  allValidations[0].datasetSchemaId,
                  datasetsSchemas
                );
                validation.tableName = validationTableAndField.tableName;
                validation.fieldName = validationTableAndField.fieldName;
                if (!isCustodian) {
                  return pick(
                    validation,
                    'tableName',
                    'fieldName',
                    'shortCode',
                    'name',
                    'description',
                    'entityType',
                    'levelError',
                    'message'
                  );
                } else {
                  return pick(
                    validation,
                    'tableName',
                    'fieldName',
                    'shortCode',
                    'name',
                    'description',
                    'entityType',
                    'levelError',
                    'message',
                    'automatic',
                    'enabled'
                  );
                }
              })
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
      <div className="dataflowHelp-datasetSchema-help-step">
        {datasetsSchemas.map((designDataset, i) => {
          return (
            <DatasetSchema
              designDataset={designDataset}
              key={i}
              index={i}
              isCustodian={isCustodian}
              uniqueList={!isUndefined(uniqueList) ? [...uniqueList] : []}
              validationList={!isUndefined(validationList) ? [...validationList] : []}
            />
          );
        })}
      </div>
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
