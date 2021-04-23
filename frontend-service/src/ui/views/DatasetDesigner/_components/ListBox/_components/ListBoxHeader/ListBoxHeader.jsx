import PropTypes from 'prop-types';
import { InputText } from 'ui/views/_components/InputText';

const ListBoxHeader = ({ filter = null, disabled = false, onFilter = null }) => {
  const onFilterListBoxHeader = event => {
    if (onFilter) {
      onFilter({
        originalEvent: event,
        query: event.target.value
      });
    }
  };
  return (
    <div className="p-listbox-header">
      <div className="p-listbox-filter-container">
        <InputText type="text" value={filter} onChange={onFilterListBoxHeader} disabled={disabled} />
        <span className="p-listbox-filter-icon pi pi-search"></span>
      </div>
    </div>
  );
};

ListBoxHeader.propTypes = {
  filter: PropTypes.string,
  disabled: PropTypes.bool,
  onFilter: PropTypes.func
};

export { ListBoxHeader };
