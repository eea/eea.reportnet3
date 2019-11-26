import React, { useContext, useState } from 'react';

import { isUndefined, isNull } from 'lodash';

import styles from './DatasetSchemas.module.css';

import { Button } from 'ui/views/_components/Button';
import { DatasetSchema } from './_components/DatasetSchema';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

const DatasetSchemas = ({ designDatasets, onLoadDesignDatasets }) => {
  const resources = useContext(ResourcesContext);

  const [isLoading, setIsLoading] = useState(false);

  const renderDatasetSchemas = () => {
    return !isUndefined(designDatasets) && !isNull(designDatasets)
      ? designDatasets.map((designDataset, i) => {
          return <DatasetSchema designDataset={designDataset} index={i} key={i} />;
        })
      : null;
  };

  const renderToolbar = () => {
    return (
      <Toolbar>
        <div className="p-toolbar-group-right">
          <Button
            className={`p-button-rounded p-button-secondary`}
            disabled={false}
            icon={'refresh'}
            label={resources.messages['refresh']}
            onClick={async () => {
              setIsLoading(true);
              await onLoadDesignDatasets();
              setIsLoading(false);
            }}
          />
        </div>
      </Toolbar>
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
