import { Fragment, useContext, useEffect, useState } from 'react';
import PropTypes from 'prop-types';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import styles from './NationalSystemsTable.module.scss';

import { IconTooltip } from 'views/_components/IconTooltip';
import { NationalSystemsRecord } from './_components/NationalSystemsRecord';
import { Spinner } from 'views/_components/Spinner';

import { DatasetService } from 'services/DatasetService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { NationalSystemsTableUtils } from './_functions/Utils/NationalSystemsTableUtils';

import { TextUtils } from 'repositories/_utils/TextUtils';

export const NationalSystemsTable = ({
  dataProviderId,
  dataflowId,
  datasetId,
  errorMessages,
  schemaTables,
  tables
}) => {
  const { parseData } = NationalSystemsTableUtils;
  const { parseText } = TextUtils;

  const resourcesContext = useContext(ResourcesContext);

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

      const tableData = await DatasetService.getTableData({
        datasetId,
        tableSchemaId: schemaTables?.tableSchemaId,
        pageSize: 100,
        fields: sortFieldSchemaId !== '' ? `${sortFieldSchemaId}:${1}` : undefined,
        levelError: ['CORRECT', 'INFO', 'WARNING', 'ERROR', 'BLOCKER']
      });

      setData(parseData(tableData.records, tables, schemaTables));
      setSchemaData(tableData);
    } catch (error) {
      console.error('NationalSystemsTable - onLoadTableData.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const renderErrors = () => {
    const errors = errorMessages(schemaData, tables.name);

    return (
      <ul>
        {errors.map(error => (
          <li key={uniqueId()}>{error}</li>
        ))}
      </ul>
    );
  };

  const renderMissingTables = tableName => (
    <h4
      dangerouslySetInnerHTML={{ __html: parseText(resourcesContext.messages['tableIsNotCreated'], { tableName }) }}
    />
  );

  const renderRecords = () => {
    if (!isEmpty(errorMessages(schemaData, tables.name))) return renderErrors();

    return data.map(record => (
      <Fragment key={uniqueId()}>
        <NationalSystemsRecord
          dataProviderId={dataProviderId}
          dataflowId={dataflowId}
          datasetId={datasetId}
          getTableErrors={getTableErrors}
          record={record}
        />
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
