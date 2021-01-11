import React, { Fragment, useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './NationalSystemsTable.module.scss';

import { NationalSystemsRecord } from './_components/NationalSystemsRecord';
import { Spinner } from 'ui/views/_components/Spinner';

import { DatasetService } from 'core/services/Dataset';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { NationalSystemsTableUtils } from './_functions/Utils/NationalSystemsTableUtils';
import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

export const NationalSystemsTable = ({ datasetId, errorMessages, schemaTables, tables }) => {
  const { parseData } = NationalSystemsTableUtils;
  const { parseText } = TextUtils;

  const resources = useContext(ResourcesContext);

  const [data, setData] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [schemaData, setSchemaData] = useState({});

  useEffect(() => {
    onLoadTableData();
  }, []);

  const onLoadTableData = async () => {
    try {
      const response = await DatasetService.tableDataById(datasetId, schemaTables?.tableSchemaId, '', 100, undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      setData(parseData(response.records, tables, schemaTables));
      setSchemaData(response);
    } catch (error) {
      console.error('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const renderErrors = () => {
    const errors = errorMessages(schemaData, tables.name);

    return (
      <ul>
        {errors.map((error, index) => (
          <li key={index}>{error}</li>
        ))}
      </ul>
    );
  };

  const renderRecords = () => {
    if (!isEmpty(errorMessages(schemaData, tables.name))) return renderErrors();

    return data.map((record, index) => (
      <Fragment key={index}>
        <NationalSystemsRecord datasetId={datasetId} record={record} />
      </Fragment>
    ));
  };

  const renderMissingTables = tableName => (
    <h4 dangerouslySetInnerHTML={{ __html: parseText(resources.messages['tableIsNotCreated'], { tableName }) }} />
  );

  if (isLoading) return <Spinner style={{ top: 0 }} />;

  return (
    <div className={styles.content}>
      {isNil(schemaTables) ? (
        renderMissingTables(tables.name)
      ) : (
        <Fragment>
          <h2>{tables.title}</h2>

          {renderRecords()}
        </Fragment>
      )}
    </div>
  );
};

NationalSystemsTable.propTypes = { tables: PropTypes.shape({ name: PropTypes.string, title: PropTypes.string }) };

NationalSystemsTable.defaultProps = { tables: { name: '', title: '' } };
