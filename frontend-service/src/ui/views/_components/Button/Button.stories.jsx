import { storiesOf } from '../../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { Button } from './Button';

storiesOf('Button', module)
  .add('Default', () => <Button label="Default" />)
  .add('Default rounded', () => <Button label="Default rounded" className={`p-button-rounded`} />)
  .add('Disabled', () => <Button label="Disabled" disabled={true} />)
  .add('Clickable', () => <Button label="Click me!" onClick={action('clicked')} />)
  .add('Secondary', () => <Button label="Secondary" className={`p-button-rounded p-button-secondary`} />)
  .add('Secondary transparent', () => (
    <Button label="Secondary transparent" className={`p-button-rounded p-button-secondary-transparent`} />
  ))
  .add('Icon Right', () => <Button label="Icon Right" icon="eye" iconPos="right" className={`p-button-rounded`} />)
  .add('Icon Left Secondary', () => (
    <Button label="Icon Left & secondary" icon="eye" iconPos="left" className={`p-button-rounded p-button-secondary`} />
  ))
  .add('Animated', () => (
    <Button
      label="Animated button"
      icon="eye"
      iconPos="left"
      className={`p-button-rounded p-button-secondary p-button-animated-blink`}
    />
  ));
