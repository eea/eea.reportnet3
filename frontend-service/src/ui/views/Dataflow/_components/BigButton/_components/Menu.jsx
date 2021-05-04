import { Component } from 'react';
import styles from './Menu.module.css';
import { Icon } from 'ui/views/_components/Icon';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class Menu extends Component {
  constructor(props) {
    console.log('props :>> ', props);
    super(props);
    this.state = {
      style: {
        display: 'none'
      },
      menuClick: false
    };

    ['show', 'hide'].map(item => {
      this[item] = this[item].bind(this);
    });
  }

  hide() {
    if (!this.state.menuClick) {
      this.setState(
        state => {
          return {
            ...state,
            style: {
              ...state.style,
              display: 'none'
            }
          };
        },
        () => {
          document.removeEventListener('click', this.hide, false);
        }
      );
    } else {
      this.setState(state => {
        return {
          ...state,
          menuClick: false
        };
      });
    }
  }
  show(event) {
    console.log('event :>> ', event.target);
    // const menu = event.target.getBoundingClientRect();
    const menu = event.currentTarget.nextSibling;
    const buttonPosition = event.target.getBoundingClientRect();
    console.log('menu :>> ', menu);

    this.setState(
      state => {
        return {
          ...state,
          style: {
            ...state.style,
            display: 'block'
          }
        };
      },
      () => {
        document.addEventListener('click', this.hide);
        this.setState(
          state => {
            return {
              ...state,
              style: {
                ...state.style,
                bottom: `-${menu.offsetHeight}px`,
                // bottom: `-${menu.bottom}px`,
                left: `${buttonPosition.left}px`
              }
            };
          },
          () => {
            setTimeout(() => {
              this.setState(state => {
                return {
                  ...state,
                  style: {
                    ...state.style,
                    opacity: 1
                  }
                };
              });
            }, 50);
          }
        );
      }
    );
  }

  render() {
    const { model } = this.props;
    if (model) {
      return (
        <div className={`${styles.dropDownMenu} p-menu-overlay-visible`} style={this.state.style}>
          <ul>
            {model ? (
              model.map((item, i) => (
                <li key={i}>
                  <a
                    className={item.disabled ? styles.menuItemDisabled : null}
                    onClick={e => {
                      e.preventDefault();
                      if (!item.disabled) item.command();
                      else
                        this.setState(state => {
                          return { ...state, menuClick: true };
                        });
                    }}
                    disabled={item.disabled}>
                      {/* <Icon icon={item.icon} /> */}
                    <FontAwesomeIcon icon={AwesomeIcons(item.icon)}/>
                    {item.label}
                  </a>
                </li>
              ))
            ) : (
              <li></li>
            )}
          </ul>
        </div>
      );
    } else {
      return <></>;
    }
  }
}

export { Menu };
