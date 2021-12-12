import React, { useContext } from 'react';

import isEmpty from 'lodash/isEmpty';

import { DataflowsItem } from '../DataflowsList/_components/DataflowsItem';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

export const List = ({ dataflows, isAdmin, isCustodian, isLoading }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  if (isLoading) return <div style={{ margin: '5rem' }}>LOADING</div>;

  if (isEmpty(dataflows)) return <div style={{ margin: '5rem' }}>There are no filters with this parameters</div>;

  return (
    <div>
      {dataflows.map(dataflow => (
        <DataflowsItem isAdmin={isAdmin} isCustodian={isCustodian} itemContent={dataflow} key={dataflow.id} />
      ))}
    </div>
  );
};
