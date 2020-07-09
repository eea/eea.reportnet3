import { DatasetConfig } from 'conf/domain/model/Dataset';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiDataset = {
  addRecordFieldDesign: async (datasetId, datasetTableRecordField) => {
    try {
      const response = await HTTPRequester.post({
        url: window.env.REACT_APP_JSON
          ? `/dataschema/${datasetId}/fieldSchema`
          : getUrl(DatasetConfig.addNewRecordFieldDesign, {
              datasetId
            }),
        data: datasetTableRecordField
      });

      return response;
    } catch (error) {
      console.error(`Error adding record to dataset design data: ${error}`);
      return false;
    }
  },
  addRecordsById: async (datasetId, tableSchemaId, datasetTableRecords) => {
    try {
      const response = await HTTPRequester.post({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/table/${tableSchemaId}/record`
          : getUrl(DatasetConfig.addNewRecord, {
              datasetId: datasetId,
              tableSchemaId: tableSchemaId
            }),
        data: datasetTableRecords
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error adding record to dataset data: ${error}`);
      return false;
    }
  },
  addTableDesign: async (datasetId, tableSchemaName) => {
    try {
      const response = await HTTPRequester.post({
        url: window.env.REACT_APP_JSON
          ? `/dataschema/${datasetId}/tableSchema`
          : getUrl(DatasetConfig.addTableDesign, {
              datasetId
            }),
        data: { nameTableSchema: tableSchemaName }
      });
      return response;
    } catch (error) {
      console.error(`Error adding table to dataset design data: ${error}`);
      return false;
    }
  },
  deleteDataById: async datasetId => {
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/deleteImportData`
          : getUrl(DatasetConfig.deleteImportData, {
              datasetId: datasetId
            })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset data: ${error}`);
      return false;
    }
  },
  deleteRecordById: async (datasetId, recordId) => {
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/record/${recordId}`
          : getUrl(DatasetConfig.deleteRecord, {
              datasetId,
              recordId
            })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset table record: ${error}`);
      return false;
    }
  },
  deleteRecordFieldDesign: async (datasetId, fieldSchemaId) => {
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataschema/${datasetId}/fieldSchema/${fieldSchemaId}`
          : getUrl(DatasetConfig.deleteRecordFieldDesign, {
              datasetId,
              fieldSchemaId
            })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset table design record: ${error}`);
      return false;
    }
  },
  deleteSchemaById: async datasetId => {
    const response = await HTTPRequester.delete({
      url: getUrl(DatasetConfig.deleteDataSchema, {
        datasetId
      })
    });
    return response.status;
  },
  deleteTableDataById: async (datasetId, tableId) => {
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/deleteImportTable/${tableId}`
          : getUrl(DatasetConfig.deleteImportTable, {
              datasetId: datasetId,
              tableId: tableId
            })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset table data: ${error}`);
      return false;
    }
  },
  deleteTableDesign: async (datasetId, tableSchemaId) => {
    try {
      const response = await HTTPRequester.delete({
        url: window.env.REACT_APP_JSON
          ? `/dataschema/${datasetId}/tableSchema/${tableSchemaId}`
          : getUrl(DatasetConfig.deleteTableDesign, {
              datasetId,
              tableSchemaId
            })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error deleting dataset table design data: ${error}`);
      return false;
    }
  },
  errorPositionByObjectId: async (objectId, datasetId, entityType) => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_getTableFromAnyObjectId.json'
        : getUrl(DatasetConfig.validationViewer, {
            objectId: objectId,
            datasetId: datasetId,
            entityType: entityType
          })
    });
    return response.data;
  },

  errorsById: async (
    datasetId,
    pageNum,
    pageSize,
    sortField,
    asc,
    levelErrorsFilter,
    typeEntitiesFilter,
    originsFilter
  ) => {
    if (asc === -1) {
      asc = 0;
    }
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-errors.json'
        : getUrl(DatasetConfig.listValidations, {
            datasetId: datasetId,
            pageNum: pageNum,
            pageSize: pageSize,
            sortField: sortField,
            asc: asc,
            levelErrorsFilter: levelErrorsFilter,
            typeEntitiesFilter: typeEntitiesFilter,
            originsFilter: originsFilter
          })
    });
    return response.data;
  },
  exportDataById: async (datasetId, fileType) => {
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetData, {
        datasetId: datasetId,
        fileType: fileType
      }),
      headers: {
        'Content-Type': 'application/octet-stream'
      }
    });
    return response.data;
  },
  exportTableDataById: async (datasetId, tableSchemaId, fileType) => {
    const response = await HTTPRequester.download({
      url: getUrl(DatasetConfig.exportDatasetTableData, {
        datasetId: datasetId,
        tableSchemaId: tableSchemaId,
        fileType: fileType
      }),
      headers: {
        'Content-Type': 'application/octet-stream'
      }
    });
    return response.data;
  },
  getMetaData: async datasetId => {
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.datasetMetaData, {
        datasetId
      })
    });
    return response.data;
  },
  getReferencedFieldValues: async (datasetId, fieldSchemaId, searchToken) => {
    const response = await HTTPRequester.get({
      url: getUrl(DatasetConfig.referencedFieldValues, {
        datasetId,
        fieldSchemaId,
        searchToken
      })
    });
    return response.data;
  },
  orderFieldSchema: async (datasetId, position, fieldSchemaId) => {
    const response = await HTTPRequester.update({
      url: window.env.REACT_APP_JSON
        ? `/dataschema/${datasetId}/fieldSchema/order`
        : getUrl(DatasetConfig.orderFieldSchemaDesign, {
            datasetId,
            position
          }),
      data: { id: fieldSchemaId, position }
    });
    return response.status >= 200 && response.status <= 299;
  },
  orderTableSchema: async (datasetId, position, tableSchemaId) => {
    const response = await HTTPRequester.update({
      url: window.env.REACT_APP_JSON
        ? `/dataschema/${datasetId}/tableSchema/order`
        : getUrl(DatasetConfig.orderTableSchemaDesign, {
            datasetId,
            position
          }),
      data: { id: tableSchemaId, position }
    });
    return response.status >= 200 && response.status <= 299;
  },
  schemaById: async datasetId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/datosDataSchema2.json'
        : getUrl(DatasetConfig.dataSchema, {
            datasetId
          })
    });
    return response.data;
  },
  statisticsById: async datasetId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/error-statistics.json'
        : getUrl(DatasetConfig.loadStatistics, {
            datasetId: datasetId
          })
    });
    return response.data;
  },
  tableDataById: async (datasetId, tableSchemaId, pageNum, pageSize, fields, levelError) => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_dataset_values2.json'
        : getUrl(DatasetConfig.dataViewer, {
            datasetId: datasetId,
            tableSchemaId: tableSchemaId,
            pageNum: pageNum,
            pageSize: pageSize,
            fields: fields,
            levelError: levelError
          })
    });

    return response.data;
  },
  updateFieldById: async (datasetId, datasetTableRecords) => {
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/updateField`
          : getUrl(DatasetConfig.updateTableDataField, {
              datasetId: datasetId
            }),
        data: datasetTableRecords
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset field: ${error}`);
      return false;
    }
  },
  updateRecordFieldDesign: async (datasetId, datasetTableRecordField) => {
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataschema/${datasetId}/fieldSchema/`
          : getUrl(DatasetConfig.updateRecordFieldDesign, {
              datasetId
            }),
        data: datasetTableRecordField
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset record design: ${error}`);
      return false;
    }
  },
  updateRecordsById: async (datasetId, datasetTableRecords) => {
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataset/${datasetId}/updateRecord`
          : getUrl(DatasetConfig.updateTableDataRecord, {
              datasetId: datasetId
            }),
        data: datasetTableRecords
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset record data: ${error}`);
      return false;
    }
  },
  updateSchemaDescriptionById: async (datasetId, datasetSchemaDescription) => {
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/${datasetId}/datasetSchema/updateDatasetSchemaDescription`
          : getUrl(DatasetConfig.updateDatasetSchemaDescriptionDesign, {
              datasetId
            }),
        data: { description: datasetSchemaDescription }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset design name: ${error}`);
      return false;
    }
  },
  updateSchemaNameById: async (datasetId, datasetSchemaName) => {
    const response = await HTTPRequester.update({
      url: getUrl(DatasetConfig.updateDataSchemaName, {
        datasetId,
        datasetSchemaName
      })
    });
    return response.status;
  },
  updateTableDescriptionDesign: async (
    tableSchemaToPrefill,
    tableSchemaId,
    tableSchemaDescription,
    tableSchemaIsReadOnly,
    datasetId
  ) => {
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataschema/${datasetId}/tableSchema`
          : getUrl(DatasetConfig.updateTableDesign, {
              datasetId
            }),
        data: {
          idTableSchema: tableSchemaId,
          description: tableSchemaDescription,
          readOnly: tableSchemaIsReadOnly,
          toPrefill: tableSchemaToPrefill
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset design name: ${error}`);
      return false;
    }
  },
  updateTableNameDesign: async (tableSchemaId, tableSchemaName, datasetId) => {
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/dataschema/${datasetId}/tableSchema`
          : getUrl(DatasetConfig.updateTableDesign, {
              datasetId
            }),
        data: {
          idTableSchema: tableSchemaId,
          nameTableSchema: tableSchemaName
        }
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error updating dataset design name: ${error}`);
      return false;
    }
  },
  validateById: async datasetId => {
    try {
      const response = await HTTPRequester.update({
        url: window.env.REACT_APP_JSON
          ? `/jsons/list-of-errors.json`
          : getUrl(DatasetConfig.validateDataset, {
              datasetId: datasetId
            })
      });

      return response.status >= 200 && response.status <= 299;
    } catch (error) {
      console.error(`Error calling dataset data validation: ${error}`);
      return false;
    }
  },
  webFormDataById: async (datasetId, tableSchemaId) => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_dataset_values2.json'
        : getUrl(DatasetConfig.webFormDataViewer, {
            datasetId: datasetId,
            tableSchemaId: tableSchemaId
          })
    });
    return response.data;
  }
};
