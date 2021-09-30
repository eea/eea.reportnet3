import { useContext, useEffect, useRef, useState } from 'react';

import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

import styles from './PropertiesDialog.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { PropertyItem } from './_components/PropertyItem';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { RodUrl } from 'repositories/config/RodUrl';

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
      { id: 0, label: resourcesContext.messages['title'], value: obligations.title },
      { id: 1, label: resourcesContext.messages['description'], value: obligations.description },
      { id: 2, label: resourcesContext.messages['comment'], value: obligations.comment },
      { id: 3, label: resourcesContext.messages['nextReportDue'], value: date },
      { id: 4, label: resourcesContext.messages['obligationId'], value: obligations.obligationId.toString() }
    ];
  };

  const getLegalInstrumentContent = () => {
    const legalName = isNil(obligations.legalInstrument) ? '' : obligations.legalInstrument.title;
    const shortName = isNil(obligations.legalInstrument) ? '' : obligations.legalInstrument.alias;

    return [
      { id: 0, label: resourcesContext.messages['shortName'], value: shortName },
      { id: 1, label: resourcesContext.messages['legalName'], value: legalName }
    ];
  };

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
              { id: 0, label: resourcesContext.messages['dataflowName'], value: name },
              { id: 1, label: resourcesContext.messages['dataflowDescription'], value: description },
              { id: 2, label: resourcesContext.messages['dataflowStatus'], value: status }
            ]}
            title={resourcesContext.messages['dataflowDetails']}
          />
          <PropertyItem
            content={getObligationsContent()}
            redirectTo={`${RodUrl.obligations}${obligations.obligationId}`}
            title={resourcesContext.messages['obligation']}
          />
          <PropertyItem
            content={getLegalInstrumentContent()}
            redirectTo={`${RodUrl.instruments}${obligations.legalInstrument.id}`}
            title={resourcesContext.messages['legalInstrument']}
          />
        </div>
      </Dialog>
    )
  );
};
