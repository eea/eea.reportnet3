import { AwesomeIcons } from 'conf/AwesomeIcons';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const getCheckTemplate = (rowData, column, styleCheckValueColumn, stylesIcon) => (
  <div className={styleCheckValueColumn}>
    {rowData[column.field] ? <FontAwesomeIcon className={stylesIcon} icon={AwesomeIcons('check')} /> : null}
  </div>
);

export const DataTableUtils = { getCheckTemplate };
