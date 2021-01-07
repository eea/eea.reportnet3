import React from 'react';

import styles from './NationalSystemsRecord.module.scss';

import { NationalSystemsField } from './_components/NationalSystemsField';

export const NationalSystemsRecord = ({ datasetId, index, record }) => {
  return (
    <div className={styles.record} key={index}>
      {record.elements.map((element, i) => {
        const { name, title, titleSource, tooltip, tooltipSource } = element;

        return (
          <NationalSystemsField
            datasetId={datasetId}
            title={titleSource || title}
            key={i}
            tooltip={tooltipSource || tooltip}
            nationalField={name}
          />
        );
      })}
    </div>
  );
};
