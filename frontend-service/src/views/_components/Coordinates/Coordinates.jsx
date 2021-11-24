import { useContext, useEffect, useState } from 'react';
import isNil from 'lodash/isNil';
import classNames from 'classnames';

import styles from './Coordinates.module.scss';

import { Button } from 'views/_components/Button';
import { Dropdown } from 'views/_components/Dropdown';
import { InputText } from 'views/_components/InputText';
import { TooltipButton } from 'views/_components/TooltipButton';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { MapUtils } from 'views/_functions/Utils/MapUtils';

export const Coordinates = ({
  crsDisabled,
  crsOptions = [],
  crsValue,
  disabled = false,
  errorMessage = '',
  hasErrorMessage = false,
  id,
  initialGeoJson,
  isCellEditor = false,
  onBlur = () => {},
  onCrsChange = () => {},
  onFocus = () => {},
  onKeyDown = () => {},
  onMapOpen = () => {},
  showMessageError = false,
  xyLabels = false
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const [hasErrors, setHasErrors] = useState({});
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');

  const { inBounds } = MapUtils;

  useEffect(() => {
    if (initialGeoJson) {
      const parsedInitialGeoJson = JSON.parse(initialGeoJson);
      setLatitude(parsedInitialGeoJson.geometry.coordinates[0]);
      setLongitude(parsedInitialGeoJson.geometry.coordinates[1]);
    }
  }, [initialGeoJson]);

  const checkCoordinates = (lat, long, checkProjected = false) => {
    setHasErrors({
      latitude: checkEmptyCoordinate(lat),
      longitude: checkEmptyCoordinate(long),
      latOutOfBounds: !inBounds({ coord: lat, coordType: 'latitude', checkProjected, crs: crsValue.value }),
      longOutOfBounds: !inBounds({ coord: long, coordType: 'longitude', checkProjected, crs: crsValue.value }),
      checkProjected
    });
  };

  const checkEmptyCoordinate = coord => (!isNil(coord) ? coord.toString().trim() === '' : true);

  const renderLabel = keys => {
    if (!xyLabels) {
      return resourcesContext.messages[isCellEditor ? keys.geographicalShort : keys.geographical];
    } else {
      return resourcesContext.messages[keys.metrical];
    }
  };

  const renderErrorMessage = () => {
    let message = errorMessage;
    if (hasErrors.latOutOfBounds || hasErrors.longOutOfBounds) {
      return `${message}: `;
    }
    return message;
  };

  const renderOutOfBoundsErrorMessage = () => {
    if (hasErrors.checkProjected) {
      return resourcesContext.messages['coordsOutOfBoundsProjected'];
    } else {
      return resourcesContext.messages['coordsOutOfBounds'];
    }
  };

  const renderTooltipButtonMessage = () => {
    const getCoordinatesBoundariesTitle = () => {
      if (hasErrors.checkProjected) {
        return resourcesContext.messages['coordsOutOfBoundsTooltipProjected'];
      } else {
        return resourcesContext.messages['coordsOutOfBoundsTooltip'];
      }
    };

    const getCoordinatesBoundaries = () => {
      if (hasErrors.checkProjected) {
        return resourcesContext.messages['coordsOutOfBoundsTooltipGeographicalProjected'];
      } else {
        return resourcesContext.messages['coordsOutOfBoundsTooltipGeographical'];
      }
    };

    if (!xyLabels) {
      return `${getCoordinatesBoundariesTitle()} ${getCoordinatesBoundaries()}`;
    } else {
      return `${getCoordinatesBoundariesTitle()} ${resourcesContext.messages['coordsOutOfBoundsTooltipMetrical']}`;
    }
  };

  return (
    <div>
      <div className={styles.coordinatesWrapper}>
        <label className={styles.epsg}>
          {renderLabel({ geographical: 'latitude', geographicalShort: 'lat', metrical: 'x' })}:
        </label>
        <InputText
          className={classNames({
            [styles.error]: hasErrors.latitude || hasErrors.latOutOfBounds,
            [styles.isCellEditor]: isCellEditor
          })}
          disabled={disabled}
          id={`${id}_lat`}
          keyfilter="coordinates"
          onBlur={e => {
            onBlur([e.target.value, longitude].join(', '));
            checkCoordinates(e.target.value, longitude);
          }}
          onChange={event => setLatitude(event.target.value)}
          onFocus={e => {
            e.preventDefault();
            onFocus();
          }}
          onKeyDown={e => onKeyDown(e, e.target.value)}
          tooltip={hasErrors.latOutOfBounds ? renderTooltipButtonMessage() : ''}
          tooltipOptions={{ position: 'top' }}
          type="text"
          value={latitude}
        />
        <label className={classNames(styles.epsg, styles.longitude)}>
          {renderLabel({ geographical: 'longitude', geographicalShort: 'long', metrical: 'y' })}:
        </label>
        <InputText
          className={classNames({
            [styles.error]: hasErrors.longitude || hasErrors.longOutOfBounds,
            [styles.isCellEditor]: isCellEditor
          })}
          disabled={disabled}
          id={`${id}_long`}
          keyfilter="coordinates"
          onBlur={e => {
            onBlur([latitude, e.target.value].join(', '));
            checkCoordinates(latitude, e.target.value);
          }}
          onChange={event => setLongitude(event.target.value)}
          onFocus={e => {
            e.preventDefault();
            onFocus();
          }}
          onKeyDown={e => onKeyDown(e, e.target.value)}
          tooltip={hasErrors.longOutOfBounds ? renderTooltipButtonMessage() : ''}
          tooltipOptions={{ position: 'top' }}
          type="text"
          value={longitude}
        />
      </div>
      {hasErrorMessage && (
        <span className={styles.errorMessage}>
          {showMessageError && <span className={styles.pointError}>{renderErrorMessage()}</span>}
          {(hasErrors.latOutOfBounds || hasErrors.longOutOfBounds) && (
            <span>
              <span className={styles.pointError}>{renderOutOfBoundsErrorMessage()}</span>
              <TooltipButton message={renderTooltipButtonMessage()} />
            </span>
          )}
        </span>
      )}
      <div className={`${!isCellEditor ? styles.pointEpsgWrapper : ''}`}>
        <label className={styles.epsg}>{resourcesContext.messages['epsg']}</label>
        <Dropdown
          appendTo={document.body}
          ariaLabel={'crs'}
          className={styles.epsgSwitcher}
          disabled={crsDisabled || hasErrors.latOutOfBounds || hasErrors.longOutOfBounds}
          onChange={e => {
            if (
              e.target.value.value === 'EPSG:3035' &&
              (!inBounds({ coord: latitude, coordType: 'latitude', checkProjected: true, crs: e.target.value.value }) ||
                !inBounds({
                  coord: longitude,
                  coordType: 'longitude',
                  checkProjected: true,
                  crs: e.target.value.value
                }))
            ) {
              checkCoordinates(latitude, longitude, true);
              return false;
            }
            onCrsChange(e.target.value);
          }}
          optionLabel="label"
          options={crsOptions}
          placeholder={resourcesContext.messages['selectCRS']}
          value={crsValue}
        />
        <Button
          className={`p-button-secondary-transparent button ${styles.mapButton}`}
          disabled={hasErrors.latOutOfBounds || hasErrors.longOutOfBounds}
          icon="marker"
          onClick={() => onMapOpen(initialGeoJson)}
          tooltip={resourcesContext.messages['selectGeographicalDataOnMap']}
          tooltipOptions={{ position: 'bottom' }}
        />
      </div>
    </div>
  );
};
