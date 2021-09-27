import { useContext, useEffect, useRef, useState } from 'react';

import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

import styles from './PropertiesDialog.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { PropertyItem } from './_components/PropertyItem';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

export const PropertiesDialog = ({ dataflowState, manageDialogs }) => {
  const { description, isPropertiesDialogVisible, name, obligations, status } = dataflowState;

  const resourcesContext = useContext(ResourcesContext);
  const {
    userProps: { dateFormat }
  } = useContext(UserContext);

  const [dialogHeight, setDialogHeight] = useState(null);

  const propertiesRef = useRef(null);

  useEffect(() => {
    if (propertiesRef.current && isPropertiesDialogVisible) {
      setDialogHeight(propertiesRef.current.getBoundingClientRect().height);
    }
  }, [propertiesRef.current, isPropertiesDialogVisible]);

  const getObligationsContent = () => {
    const date = !isNil(obligations.expirationDate) ? dayjs(obligations.expirationDate).format(dateFormat) : '-';

    return [
      { id: 0, labelKey: 'title:', labelValue: obligations.title },
      { id: 1, labelKey: 'description:', labelValue: obligations.description },
      { id: 2, labelKey: 'comment:', labelValue: obligations.comment },
      { id: 3, labelKey: 'nextReportDue:', labelValue: date },
      { id: 5, labelKey: 'Frecuency:', labelValue: obligations.reportingFrequency },
      { id: 4, labelKey: 'id:', labelValue: obligations.obligationId }
    ];
  };

  const parseLegalInstrument = () => ({
    shortName: isNil(obligations.legalInstrument) ? '' : obligations.legalInstrument.alias,
    legalName: isNil(obligations.legalInstrument) ? '' : obligations.legalInstrument.title
  });

  const renderDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink button-right-aligned"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => manageDialogs('isPropertiesDialogVisible', false)}
    />
  );

  return (
    isPropertiesDialogVisible && (
      <Dialog
        className={styles.propertiesDialog}
        footer={renderDialogFooter}
        header={resourcesContext.messages['properties']}
        onHide={() => manageDialogs('isPropertiesDialogVisible', false)}
        visible={isPropertiesDialogVisible}>
        <div className={styles.propertiesWrap} ref={propertiesRef} style={{ height: dialogHeight }}>
          <PropertyItem
            content={[
              { id: 0, labelKey: 'Dataflow name:', labelValue: name },
              { id: 1, labelKey: 'Dataflow description:', labelValue: description },
              { id: 2, labelKey: 'Dataflow status:', labelValue: status }
            ]}
            title={resourcesContext.messages['dataflowDetails']}
          />
          <PropertyItem content={getObligationsContent()} title={resourcesContext.messages['obligation']} />
          <PropertyItem
            content={[
              { id: 0, labelKey: 'Short name: ', labelValue: parseLegalInstrument().shortName },
              { id: 1, labelKey: 'Legal name: ', labelValue: parseLegalInstrument().legalName }
            ]}
            title={resourcesContext.messages['legalInstrument']}
          />
        </div>
      </Dialog>
    )
  );
};
