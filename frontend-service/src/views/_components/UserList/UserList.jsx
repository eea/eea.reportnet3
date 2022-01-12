import { Fragment, useContext, useEffect, useState } from 'react';
import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './UserList.module.scss';

import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { MyFilters } from 'views/_components/MyFilters';
import { Spinner } from 'views/_components/Spinner';

import { DataflowService } from 'services/DataflowService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { filterByState } from 'views/_components/MyFilters/_functions/Stores/filtersStores';

import { TextByDataflowTypeUtils } from 'views/_functions/Utils/TextByDataflowTypeUtils';

export const UserList = ({ dataflowId, dataflowType, representativeId }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const filterBy = useRecoilValue(filterByState('userList'));

  const [isDataFiltered, setIsDataFiltered] = useState(false);
  const [userListData, setUserListData] = useState([]);
  const [filteredData, setFilteredData] = useState(userListData);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    const isDataFiltered = existFilterByWithData(filterBy);
    setIsDataFiltered(isDataFiltered);
    if (!isDataFiltered) {
      setFilteredData(userListData);
    }
  }, [filterBy]);

  const existFilterByWithData = filterBy => {
    return Object.values(filterBy).filter(item => !isEmpty(item)).length > 0;
  };

  const fetchData = async () => {
    try {
      let userData;
      setIsLoading(true);
      if (isNil(representativeId) && isNil(dataflowId)) {
        userData = await DataflowService.getAllDataflowsUserList();
      } else if (isNil(representativeId) && !isNil(dataflowId)) {
        userData = await DataflowService.getRepresentativesUsersList(dataflowId);
      } else {
        userData = await DataflowService.getUserList(dataflowId, representativeId);
      }
      setUserListData(userData);
      setFilteredData(userData);
    } catch (error) {
      console.error('UserList - fetchData.', error);
      notificationContext.add({ type: 'LOAD_USERS_LIST_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const getFilters = filterOptions => {
    return (
      <MyFilters
        className="userList"
        data={userListData}
        getFilteredData={onLoadFilteredData}
        options={filterOptions}
        viewType="userList"
      />
    );
  };

  const getPaginatorRecordsCount = () => (
    <Fragment>
      {isDataFiltered && userListData.length !== filteredData.length
        ? `${resourcesContext.messages['filtered']} : ${filteredData.length} | `
        : ''}
      {resourcesContext.messages['totalRecords']} {userListData.length}{' '}
      {resourcesContext.messages['records'].toLowerCase()}
      {isDataFiltered && userListData.length === filteredData.length
        ? ` (${resourcesContext.messages['filtered'].toLowerCase()})`
        : ''}
    </Fragment>
  );

  const onLoadFilteredData = value => setFilteredData(value);

  // const filterOptionsWithDataflowIdRepresentativeId = [
  //   { type: 'multiselect', properties: [{ name: 'role' }] },
  //   { type: 'input', properties: [{ name: 'email' }] },
  //   {
  //     type: 'multiselect',
  //     properties: [
  //       {
  //         name: 'dataProviderName',
  //         showInput: true,
  //         label: TextByDataflowTypeUtils.getLabelByDataflowType(
  //           resourcesContext.messages,
  //           dataflowType,
  //           'userListDataProviderFilterLabel'
  //         )
  //       }
  //     ]
  //   }
  // ];

  // const filterOptionsNoRepresentative = [
  //   { type: 'input', properties: [{ name: 'dataflowName' }] },
  //   { type: 'multiselect', properties: [{ name: 'role' }] },
  //   { type: 'input', properties: [{ name: 'email' }] }
  // ];

  // const filterOptionsHasRepresentativeId = [
  //   { type: 'multiselect', properties: [{ name: 'role' }] },
  //   { type: 'input', properties: [{ name: 'email' }] }
  // ];

  const filterOptionsWithDataflowIdRepresentativeId = [
    {
      type: 'MULTI_SELECT',
      nestedOptions: [
        { key: 'role', label: resourcesContext.messages['role'] },
        {
          key: 'dataProviderName',
          showInput: true,
          label: TextByDataflowTypeUtils.getLabelByDataflowType(
            resourcesContext.messages,
            dataflowType,
            'userListDataProviderFilterLabel'
          )
        }
      ]
    },
    { type: 'INPUT', key: 'email', label: resourcesContext.messages['email'] }
  ];

  const filterOptionsNoRepresentative = [
    {
      type: 'INPUT',
      nestedOptions: [
        { key: 'dataflowName', label: resourcesContext.messages['dataflowName'] },
        { key: 'email', label: resourcesContext.messages['email'] }
      ]
    },
    { type: 'MULTI_SELECT', nestedOptions: [{ key: 'role', label: resourcesContext.messages['role'] }] }
  ];

  const filterOptionsHasRepresentativeId = [
    { type: 'MULTI_SELECT', nestedOptions: [{ key: 'role', label: resourcesContext.messages['role'] }] },
    { type: 'INPUT', key: 'email', label: resourcesContext.messages['email'] }
  ];

  const renderFilters = () => {
    if (isNil(representativeId) && isNil(dataflowId)) {
      return getFilters(filterOptionsNoRepresentative);
    } else if (isNil(representativeId) && !isNil(dataflowId)) {
      return getFilters(filterOptionsWithDataflowIdRepresentativeId);
    } else {
      return getFilters(filterOptionsHasRepresentativeId);
    }
  };

  const renderUsersListContent = () => {
    if (isLoading) {
      return <Spinner />;
    }

    if (isEmpty(userListData)) {
      return <div className={styles.noUsers}>{resourcesContext.messages['noUsers']}</div>;
    }

    return (
      <div className={styles.users}>
        {renderFilters()}
        {renderUsersListTable()}
      </div>
    );
  };

  const renderUsersListTable = () => {
    if (isEmpty(filteredData)) {
      return (
        <div className={styles.emptyFilteredData}>{resourcesContext.messages['noUsersWithSelectedParameters']}</div>
      );
    } else {
      return (
        <DataTable
          paginator={true}
          paginatorRight={!isNil(filteredData) && getPaginatorRecordsCount()}
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          summary="usersList"
          totalRecords={userListData.length}
          value={filteredData}>
          {isNil(representativeId) && isNil(dataflowId) && (
            <Column field="dataflowName" header={resourcesContext.messages['dataflowName']} sortable={true} />
          )}
          <Column field="role" header={resourcesContext.messages['role']} sortable={true} />
          <Column field="email" header={resourcesContext.messages['user']} sortable={true} />
          {isNil(representativeId) && !isNil(dataflowId) && (
            <Column
              field="dataProviderName"
              header={TextByDataflowTypeUtils.getLabelByDataflowType(
                resourcesContext.messages,
                dataflowType,
                'userListDataProviderColumnHeader'
              )}
              sortable={true}
            />
          )}
        </DataTable>
      );
    }
  };

  return <div className={styles.container}>{renderUsersListContent()} </div>;
};
