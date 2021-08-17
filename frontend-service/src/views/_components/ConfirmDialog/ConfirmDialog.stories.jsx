import './ConfirmDialog.stories.module.css';

import { storiesOf } from '../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { ConfirmDialog } from './ConfirmDialog';

storiesOf('Confirm Dialog', module)
  .add('Default', () => (
    <ConfirmDialog
      header={'This is the confirm dialog header'}
      labelCancel={'Cancel'}
      labelConfirm={'Yes'}
      onConfirm={action('Confirm')}
      onHide={action('hide')}
      visible={true}>
      {'This is the Confirm dialog body'}
    </ConfirmDialog>
  ))
  .add('With content', () => (
    <ConfirmDialog
      header={'This is a confirm dialog with content header'}
      labelCancel={'Cancel'}
      labelConfirm={'Yes'}
      onConfirm={action('Confirm')}
      onHide={action('hide')}
      visible={true}>
      {
        <table>
          <thead>
            <tr>
              <th>Company</th>
              <th>Contact</th>
              <th>Country</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Alfreds Futterkiste</td>
              <td>Maria Anders</td>
              <td>Germany</td>
            </tr>
            <tr>
              <td>Centro comercial Moctezuma</td>
              <td>Francisco Chang</td>
              <td>Mexico</td>
            </tr>
            <tr>
              <td>Ernst Handel</td>
              <td>Roland Mendel</td>
              <td>Austria</td>
            </tr>
            <tr>
              <td>Island Trading</td>
              <td>Helen Bennett</td>
              <td>UK</td>
            </tr>
            <tr>
              <td>Laughing Bacchus Winecellars</td>
              <td>Yoshi Tannamuri</td>
              <td>Canada</td>
            </tr>
            <tr>
              <td>Magazzini Alimentari Riuniti</td>
              <td>Giovanni Rovelli</td>
              <td>Italy</td>
            </tr>
          </tbody>
        </table>
      }
    </ConfirmDialog>
  ));
