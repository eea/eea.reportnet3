import React, { Fragment } from 'react';

import { NationalSystemsField } from './_components/NationalSystemsField';

export const NationalSystemsRecord = ({ index, record }) => {
  return (
    <Fragment>
      Record: {index}
      {record.elements.map((element, i) => {
        const { name, title, titleSource, tooltip, tooltipSource } = element;

        return (
          <NationalSystemsField title={titleSource || title} key={i} tooltip={tooltipSource || tooltip} field={name} />
        );
      })}
    </Fragment>
  );
};
