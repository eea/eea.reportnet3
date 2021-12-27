import { useContext } from 'react';

import styles from './ErrorBoundaryFallback.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { Button } from 'views/_components/Button';
import { MainLayout } from 'views/_components/Layout';

import { TooltipUtils } from 'views/_functions/Utils/TooltipUtils';

export const ErrorBoundaryFallback = ({ error, resetErrorBoundary }) => {
  const resourcesContext = useContext(ResourcesContext);

  const onCopyErrorToClipboard = error => {
    const stringError = JSON.stringify({
      msg: error.message,
      stack: error.stack
    });

    const tempTextArea = document.createElement('textarea');
    tempTextArea.value = stringError;
    tempTextArea.setAttribute('readonly', '');
    tempTextArea.style = { position: 'absolute', left: '-9999px' };
    document.body.appendChild(tempTextArea);
    tempTextArea.select();
    document.execCommand('copy');
    document.body.removeChild(tempTextArea);

    setTimeout(() => {
      TooltipUtils.removeElementsByClass('p-tooltip');
    }, 750);
  };

  return (
    <MainLayout>
      <div className="rep-container">
        <div className={styles.boundaryWrap}>
          <div className={styles.boundaryContainer}>
            <h2>{resourcesContext.messages['errorBoundaryTitle']}</h2>
            <h3 className={styles.errorMessage}>{error.message}</h3>
            <FontAwesomeIcon
              aria-hidden={false}
              className="p-breadcrumb-home"
              icon={AwesomeIcons('meteor')}
              role="presentation"
            />
            <div className={styles.errorTexts}>
              <p
                dangerouslySetInnerHTML={{
                  __html: resourcesContext.messages['errorBoundaryMessageCopy']
                }}></p>
              <p>{resourcesContext.messages['errorBoundaryMessageRefresh']}</p>
            </div>

            <div className={styles.boundaryButtonsWrap}>
              <div>
                <Button
                  icon="copy"
                  label={resourcesContext.messages['errorBoundaryClipboardButtonLabel']}
                  onClick={() => onCopyErrorToClipboard(error)}
                  tooltip={resourcesContext.messages['copyToClipboardSuccess']}
                  tooltipOptions={{ event: 'focus', hideDelay: 750, position: 'top' }}
                />
              </div>
              <div>
                <Button icon="refresh" label={resourcesContext.messages['refresh']} onClick={resetErrorBoundary} />
              </div>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};
