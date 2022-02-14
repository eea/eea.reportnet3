import { Fragment, useContext, useLayoutEffect, useState } from 'react';

import styles from './AddNationalCoordinator.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { Spinner } from 'views/_components/Spinner';

import { RepresentativeService } from 'services/RepresentativeService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const AddNationalCoordinator = () => {
  const resourcesContext = useContext(ResourcesContext);

  const [isLoading, setIsLoading] = useState(false);
  const [isAddDialogVisible, setIsAddDialogVisible] = useState(false);

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

  const onAddDialogClose = () => {
    setIsAddDialogVisible(false);
  };

  const addDialogFooter = (
    <Fragment>
      <Button
        className="p-button-primary"
        icon="check"
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
          //onChange={}
          placeholder={resourcesContext.messages['nationalCoordinatorsEmailColumn']}
          //value={webformConfiguration.name}
        />

        <label className={styles.label} htmlFor="rolesDropdown">
          {resourcesContext.messages['countryColumn']}
        </label>
        <Dropdown
          appendTo={document.body}
          ariaLabel="groupOfCountries"
          className={styles.groupOfCountriesWrapper}
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
        <Dialog
          blockScroll={false}
          className={styles.containerAddDialog}
          footer={addDialogFooter}
          header={resourcesContext.messages['addNationalCoordinatorsDialogHeader']}
          modal
          onHide={onAddDialogClose}
          visible={isAddDialogVisible}>
          {renderDialogContent()}
        </Dialog>
      )}
    </Fragment>
  );
};
