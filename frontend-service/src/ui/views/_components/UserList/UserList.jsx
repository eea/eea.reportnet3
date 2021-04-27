import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './UserList.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
import { Filters } from 'ui/views/_components/Filters';
import { Spinner } from 'ui/views/_components/Spinner';

import { DataflowService } from 'core/services/Dataflow';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const UserList = ({ dataflowId, representativeId }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [userListData, setUserListData] = useState([]);
  const [filteredData, setFilteredData] = useState(userListData);
  const [isDataFiltered, setIsDataFiltered] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    if (!isDataFiltered) {
      setFilteredData(userListData);
    }
  }, [isDataFiltered]);

  const fetchData = async () => {
    try {
      let response;
      setIsLoading(true);
      if (isNil(representativeId)) {
        response = await DataflowService.getAllDataflowsUserList();
      } else if (isNil(representativeId) && !isNil(dataflowId)) {
        response = await DataflowService.getRepresentativesUsersList(dataflowId);
      } else {
        response = await DataflowService.getUserList(dataflowId, representativeId);
      }

      setUserListData(response.data);
      setFilteredData(response.data);
    } catch (error) {
      notificationContext.add({ type: 'LOAD_USERS_LIST_ERROR' });
    } finally {
      setIsLoading(false);
    }
  };

  const getFilteredState = value => setIsDataFiltered(value);

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isDataFiltered && userListData.length !== filteredData.length
        ? `${resources.messages['filtered']} : ${filteredData.length} | `
        : ''}
      {resources.messages['totalRecords']} {userListData.length} {resources.messages['records'].toLowerCase()}
      {isDataFiltered && userListData.length === filteredData.length
        ? ` (${resources.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const onLoadFilteredData = value => setFilteredData(value);

  const filterOptionsNoRepresentative = [
    { type: 'input', properties: [{ name: 'dataflowName' }] },
    { type: 'multiselect', properties: [{ name: 'role' }] },
    { type: 'input', properties: [{ name: 'email' }] }
  ];

  const filterOptionsHasRepresentativeId = [
    { type: 'multiselect', properties: [{ name: 'role' }] },
    { type: 'input', properties: [{ name: 'email' }] }
  ];

  return (
    <div className={styles.container}>
      {isLoading ? (
        <Spinner />
      ) : isEmpty(userListData) ? (
        <div className={styles.noUsers}>{resources.messages['noUsers']}</div>
      ) : (
        <div className={styles.users}>
          {isNil(representativeId) ? (
            <Filters
              data={userListData}
              getFilteredData={onLoadFilteredData}
              getFilteredSearched={getFilteredState}
              options={filterOptionsNoRepresentative}
            />
          ) : (
            <Filters
              data={userListData}
              getFilteredData={onLoadFilteredData}
              getFilteredSearched={getFilteredState}
              options={filterOptionsHasRepresentativeId}
            />
          )}

          {!isEmpty(filteredData) ? (
            <DataTable
              value={filteredData}
              paginatorRight={!isNil(filteredData) && getPaginatorRecordsCount()}
              paginator={true}
              rows={10}
              rowsPerPageOptions={[5, 10, 15]}
              totalRecords={userListData.length}>
              {isNil(representativeId) && (
                <Column field="dataflowName" header={resources.messages['dataflowName']} sortable={true} />
              )}
              <Column field="role" header={resources.messages['role']} sortable={true} />
              <Column field="email" header={resources.messages['user']} sortable={true} />
            </DataTable>
          ) : (
            <div className={styles.emptyFilteredData}>{resources.messages['noUsersWithSelectedParameters']}</div>
          )}
        </div>
      )}
    </div>
  );
};
