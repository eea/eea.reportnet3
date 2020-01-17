import React, { useContext, useEffect, useState } from 'react';

import { isUndefined, isNull } from 'lodash';

import styles from './DatasetSchemas.module.css';

import { Button } from 'ui/views/_components/Button';
import { DatasetSchema } from './_components/DatasetSchema';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { Toolbar } from 'ui/views/_components/Toolbar';

import { CodelistService } from 'core/services/Codelist';

const DatasetSchemas = ({ datasetsSchemas, isCustodian, onLoadDatasetsSchemas }) => {
  const resources = useContext(ResourcesContext);

  const [isLoading, setIsLoading] = useState(false);
  const [codelistsList, setCodelistsList] = useState();

  useEffect(async () => {
    setCodelistsList(await CodelistService.getCodelistsList(datasetsSchemas));
  }, []);

  const getCodelistsList = async () => {
    const codelistsList = await getCodelistsList(datasetsSchemas);
    return codelistsList;
  };

  const renderDatasetSchemas = () => {
    return !isUndefined(datasetsSchemas) && !isNull(datasetsSchemas) && datasetsSchemas.length > 0 ? (
      datasetsSchemas.map((designDataset, i) => {
        return <DatasetSchema designDataset={designDataset} index={i} key={i} />;
      })
    ) : (
      <h3>{`${resources.messages['noDesignSchemasCreated']}`}</h3>
    );
  };

  const renderToolbar = () => {
    return isCustodian ? (
      <Toolbar>
        <div className="p-toolbar-group-right">
          <Button
            className={`p-button-rounded p-button-secondary`}
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
