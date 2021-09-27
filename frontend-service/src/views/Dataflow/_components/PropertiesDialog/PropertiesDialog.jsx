import { useContext, useEffect, useRef, useState } from 'react';

import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

import styles from './PropertiesDialog.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { RodUrl } from 'repositories/config/RodUrl';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

export const PropertiesDialog = ({ dataflowState, manageDialogs }) => {
  const { description, isPropertiesDialogVisible, name, obligations, status } = dataflowState;

  const resourcesContext = useContext(ResourcesContext);
  const {
    userProps: { dateFormat }
  } = useContext(UserContext);

  const [dialogHeight, setDialogHeight] = useState(null);
  const [isOpen, setIsOpen] = useState(true);

  const propertiesRef = useRef(null);

  useEffect(() => {
    if (propertiesRef.current && isPropertiesDialogVisible) {
      setDialogHeight(propertiesRef.current.getBoundingClientRect().height);
    }
  }, [propertiesRef.current, isPropertiesDialogVisible]);

  const parseObligations = () => {
    return {
      label: 'obligation',
      data: {
        comment: obligations.comment,
        description: obligations.description,
        id: obligations.obligationId,
        nextReportDue: !isNil(obligations.expirationDate) ? dayjs(obligations.expirationDate).format(dateFormat) : '-',
        reportingFrequency: obligations.reportingFrequency,
        title: obligations.title
      }
    };
  };

  const parseLegalInstrument = () => {
    return {
      label: 'legalInstrument',
      data: {
        shortName: isNil(obligations.legalInstrument) ? '' : obligations.legalInstrument.alias,
        legalName: isNil(obligations.legalInstrument) ? '' : obligations.legalInstrument.title
      }
    };
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
          <div style={{ marginTop: '1rem', marginBottom: '2rem' }}>
            <h3 className={styles.title}>
              <FontAwesomeIcon
                className={styles.icon}
                icon={AwesomeIcons(isOpen ? 'angleDown' : 'angleRight')}
                onClick={() => setIsOpen(!isOpen)}
              />
              {resourcesContext.messages['dataflowDetails']}
            </h3>
            <div className={`${styles.content} ${isOpen ? '' : styles.hide}`}>
              <span>
                <strong>Dataflow name: </strong>
                {name}
              </span>
              <span>
                <strong>Dataflow description: </strong>
                {description}
              </span>
              <span>
                <strong>Dataflow status: </strong>
                {status}
              </span>
            </div>
          </div>
          <div style={{ marginTop: '2rem', marginBottom: '1rem' }}>
            <h3 className={styles.title}>
              <FontAwesomeIcon
                className={styles.icon}
                icon={AwesomeIcons(isOpen ? 'angleDown' : 'angleRight')}
                onClick={() => setIsOpen(!isOpen)}
              />
              {resourcesContext.messages['obligation']}
            </h3>
            <div className={`${styles.content}`}>
              <span>
                <strong>Title: </strong>
                {parseObligations().data.title}
              </span>
              <span>
                <strong>Description: </strong>
                {parseObligations().data.description}
              </span>
              <span>
                <strong>Comment: </strong>
                {parseObligations().data.comment}
              </span>
              <span>
                <strong>Next Report Due: </strong>
                {parseObligations().data.nextReportDue}
              </span>
              <span>
                <strong>Id: </strong>
                {parseObligations().data.id}
              </span>
            </div>

            <Button
              className={'p-button-secondary-transparent'}
              icon={'externalUrl'}
              onMouseDown={() => window.open(`${RodUrl.obligations}${obligations.obligationId}`)}
              tooltip={resourcesContext.messages['viewMore']}
            />
          </div>
          <div style={{ marginTop: '2rem', marginBottom: '1rem' }}>
            <h3 className={styles.title}>
              <FontAwesomeIcon
                className={styles.icon}
                icon={AwesomeIcons(isOpen ? 'angleDown' : 'angleRight')}
                onClick={() => setIsOpen(!isOpen)}
              />
              {resourcesContext.messages['obligation']}
            </h3>
            <Button
              className={'p-button-secondary-transparent'}
              icon={'externalUrl'}
              onMouseDown={() => window.open(`${RodUrl.instruments}${obligations.legalInstrument.id}`)}
              tooltip={resourcesContext.messages['viewMore']}
            />
          </div>
        </div>
      </Dialog>
    )
  );
};
