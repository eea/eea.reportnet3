import { Component } from 'react';
import styles from './Menu.module.css';
import { Icon } from 'ui/views/_components/Icon';

class Menu extends Component {
  constructor(props) {
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
    const menu = event.currentTarget.nextSibling;

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
                bottom: `-${menu.offsetHeight}px`
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
                    <Icon icon={item.icon} />
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
