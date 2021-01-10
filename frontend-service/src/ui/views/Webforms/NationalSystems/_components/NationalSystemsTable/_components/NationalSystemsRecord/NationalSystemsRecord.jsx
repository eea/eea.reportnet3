import React, { Fragment } from 'react';

import styles from './NationalSystemsRecord.module.scss';

import { NationalSystemsField } from './_components/NationalSystemsField';

export const NationalSystemsRecord = ({ datasetId, record }) => {
  return (
    <div className={styles.record}>
      {record.elements.map((element, index) => {
        const { name, title, titleSource, tooltipSource } = element;

        return (
          <Fragment key={index}>
            <NationalSystemsField
              datasetId={datasetId}
              // key={i}
              nationalField={name}
              title={titleSource || title}
              tooltip={tooltipSource}
            />
          </Fragment>
        );
      })}
    </div>
  );
};
