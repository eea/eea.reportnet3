import { Fragment, useContext, useEffect, useState } from 'react';

import styles from './DatasetValidateDialog.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { RepresentativeService } from 'services/RepresentativeService';
import { AddOrganizationsService } from 'services/AddOrganizationsService';
import { config } from 'conf';

export const DatasetValidateDialog = ({
  disabled,
  icon,
  label,
  onConfirmValidate,
  onConfirmValidateAsProvider,
  dataflowId,
  dataflowType,
  isTestDataset
}) => {
  const resourcesContext = useContext(ResourcesContext);

  const [isValidateDialogVisible, setIsValidateDialogVisible] = useState(false);
  const [selectedProvider, setSelectedProvider] = useState(null);
  const [providers, setProviders] = useState([]);
  const [isLoadingProviders, setIsLoadingProviders] = useState(false);

  useEffect(() => {
    if (isValidateDialogVisible && dataflowId) {
      fetchProviders();
    }
  }, [isValidateDialogVisible, dataflowId]);

  const fetchProviders = async () => {
    try {
      setIsLoadingProviders(true);
      const representativesData = await RepresentativeService.getRepresentatives(dataflowId);

      let dataProviderGroup = representativesData?.group;

      // If no group found, fetch and use the first available group based on dataflow type
      if (!dataProviderGroup?.dataProviderGroupId && dataflowType) {
        let groups = [];

        if (dataflowType === config.dataflowType.REPORTING.value) {
          const allProviderGroups = await AddOrganizationsService.getProviderGroups();
          groups = allProviderGroups.filter(
            group => group.dataProviderGroupId === 2 || group.dataProviderGroupId === 8
          );
        } else if (
          dataflowType === config.dataflowType.CITIZEN_SCIENCE.value ||
          dataflowType === config.dataflowType.BUSINESS.value
        ) {
          const response = await RepresentativeService.getGroupOrganizations();
          groups = response.data;
        } else {
          const response = await RepresentativeService.getGroupCountries();
          groups = response.data;
        }

        dataProviderGroup = groups[0];
      }
      if (dataProviderGroup?.dataProviderGroupId) {
        const dataProvidersData = await RepresentativeService.getDataProviders(dataProviderGroup);

        const formattedProviders = dataProvidersData.map(provider => ({
          id: provider.dataProviderId,
          label: provider.label,
          code: provider.code
        }));

        setProviders(formattedProviders);
      } else {
        setProviders([]);
      }
    } catch (error) {
      console.error('DatasetValidateDialog - fetchProviders.', error);
      setProviders([]);
    } finally {
      setIsLoadingProviders(false);
    }
  };

  const onConfirmSimpleValidation = () => {
    setIsValidateDialogVisible(false);
    onConfirmValidate();
  };

  const onConfirmProviderValidation = () => {
    setIsValidateDialogVisible(false);
    onConfirmValidateAsProvider(selectedProvider?.code);
    setSelectedProvider(null);
  };

  const onHideDialog = () => {
    setIsValidateDialogVisible(false);
    setSelectedProvider(null);
  };

  const renderValidateButton = () => {
    return (
      <Button
        className={`p-button-rounded p-button-secondary-transparent ${!disabled ? ' p-button-animated-blink' : null}`}
        disabled={disabled}
        icon={icon}
        label={label}
        onClick={() => setIsValidateDialogVisible(true)}
      />
    );
  };

  const renderValidateDialog = () => {
    if (isValidateDialogVisible) {
      return (
        <Dialog
          className={styles.validateDialog}
          footer={null}
          header={resourcesContext.messages['validateDataset']}
          onHide={onHideDialog}
          visible={isValidateDialogVisible}>
          <div className={styles.validationOption}>
            <h4 className={styles.optionTitle}>{resourcesContext.messages['validation']}</h4>
            <p
              className={styles.optionDescription}
              dangerouslySetInnerHTML={{ __html: resourcesContext.messages['validateDatasetConfirm'] }}
            />
            <Button
              className={`p-button-rounded p-button-primary ${!disabled ? ' p-button-animated-blink' : null}`}
              icon={icon}
              label={resourcesContext.messages['validate']}
              onClick={onConfirmSimpleValidation}
            />
          </div>

          {(isTestDataset === undefined || isTestDataset === true) && (
            <div className={styles.validationOption}>
              <h4 className={styles.optionTitle}>{resourcesContext.messages['validateWithProvider']}</h4>
              <p
                className={styles.optionDescription}
                dangerouslySetInnerHTML={{ __html: resourcesContext.messages['validateWithProviderDescription'] }}
              />
              <div className={styles.providerValidationSection}>
                <Dropdown
                  appendTo={document.body}
                  className={styles.providerDropdown}
                  disabled={isLoadingProviders || providers.length === 0}
                  onChange={e => setSelectedProvider(e.value)}
                  optionLabel="label"
                  options={providers}
                  placeholder={
                    isLoadingProviders
                      ? resourcesContext.messages['loading']
                      : providers.length === 0
                      ? resourcesContext.messages['noProvidersAvailable']
                      : resourcesContext.messages['selectProvider']
                  }
                  value={selectedProvider}
                />
                <Button
                  className={`p-button-rounded p-button-primary ${!disabled ? ' p-button-animated-blink' : null}`}
                  disabled={!selectedProvider || isLoadingProviders}
                  icon={icon}
                  label={resourcesContext.messages['validateWithProvider']}
                  onClick={onConfirmProviderValidation}
                />
              </div>
            </div>
          )}
        </Dialog>
      );
    }
  };

  return (
    <Fragment>
      {renderValidateButton()}
      {renderValidateDialog()}
    </Fragment>
  );
};
