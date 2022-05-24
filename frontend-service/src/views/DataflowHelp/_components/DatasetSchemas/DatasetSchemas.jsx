import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';
import pick from 'lodash/pick';

import styles from './DatasetSchemas.module.scss';

import { Button } from 'views/_components/Button';
import { DatasetSchema } from './_components/DatasetSchema';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'views/_components/Spinner';
import { Toolbar } from 'views/_components/Toolbar';

import { getExpressionString } from 'views/DatasetDesigner/_components/Validations/_functions/Utils/getExpressionString';

import { TextUtils } from 'repositories/_utils/TextUtils';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';

import { DataflowService } from 'services/DataflowService';
import { IntegrationService } from 'services/IntegrationService';
import { UniqueConstraintService } from 'services/UniqueConstraintService';
import { ValidationService } from 'services/ValidationService';

export const DatasetSchemas = ({ dataflowId, datasetsSchemas, hasCustodianPermissions, onLoadDatasetsSchemas }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [isLoading, setIsLoading] = useState(!isEmpty(datasetsSchemas));
  const [isDownloading, setIsDownloading] = useState(false);
  const [extensionsOperationsList, setExtensionsOperationsList] = useState();
  const [uniqueList, setUniqueList] = useState();
  const [qcList, setQCList] = useState();

  useEffect(() => {
    if (!isEmpty(datasetsSchemas)) {
      getValidationList(datasetsSchemas);
      getUniqueList(datasetsSchemas);
      getExtensionsOperations(datasetsSchemas);
    }
  }, [datasetsSchemas]);

  useEffect(() => {
    if (!isUndefined(extensionsOperationsList) && !isUndefined(uniqueList) && !isUndefined(qcList)) {
      setIsLoading(false);
    }
  }, [extensionsOperationsList, uniqueList, qcList]);

  useEffect(() => {
    if (
      notificationContext.hidden.some(
        notification =>
          notification.key === 'EXPORT_SCHEMA_INFORMATION_COMPLETED_EVENT' ||
          notification.key === 'EXPORT_SCHEMA_INFORMATION_FAILED_EVENT'
      )
    ) {
      setIsDownloading(false);
    }
  }, [notificationContext.hidden]);

  const onDownloadAllSchemasInfo = async dataflowId => {
    try {
      setIsDownloading(true);
      await DataflowService.generateAllSchemasInfoFile(dataflowId);
      notificationContext.add({ type: 'DOWNLOAD_SCHEMAS_INFO_START' });
    } catch (error) {
      console.error('DatasetSchema - onDownloadAllSchemasInfo .', error);
      if (error.response?.status === 400) {
        notificationContext.add({ type: 'DOWNLOAD_FILE_BAD_REQUEST_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'GENERATE_SCHEMAS_INFO_FILE_ERROR' }, true);
      }
      setIsDownloading(false);
    }
  };

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
              if (table.records?.some(record => record.recordSchemaId === referenceId)) {
                additionalInfo.tableName = !isUndefined(table.tableSchemaName) ? table.tableSchemaName : table.header;
              }
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
        return await IntegrationService.getAllExtensionsOperations(dataflowId, datasetSchema.datasetSchemaId);
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
      console.error('DatasetSchemas - getExtensionsOperations.', error);
      const schemaError = {
        type: error.message
      };
      notificationContext.add(schemaError, true);
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
        return await UniqueConstraintService.getAll(dataflowId, datasetSchema.datasetSchemaId);
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
      console.error('DatasetSchemas - getUniqueList.', error);
      const schemaError = {
        type: error.message
      };
      notificationContext.add(schemaError, true);
    }
  };

  const getQCList = allQCs => {
    if (!isEmpty(allQCs)) {
      return allQCs
        .map(allQCs =>
          allQCs.validations.map(qc => {
            const datasetSchema = datasetsSchemas.filter(
              datasetSchema => datasetSchema.datasetSchemaId === allQCs.datasetSchemaId
            );

            const additionalInfo = getAdditionalValidationInfo(
              qc.referenceId,
              qc.entityType,
              qc.relations,
              datasetsSchemas,
              allQCs.datasetSchemaId
            );
            qc.tableName = additionalInfo.tableName || '';
            qc.fieldName = additionalInfo.fieldName || '';
            qc.expression = getExpressionString(qc, datasetSchema[0].tables);
            qc.datasetSchemaId = allQCs.datasetSchemaId;
            if (!hasCustodianPermissions) {
              return pick(
                qc,
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
                qc,
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
        .flat();
    } else {
      return [];
    }
  };

  const getValidationList = async datasetsSchemas => {
    try {
      setIsLoading(true);
      const datasetValidations = datasetsSchemas.map(async datasetSchema => {
        return await ValidationService.getAll(dataflowId, datasetSchema.datasetSchemaId, !hasCustodianPermissions);
      });
      Promise.all(datasetValidations).then(allQCs => {
        allQCs = allQCs.filter(qc => !isUndefined(qc));
        if (!hasCustodianPermissions) {
          allQCs.forEach(qc => (qc = qc.validations.filter(validation => validation.enabled !== false)));
        }
        setQCList(getQCList(allQCs));
      });
    } catch (error) {
      console.error('DatasetSchemas - getValidationList.', error);
      const schemaError = {
        type: error.message
      };
      notificationContext.add(schemaError, true);
    }
  };

  const renderAnchors = () => {
    return !isNil(datasetsSchemas) && datasetsSchemas.length > 0 ? (
      <div className={`dataflowHelp-datasetSchema-help-step ${styles.index}`}>
        <h3>{resourcesContext.messages['datasetSchemas']}</h3>
        {datasetsSchemas.map(designDataset => (
          <div key={designDataset.datasetSchemaName}>
            <a href={`#${designDataset.datasetSchemaId}`}>{`• ${designDataset.datasetSchemaName}`}</a>
          </div>
        ))}
      </div>
    ) : null;
  };

  const renderDatasetSchemas = () => {
    return !isNil(datasetsSchemas) && datasetsSchemas.length > 0 ? (
      <div className="dataflowHelp-datasetSchema-help-step">
        {datasetsSchemas.map((designDataset, i) => (
          <DatasetSchema
            designDataset={designDataset}
            extensionsOperationsList={filterData(designDataset, extensionsOperationsList)}
            hasCustodianPermissions={hasCustodianPermissions}
            index={i}
            key={designDataset.datasetSchemaId}
            onGetReferencedFieldName={onGetReferencedFieldName}
            qcList={filterData(designDataset, qcList)}
            uniqueList={filterData(designDataset, uniqueList)}
          />
        ))}
      </div>
    ) : (
      <h3>{`${resourcesContext.messages['noDesignSchemasCreated']}`}</h3>
    );
  };

  const renderToolbar = () => {
    return (
      <Toolbar className={styles.datasetSchemasToolbar} id="datasetSchemaIndex">
        <div className="p-toolbar-group-left">
          <Button
            className={`p-button-rounded p-button-secondary-transparent ${
              !isDownloading ? 'p-button-animated-blink' : ''
            }`}
            disabled={isDownloading}
            icon={isDownloading ? 'spinnerAnimate' : 'export'}
            label={resourcesContext.messages['downloadSchemasInfo']}
            onClick={() => onDownloadAllSchemasInfo(dataflowId)}
            tooltip={resourcesContext.messages['downloadSchemasInfoTooltip']}
            tooltipOptions={{ position: 'top' }}
          />
          <Button
            className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${
              isLoading ? 'p-button-animated-spin' : ''
            }`}
            icon="refresh"
            label={resourcesContext.messages['refresh']}
            onClick={async () => {
              setIsLoading(true);
              await onLoadDatasetsSchemas();
              setIsLoading(false);
            }}
          />
        </div>
      </Toolbar>
    );
  };

  return (
    <Fragment>
      {renderToolbar()}
      {isLoading ? (
        <Spinner className={styles.positioning} />
      ) : (
        <div>
          {renderAnchors()}
          {renderDatasetSchemas()}
        </div>
      )}
    </Fragment>
  );
};
