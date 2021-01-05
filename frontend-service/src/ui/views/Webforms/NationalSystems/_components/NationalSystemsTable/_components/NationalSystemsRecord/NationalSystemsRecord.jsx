import React from 'react';

import styles from './NationalSystemsRecord.module.scss';

import { NationalSystemsField } from './_components/NationalSystemsField';

export const NationalSystemsRecord = ({ index, record }) => {
  return (
    <div className={styles.record} key={index}>
      {record.elements.map((element, i) => {
        const { name, title, titleSource, tooltip, tooltipSource } = element;

        return (
          <NationalSystemsField title={titleSource || title} key={i} tooltip={tooltipSource || tooltip} field={name} />
        );
      })}
    </div>
  );
};
