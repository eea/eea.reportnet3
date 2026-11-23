import { Fragment } from 'react';
import uniqueId from 'lodash/uniqueId';

import styles from './QuestionAnswerWebformRecord.module.scss';

import { QuestionAnswerWebformField } from './_components/QuestionAnswerWebformField';

export const QuestionAnswerWebformRecord = ({
  bigData,
  dataProviderId,
  dataflowId,
  datasetId,
  getTableErrors,
  preparationCode,
  record,
  tableSchemaId,
  tableSchemaName,
  updatingFieldStore
}) => (
  <div className={styles.record}>
    {record.elements.map(element => {
      const { name, title, titleSource, tooltipSource } = element;

      return (
        <Fragment key={uniqueId()}>
          <QuestionAnswerWebformField
            bigData={bigData}
            dataflowId={dataflowId}
            dataProviderId={dataProviderId}
            datasetId={datasetId}
            getTableErrors={getTableErrors}
            nationalField={name}
            preparationCode={preparationCode}
            recordId={record.recordId}
            recordValidations={record.validations}
            tableSchemaId={tableSchemaId}
            tableSchemaName={tableSchemaName}
            title={titleSource || title}
            tooltip={tooltipSource}
            updatingFieldStore={updatingFieldStore}
          />
        </Fragment>
      );
    })}
  </div>
);
