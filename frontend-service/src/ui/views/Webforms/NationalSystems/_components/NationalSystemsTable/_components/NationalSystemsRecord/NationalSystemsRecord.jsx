import React from 'react';

import styles from './NationalSystemsRecord.module.scss';

import { NationalSystemsField } from './_components/NationalSystemsField';

export const NationalSystemsRecord = ({ datasetId, index, record }) => {
  return (
    <div className={styles.record} key={index}>
      {record.elements.map((element, i) => {
        const { name, title, titleSource, tooltipSource } = element;

        return (
          <NationalSystemsField
            datasetId={datasetId}
            key={i}
            nationalField={name}
            title={titleSource || title}
            tooltip={tooltipSource}
          />
        );
      })}
    </div>
  );
};
