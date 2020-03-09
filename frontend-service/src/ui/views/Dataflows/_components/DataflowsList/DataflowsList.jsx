import React from 'react';

import styles from './DataflowsList.module.scss';

import { config } from 'conf';

import { DataflowsItem } from './_components/DataflowsItem';

import { DataflowsUtils } from './_functions/Utils/DataflowsUtils';

const DataflowsList = ({
  className,
  content,
  dataFetch,
  dataflowNewValues,
  description,
  selectedDataflowId,
  title,
  type,
  userContextRoles
}) => {
  const dataflows = [];
  const userRoles = [];

  const dataflowsRoles = userContextRoles.filter(role => role.includes(config.permissions['DATAFLOW']));

  dataflowsRoles.map((item, i) => {
    const role = DataflowsUtils.reduceString(item, `${item.replace(/\D/g, '')}-`);
    return (userRoles[i] = { id: parseInt(item.replace(/\D/g, '')), userRole: role });
  });

  for (let i = 0; i < content.length; i++) {
    const isDuplicated = DataflowsUtils.isDuplicatedInObject(userRoles, 'id');
    dataflows.push({
      ...content[i],
      ...(isDuplicated
        ? userRoles.filter(item =>
            item.duplicatedRoles
              ? item.userRole === config.permissions['CUSTODIAN'] && delete item.duplicatedRoles
              : item
          )
        : userRoles
      ).find(item => item.id === content[i].id)
    });
  }

  return (
    <div className={`${styles.wrap} ${className}`}>
      <h2>{title}</h2>
      <p>{description}</p>
      {dataflows.map(item => (
        <DataflowsItem
          dataFetch={dataFetch}
          dataflowNewValues={dataflowNewValues}
          itemContent={item}
          key={item.id}
          selectedDataflowId={selectedDataflowId}
          type={type}
        />
      ))}
    </div>
  );
};

export { DataflowsList };
