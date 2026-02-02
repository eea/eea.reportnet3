import { Fragment, useContext, useEffect, useState } from 'react';

import styles from './DatasetValidateDialog.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { RepresentativeService } from 'services/RepresentativeService';

export const DatasetValidateDialog = ({
  disabled,
  icon,
  label,
  onConfirmValidate,
  onConfirmValidateWithProvider,
  dataflowId
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
      console.log('representativesData', representativesData);

      if (
        representativesData &&
        representativesData.representatives &&
        representativesData.representatives.length > 0
      ) {
        // Get the unique dataProviderIds from representatives
        const providerIds = representativesData.representatives.map(rep => rep.dataProviderId);

        if (representativesData.group && representativesData.group.dataProviderGroupId) {
          const allDataProvidersData = await RepresentativeService.getDataProviders(representativesData.group);

          // Filter to only include providers that are representatives for this dataflow
          const filteredProviders = allDataProvidersData.filter(provider =>
            providerIds.includes(provider.dataProviderId)
          );

          const formattedProviders = filteredProviders.map(provider => ({
            id: provider.dataProviderId,
            label: provider.label,
            code: provider.code
          }));

          setProviders(formattedProviders);
        }
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
    if (selectedProvider && onConfirmValidateWithProvider) {
      setIsValidateDialogVisible(false);
      onConfirmValidateWithProvider(selectedProvider);
      setSelectedProvider(null);
    }
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
          header={resourcesContext.messages['validateDataset']}
          onHide={onHideDialog}
          visible={isValidateDialogVisible}
          footer={null}>
          <div className={styles.validationOption}>
            <h4 className={styles.optionTitle}>{resourcesContext.messages['validation']}</h4>
            <p className={styles.optionDescription}>{resourcesContext.messages['validateDatasetConfirm']}</p>
            <Button
              className={`p-button-rounded p-button-primary ${!disabled ? ' p-button-animated-blink' : null}`}
              label={resourcesContext.messages['validate']}
              icon={icon}
              onClick={onConfirmSimpleValidation}
            />
          </div>

          <div className={styles.validationOption}>
            <h4 className={styles.optionTitle}>{resourcesContext.messages['validateWithProvider']}</h4>
            <p className={styles.optionDescription}>{resourcesContext.messages['validateWithProviderDescription']}</p>

            <div className={styles.providerValidationSection}>
              <Dropdown
                appendTo={document.body}
                className={styles.providerDropdown}
                disabled={isLoadingProviders || providers.length === 0}
                optionLabel="label"
                optionValue="id"
                options={providers}
                placeholder={
                  isLoadingProviders
                    ? resourcesContext.messages['loading']
                    : providers.length === 0
                    ? resourcesContext.messages['noProvidersAvailable']
                    : resourcesContext.messages['selectProvider']
                }
                onChange={e => setSelectedProvider(e.value)}
                value={selectedProvider}
              />
              <Button
                className={`p-button-rounded p-button-primary ${!disabled ? ' p-button-animated-blink' : null}`}
                icon={icon}
                label={resourcesContext.messages['validateWithProviderBtn'] || 'Validate with Provider'}
                onClick={onConfirmProviderValidation}
                disabled={!selectedProvider || isLoadingProviders}
              />
            </div>
          </div>
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
