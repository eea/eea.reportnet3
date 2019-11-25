import React, { useEffect, useState } from 'react';

import { isUndefined, isNull, isEmpty } from 'lodash';

import { DatasetSchema } from './_components/DatasetSchema';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/DataFlow';

const DatasetSchemas = ({ dataflowId }) => {
  const [designDatasets, setDesignDatasets] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    onLoadDesignDatasets();
  }, []);

  const onLoadDesignDatasets = async () => {
    try {
      setIsLoading(true);
      const dataflow = await DataflowService.reporting(dataflowId);
      if (!isEmpty(dataflow.designDatasets)) {
        setDesignDatasets(dataflow.designDatasets);
      }
    } catch (error) {
      // if (error.response.status === 401 || error.response.status === 403) {
      //   history.push(getUrl(routes.DATAFLOWS));
      // }
    } finally {
      setIsLoading(false);
    }
  };

  const renderDatasetSchemas = () => {
    if (isLoading) {
      return <Spinner style={{ top: '5px' }} />;
    } else {
      return !isUndefined(designDatasets) && !isNull(designDatasets)
        ? designDatasets.map(designDataset => {
            return <DatasetSchema datasetId={designDataset.datasetId} />;
          })
        : null;
    }
  };

  return renderDatasetSchemas();
};

export { DatasetSchemas };
