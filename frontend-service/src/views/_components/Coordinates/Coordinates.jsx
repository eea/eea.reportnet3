import { useContext, useEffect, useState } from 'react';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';
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
  onCoordinatesMoreInfoClick = () => {},
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

  const { inBounds, projectCoordinates } = MapUtils;

  const hasValidCRS = MapUtils.hasValidCRS(initialGeoJson, crsOptions);

  useEffect(() => {
    if (crsValue.value !== 'EPSG:3035' && showMessageError) {
      checkCoordinates(latitude, longitude);
    }
  }, [latitude, longitude]);

  useEffect(() => {
    if (initialGeoJson) {
      const parsedInitialGeoJson = JSON.parse(initialGeoJson);
      setLatitude(parsedInitialGeoJson.geometry.coordinates[0]);
      setLongitude(parsedInitialGeoJson.geometry.coordinates[1]);
    }
  }, [initialGeoJson]);

  const checkCoordinates = (lat, long, checkProjected = false) => {
    if (checkProjected && lat !== '' && long !== '') {
      const projectedCoords = projectCoordinates({
        coordinates: [parseFloat(lat), parseFloat(long)],
        currentCRS: crsValue,
        newCRS: 'EPSG:4326'
      });
      lat = projectedCoords[0];
      long = projectedCoords[1];
    }

    setHasErrors({
      latitude: checkEmptyCoordinate(lat),
      longitude: checkEmptyCoordinate(long),
      latOutOfBounds: !inBounds({ coord: lat, coordType: 'latitude', checkProjected }),
      longOutOfBounds: !inBounds({ coord: long, coordType: 'longitude', checkProjected }),
      checkProjected
    });
  };

  const checkEmptyCoordinate = coord => (!isNil(coord) ? coord.toString().trim() === '' : true);

  const getEPSGLabelClassName = () => {
    if (!isCellEditor) {
      return styles.pointEpsgWrapper;
    } else if (!hasValidCRS) {
      return styles.pointEpsgWrapperCellEditor;
    }
  };

  const getProjectedCoordinates = () => {
    const projectedCoords = projectCoordinates({
      coordinates: [parseFloat(latitude), parseFloat(longitude)],
      currentCRS: crsValue,
      newCRS: 'EPSG:4326'
    });
    return projectedCoords;
  };

  const renderLabel = keys => {
    if (!xyLabels) {
      return `${resourcesContext.messages[keys.geographical]}${!hasValidCRS ? ':' : ''}`;
    } else {
      return `${resourcesContext.messages[keys.metrical]}${!hasValidCRS ? ':' : ''}`;
    }
  };

  const renderEPSG = () => {
    const renderButton = () => {
      if (hasValidCRS) {
        return (
          <Button
            className={`p-button-secondary-transparent button ${styles.mapButton}`}
            disabled={hasErrors.latOutOfBounds || hasErrors.longOutOfBounds}
            icon="marker"
            onClick={() => onMapOpen(initialGeoJson)}
            tooltip={resourcesContext.messages['selectGeographicalDataOnMap']}
            tooltipOptions={{ position: 'bottom' }}
          />
        );
      }
    };

    const renderDropdown = () => {
      if (hasValidCRS) {
        return (
          <Dropdown
            appendTo={document.body}
            ariaLabel={'crs'}
            className={styles.epsgSwitcher}
            disabled={crsDisabled || hasErrors.latOutOfBounds || hasErrors.longOutOfBounds}
            onChange={e => {
              if (
                e.target.value.value === 'EPSG:3035' &&
                (!inBounds({
                  coord: getProjectedCoordinates()[0],
                  coordType: 'latitude',
                  checkProjected: true
                }) ||
                  !inBounds({
                    coord: getProjectedCoordinates()[1],
                    coordType: 'longitude',
                    checkProjected: true
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
        );
      } else {
        return <span>{JSON.parse(initialGeoJson)?.properties?.srid?.split(':')[1]}</span>;
      }
    };

    const renderMoreInfoTooltip = () => {
      if (!hasValidCRS) {
        return (
          <TooltipButton
            message={resourcesContext.messages['coordinatesMoreInfo']}
            onClick={() => onCoordinatesMoreInfoClick(initialGeoJson)}
            uniqueIdentifier={uniqueId('coordinates_more_info')}></TooltipButton>
        );
      }
    };

    return (
      <div className={getEPSGLabelClassName()}>
        <div>
          <label className={styles.epsg}>{`${resourcesContext.messages['epsg']}${!hasValidCRS ? ':' : ''}`}</label>
        </div>
        <div className={`${!isCellEditor ? styles.pointEpsgAndButtonWrapper : ''}`}>
          {renderDropdown()}
          {renderButton()}
          {renderMoreInfoTooltip()}
        </div>
      </div>
    );
  };

  const renderErrorMessage = () => {
    if (hasErrorMessage && hasValidCRS) {
      return (
        <span className={styles.errorMessage}>
          {showMessageError && <span className={styles.pointError}>{renderErrorMessageSeparator()}</span>}
          {(hasErrors.latOutOfBounds || hasErrors.longOutOfBounds) && (
            <span>
              <span className={styles.pointError}>{renderOutOfBoundsErrorMessage()}</span>
              <TooltipButton message={renderTooltipButtonMessage()} />
            </span>
          )}
        </span>
      );
    }
  };

  const renderErrorMessageSeparator = () => {
    let message = errorMessage;
    if (hasErrors.latOutOfBounds || hasErrors.longOutOfBounds) {
      return `${message}: `;
    }
    return message;
  };

  const renderLatitudeLongitudeInput = () => {
    const renderLatitude = () => {
      if (hasValidCRS) {
        return (
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
              checkCoordinates(e.target.value, longitude, crsValue.value === 'EPSG:3035');
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
        );
      } else {
        return <span>{latitude}</span>;
      }
    };

    const renderLongitude = () => {
      if (hasValidCRS) {
        return (
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
              checkCoordinates(latitude, e.target.value, crsValue.value === 'EPSG:3035');
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
        );
      } else {
        return <span>{longitude}</span>;
      }
    };

    return (
      <div className={!isCellEditor ? styles.coordinatesModalWrapper : ''}>
        <div>
          <label className={styles.epsg}>{renderLabel({ geographical: 'latitude', metrical: 'x' })}</label>
          {renderLatitude()}
        </div>
        <div>
          <label className={classNames(styles.epsg, styles.longitude)}>
            {renderLabel({ geographical: 'longitude', metrical: 'y' })}
          </label>
          {renderLongitude()}
        </div>
      </div>
    );
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
        return `${resourcesContext.messages['coordsOutOfBoundsTooltipGeographicalProjected']} ${
          resourcesContext.messages['coordsOutOfBoundsTooltipGeographicalProjectedValues']
        } ${getProjectedCoordinates().join(', ')}`;
      } else {
        return resourcesContext.messages['coordsOutOfBoundsTooltipGeographical'];
      }
    };

    if (!xyLabels) {
      return `${getCoordinatesBoundariesTitle()} ${getCoordinatesBoundaries()}`;
    } else {
      return `${getCoordinatesBoundariesTitle()} ${
        resourcesContext.messages['coordsOutOfBoundsTooltipGeographicalProjected']
      } ${
        resourcesContext.messages['coordsOutOfBoundsTooltipGeographicalProjectedValues']
      } ${getProjectedCoordinates().join(', ')}`;
    }
  };

  return (
    <div>
      <div className={styles.coordinatesWrapper}>{renderLatitudeLongitudeInput()}</div>
      {renderErrorMessage()}
      {renderEPSG()}
    </div>
  );
};
