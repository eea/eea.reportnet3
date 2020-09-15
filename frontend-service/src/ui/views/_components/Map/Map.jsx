import React, { useContext, useState, useEffect, useRef } from 'react';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import styles from './Map.module.scss';
import 'leaflet/dist/leaflet.css';
import 'esri-leaflet-geocoder/dist/esri-leaflet-geocoder.css';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { Dropdown } from 'ui/views/_components/Dropdown';
import { InputSwitch } from 'ui/views/_components/InputSwitch';

import 'proj4leaflet';
import L from 'leaflet';
import proj4 from 'proj4';
import { CRS } from 'leaflet';
import * as ELG from 'esri-leaflet-geocoder';
import * as esri from 'esri-leaflet';
import { Map as MapComponent, GeoJSON, TileLayer, Marker, LayersControl, Popup } from 'react-leaflet';
// import ReactMapboxGl, { Feature, Layer, Marker, Popup } from 'react-mapbox-gl';

import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import newMarkerIcon from 'assets/images/newMarker.png';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
// import { svg } from 'assets/svg/marker';

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 36]
});

// 3035 --> ETRS89 / ETRS-LAEA
// 4258 --> ETRS89
// 4326 --> WGS84

var geojson = `{"type":"featureCollections","features":[{"type":"Feature","properties":{"popupContent":"This is the Auraria West Campus","style":{"weight":2,"color":"#999","opacity":1,"fillColor":"#B0DE5C","fillOpacity":0.8}},"geometry":{"type":"MultiPolygon","coordinates":[[[[-105.00432014465332,39.74732195489861],[-105.00715255737305,39.7462000683517],[-105.00921249389647,39.74468219277038],[-105.01067161560059,39.74362625960105],[-105.01195907592773,39.74290029616054],[-105.00989913940431,39.74078835902781],[-105.00758171081543,39.74059036160317],[-105.00346183776855,39.74059036160317],[-105.00097274780272,39.74059036160317],[-105.00062942504881,39.74072235994946],[-105.00020027160645,39.74191033368865],[-105.0007152557373,39.74276830198601],[-105.00097274780272,39.74369225589818],[-105.00097274780272,39.74461619742136],[-105.00123023986816,39.74534214278395],[-105.00183105468751,39.74613407445653],[-105.00432014465332,39.74732195489861]],[[-105.00361204147337,39.74354376414072],[-105.00301122665405,39.74278480127163],[-105.00221729278564,39.74316428375108],[-105.00283956527711,39.74390674342741],[-105.00361204147337,39.74354376414072]]],[[[-105.00942707061768,39.73989736613708],[-105.00942707061768,39.73910536278566],[-105.00685214996338,39.73923736397631],[-105.00384807586671,39.73910536278566],[-105.00174522399902,39.73903936209552],[-105.00041484832764,39.73910536278566],[-105.00041484832764,39.73979836621592],[-105.00535011291504,39.73986436617916],[-105.00942707061768,39.73989736613708]]]]}},{"type":"Feature","geometry":{"type":"Point","coordinates":[-104.99728202819826,39.7449215603964]},"properties":{"GPSId":"9","DateTime":"7/12/2013 9:05:00 AM","GPSUserName":"fake4","GPSUserColor":"#00FF57"}}]}`;

proj4.defs([
  ['EPSG:4258', '+proj=longlat +ellps=GRS80 +no_defs'],
  ['EPSG:3035', '+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +units=m +no_defs'],
  ['EPSG:4326', '+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs']
]);

const crsProj4258 = new L.Proj.CRS('EPSG:4258', '+proj=longlat +ellps=GRS80 +no_defs', {
  origin: [0, 0],
  resolutions: [8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5, 0.25, 0.125, 0.0625, 0.03125]
});

const crsProj3035 = new L.Proj.CRS(
  'EPSG:3035',
  '+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +units=m +no_defs',
  {
    origin: [0, 0],
    resolutions: [8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5, 0.25, 0.125, 0.0625, 0.03125]
  }
);

