import { Fragment, useContext, useEffect, useLayoutEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

import styles from './ManageNationalCoordinators.module.scss';

import { Button } from 'views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { Filters } from 'views/_components/Filters';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';

import { RepresentativeService } from 'services/RepresentativeService';
import { UserRightService } from 'services/UserRightService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { CountryUtils } from 'views/_functions/Utils/CountryUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';

import { useFilters } from 'views/_functions/Hooks/useFilters';

export const ManageNationalCoordinators = ({ onCloseDialog, isDialogVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [errors, setErrors] = useState({
    name: { hasErrors: false, message: '' },
    type: { hasErrors: false, message: '' },
    content: { hasErrors: false, message: '' }
  });

  const [nationalCoordinatorsData, setNationalCoordinatorsData] = useState([]);

  const [isAddDialogVisible, setisAddDialogVisible] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');

  //const { filteredData, isFiltered } = useFilters('manageNationalCoordinators');

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const { data } = await UserRightService.getNationalCoordinators();
      setNationalCoordinatorsData(parseNationalCoordinatorsList(data));
    } catch (error) {
      console.error('NationalCoordinators - fetchData.', error);
      notificationContext.add({ type: 'LOAD_USERS_LIST_ERROR' }, true);
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

  const [groupOfCountries, setGroupOfCountries] = useState([]);

  const getDropdownsOptions = async () => {
    setIsLoading(true);
    const allCountries = { dataProviderGroupId: 2 };

    try {
      const responseGroupOfCountries = await RepresentativeService.getDataProviders(allCountries);
      setGroupOfCountries(responseGroupOfCountries);
    } catch (error) {
      console.error('NationalCoordinators - getDropdownsOptions.', error);
    } finally {
      setIsLoading(false);
    }
  };

  useLayoutEffect(() => {
    getDropdownsOptions();
  }, []);

  const getActionsTemplate = () => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent p-button-animated-blink ${styles.deleteButton}`}
        disabled={loadingStatus === 'pending'}
        icon="trash"
        //onClick={() => onShowDeleteDialog(webformRow)}
      />
    );
  };

  const renderTableColumns = () => {
    const columns = [
      {
        key: 'email',
        header: resourcesContext.messages['manageNationalCoordinatorsDialogColumn']
      },
      {
        key: 'countryName',
        header: resourcesContext.messages['manageRolesDialogDataProviderColumn']
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
        sortable
      />
    ));
  };

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
      <div className={styles.table}>
        <Filters
          className="reportingObligations"
          // onFilter={onLoadReportingObligations}
          // onReset={onLoadReportingObligations}
          //  options={filterOptions}
          recoilId="reportingObligations"
        />

        <DataTable
          autoLayout
          className={styles.dialogContent}
          hasDefaultCurrentPage
          paginator
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={nationalCoordinatorsData.length}
          value={nationalCoordinatorsData}>
          {renderTableColumns()}
        </DataTable>
      </div>
    );
  };

  const onAddDialogClose = () => {
    setisAddDialogVisible(false);
    setErrors({ name: false, type: false, content: false });
  };

  const addDialogFooter = (
    <Fragment>
      <Button
        className="p-button-primary"
        //disabled={getIsDisabledConfirmBtn()}
        icon={loadingStatus === 'pending' ? 'spinnerAnimate' : 'check'}
        label={resourcesContext.messages['save']}
        //onClick={onConfirm}
      />

      <Button
        className="p-button-secondary p-button-animated-blink"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={onAddDialogClose}
      />
    </Fragment>
  );

  const dialogFooter = (
    <div className={styles.buttonsDialogFooter}>
      <Button
        className="p-button-primary"
        icon="plus"
        label={resourcesContext.messages['add']}
        onClick={() => setisAddDialogVisible(true)}
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
        footer={dialogFooter}
        header={resourcesContext.messages['manageNationalCoordinators']}
        modal
        onHide={onCloseDialog}
        style={{ width: '80%', maxWidth: '650px' }}
        visible={isDialogVisible}>
        {renderDialogContent()}
      </Dialog>

      {isAddDialogVisible && (
        <Dialog
          blockScroll={false}
          className={styles.addDialog}
          footer={addDialogFooter}
          header={resourcesContext.messages['addNationalCoordinatorsDialogHeader']}
          modal
          onHide={onAddDialogClose}
          visible={isAddDialogVisible}>
          <label className={styles.label} htmlFor="name">
            {resourcesContext.messages['manageNationalCoordinatorsDialogColumn']}
          </label>
          <InputText
            className={`${styles.nameInput} ${errors.name.hasErrors ? styles.inputError : ''}`}
            id="name"
            maxLength={50}
            //onBlur={() => checkHasErrors('name')}
            //onChange={}
            placeholder={resourcesContext.messages['nationalCoordinatorsEmailColumn']}
            //value={webformConfiguration.name}
          />
          {errors.name.hasErrors && <ErrorMessage className={styles.errorMessage} message={errors.name.message} />}

          <label className={styles.label} htmlFor="rolesDropdown">
            {resourcesContext.messages['countryColumn']}
          </label>
          <Dropdown
            appendTo={document.body}
            ariaLabel="groupOfCountries"
            className={styles.groupOfCountriesWrapper}
            //disabled={!isAdmin || hasRepresentatives}
            name="groupOfCountries"
            //onChange={event => onSelectGroup(event.target.value)}
            //onFocus={() => handleErrors({ field: 'groupOfCountries', hasErrors: false, message: '' })}
            optionLabel="label"
            options={groupOfCountries}
            placeholder={resourcesContext.messages['manageNationalCoordinatorsPlaceholder']}
            /* tooltip={
      isAdmin && hasRepresentatives ? resourcesContext.messages['groupOfCountriesDisabledTooltip'] : ''
    }
    value={selectedGroup}*/
          />
          {errors.type.hasErrors && <ErrorMessage className={styles.errorMessage} message={errors.type.message} />}
        </Dialog>
      )}
    </Fragment>
  );
};
