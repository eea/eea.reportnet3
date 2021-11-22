import { Fragment, useContext, useEffect, useRef, useState } from 'react';
import isNil from 'lodash/isNil';
import cloneDeep from 'lodash/cloneDeep';

import styles from './Map.module.scss';
import 'leaflet/dist/leaflet.css';
import 'esri-leaflet-geocoder/dist/esri-leaflet-geocoder.css';
import 'leaflet-draw/dist/leaflet.draw.css';

import 'proj4leaflet';
import L from 'leaflet';
import proj4 from 'proj4';
import * as ELG from 'esri-leaflet-geocoder';
import * as esri from 'esri-leaflet';

import { Dropdown } from 'views/_components/Dropdown';
import { Map as MapComponent, GeoJSON, Marker, Popup } from 'react-leaflet';
// import { EditControl } from 'react-leaflet-draw';
// import ReactTooltip from 'react-tooltip';

import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import newMarkerIcon from 'views/_assets/images/logos/newMarker.png';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';
import { MapUtils } from 'views/_functions/Utils';

import { TextUtils } from 'repositories/_utils/TextUtils';

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 36]
});

// 3035 --> ETRS89 / ETRS-LAEA
// 4258 --> ETRS89
// 4326 --> WGS84

proj4.defs([
  ['EPSG:4258', '+proj=longlat +ellps=GRS80 +no_defs'],
  ['EPSG:3035', '+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +units=m +no_defs'],
  ['EPSG:4326', '+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs']
]);

L.Marker.prototype.options.icon = DefaultIcon;

