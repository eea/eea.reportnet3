import { useContext } from 'react';

import styles from './GeoJSONError.module.scss';

import { useSetRecoilState } from 'recoil';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { geoJSONListStore } from 'views/_components/CoordinatesMoreInfo/_functions/Stores/geoJSONListStore';

export const GeoJSONError = ({ line, message }) => {
  const resourcesContext = useContext(ResourcesContext);
  const setSelectedLine = useSetRecoilState(geoJSONListStore);

  return (
    <li className={styles.geoJSONError} onClick={() => setSelectedLine(line)}>
      {message} - {`${resourcesContext.messages['line']}: ${line}`}
    </li>
  );
};
