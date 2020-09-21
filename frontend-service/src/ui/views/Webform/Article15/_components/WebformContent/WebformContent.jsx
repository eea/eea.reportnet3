import React, { Fragment, useContext, useEffect, useReducer, useRef, useState } from 'react';
import PropTypes from 'prop-types';

import styles from './WebformContent.module.scss';

import { Button } from 'ui/views/_components/Button';
import { WebformRecord } from './_components/WebformRecord';

import { DatasetService } from 'core/services/Dataset';

export const WebformContent = ({ datasetId, webform }) => {
  const [webformData, setWebformData] = useState(webform);
  console.log('webformData', webformData);

  useEffect(() => {
    onLoadTableData();
  }, []);

  const onAddMultipleWebform = () => {};

  const onLoadTableData = async () => {
    try {
      const tableData = await DatasetService.tableDataById(datasetId, webform.tableSchemaId, '', '', undefined, [
        'CORRECT',
        'INFO',
        'WARNING',
        'ERROR',
        'BLOCKER'
      ]);

      console.log('tableData', tableData);

      // const result = [];

      // for (let i = 0; i < webformData.records.length; i++) {
      //   const tableFields = tableData.records.find(
      //     element => element['recordSchemaId'] === webformData.records[i]['recordSchemaId']
      //   ).fields;

      //   const webformFields = webformData.records[i].fields;

      //   result.push({
      //     ...webformData.records[i],
      //     ...tableData.records.find(element => element['recordSchemaId'] === webformData.records[i]['recordSchemaId']),
      //     allFields: tableFields.map(element => {
      //       const field = Object.assign({}, element);
      //       field['name'] = webformFields.filter(wffield => wffield.fieldId === field.fieldSchemaId)[0].name;
      //       // TODO: Continue adding parameters
      //       return field;
      //     })
      //   });
      // }

      // // console.log('result', result);
      // return result;
    } catch (error) {
      console.log('error', error);
    }
  };

  const renderWebformRecords = () => {
    return webform.webformRecords.map((record, i) => (
      <WebformRecord key={i} record={record} datasetId={datasetId} tableId={webform.tableSchemaId} />
    ));
  };

  return (
    <div className={styles.body}>
      <h3 className={styles.title}>
        {webform.webformTitle}
        {webform.multipleRecords ? (
          <Button label={'Add'} icon={'plus'} onClick={() => onAddMultipleWebform()} />
        ) : (
          <Fragment />
        )}
      </h3>
      {webform.description ? <h3 className={styles.description}>{webform.description}</h3> : <Fragment />}
      {renderWebformRecords()}

      {/* {webform.multiple
        ? webformState.multipleView.map(element => renderContent(webform.webformFields, webform.multiple, element.id))
        : renderContent(webform.webformFields, webform.multiple)} */}
    </div>
  );
};

WebformContent.propTypes = { webform: PropTypes.object };

WebformContent.defaultProps = { webform: {} };
