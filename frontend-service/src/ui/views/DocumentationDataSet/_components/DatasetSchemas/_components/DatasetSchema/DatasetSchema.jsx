import React, { useEffect, useState } from 'react';

import { isUndefined, isNull, isEmpty } from 'lodash';

import { Spinner } from 'ui/views/_components/Spinner';
import { TreeView } from 'ui/views/_components/TreeView';

import { DatasetService } from 'core/services/DataSet';

const DatasetSchema = ({ datasetId }) => {
  const [designDataset, setDesignDataset] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    onLoadDatasetDesignSchema();
  }, []);

  const onLoadDatasetDesignSchema = async () => {
    try {
      // setIsLoading(true);
      const datasetSchema = await DatasetService.schemaById(datasetId);
      if (!isEmpty(datasetSchema)) {
        setDesignDataset(datasetSchema);
      }
    } catch (error) {
      // if (error.response.status === 401 || error.response.status === 403) {
      //   history.push(getUrl(routes.DATAFLOWS));
      // }
    } finally {
      // setIsLoading(false);
    }
  };

  const renderDatasetSchema = () => {
    // if (isLoading) {
    //   return <Spinner />;
    // } else {
    return !isUndefined(designDataset) && !isNull(designDataset) ? (
      <div>
        <TreeView
          property={parseDesignDataset(designDataset)}
          propertyName={''}
          excludeBottomBorder={false}
          rootProperty={''}
          groupableProperties={['fields']}
        />
      </div>
    ) : null;
    // }
  };

  return renderDatasetSchema();
};

const parseDesignDataset = design => {
  const parsedDataset = {};
  parsedDataset.levelErrorTypes = design.levelErrorTypes;

  if (!isUndefined(design.tables) && !isNull(design.tables) && design.tables.length > 0) {
    const tables = design.tables.map(tableDTO => {
      const table = {};
      table.tableSchemaName = tableDTO.tableSchemaName;
      if (
        !isUndefined(tableDTO.records[0].fields) &&
        !isNull(tableDTO.records[0].fields) &&
        tableDTO.records[0].fields.length > 0
      ) {
        const fields = tableDTO.records[0].fields.map(fieldDTO => {
          return { name: fieldDTO.name, type: fieldDTO.type };
        });
        table.fields = fields;
      }

      return table;
    });

    parsedDataset.tables = tables;
  }

  const dataset = {};
  dataset[design.datasetSchemaName] = parsedDataset;
  return dataset;
};

export { DatasetSchema };
