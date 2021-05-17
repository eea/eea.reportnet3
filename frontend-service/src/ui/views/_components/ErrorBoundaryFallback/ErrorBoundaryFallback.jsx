import { useContext } from 'react';

import styles from './ErrorBoundaryFallback.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { Button } from 'ui/views/_components/Button';
import { MainLayout } from 'ui/views/_components/Layout';

import { TooltipUtils } from 'ui/views/_functions/Utils/TooltipUtils';

export const ErrorBoundaryFallback = ({ error, resetErrorBoundary }) => {
  const resources = useContext(ResourcesContext);
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
          <div>
            <div>
              <h2 className="warning">{resources.messages['errorBoundaryTitle']}</h2>
            </div>
            <div>
              <p className="warning">{error.message}</p>
              <p>{resources.messages['errorBoundaryMessageCopy']}</p>
              <p>{resources.messages['errorBoundaryMessageRefresh']}</p>
            </div>

            <div className={styles.boundaryButtonsWrap}>
              <div>
                <Button
                  icon={'copy'}
                  label={'Copy to clipboard'}
                  onClick={() => onCopyErrorToClipboard(error)}
                  tooltip={resources.messages['copyToClipboardSuccess']}
                  tooltipOptions={{ event: 'focus', hideDelay: 750, position: 'top' }}
                />
              </div>
              <div>
                <Button icon={'refresh'} label={'Refresh'} onClick={resetErrorBoundary} />
              </div>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
};
