import { Fragment, useContext, useEffect, useState } from 'react';
import { useRecoilValue } from 'recoil';

import isEmpty from 'lodash/isEmpty';
import uniqBy from 'lodash/uniqBy';

import styles from './ManageNationalCoordinators.module.scss';

import { AddNationalCoordinator } from './_components/AddNationalCoordinator';
import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Filters } from 'views/_components/Filters';
import { Spinner } from 'views/_components/Spinner';

import { UserRightService } from 'services/UserRightService';

import { filteredDataStore } from 'views/_components/Filters/_functions/Stores/filterStore';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { CountryUtils } from 'views/_functions/Utils/CountryUtils';
import { PaginatorRecordsCount } from 'views/_components/DataTable/_functions/Utils/PaginatorRecordsCount';

import { useApplyFilters } from 'views/_functions/Hooks/useApplyFilters';

export const ManageNationalCoordinators = ({ onCloseDialog, isDialogVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [deleteNationalCoordinator, setDeleteNationalCoordinator] = useState({});
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [nationalCoordinatorsData, setNationalCoordinatorsData] = useState([]);

  const filteredData = useRecoilValue(filteredDataStore('manageNationalCoordinators'));
  const { isFiltered, setData } = useApplyFilters('manageNationalCoordinators');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const { data } = await UserRightService.getNationalCoordinators();
      setNationalCoordinatorsData(parseNationalCoordinatorsList(data));
      setData(parseNationalCoordinatorsList(data));
    } catch (error) {
      console.error('NationalCoordinators - fetchData.', error);
      notificationContext.add({ type: 'LOAD_NATIONAL_COORDINATORS_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const parseNationalCoordinatorsList = nationalCoordinatorsList => {
    const nationalCoordinators = [];

    if (!isEmpty(nationalCoordinatorsList)) {
      nationalCoordinatorsList.forEach(nationalCoordinator => {
        const countryName = CountryUtils.getCountryName(nationalCoordinator.countryCode);
        nationalCoordinator.countryName = countryName;
        nationalCoordinators.push(nationalCoordinator);
      });
    }

    return nationalCoordinators;
  };

  const deleteNationalCoordinators = async () => {
    try {
      await UserRightService.deleteNationalCoordinator(deleteNationalCoordinator);
    } catch (error) {
      console.error('NationalCoordinators - deleteNationalCoordinator.', error);
      notificationContext.add({ type: 'DELETE_NATIONAL_COORDINATORS_ERROR' }, true);
    } finally {
      setIsDeleteDialogVisible(false);
      setDeleteNationalCoordinator({});
    }
  };

  const onDeleteDialogClose = () => setIsDeleteDialogVisible(false);

  const getActionsTemplate = () => (
    <Button
      className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteButton}`}
      icon="trash"
      onClick={() => setIsDeleteDialogVisible(true)}
    />
  );

  const checkDuplicateNationalCoordinator = nationalCoordinator =>
    nationalCoordinatorsData.some(
      user => user.countryCode === nationalCoordinator.countryCode && user.email === nationalCoordinator.email
    );

  const renderTableColumns = () => {
    const columns = [
      {
        key: 'email',
        header: resourcesContext.messages['account']
      },
      {
        key: 'countryName',
        header: resourcesContext.messages['countries']
      },
      {
        key: 'actions',
        header: resourcesContext.messages['actions'],
        template: getActionsTemplate
      }
    ];

    return columns.map(column => (
      <Column
        body={column.template}
        columnResizeMode="expand"
        field={column.key}
        header={column.header}
        key={column.key}
        sortable={column.key !== 'actions'}
      />
    ));
  };

  const parseMultiSelectOptions = () =>
    uniqBy(nationalCoordinatorsData, 'countryName').map(option => ({
      type: option.countryName,
      value: option.countryCode
    }));

  const filterOptions = [
    {
      key: 'email',
      label: resourcesContext.messages['account'],
      type: 'INPUT'
    },
    {
      key: 'countryCode',
      label: resourcesContext.messages['countries'],
      multiSelectOptions: parseMultiSelectOptions(),
      type: 'MULTI_SELECT'
    }
  ];

  const renderDialogContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataContent}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    if (isEmpty(nationalCoordinatorsData)) {
      return (
        <div className={styles.noDataContent}>
          <span>{resourcesContext.messages['noData']}</span>
        </div>
      );
    }

    return (
      <Fragment>
        <Filters className="lineItems" options={filterOptions} recoilId="manageNationalCoordinators" />

        <DataTable
          autoLayout
          className={styles.dialogContent}
          hasDefaultCurrentPage
          onRowClick={event => setDeleteNationalCoordinator(event.data)}
          paginator
          paginatorRight={
            <PaginatorRecordsCount
              dataLength={nationalCoordinatorsData.length}
              filteredDataLength={filteredData.length}
              isFiltered={isFiltered}
            />
          }
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={nationalCoordinatorsData.length}
          value={filteredData}>
          {renderTableColumns()}
        </DataTable>
      </Fragment>
    );
  };

  const dialogFooter = (
    <div className={styles.buttonsDialogFooter}>
      <AddNationalCoordinator
        checkDuplicateNationalCoordinator={checkDuplicateNationalCoordinator}
        onConfirmAddition={onCloseDialog}
      />

      <Button
        className="p-button-secondary p-button-animated-blink p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </div>
  );

  return (
    <Fragment>
      <Dialog
        blockScroll={false}
        className={styles.dialog}
        footer={dialogFooter}
        header={resourcesContext.messages['manageNationalCoordinators']}
        modal
        onHide={onCloseDialog}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isDeleteDialogVisible && (
        <ConfirmDialog
          classNameConfirm="p-button-danger"
          header={resourcesContext.messages['deleteNationalCoordinatorsHeader']}
          iconConfirm={'check'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={() => {
            deleteNationalCoordinators();
            onCloseDialog();
          }}
          onHide={onDeleteDialogClose}
          visible={isDeleteDialogVisible}>
          {resourcesContext.messages['deleteNationalCoordinatorsConfirm']}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
