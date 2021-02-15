import React, { Fragment, useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './NationalSystemsTable.module.scss';

import { IconTooltip } from 'ui/views/_components/IconTooltip';
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
  const [hasErrors, setHasErrors] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [schemaData, setSchemaData] = useState({});

  useEffect(() => {
    onLoadTableData();
  }, []);

  const getFieldSchemaColumnIdByHeader = (records, fieldName) => {
    if (!isEmpty(records)) {
      const filtered = records[0].fields.filter(field => TextUtils.areEquals(field.name, fieldName));

      return !isNil(filtered) && !isEmpty(filtered) ? filtered[0].fieldId : '';
    }
  };

  const getTableErrors = errors => setHasErrors(errors);

  const onLoadTableData = async () => {
    try {
      const sortFieldSchemaId = !isNil(tables.sortBy)
        ? getFieldSchemaColumnIdByHeader(schemaTables.records, tables.sortBy)
        : undefined;

      const response = await DatasetService.tableDataById(
        datasetId,
        schemaTables?.tableSchemaId,
        '',
        100,
        sortFieldSchemaId !== '' ? `${sortFieldSchemaId}:${1}` : undefined,
        ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
      );

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

  const renderMissingTables = tableName => (
    <h4 dangerouslySetInnerHTML={{ __html: parseText(resources.messages['tableIsNotCreated'], { tableName }) }} />
  );

  const renderRecords = () => {
    if (!isEmpty(errorMessages(schemaData, tables.name))) return renderErrors();

    return data.map((record, index) => (
      <Fragment key={index}>
        <NationalSystemsRecord datasetId={datasetId} record={record} getTableErrors={getTableErrors} />
      </Fragment>
    ));
  };

  return (
    <div className={styles.content}>
      {isNil(schemaTables) ? (
        renderMissingTables(tables.name)
      ) : (
        <Fragment>
          <div className={styles.titleWrapper}>
            <h2>{tables.title}</h2>
            {hasErrors && <IconTooltip className={`webform-validationErrors `} levelError={'ERROR'} />}
          </div>

          {isLoading ? <Spinner style={{ top: 0 }} /> : renderRecords()}
        </Fragment>
      )}
    </div>
  );
};

NationalSystemsTable.propTypes = { tables: PropTypes.shape({ name: PropTypes.string, title: PropTypes.string }) };

NationalSystemsTable.defaultProps = { tables: { name: '', title: '' } };
