import { Fragment } from 'react';

import styles from './NationalSystemsRecord.module.scss';

import { NationalSystemsField } from './_components/NationalSystemsField';

export const NationalSystemsRecord = ({ datasetId, getTableErrors, record }) => (
  <div className={styles.record}>
    {record.elements.map((element, index) => {
      const { name, title, titleSource, tooltipSource } = element;

      return (
        <Fragment key={index}>
          <NationalSystemsField
            datasetId={datasetId}
            getTableErrors={getTableErrors}
            nationalField={name}
            recordValidations={record.validations}
            title={titleSource || title}
            tooltip={tooltipSource}
          />
        </Fragment>
      );
    })}
  </div>
);
