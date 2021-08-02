import { storiesOf } from '../../../../../.storybook/storiesOf';
import { action } from '@storybook/addon-actions';
import { Button } from './Button';

storiesOf('Button', module)
  .add('Default', () => <Button label="Default" />)
  .add('Default rounded', () => <Button className={`p-button-rounded`} label="Default rounded" />)
  .add('Disabled', () => <Button disabled={true} label="Disabled" />)
  .add('Clickable', () => <Button label="Click me!" onClick={action('clicked')} />)
  .add('Secondary', () => <Button className={`p-button-rounded p-button-secondary`} label="Secondary" />)
  .add('Secondary transparent', () => (
    <Button className={`p-button-rounded p-button-secondary-transparent`} label="Secondary transparent" />
  ))
  .add('Icon Right', () => <Button className={`p-button-rounded`} icon="eye" iconPos="right" label="Icon Right" />)
  .add('Icon Left Secondary', () => (
    <Button className={`p-button-rounded p-button-secondary`} icon="eye" iconPos="left" label="Icon Left & secondary" />
  ))
  .add('Animated', () => (
    <Button
      className={`p-button-rounded p-button-secondary p-button-animated-blink`}
      icon="eye"
      iconPos="left"
      label="Animated button"
    />
  ));
