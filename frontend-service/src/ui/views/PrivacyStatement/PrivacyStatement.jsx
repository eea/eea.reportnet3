import React, { useContext, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './PrivacyStatement.module.scss';

import { routes } from 'ui/routes';
import { MainLayout } from 'ui/views/_components/Layout';
import { Title } from 'ui/views/_components/Title';
import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { LeftSideBarContext } from 'ui/views/_functions/Contexts/LeftSideBarContext';

const PrivacyStatement = withRouter(({ history }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const resources = useContext(ResourcesContext);
  const leftSideBarContext = useContext(LeftSideBarContext);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: '',
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['privacyPolicy'],
        icon: 'info',
        href: getUrl(routes.PRIVACY_STATEMENT),
        command: () => history.push(getUrl(routes.PRIVACY_STATEMENT))
      }
    ]);
  }, []);

  useEffect(() => {
    leftSideBarContext.addModels([]);
  }, []);

  const layout = children => {
    return (
      <MainLayout
        leftSideBarConfig={{
          buttons: []
        }}>
        <div className>
          <div className="rep-container">{children}</div>
        </div>
      </MainLayout>
    );
  };

  return layout(
    <div className="rep-row">
      <div className={` rep-col-12 rep-col-sm-12`}>
        <Title
          title={resources.messages['PrivacyPolicyTitle']}
          icon="info"
          iconSize="4rem"
          subtitle={resources.messages['PrivacyPolicySubtitle']}
        />

        <div className={styles.sectionMainContent}>
          <h3>{resources.messages['gdprIntroduction']}</h3>
          <p>{resources.messages['gdprIntroductionMessage']}</p>
          <h3>{resources.messages['gdprDataCollectedTitle']}</h3>
          <p>
            {resources.messages['gdprDataCollectedMessage']}
            <ul>
              <li>
                {resources.messages['gdprUserId']}
                {resources.messages['gdprRequiredCharacter']}
              </li>
              <li>
                {resources.messages['gdprFirstName']} {resources.messages['gdprRequiredCharacter']}
              </li>
              <li>
                {resources.messages['gdprLastName']} {resources.messages['gdprRequiredCharacter']}
              </li>
              <li>
                {resources.messages['gdprFullName']} {resources.messages['gdprNativeLanguage']}
              </li>
              <li>
                {resources.messages['gdprReason']} {resources.messages['gdprRequiredCharacter']}
              </li>
              <li>{resources.messages['gdprJobTitle']}</li>
              <li>
                {resources.messages['gdprEmail']}
                {resources.messages['gdprRequiredCharacter']}
              </li>
              <li>{resources.messages['gdprURL']}</li>
              <li>{resources.messages['gdprPostalAddress']}</li>
              <li>{resources.messages['gdprTelephoneNumber']}</li>
              <li>{resources.messages['gdprMobileTelephoneNumber']}</li>
              <li>{resources.messages['gdprFaxNumber']}</li>
              <li>
                {resources.messages['gdprOrganisation']} {resources.messages['gdprRequiredCharacter']}
              </li>
              <li>{resources.messages['gdprDepartment']}</li>
              {resources.messages['gdprRequiredCharacter']} {resources.messages['gdprRequiredMessage']}
            </ul>
          </p>
          <h3>{resources.messages['gdprWhoCanSee']}</h3>
          <p>
            {resources.messages['gdprWhoCanSeeMessage']}
            <ul>
              <li>
                {resources.messages['gdprJobTitle']} {resources.messages['gdprIfAvailable']}
              </li>
              <li>{resources.messages['gdprGivenName']}</li>
              <li>{resources.messages['gdprSurname']}</li>
              <li>{resources.messages['gdprOrganisation']}</li>
              <li>
                {resources.messages['gdprUserProfilePicture']} {resources.messages['gdprUserProfilePictureMessage']}
              </li>
              <li>{resources.messages['gdprEmail']}</li>
              <li>
                {resources.messages['gdprDepartment']} {resources.messages['gdprIfAvailable']}
              </li>
              <li>
                {resources.messages['gdprTelephoneNumber']} {resources.messages['gdprIfAvailable']}
              </li>
              <li>
                {resources.messages['gdprMobileTelephoneNumber']} {resources.messages['gdprIfAvailable']}
              </li>
              <li>
                {resources.messages['gdprFaxNumber']} {resources.messages['gdprIfAvailable']}
              </li>
              <li>
                {resources.messages['gdprPostalAddress']} {resources.messages['gdprIfAvailable']}
              </li>
            </ul>
          </p>
        </div>
      </div>
    </div>
  );
});

export { PrivacyStatement };
