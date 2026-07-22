import { forwardRef, useImperativeHandle, useRef, useContext } from 'react';
import PropTypes from 'prop-types';
import { Toast } from 'primereact/toast';


import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import classNames from 'classnames';

export const Growl = forwardRef((props, ref) => {
  const toastRef = useRef(null);
  const resourcesContext = useContext(ResourcesContext); // Access your global language resources

  useImperativeHandle(ref, () => ({
    show: (value) => {
      if (!value) return;
      toastRef.current?.show(value);
    },
    clear: () => {
      toastRef.current?.clear();
    }
  }));

  const getPosition = (pos) => {
    switch (pos) {
      case 'topright': return 'top-right';
      case 'topleft': return 'top-left';
      case 'bottomright': return 'bottom-right';
      case 'bottomleft': return 'bottom-left';
      default: return 'top-right';
    }
  };


  const itemTemplate = (message) => {

    if (message.system) {
      const title = resourcesContext?.messages?.['systemNotification']?.toUpperCase() || 'SYSTEM NOTIFICATION';
      return (
        <div className="p-toast-message-content p-growl-message-system-notification">
          <FontAwesomeIcon
            className="p-toast-message-icon p-growl-message-system-notification-icon"
            icon={AwesomeIcons('bullhorn')}
            role="presentation"
          />
          <div className="p-toast-message-text">
            <span className="p-toast-summary p-growl-title">{title}</span>
            {message.detail && <div className="p-toast-detail p-growl-details">{message.detail}</div>}
          </div>
        </div>
      );
    }


    return null;
  };

  return (
    <Toast
      id={props.id}
      ref={toastRef}
      position={getPosition(props.position)}
      style={props.style}
      className={props.className}
      baseZIndex={props.baseZIndex}
      onClick={props.onClick}
      onRemove={props.onRemove}
      itemTemplate={itemTemplate} // Pass our template interceptor here
    />
  );
});

Growl.displayName = 'Growl';

Growl.propTypes = {
  baseZIndex: PropTypes.number,
  className: PropTypes.string,
  id: PropTypes.string,
  onClick: PropTypes.func,
  onRemove: PropTypes.func,
  position: PropTypes.string,
  style: PropTypes.object
};