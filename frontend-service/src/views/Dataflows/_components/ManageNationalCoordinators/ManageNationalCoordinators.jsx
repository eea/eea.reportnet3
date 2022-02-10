import { Fragment, useContext, useLayoutEffect, useState, useReducer } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './ManageNationalCoordinators.module.scss';

import { Button } from 'views/_components/Button';
import { ActionsColumn } from 'views/_components/ActionsColumn';
import { Column } from 'primereact/column';
import { DataTable } from 'views/_components/DataTable';
import { Dropdown } from 'views/_components/Dropdown';
import { ErrorMessage } from 'views/_components/ErrorMessage';
import { InputText } from 'views/_components/InputText';
import { Dialog } from 'views/_components/Dialog';
import { Spinner } from 'views/_components/Spinner';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { RepresentativeService } from 'services/RepresentativeService';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { TextUtils } from 'repositories/_utils/TextUtils';

import { useFilters } from 'views/_functions/Hooks/useFilters';

//Ir preparando un botón para el admin en el left bar y un modal con un listado y botones para borrar y añadir

export const ManageNationalCoordinators = ({ onCloseDialog, isDialogVisible }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [errors, setErrors] = useState({
    name: { hasErrors: false, message: '' },
    type: { hasErrors: false, message: '' },
    content: { hasErrors: false, message: '' }
  });

  const [isAddEditDialogVisible, setIsAddEditDialogVisible] = useState(false);
  const [isDeleteDialogVisible, setIsDeleteDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [loadingStatus, setLoadingStatus] = useState('idle');

  //const { filteredData, isFiltered } = useFilters('manageNationalCoordinators');

  const filteredData = [
    {
      account: 'sandra.custodian@reportnet.net',
      filteredRole: 'CUSTODIAN',
      id: '10044',
      isNew: false,
      isValid: true,
      role: 'DATA_CUSTODIAN'
    },
    {
      account: 'sandra.provider@reportnet.net',
      filteredRole: 'STEWARD',
      id: '10045',
      isNew: false,
      isValid: true,
      role: 'DATA_STEWARD'
    }
  ];

  const [groupOfCountries, setGroupOfCountries] = useState([]);

  const getDropdownsOptions = async () => {
    setIsLoading(true);
    try {
      const responseGroupOfCountries = await RepresentativeService.getGroupCountries();
      setGroupOfCountries(responseGroupOfCountries.data);
    } catch (error) {
      console.error('NationalCoordinator - getDropdownsOptions.', error);
    } finally {
      setIsLoading(false);
    }
  };

  useLayoutEffect(() => {
    getDropdownsOptions();
  }, []);

  const renderNationalCoordinatorDropdown = () => {
    /*if (TextUtils.areEquals(dataflowType, config.dataflowType.BUSINESS.value)) {
      return (
        <Dropdown
          ariaLabel="dataProviders"
          className={styles.dataProvidersDropdown}
          disabled
          name="dataProvidersDropdown"
          optionLabel="label"
          options={[formState.selectedDataProviderGroup]}
          value={formState.selectedDataProviderGroup}
        />
      );
    }*/

    return (
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
    );
  };

  const renderRepresentingColumn = userRight => {
    return renderNationalCoordinatorDropdown();
  };

  const renderNationalCoordinatorColumn = userRight => {
    return <p>National coordinator</p>;
  };

  const getManageNationalCoordinatorsColumns = () => {
    const columns = [
      {
        key: 'representing',
        header: resourcesContext.messages['manageRolesDialogDataProviderColumn'],
        template: renderRepresentingColumn
      },

      {
        key: 'nationalCoordinator',
        header: resourcesContext.messages['manageNationalCoordinatorsDialogColumn'],
        template: renderNationalCoordinatorColumn
      }
    ];

    return columns.map(column => (
      <Column body={column.template} field={column.key} header={column.header} key={column.key} sortable />
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

    if (isEmpty(filteredData)) {
      return (
        <div className={styles.noDataContent}>
          <span>{resourcesContext.messages['noData']}</span>
        </div>
      );
    }

    return (
      <div className={styles.table}>
        <DataTable
          autoLayout
          className={styles.dialogContent}
          hasDefaultCurrentPage
          paginator
          rows={10}
          rowsPerPageOptions={[5, 10, 15]}
          totalRecords={filteredData.length}
          value={filteredData}>
          {getManageNationalCoordinatorsColumns()}
        </DataTable>
      </div>
    );
  };

  const onAddDialogClose = () => {
    setIsAddEditDialogVisible(false);
    setErrors({ name: false, type: false, content: false });
  };

  const addDialogFooter = (
    <Fragment>
      <span data-for="confirmBtn" data-tip>
        <Button
          className="p-button-primary"
          //disabled={getIsDisabledConfirmBtn()}
          icon={loadingStatus === 'pending' ? 'spinnerAnimate' : 'check'}
          // label={ }
          //onClick={onConfirm}
        />
      </span>

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
        onClick={() => setIsAddEditDialogVisible(true)}
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
        visible={isDialogVisible}
        zIndex={3003}>
        {renderDialogContent()}
      </Dialog>

      {
        <Dialog
          blockScroll={false}
          className={`responsiveDialog ${styles.addEditDialog}`}
          footer={addDialogFooter}
          header={resourcesContext.messages['addWebformConfigurationDialogHeader']}
          modal
          onHide={onAddDialogClose}
          visible={isAddEditDialogVisible}>
          <InputText
            className={`${styles.nameInput} ${errors.name.hasErrors ? styles.inputError : ''}`}
            id="name"
            maxLength={50}
            //onBlur={() => checkHasErrors('name')}
            //onChange={}
            placeholder={resourcesContext.messages['name']}
            //value={webformConfiguration.name}
          />
          {errors.name.hasErrors && <ErrorMessage className={styles.errorMessage} message={errors.name.message} />}

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
      }
    </Fragment>
  );
};
