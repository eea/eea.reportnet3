import { AwesomeIcons } from 'conf/AwesomeIcons';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const getCheckTemplate = (rowData, column) => (
  <div style={{ textAlign: 'center' }}>
    {rowData[column.field] ? (
      <FontAwesomeIcon icon={AwesomeIcons('check')} style={{ color: 'var(--main-font-color)' }} />
    ) : null}
  </div>
);

export const DataTableUtils = { getCheckTemplate };