const crsProj4326 = new L.Proj.CRS('EPSG:4326', '+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs', {
  origin: [0, 0],
  resolutions: [8192, 4096, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5, 0.25, 0.125, 0.0625, 0.03125]
});

var crs25830 = new L.Proj.CRS(
  'EPSG:25830',
  '+proj=utm +zone=30 +ellps=GRS80 +units=m +no_defs', //http://spatialreference.org/ref/epsg/25830/proj4/
  {
    resolutions: [2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1, 0.5]
    //Origen de servicio tileado
    //		origin:[0,0]
  }
);

L.Marker.prototype.options.icon = DefaultIcon;

let NewMarkerIcon = L.icon({
  iconUrl: newMarkerIcon,
  iconSize: [25, 41],
  iconAnchor: [12, 36]
  // iconSize: [27, 31],
  // iconAnchor: [13.5, 17.5],
  // popupAnchor: [0, -11]
});

export const Map = ({
  centerToCoordinates = false,
  coordinates = '',
  onSelectPoint,
  options = {
    zoom: [15],
    bearing: [0],
    pitch: [0],
    center: coordinates !== '' ? coordinates : `55.6811608, 12.5844761, EPSG:4326`
  },
  selectedCRS = { label: 'WGS84', value: 'EPSG:4326' }
}) => {
  console.log({ coordinates }, options.center);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const { BaseLayer, Overlay } = LayersControl;

  const crs = [
    { label: 'WGS84', value: 'EPSG:4326' },
    { label: 'ETRS89', value: 'EPSG:4258' },
    { label: 'LAEA-ETRS89', value: 'EPSG:3035' }
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
  console.log(
    { selectedCRS },
    !isNil(selectedCRS) ? crs.filter(crsItem => crsItem.value === selectedCRS)[0] : selectedCRS
  );
  const [currentCRS, setCurrentCRS] = useState(
    !isNil(selectedCRS) ? crs.filter(crsItem => crsItem.value === selectedCRS)[0] : selectedCRS
  );

  const [marker, setMarker] = useState(options.center);
  const [newPositionMarker, setNewPositionMarker] = useState();
  const [isNewPositionMarkerVisible, setIsNewPositionMarkerVisible] = useState(false);
  const [popUpVisible, setPopUpVisible] = useState(false);

  const mapRef = useRef();

  useEffect(() => {
    const map = mapRef.current.leafletElement;
    console.log('map crs: ' + map.options.crs.code, map.options.crs);
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

    // mapRef.current.leafletElement.setView(options.center, 2);
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
      for (let i = data.results.length - 1; i >= 0; i--) {
        setNewPositionMarker(`${data.results[i].latlng.lat}, ${data.results[i].latlng.lng}, EPSG:4326`);
        // results.addLayer(L.marker(data.results[i].latlng));
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
    console.log('ESRI::', esri);

    // esri
    //   .featureLayer({
    //     url: 'https://sampleserver6.arcgisonline.com/arcgis/rest/services/Earthquakes_Since1970/MapServer/0'
    //   })
    //   .addTo(map);
  }, []);

  useEffect(() => {
    if (!isNewPositionMarkerVisible && !isNil(newPositionMarker)) {
      setIsNewPositionMarkerVisible(true);
    }
  }, [newPositionMarker]);

  useEffect(() => {
    console.log({ isNewPositionMarkerVisible, newPositionMarker, marker });
    if (isNewPositionMarkerVisible && !isNil(newPositionMarker)) {
      console.log({ newPositionMarker });
      console.log(projectCoordinates(newPositionMarker));
      setNewPositionMarker(`${projectCoordinates(newPositionMarker).join(', ')}, ${currentCRS.value}`);
    }
    // if (!isNil(marker)) {
    //   setMarker(
    //     `${projectCoordinates(Array.isArray(marker) ? marker.join(', ') : marker).join(', ')}, ${currentCRS.value}`
    //   );
    // }
  }, [currentCRS]);

  useEffect(() => {
    console.log({ newPositionMarker, marker });
  }, [newPositionMarker, marker]);

  useEffect(() => {
    const map = mapRef.current.leafletElement;
    // esri.removeLayer();
    esri.basemapLayer(currentTheme.value).addTo(map);
  }, [currentTheme]);

  // const onDragEndPolygon = (evt, index) => {
  //   const inmDraggablePointsCoordinates = [...draggablePointsCoordinates];
  //   inmDraggablePointsCoordinates[index] = [evt.lngLat.lng, evt.lngLat.lat];
  //   setDraggablePointsCoordinates(inmDraggablePointsCoordinates);
  // };

  // const basemapTemplate = option => {
  //   console.log({ option });
  //   if (!option.value) {
  //     return option.label;
  //   } else {
  //     return (
  //       <div className={`p-clearfix ${styles.basemapItem}`}>
  //         <span style={{ margin: '.5em .25em 0 0.5em' }}>{option.label}</span>
  //         <img alt={option.label} src={`assets/img/layers/Streets.png`} />
  //       </div>
  //     );
  //   }
  // };

  const onCRSChange = item => {
    const selectedCRS = crs.filter(t => t.value === item.value)[0];
    setCurrentCRS(selectedCRS);
    // if (!isNewPositionMarkerVisible) {
    //   console.log(options.center, parseCoordinates(options.center));
    //   console.log(proj4(proj4('EPSG:4326'), proj4(selectedCRS.value), parseCoordinates(options.center)));
    //   onSelectPoint(
    //     proj4(proj4('EPSG:4326'), proj4(selectedCRS.value), parseCoordinates(options.center)),
    //     selectedCRS.value
    //   );
    // }
  };

  const onPrintCoordinates = coordinates => `{Lat: ${coordinates.split(', ')[0]}, Lng: ${coordinates.split(', ')[1]}}`;

  const onThemeChange = item => {
    const selectedTheme = themes.filter(t => t.value === item.value)[0];
    setCurrentTheme(selectedTheme);
  };

  const parseCoordinates = coordinates => {
    console.log({ coordinates });
    return [parseFloat(coordinates.split(', ')[0]), parseFloat(coordinates.split(', ')[1])];
  };

  const projectCoordinates = coordinates => {
    console.log(coordinates);
    console.log(
      proj4(
        proj4(!isNil(coordinates.split(', ')[2]) ? coordinates.split(', ')[2] : currentCRS.value),
        proj4('EPSG:4326'),
        parseCoordinates(coordinates)
      )
    );
    // console.log(proj4(proj4(coordinates.split(', ')[2]), proj4(currentCRS.value), parseCoordinates(coordinates)));
    // console.log(proj4(proj4(currentCRS.value), proj4('EPSG:4326'), parseCoordinates(coordinates)));
    return proj4(
      proj4(!isNil(coordinates.split(', ')[2]) ? coordinates.split(', ')[2] : currentCRS.value),
      proj4('EPSG:4326'),
      parseCoordinates(coordinates)
    );
  };

  // const parseCoordinatesSRID = coord => {
  //   coord = 'SRID=4326;POINT(111.1111111 1.1111111)';
  //   const splitedCoords = coord.split(';');
  //   const SRID = splitedCoords[0];
  //   const coordData = splitedCoords[1];

  //   const parsedCoords = coordData
  //     .substring(coordData.indexOf('(') + 1)
  //     .replace(/\(/g, '')
  //     .replace(/\)/g, '')
  //     .split(' ')
  //     .map(c => Number(c))
  //     .reduce((result, value, i, array) => {
  //       if (i % 2 === 0) result.push(array.slice(i, i + 2));
  //       return result;
  //     }, []);
  //   if (parsedCoords.length > 1) {
  //     return parsedCoords;
  //   } else {
  //     return parsedCoords[0];
  //   }
  // };

  return (
    <>
      <Dropdown
        ariaLabel={'themes'}
        className={styles.themeSwitcherSplitButton}
        // itemTemplate={basemapTemplate}
        options={themes}
        optionLabel="label"
        onChange={e => {
          onThemeChange(e.target.value);
        }}
        placeholder="Select a theme"
        value={currentTheme}
        style={{ width: '20%' }}
      />
      <Dropdown
        ariaLabel={'crs'}
        className={styles.crsSwitcherSplitButton}
        options={crs}
        optionLabel="label"
        onChange={e => {
          onCRSChange(e.target.value);
        }}
        placeholder="Select a CRS"
        value={currentCRS}
        style={{ width: '20%' }}
      />
      <MapComponent
        // crs={CRS[currentCRS.value]}
        // crs={crs3857}
        // continuousWorld={true}
        // worldCopyJump={false}
        style={{ height: '60vh' }}
        doubleClickZoom={false}
        // fitBounds={[[40.712, -74.227]]}
        center={projectCoordinates(options.center)}
        // setView={([42.528, -12.68], 2)}
        zoom="10"
        ref={mapRef}
        onClick={e => {
          // console.log(
          //   [e.latlng.lat, e.latlng.lng],
          //   proj4(proj4('EPSG:4326'), proj4(currentCRS.value), [e.latlng.lat, e.latlng.lng])
          // );
          // var proj = crsProj4258.projection.project(e.latlng);
          // console.log(proj4(proj4('EPSG:4326'), proj4('EPSG:3035'), [e.latlng.lat, e.latlng.lng]));
          // console.log(proj4(proj4('EPSG:4326'), proj4('EPSG:4258'), [e.latlng.lat, e.latlng.lng]));
          // console.log(proj4(proj4('EPSG:3035'), proj4('EPSG:4326'), [9323919.149606757, 307743.5211649621]));
          // console.log(proj);
          console.log(e.latlng.lat);
          setNewPositionMarker(`${e.latlng.lat}, ${e.latlng.lng}, EPSG:4326`);
          onSelectPoint(
            // [e.latlng.lat, e.latlng.lng],
            proj4(proj4('EPSG:4326'), proj4(currentCRS.value), [e.latlng.lat, e.latlng.lng]),
            currentCRS.value
          );
        }}>
        <LayersControl position="topright">
          <BaseLayer checked name="EEA Countries">
            <TileLayer
              // attribution="Tiles &copy; Esri &mdash; Source: Esri, DeLorme, NAVTEQ, USGS, Intermap, iPC, NRCAN, Esri Japan, METI, Esri China (Hong Kong), Esri (Thailand), TomTom, 2012"
              url="https://land.discomap.eea.europa.eu/arcgis/rest/services/Background/Background_Cashed_WGS84/MapServer/tile/{z}/{y}/{x}"
              opacity={0.35}
            />
          </BaseLayer>
          <BaseLayer name="None">
            <TileLayer url="" />
          </BaseLayer>
        </LayersControl>
        {/* <TileLayer
          attribution='&amp;copy <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
          url="https://land.discomap.eea.europa.eu/arcgis/rest/services/Background/Background_Cashed_WGS84/MapServer"
        /> */}
        {<GeoJSON data={JSON.parse(geojson)} />}
        {console.log(newPositionMarker)}
        {isNewPositionMarkerVisible && (
          <Marker
            className={`${styles.marker} ${styles.bounce}`}
            draggable={true}
            icon={NewMarkerIcon}
            position={projectCoordinates(newPositionMarker)}
            onClick={e => {
              if (!popUpVisible) {
                setPopUpVisible(true);
              }
            }}
            onDrag={e => setNewPositionMarker(`${e.latlng.lat}, ${e.latlng.lng}, EPSG:4326`)}>
            <Popup>{onPrintCoordinates(newPositionMarker)}</Popup>
          </Marker>
        )}
        {console.log(marker)}
        <Marker
          position={projectCoordinates(marker)}
          onClick={e => {
            if (!popUpVisible) {
              setPopUpVisible(true);
            }
          }}>
          {/* <div className={`${styles.marker} ${styles.bounce}`}></div>
          <div className={styles.pulse}></div> */}
          <Popup>{onPrintCoordinates(marker)}</Popup>
        </Marker>
        {/* <div className="pointer" /> */}
      </MapComponent>
    </>
  );
};
