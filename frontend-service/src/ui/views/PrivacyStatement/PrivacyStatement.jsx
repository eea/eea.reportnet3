import React, { useContext, useEffect } from 'react';
import { withRouter } from 'react-router-dom';

import styles from './PrivacyStatement.module.scss';

import { PublicLayout } from 'ui/views/_components/Layout';
import { Title } from 'ui/views/_components/Title';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const PrivacyStatement = withRouter(({ history }) => {
  const resources = useContext(ResourcesContext);

  const onClickAnchorLink = e => {
    e.preventDefault();
    const anchorLink = e.target;
    const anchorTarget = document.querySelector(`#${anchorLink.href.split('#')[1]}`);
    const header = document.querySelector('#header');
    let scrollHeight = anchorTarget.getBoundingClientRect().top;

    if (header.clientHeight === 180) {
      scrollHeight = anchorTarget.getBoundingClientRect().top - 100;
      if (window.innerWidth <= 768) {
        scrollHeight = scrollHeight - 100;
      }
    }
    window.scroll({
      top: scrollHeight,
      left: 0,
      behavior: 'smooth'
    });
  };

  const layout = children => {
    return (
      <PublicLayout>
        <div className>
          <div className="rep-container">{children}</div>
        </div>
      </PublicLayout>
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
        <div className={styles.contentMain}>
          <aside>
            <ul>
              <li>
                <a className={styles.anchorLink} onClick={e => onClickAnchorLink(e)} href="#gdprIntroduction">
                  {resources.messages['gdprIntroduction']}
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} onClick={e => onClickAnchorLink(e)} href="#gdprDataCollectedTitle">
                  {resources.messages['gdprDataCollectedTitle']}
                </a>
              </li>
              <li>
                <a className={styles.anchorLink} onClick={e => onClickAnchorLink(e)} href="#gdprWhoCanSee">
                  {resources.messages['gdprWhoCanSee']}
                </a>
              </li>
            </ul>
          </aside>
          <div className={styles.privacyStatementContent}>
            <section id="gdprIntroduction">
              <h3>{resources.messages['gdprIntroduction']}</h3>
              <p>{resources.messages['gdprIntroductionMessage']}</p>
            </section>
            <section id="gdprDataCollectedTitle">
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
            </section>
            <section id="gdprWhoCanSee">
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
            </section>
          </div>
        </div>
      </div>
    </div>
  );
});

export { PrivacyStatement };