let NewMarkerIcon = L.icon({
  iconUrl: newMarkerIcon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 36]
});
export const Map = ({
  enabledDrawElements = {
    circle: false,
    circlemarker: false,
    polyline: false,
    marker: false,
    point: false,
    polygon: false,
    rectangle: false
  },
  geoJson = '',
  geometryType = '',
  hasLegend = false,
  onSelectPoint,
  options = {
    zoom: [15],
    bearing: [0],
    pitch: [0]
  },
  selectedCRS = { label: 'WGS84 - 4326', value: 'EPSG:4326' }
}) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);
  // const { BaseLayer, Overlay } = LayersControl;

  const crs = [
    { label: 'WGS84 - 4326', value: 'EPSG:4326' },
    { label: 'ETRS89 - 4258', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89 - 3035', value: 'EPSG:3035' }
  ];

  const themes = [
    { label: 'Topographic', value: 'Topographic' },
    { label: 'Streets', value: 'Streets' },
    { label: 'National Geographic', value: 'NationalGeographic' },
    { label: 'Oceans', value: 'Oceans' },
    { label: 'Gray', value: 'Gray' },
    { label: 'Dark Gray', value: 'DarkGray' },
    { label: 'Imagery', value: 'Imagery' },
    { label: 'Imagery (Clarity)', value: 'ImageryClarity' },
    { label: 'Imagery (Firefly)', value: 'ImageryFirefly' },
    { label: 'Shaded Relief', value: 'ShadedRelief' }
  ];

  const [currentTheme, setCurrentTheme] = useState(
    themes.filter(theme => theme.value === userContext.userProps.basemapLayer)[0] || themes[0]
  );

  const [currentCRS, setCurrentCRS] = useState(
    !isNil(selectedCRS)
      ? crs.find(crsItem => crsItem.value === selectedCRS) || { label: 'WGS84 - 4326', value: 'EPSG:4326' }
      : selectedCRS
  );

  const [newPositionMarker, setNewPositionMarker] = useState();
  const [mapGeoJson, setMapGeoJson] = useState(
    geoJson === ''
      ? `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"srid": "EPSG:4326"}}`
      : geoJson
  );
  const [isNewPositionMarkerVisible, setIsNewPositionMarkerVisible] = useState(false);
  const [popUpVisible, setPopUpVisible] = useState(false);

  const mapRef = useRef();

  useEffect(() => {
    const map = mapRef.current.leafletElement;
    esri.basemapLayer(currentTheme.value).addTo(map);

    // const geojsonLayer = L.geoJson(geojson, {
    //   style: function (feature) {
    //     return { color: feature.properties.GPSUserColor };
    //   },
    //   pointToLayer: function (feature, latlng) {
    //     return new L.CircleMarker(latlng, { radius: 10, fillOpacity: 0.85 });
    //   },
    //   onEachFeature: function (feature, layer) {
    //     layer.bindPopup(feature.properties.GPSUserName);
    //   }
    // });

    // map.addLayer(geojsonLayer);

    // esri
    //   .tiledMapLayer({
    //     url: 'https://services.arcgisonline.com/ArcGIS/rest/services/USA_Topo_Maps/MapServer',
    //     tileSize: 256,
    //     maxZoom: 20,
    //     minZoom: 0
    //   })
    //   .addTo(map);

    // var defaultLayer = L.tileLayer(
    //   '//services.arcgisonline.com/arcgis/rest/services/ESRI_Imagery_World_2D/MapServer/tile/{z}/{y}/{x}',
    //   {
    //     attribution: false,
    //     continuousWorld: true,
    //     crs: '4326',
    //     tileSize: 512
    //   }
    // );
    // defaultLayer.addTo(map);

    // var map = new L.Map(mapRef.current.leafletElement).setView([45.543, -122.621], 5);

    const searchControl = new ELG.Geosearch().addTo(map);
    const results = new L.LayerGroup().addTo(map);

    searchControl.on('results', function (data) {
      results.clearLayers();
      if (TextUtils.areEquals(geometryType, 'POINT')) {
        for (let i = data.results.length - 1; i >= 0; i--) {
          setNewPositionMarker(`${data.results[i].latlng.lat}, ${data.results[i].latlng.lng}, EPSG:4326`);
          onSelectPoint(
            proj4(proj4('EPSG:4326'), proj4(currentCRS.value), [
              data.results[i].latlng.lat,
              data.results[i].latlng.lng
            ]),
            currentCRS.value
          );
        }
      }
    });

    // var service = esri.mapService({
    //   url: 'https://land.discomap.eea.europa.eu/arcgis/rest/services/Background/Background_Cashed_WGS84/MapServer'
    // });

    // service
    //   .identify()
    //   .on(map)
    //   .at([45.543, -12.621])
    //   .layers('Countries')
    //   .run(function (error, featureCollection, response) {
    //     if (error) {
    //       console.log(error);
    //       return;
    //     }
    //     console.log('UTC Offset: ' + featureCollection.features[0].properties.ZONE);
    //   });

    // setDraggablePointsCoordinates([
    //   [12.5874761, 55.6811578],
    //   [12.5944761, 55.6811578]
    // ]);
    // let map = L.map(element).setView([-41.2858, 174.78682], 14);

    // esri
    //   .featureLayer({
    //     url: 'https://sampleserver6.arcgisonline.com/arcgis/rest/services/Earthquakes_Since1970/MapServer/0'
    //   })
    //   .addTo(map);
  }, []);

  useEffect(() => {
    const inmMapGeoJson = JSON.parse(cloneDeep(mapGeoJson));
    if (inmMapGeoJson.properties.srid !== 'EPSG:4326') {
      inmMapGeoJson.geometry.coordinates = projectGeoJsonCoordinates(geoJson);
      setMapGeoJson(JSON.stringify(inmMapGeoJson));
    }
  }, [geoJson]);

  useEffect(() => {
    if (!isNewPositionMarkerVisible && !isNil(newPositionMarker)) {
      setIsNewPositionMarkerVisible(true);
    }
  }, [newPositionMarker]);

  useEffect(() => {
    const map = mapRef.current.leafletElement;
    // esri.removeLayer();
    esri.basemapLayer(currentTheme.value).addTo(map);
  }, [currentTheme]);

  const getCenter = () => {
    const defaultCenter = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"srid": "EPSG:4326"}}`;
    if (TextUtils.areEquals(geometryType, 'POINT')) {
      if (MapUtils.checkValidJSONCoordinates(geoJson)) {
        if (typeof geoJson === 'object') {
          return JSON.stringify(geoJson);
        } else {
          return geoJson;
        }
      } else {
        return defaultCenter;
      }
    } else {
      if (MapUtils.checkValidJSONMultipleCoordinates(geoJson)) {
        return `{"type": "Feature", "geometry": {"type":"${geometryType}","coordinates":[${MapUtils.getFirstPointComplexGeometry(
          geoJson,
          geometryType
        ).toString()}]}, "properties": {"srid": "${MapUtils.getSrid(geoJson)}"}}`;
      } else {
        return defaultCenter;
      }
    }
  };

  const getGeoJson = () => {
    switch (geometryType.toUpperCase()) {
      case 'POINT':
        if (MapUtils.checkValidJSONCoordinates(mapGeoJson)) {
          return (
            <GeoJSON
              coordsToLatLng={coords => new L.LatLng(coords[0], coords[1], coords[2])}
              data={JSON.parse(mapGeoJson)}
              onEachFeature={onEachFeature}
            />
          );
        }
        break;
      case 'LINESTRING':
      case 'MULTILINESTRING':
      case 'MULTIPOINT':
      case 'POLYGON':
      case 'MULTIPOLYGON':
        if (MapUtils.checkValidJSONMultipleCoordinates(mapGeoJson)) {
          return (
            <GeoJSON
              coordsToLatLng={coords => new L.LatLng(coords[0], coords[1], coords[2])}
              data={JSON.parse(mapGeoJson)}
              onEachFeature={onEachFeature}
            />
          );
        }
        break;
      default:
        break;
    }
  };

  const onCRSChange = item => {
    const selectedCRS = crs.filter(t => t.value === item.value)[0];
    setCurrentCRS(selectedCRS);
  };

  const onEachFeature = (feature, layer) => {
    if (TextUtils.areEquals(geometryType, 'POINT')) {
      layer.bindPopup(renderCoordinates({ data: feature.geometry.coordinates, isGeoJson: false }));
      layer.on({
        click: () =>
          mapRef.current.leafletElement.setView(feature.geometry.coordinates, mapRef.current.leafletElement.zoom)
      });
    } else {
      var bounds = layer.getBounds();
      var center = bounds.getCenter();
      mapRef.current.leafletElement.setView(center, mapRef.current.leafletElement.zoom);
    }
  };

  const onPrintCoordinates = coordinates => `{Lat: ${coordinates.split(', ')[0]}, Lng: ${coordinates.split(', ')[1]}}`;

  const onThemeChange = item => {
    const selectedTheme = themes.filter(t => t.value === item.value)[0];
    setCurrentTheme(selectedTheme);
  };

  const getSRID = srid => (!['EPSG:4326', 'EPSG:4258', 'EPSG:3035'].includes(srid) ? 'EPSG:4326' : srid);

  const projectGeoJsonCoordinates = (geoJsonData, isCenter = false) => {
    const parsedGeoJsonData = typeof geoJsonData === 'object' ? geoJsonData : JSON.parse(geoJsonData);
    const projectPoint = coordinate => {
      return MapUtils.checkValidCoordinates(coordinate)
        ? proj4(
            proj4(!isNil(parsedGeoJsonData) ? getSRID(parsedGeoJsonData.properties.srid) : currentCRS.value),
            proj4('EPSG:4326'),
            coordinate
          )
        : coordinate;
    };
    if (isCenter) {
      return projectPoint(parsedGeoJsonData.geometry.coordinates);
    } else {
      if (TextUtils.areEquals(geometryType, 'POINT')) {
        return projectPoint(parsedGeoJsonData.geometry.coordinates);
      } else {
        let projectedCoordinates = [];
        if (['POLYGON', 'MULTILINESTRING'].includes(geometryType)) {
          projectedCoordinates = parsedGeoJsonData.geometry.coordinates.map(ring =>
            ring.map(coordinate => projectPoint(coordinate))
          );
        } else if (['MULTIPOLYGON'].includes(geometryType)) {
          projectedCoordinates = parsedGeoJsonData.geometry.coordinates.map(polygon =>
            polygon.map(ring => ring.map(coordinate => projectPoint(coordinate)))
          );
        } else {
          projectedCoordinates = parsedGeoJsonData.geometry.coordinates.map(coordinate => projectPoint(coordinate));
        }
        return projectedCoordinates;
      }
    }
  };

  const projectPointCoordinates = coordinates => {
    return proj4(
      proj4(!isNil(coordinates.split(', ')[2]) ? coordinates.split(', ')[2] : currentCRS.value),
      proj4('EPSG:4326'),
      MapUtils.parseCoordinates(coordinates.split(', '))
    );
  };

  const renderCoordinates = ({ data, isGeoJson = false }) => {
    let popupContent = MapUtils.printCoordinates({
      data,
      isGeoJson,
      geometryType,
      firstCoordinateText: resourcesContext.messages['latitude'],
      secondCoordinateText: resourcesContext.messages['longitude']
    });

    if (!isNil(data) && currentCRS.value !== 'EPSG:4326') {
      popupContent = `${popupContent}             
      ${resourcesContext.messages['projectedCoordinates']}      
      ${MapUtils.printCoordinates({
        data: projectPointCoordinates({
          coordinates: isGeoJson ? JSON.parse(data).geometry.coordinates : data,
          CRS: 'EPSG:4326',
          newCRS: currentCRS.value
        }),
        isGeoJson,
        geometryType,
        firstCoordinateText: resourcesContext.messages[currentCRS.value === 'EPSG:3035' ? 'x' : 'latitude'],
        secondCoordinateText: resourcesContext.messages[currentCRS.value === 'EPSG:3035' ? 'y' : 'longitude']
      })}`;
    }
    return popupContent;
  };

  return (
    <Fragment>
      {hasLegend && (
        <div className={styles.pointLegendWrapper}>
          <div className={styles.pointLegendItem}>
            <div className={`${styles.pointLegendItemColour} ${styles.pointLegendItemColourCurrent}`} />
            <div className={styles.pointLegendItemLabel}>
              <label>
                {TextUtils.areEquals(geometryType, 'POINT')
                  ? resourcesContext.messages['currentPoint']
                  : resourcesContext.messages['geometryCoordinates']}
                :{' '}
              </label>
              <label data-for="coordinatesTooltip" data-tip>
                {MapUtils.checkValidJSONCoordinates(geoJson) || MapUtils.checkValidJSONMultipleCoordinates(geoJson)
                  ? MapUtils.printCoordinates(mapGeoJson, true, geometryType)
                  : `{Latitude: , Longitude: }`}
              </label>
            </div>
          </div>
          {TextUtils.areEquals(geometryType, 'POINT') && (
            <Fragment>
              <div className={styles.pointLegendItem}>
                <div className={`${styles.pointLegendItemColour} ${styles.pointLegendItemColourNew}`} />
                <div className={styles.pointLegendItemLabel}>
                  <label>{resourcesContext.messages['newPoint']}: </label>
                  <label>{MapUtils.printCoordinates(newPositionMarker, false, geometryType)}</label>
                </div>
              </div>
              <div className={styles.pointLegendItem}>
                <div className={styles.pointLegendItemInfoLabel}>
                  <label>{resourcesContext.messages['mapSelectPointMessage']}</label>
                </div>
              </div>
            </Fragment>
          )}
        </div>
      )}
      <div style={{ display: 'inline-flex', width: '80%' }}>
        <Dropdown
          ariaLabel={'themes'}
          className={styles.themeSwitcherSplitButton}
          onChange={e => onThemeChange(e.target.value)}
          optionLabel="label"
          options={themes}
          placeholder="Select a theme"
          style={{ width: '20%' }}
          value={currentTheme}
        />
        <Dropdown
          ariaLabel={'crs'}
          className={styles.crsSwitcherSplitButton}
          disabled={!MapUtils.checkValidJSONCoordinates(geoJson) && !isNewPositionMarkerVisible}
          onChange={e => {
            onCRSChange(e.target.value);
            onSelectPoint(
              proj4(
                proj4('EPSG:4326'),
                proj4(e.target.value.value),
                isNewPositionMarkerVisible
                  ? MapUtils.parseCoordinates(newPositionMarker.split(', '))
                  : JSON.parse(mapGeoJson).geometry.coordinates
              ),
              e.target.value.value
            );
          }}
          optionLabel="label"
          options={crs}
          placeholder="Select a CRS"
          style={{ width: '20%' }}
          value={currentCRS}
        />
      </div>
      <div>
        <MapComponent
          center={projectGeoJsonCoordinates(getCenter(), true)}
          doubleClickZoom={false}
          onDblclick={
            TextUtils.areEquals(geometryType, 'POINT')
              ? e => {
                  setNewPositionMarker(`${e.latlng.lat}, ${e.latlng.lng}, EPSG:4326`);
                  onSelectPoint(
                    proj4(proj4('EPSG:4326'), proj4(currentCRS.value), [e.latlng.lat, e.latlng.lng]),
                    currentCRS.value
                  );
                  mapRef.current.leafletElement.setView(e.latlng, mapRef.current.leafletElement.zoom);
                }
              : null
          }
          ref={mapRef}
          style={{ height: '60vh', marginTop: '6px' }}
          zoom="4">
          {getGeoJson()}
          {isNewPositionMarkerVisible && (
            <Marker
              draggable={false}
              icon={NewMarkerIcon}
              onClick={e => {
                if (!popUpVisible) {
                  setPopUpVisible(true);
                }
                mapRef.current.leafletElement.setView(e.latlng, mapRef.current.leafletElement.zoom);
              }}
              position={projectPointCoordinates(newPositionMarker)}>
              <Popup>{onPrintCoordinates(newPositionMarker)}</Popup>
            </Marker>
          )}
        </MapComponent>
      </div>
    </Fragment>
  );
};
