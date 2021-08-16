import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';
import pick from 'lodash/pick';

import styles from './DatasetSchemas.module.css';

import { Button } from 'ui/views/_components/Button';
import { DatasetSchema } from './_components/DatasetSchema';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { getExpressionString } from 'ui/views/DatasetDesigner/_components/Validations/_functions/Utils/getExpressionString';
import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

import { IntegrationService } from 'core/services/Integration';
import { UniqueConstraintsService } from 'core/services/UniqueConstraints';
import { ValidationService } from 'core/services/Validation';

const DatasetSchemas = ({ dataflowId, datasetsSchemas, isCustodian, onLoadDatasetsSchemas }) => {
  const resources = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [expandAll, setExpandAll] = useState(true);
  const [isLoading, setIsLoading] = useState(!isEmpty(datasetsSchemas));
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
    if (!isUndefined(extensionsOperationsList) && !isUndefined(uniqueList) && !isUndefined(validationList)) {
      setIsLoading(false);
    }
  }, [extensionsOperationsList, uniqueList, validationList]);

  const onGetReferencedFieldName = (referenceField, isExternalLink) => {
    const fieldObj = {};
    if (!isNil(datasetsSchemas) && !isEmpty(datasetsSchemas)) {
      datasetsSchemas.forEach(dataset => {
        if (!isUndefined(dataset.tables)) {
          dataset.tables.forEach(table => {
            table.records.forEach(record => {
              record.fields.forEach(field => {
                if (field.fieldId === referenceField.idPk) {
                  fieldObj.tableName = table.tableSchemaName;
                  fieldObj.fieldName = field.name;
                }
                if (!isExternalLink) {
                  if (field.fieldId === referenceField.labelId) {
                    fieldObj.linkedTableLabel = field.name;
                  }
                  if (field.fieldId === referenceField.linkedConditionalFieldId) {
                    fieldObj.linkedTableConditional = field.name;
                  }
                  if (field.fieldId === referenceField.masterConditionalFieldId) {
                    fieldObj.masterTableConditional = field.name;
                  }
                }
              });
            });
          });
        }
      });
      if (isExternalLink) {
        fieldObj.tableName = referenceField.tableSchemaName;
        fieldObj.fieldName = referenceField.fieldSchemaName;
      }
      return fieldObj;
    }
  };

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

  const getAdditionalValidationInfo = (referenceId, entityType, relations, datasets, datasetSchemaId) => {
    const additionalInfo = {};
    const dataset = datasets.filter(datasetSchema => datasetSchema.datasetSchemaId === datasetSchemaId);
    if (dataset.length > 0) {
      if (!isUndefined(dataset[0].tables)) {
        dataset[0].tables.forEach(table => {
          if (!isUndefined(table.records)) {
            if (TextUtils.areEquals(entityType, 'TABLE')) {
              if (table.tableSchemaId === referenceId)
                additionalInfo.tableName = !isUndefined(table.tableSchemaName) ? table.tableSchemaName : table.header;
            } else if (TextUtils.areEquals(entityType, 'RECORD')) {
              additionalInfo.tableName = !isUndefined(table.tableSchemaName) ? table.tableSchemaName : table.header;
            } else if (TextUtils.areEquals(entityType, 'FIELD') || TextUtils.areEquals(entityType, 'TABLE')) {
              table.records.forEach(record =>
                record.fields.forEach(field => {
                  if (!isNil(field)) {
                    if (TextUtils.areEquals(entityType, 'FIELD')) {
                      if (field.fieldId === referenceId) {
                        additionalInfo.tableName = !isUndefined(table.tableSchemaName)
                          ? table.tableSchemaName
                          : table.header;
                        additionalInfo.fieldName = field.name;
                      }
                    } else {
                      if (!isEmpty(relations)) {
                        if (field.fieldId === relations.links[0].originField.code) {
                          additionalInfo.tableName = !isUndefined(table.tableSchemaName)
                            ? table.tableSchemaName
                            : table.header;
                          additionalInfo.fieldName = field.name;
                        }
                      }
                    }
                  }
                })
              );
            }
          }
        });
      }
    }
    return additionalInfo;
  };

  const getExtensionsOperations = async datasetsSchemas => {
    try {
      const datasetExtensionsOperations = datasetsSchemas.map(async datasetSchema => {
        return await IntegrationService.allExtensionsOperations(dataflowId, datasetSchema.datasetSchemaId);
      });

      Promise.all(datasetExtensionsOperations).then(allExtensionsOperations => {
        const parseExtensionsOperations = extensionsOperations => {
          const parsedExtensionsOperations = [];
          extensionsOperations.forEach(extensionOperation => {
            parsedExtensionsOperations.push(
              pick(extensionOperation, 'datasetSchemaId', 'operation', 'fileExtension', 'id')
            );
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
    }
  };

  const getFieldName = (referenceId, datasetSchemaId, datasets) => {
    const fieldObj = {};
    const dataset = datasets.filter(datasetSchema => datasetSchema.datasetSchemaId === datasetSchemaId);
    if (dataset.length > 0) {
      if (!isUndefined(dataset[0].tables)) {
        dataset[0].tables.forEach(table => {
          table.records.forEach(record => {
            record.fields.forEach(field => {
              if (field.fieldId === referenceId) {
                fieldObj.tableName = table.tableSchemaName;
                fieldObj.fieldName = field.name;
              }
            });
          });
        });
        return fieldObj;
      }
    }
  };

  const getUniqueList = async datasetsSchemas => {
    try {
      const datasetUniques = datasetsSchemas.map(async datasetSchema => {
        const response = await UniqueConstraintsService.all(dataflowId, datasetSchema.datasetSchemaId);
        return response.data;
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
                } else {
                  return [];
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
    }
  };

  const getValidationList = async datasetsSchemas => {
    try {
      setIsLoading(true);
      const datasetValidations = datasetsSchemas.map(async datasetSchema => {
        return await ValidationService.getAll(datasetSchema.datasetSchemaId, !isCustodian);
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
                    const datasetSchema = datasetsSchemas.filter(
                      datasetSchema => datasetSchema.datasetSchemaId === allValidation.datasetSchemaId
                    );

                    const additionalInfo = getAdditionalValidationInfo(
                      validation.referenceId,
                      validation.entityType,
                      validation.relations,
                      datasetsSchemas,
                      allValidation.datasetSchemaId
                    );
                    validation.tableName = additionalInfo.tableName || '';
                    validation.fieldName = additionalInfo.fieldName || '';
                    validation.expression = getExpressionString(validation, datasetSchema[0].tables);
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
    }
  };

  const renderDatasetSchemas = () => {
    return !isUndefined(datasetsSchemas) && !isNull(datasetsSchemas) && datasetsSchemas.length > 0 ? (
      <div className="dataflowHelp-datasetSchema-help-step">
        {datasetsSchemas.map((designDataset, i) => (
          <DatasetSchema
            designDataset={designDataset}
            expandAll={expandAll}
            extensionsOperationsList={filterData(designDataset, extensionsOperationsList)}
            index={i}
            isCustodian={isCustodian}
            key={designDataset.datasetSchemaId}
            onGetReferencedFieldName={onGetReferencedFieldName}
            uniqueList={filterData(designDataset, uniqueList)}
            validationList={filterData(designDataset, validationList)}
          />
        ))}
      </div>
    ) : (
      <h3>{`${resources.messages['noDesignSchemasCreated']}`}</h3>
    );
  };

  const renderToolbar = () => {
    return (
      isCustodian && (
        <Toolbar className={styles.datasetSchemasToolbar}>
          <div className="p-toolbar-group-left">
            <Button
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink`}
              icon={expandAll ? 'angleRight' : 'angleDown'}
              label={expandAll ? resources.messages['collapseAll'] : resources.messages['expandAll']}
              onClick={() => setExpandAll(!expandAll)}
            />
            <Button
              className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${
                isLoading ? 'p-button-animated-spin' : ''
              }`}
              icon="refresh"
              label={resources.messages['refresh']}
              onClick={async () => {
                setIsLoading(true);
                await onLoadDatasetsSchemas();
                setIsLoading(false);
              }}
            />
          </div>
        </Toolbar>
      )
    );
  };

  return (
    <Fragment>
      {renderToolbar()}
      {isLoading ? <Spinner className={styles.positioning} /> : renderDatasetSchemas()}
    </Fragment>
  );
};

export { DatasetSchemas };
