import { Fragment, useContext, useEffect, useState } from 'react';

import isEmpty from 'lodash/isEmpty';

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

export const AddNationalCoordinator = ({ onConfirmAddition, checkDuplicateNationalCoordinator }) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [groupOfCountries, setGroupOfCountries] = useState([]);
  const [hasEmailError, setHasEmailError] = useState(false);
  const [isAddDialogVisible, setIsAddDialogVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [nationalCoordinator, setNationalCoordinator] = useState({ email: '' });

  useEffect(() => {
    getDropdownOptions();
  }, []);

  useEffect(() => {
    if (!isAddDialogVisible) {
      setNationalCoordinator({ email: '' });
      setHasEmailError(false);
    }
  }, [isAddDialogVisible]);

  const getDropdownOptions = async () => {
    setIsLoading(true);

    const allCountries = { dataProviderGroupId: 2 };

    try {
      const responseGroupOfCountries = await RepresentativeService.getDataProviders(allCountries);
      setGroupOfCountries(responseGroupOfCountries);
    } catch (error) {
      console.error('NationalCoordinators - getDropdownOptions.', error);
      notificationContext.add({ type: 'LOAD_COUNTRIES_ERROR' }, true);
    } finally {
      setIsLoading(false);
    }
  };

  const addNationalCoordinator = async () => {
    try {
      await UserRightService.createNationalCoordinator(nationalCoordinator);
      setIsAddDialogVisible(false);
    } catch (error) {
      if (error?.response?.status === 404) {
        notificationContext.add({ type: 'EMAIL_NOT_FOUND_ERROR' }, true);
      } else {
        notificationContext.add({ type: 'CREATE_NATIONAL_COORDINATORS_ERROR' }, true);
      }
      console.error('NationalCoordinators - createNationalCoordinator.', error);
    }
  };

  const onAddDialogClose = () => {
    setIsAddDialogVisible(false);
  };

  const onChangeEmail = email => setNationalCoordinator({ email: email, countryCode: nationalCoordinator.countryCode });

  const onChangeCountryCode = countryCode =>
    setNationalCoordinator({ email: nationalCoordinator.email, countryCode: countryCode });

  const isValidEmail = email => RegularExpressions['email'].test(email);

  const getTooltipMessage = () => {
    if (isEmpty(nationalCoordinator.email) && isEmpty(nationalCoordinator.countryCode)) {
      return resourcesContext.messages['incompleteDataTooltip'];
    } else if (!isValidEmail(nationalCoordinator.email)) {
      return resourcesContext.messages['notValidEmailTooltip'];
    } else if (isEmpty(nationalCoordinator.countryCode)) {
      return resourcesContext.messages['emptyNationalCoordinatorsCountryError'];
    } else if (checkDuplicateNationalCoordinator(nationalCoordinator)) {
      return resourcesContext.messages['addedDuplicateNationalCoordinatorsError'];
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
          className={hasEmailError ? styles.error : ''}
          id="name"
          keyfilter="email"
          maxLength={50}
          onBlur={event => setHasEmailError(!isValidEmail(event.target.value))}
          onChange={event => onChangeEmail(event.target.value.trim())}
          placeholder={resourcesContext.messages['nationalCoordinatorsEmail']}
          value={nationalCoordinator.email}
        />

        <label className={styles.label} htmlFor="rolesDropdown">
          {resourcesContext.messages['dataProviderName']}
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
          classNameConfirm="p-button-primary"
          confirmTooltip={getTooltipMessage()}
          disabledConfirm={
            checkDuplicateNationalCoordinator(nationalCoordinator) ||
            !isValidEmail(nationalCoordinator.email) ||
            isEmpty(nationalCoordinator.email) ||
            isEmpty(nationalCoordinator.countryCode)
          }
          header={resourcesContext.messages['addNationalCoordinatorsDialogHeader']}
          iconConfirm={'check'}
          labelCancel={resourcesContext.messages['cancel']}
          labelConfirm={resourcesContext.messages['save']}
          onConfirm={() => {
            addNationalCoordinator();
            onConfirmAddition();
          }}
          onHide={onAddDialogClose}
          visible={isAddDialogVisible}>
          {renderDialogContent()}
        </ConfirmDialog>
      )}
    </Fragment>
  );
};
