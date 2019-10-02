import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './VisibilityMenu.module.css';

class VisibilityMenu extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      style: {
        display: 'none'
      },
      htmlElement: document.getElementsByTagName('html')[0],
      listener: null,
      fields: []
    };
    ['show', 'updateChecked'].map(item => {
      this[item] = this[item].bind(this);
    });
  }

  componentDidUpdate(prevProps) {
    const { columns } = this.props;
    if (prevProps.columns !== columns) {
      const fields = columns.map(column => ({
        checked: true,
        label: column.label,
        key: column.key
      }));
      this.setState(state => {
        return { ...state, fields: fields };
      });
    }
  }

  show(event) {
    // if (this.state.listener) this.state.htmlElement.removeEventListener('click', this.state.listener, false);
    const { currentTarget } = event;
    const { nextSibling } = currentTarget;
    const left = currentTarget.offsetLeft;

    if (nextSibling.style.display === 'none') {
      nextSibling.style.display = 'block';
      nextSibling.style.left = `${left}px`;

      setTimeout(() => {
        nextSibling.style.opacity = 1;
      }, 50);
    } else {
      nextSibling.style.display = 'none';
      nextSibling.style.opacity = 0;
    }
  }
  updateChecked(fieldKey) {
    let checked = null;
    const fields = this.state.fields.map(field => {
      if (field.key === fieldKey) {
        field.checked = !field.checked;
        checked = !field.checked;
      }
      return field;
    });
    if (checked) {
      this.props.hideColumn(fieldKey);
    } else {
      this.props.addColumn(fieldKey);
    }
    this.setState(state => {
      return {
        ...state,
        fields: fields
      };
    });
  }
  render() {
    const { fields } = this.state;
    return (
      <div className={styles.visibilityMenu} style={{ display: 'none', opacity: 0 }}>
        <ul>
          {fields.map((field, i) => (
            <li key={i}>
              <a
                onClick={e => {
                  this.updateChecked(field.key);
                }}>
                <FontAwesomeIcon icon={field.checked ? AwesomeIcons('checkedSquare') : AwesomeIcons('square')} />
                {field.label}
              </a>
            </li>
          ))}
        </ul>
      </div>
    );
  }
}

export { VisibilityMenu };
