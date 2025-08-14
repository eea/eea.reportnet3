import { Fragment, useContext, useState } from 'react';
import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { Dropdown } from 'views/_components/Dropdown';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { ValidationService } from 'services/ValidationService';

import styles from './QcSeverityDialog.module.scss';

export const QcSeverityDialog = ({ datasetId, datasetSchemaId, isVisible, onHide }) => {
  const [severity, setSeverity] = useState('ERROR');
  const [saving, setSaving] = useState(false);

  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const severityOptions = [
    { label: 'INFO', value: 'INFO' },
    { label: 'WARNING', value: 'WARNING' },
    { label: 'ERROR', value: 'ERROR' },
    { label: 'BLOCKER', value: 'BLOCKER' }
  ];

  const onSave = async () => {
    try {
      setSaving(true);
      await ValidationService.setDefaultSeverity(datasetId, datasetSchemaId, severity);
      notificationContext.add({ type: 'SET_DEFAULT_SEVERITY_SUCCESS', content: { severity: severity } }, true);
      onHide();
    } catch (error) {
      console.error('QcSeverityDialog - onSave', error);
      notificationContext.add({ type: 'SET_DEFAULT_SEVERITY_ERROR' }, true);
    } finally {
      setSaving(false);
    }
  };

  const renderFooter = (
    <Fragment>
      <Button
        className={!saving ? 'p-button-animated-blink' : ''}
        disabled={saving}
        icon={saving ? 'spinnerAnimate' : 'check'}
        label={resourcesContext.messages['save']}
        onClick={onSave}
      />
      <Button
        className="p-button-secondary"
        icon="cancel"
        label={resourcesContext.messages['cancel']}
        onClick={onHide}
      />
    </Fragment>
  );

  const selectedOption = severityOptions.find(option => option.value === severity);

  return (
    isVisible && (
      <Dialog
        className={styles.dialog}
        footer={renderFooter}
        header={resourcesContext.messages['defaultSeverity']}
        onHide={onHide}
        style={{ width: '500px' }}
        visible={isVisible}>
        <div className={styles.content}>
          <div className={styles.messageContainer}>
            <p>{resourcesContext.messages['defaultSeverityMessage']}</p>
          </div>

          <div className={styles.formField}>
            <label htmlFor="severityDropdown">{resourcesContext.messages['severity']}</label>
            <Dropdown
              id="severityDropdown"
              appendTo={document.body}
              optionLabel="label"
              optionValue="value"
              options={severityOptions}
              value={selectedOption}
              onChange={e => {
                setSeverity(e.target.value.value);
              }}
            />
          </div>
        </div>
      </Dialog>
    )
  );
};
