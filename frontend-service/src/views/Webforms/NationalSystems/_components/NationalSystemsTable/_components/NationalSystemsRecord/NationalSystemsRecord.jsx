import { Fragment } from 'react';
import uniqueId from 'lodash/uniqueId';

import styles from './NationalSystemsRecord.module.scss';

import { NationalSystemsField } from './_components/NationalSystemsField';

export const NationalSystemsRecord = ({ dataProviderId, dataflowId, datasetId, getTableErrors, record }) => (
  <div className={styles.record}>
    {record.elements.map(element => {
      const { name, title, titleSource, tooltipSource } = element;

      return (
        <Fragment key={uniqueId()}>
          <NationalSystemsField
            dataflowId={dataflowId}
            dataProviderId={dataProviderId}
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
