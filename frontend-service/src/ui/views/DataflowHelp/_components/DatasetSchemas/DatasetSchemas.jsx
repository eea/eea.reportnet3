import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined, isNull, pick } from 'lodash';

import styles from './DatasetSchemas.module.css';

import { Button } from 'ui/views/_components/Button';
import { DatasetSchema } from './_components/DatasetSchema';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { getExpressionString } from 'ui/views/DatasetDesigner/_components/Validations/_functions/utils/getExpressionString';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { IntegrationService } from 'core/services/Integration';
import { UniqueConstraintsService } from 'core/services/UniqueConstraints';
import { ValidationService } from 'core/services/Validation';

const DatasetSchemas = ({ datasetsSchemas, isCustodian, onLoadDatasetsSchemas }) => {
  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isLoading, setIsLoading] = useState(false);
  const [extensionsOperationsList, setExtensionsOperationsList] = useState();
  const [uniqueList, setUniqueList] = useState();
  const [validationList, setValidationList] = useState();

  useEffect(() => {
    if (!isEmpty(datasetsSchemas)) {
      getValidationList(datasetsSchemas);
      getUniqueList(datasetsSchemas);
      getExtensionsOperations(datasetsSchemas);
    }
  }, [datasetsSchemas]);

  useEffect(() => {
    renderDatasetSchemas();
  }, [uniqueList, validationList]);

  const filterData = (designDataset, data) => {
    if (!isUndefined(data)) {
      const filteredData = data.filter(list => list.datasetSchemaId === designDataset.datasetSchemaId);
      if (!isUndefined(filteredData[0])) {
        return filteredData;
      } else {
        return [];
      }
    } else {
      return [];
    }
  };

  const getExtensionsOperations = async datasetsSchemas => {
    try {
      const datasetExtensionsOperations = datasetsSchemas.map(async datasetSchema => {
        return await IntegrationService.allExtensionsOperations(datasetSchema.datasetSchemaId);
      });

      Promise.all(datasetExtensionsOperations).then(allExtensionsOperations => {
        const parseExtensionsOperations = extensionsOperations => {
          const parsedExtensionsOperations = [];
          extensionsOperations.forEach(extensionOperation => {
            parsedExtensionsOperations.push(pick(extensionOperation, 'datasetSchemaId', 'operation', 'fileExtension'));
          });
          return parsedExtensionsOperations;
        };

        const parsedExtensionsOperations = parseExtensionsOperations(allExtensionsOperations.flat());
        setExtensionsOperationsList(!isUndefined(parsedExtensionsOperations) ? parsedExtensionsOperations : []);
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
        setUniqueList(
          !isUndefined(parsedUniques)
            ? parsedUniques.map(unique => {
                if (!isEmpty(unique)) {
                  const uniqueTableAndField = getFieldName(unique.referenceId, unique.datasetSchemaId, datasetsSchemas);
                  if (!isUndefined(uniqueTableAndField)) {
                    unique.tableName = uniqueTableAndField.tableName;
                    unique.fieldName = uniqueTableAndField.fieldName;
                  }
                  return pick(unique, 'tableName', 'fieldName', 'datasetSchemaId');
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
        allValidations = allValidations.filter(allValidation => !isUndefined(allValidation));
        if (!isCustodian) {
          allValidations.forEach(
            allValidation =>
              (allValidation = allValidation.validations.filter(validation => validation.enabled !== false))
          );
        }
        setValidationList(
          !isUndefined(allValidations[0])
            ? allValidations
                .map(allValidation =>
                  allValidation.validations.map(validation => {
                    const validationTableAndField = getFieldName(
                      validation.referenceId,
                      //validation.idDatasetSchema,
                      allValidation.datasetSchemaId,
                      datasetsSchemas
                    );
                    validation.tableName = validationTableAndField.tableName;
                    validation.fieldName = validationTableAndField.fieldName;
                    validation.expression = getExpressionString(validation.expressions, {
                      label: validation.fieldName,
                      code: validation.id
                    });
                    validation.datasetSchemaId = allValidation.datasetSchemaId;
                    if (!isCustodian) {
                      return pick(
                        validation,
                        'tableName',
                        'fieldName',
                        'shortCode',
                        'name',
                        'description',
                        'expression',
                        'entityType',
                        'levelError',
                        'message',
                        'datasetSchemaId'
                      );
                    } else {
                      return pick(
                        validation,
                        'tableName',
                        'fieldName',
                        'shortCode',
                        'name',
                        'description',
                        'expression',
                        'entityType',
                        'levelError',
                        'message',
                        'automatic',
                        'enabled',
                        'datasetSchemaId'
                      );
                    }
                  })
                )
                .flat()
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
              extensionsOperationsList={filterData(designDataset, extensionsOperationsList)}
              uniqueList={filterData(designDataset, uniqueList)}
              validationList={filterData(designDataset, validationList)}
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
