import { Fragment, useContext, useLayoutEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import styles from './AddNationalCoordinator.module.scss';

import { Button } from 'views/_components/Button';
import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';

import { RepresentativeService } from 'services/RepresentativeService';
import { UserRightService } from 'services/UserRightService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { RegularExpressions } from 'views/_functions/Utils/RegularExpressions';

export const AddNationalCoordinator = () => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [nationalCoordinator, setNationalCoordinator] = useState({ countryCode: '', email: '' });
  const [isAddDialogVisible, setIsAddDialogVisible] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

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

  const addNationalCoordinators = async () => {
    try {
      setIsAdding(true);
      await UserRightService.createNationalCoordinators(nationalCoordinator);
    } catch (error) {
      console.error('NationalCoordinators - updateNationalCoordinators.', error);
      notificationContext.add({ type: 'CREATE_NATIONAL_COORDINATORS_ERROR' }, true);
    } finally {
      setIsAdding(false);
      setIsAddDialogVisible(false);
      setNationalCoordinator({});
    }
  };

  const onAddDialogClose = () => {
    setIsAddDialogVisible(false);
    setNationalCoordinator({});
  };

  const onChangeEmail = email => {
    setNationalCoordinator({ email: email, countryCode: nationalCoordinator.countryCode });
  };

  const onChangeCountryCode = countryCode => {
    setNationalCoordinator({ email: nationalCoordinator.email, countryCode: countryCode });
  };

  const isValidEmail = email => RegularExpressions['email'].test(email);

  const getTooltipMessage = () => {
    //console.log('isEmpty(nationalCoordinator.email) :>> ', isEmpty(nationalCoordinator.email));
    console.log('isEmpty(nationalCoordinator.country) :>> ', isEmpty(nationalCoordinator.country));
    //console.log('!isValidEmail(nationalCoordinator.email) :>> ', !isValidEmail(nationalCoordinator.email));

    if (isEmpty(nationalCoordinator.email) && isEmpty(nationalCoordinator.country)) {
      return resourcesContext.messages['incompleteDataTooltip'];
    } else if (!isValidEmail(nationalCoordinator.email)) {
      return resourcesContext.messages['notValidEmailTooltip'];
    } else if (isEmpty(nationalCoordinator.country)) {
      return resourcesContext.messages['emptyNationalCoordinatorsCountryError'];
    } else {
      return null;
    }
  };

  const renderDialogContent = () => {
    if (isLoading) {
      return (
        <div className={styles.noDataContent}>
          <Spinner className={styles.spinnerPosition} />
        </div>
      );
    }

    return (
      <div className={styles.addDialog}>
        <label className={styles.label}>{resourcesContext.messages['manageNationalCoordinatorsDialogColumn']}</label>
        <InputText
          className={styles.nameInput}
          id="name"
          maxLength={50}
          onChange={event => onChangeEmail(event.target.value)}
          placeholder={resourcesContext.messages['nationalCoordinatorsEmail']}
          value={nationalCoordinator.email}
        />

        <label className={styles.label} htmlFor="rolesDropdown">
          {resourcesContext.messages['countryColumn']}
        </label>
        <Dropdown
          appendTo={document.body}
          id="groupOfCountries"
          onChange={event => onChangeCountryCode(event.target.value.code)}
          optionLabel="label"
          options={groupOfCountries}
          placeholder={resourcesContext.messages['manageNationalCoordinatorsPlaceholder']}
          value={groupOfCountries?.find(country => country.code === nationalCoordinator.countryCode)}
        />
      </div>
    );
  };

  return (
    <Fragment>
      <Button
        className="p-button-primary"
        icon="plus"
        label={resourcesContext.messages['add']}
        onClick={() => setIsAddDialogVisible(true)}
      />

      {isAddDialogVisible && (
        <ConfirmDialog
          className={styles.confirmDialog}
          classNameConfirm={'p-button-primary'}
          confirmTooltip={getTooltipMessage()}
          disabledConfirm={!isNil(getTooltipMessage())}
          header={resourcesContext.messages['addNationalCoordinatorsDialogHeader']}
          iconConfirm={isAdding ? 'spinnerAnimate' : 'check'}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['save']}
          onConfirm={addNationalCoordinators}
          onHide={onAddDialogClose}
          visible={isAddDialogVisible}>
          {renderDialogContent()}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
