import React, { Fragment } from 'react';

import styles from './EuFooter.module.scss';

export const EuFooter = ({ leftMargin }) => {
  const topContent = [
    {
      blockTitle: 'Contact the EU',
      content: [
        {
          title: 'Call 00 800 6 7 8 9 10 11. See details of service',
          url: 'https://europa.eu/european-union/contact/call-us_en'
        },
        {
          title: 'E-mail your questions about the EU',
          url: 'https://europa.eu/european-union/contact/write-to-us_en'
        },
        {
          title: 'EU in your country',
          url: 'https://europa.eu/european-union/contact_en#row_4'
        },
        {
          title: 'Local information services in the EU',
          url: 'https://europa.eu/european-union/contact/meet-us_en'
        },
        {
          title: 'Institutions, bodies and agencies',
          url: 'https://europa.eu/european-union/contact/institutions-bodies_en'
        },
        {
          title: 'Press contacts',
          url: 'https://europa.eu/newsroom/press-contacts_en'
        }
      ]
    },
    {
      blockTitle: 'Find EU social media accounts',
      content: [
        {
          title: 'Use this tool to search for EU social media ',
          url: 'https://europa.eu/european-union/contact/social-networks_en'
        },
        {
          title: 'channels',
          url: 'https://europa.eu/european-union/contact/social-networks_en'
        }
      ]
    },
    {
      blockTitle: 'Find a European institution',
      content: [
        {
          title: 'European Parliament',
          url: 'http://www.europarl.europa.eu/portal/en'
        },
        {
          title: 'European Council',
          url: 'http://www.consilium.europa.eu/en/european-council/'
        },
        {
          title: 'Council of the European Union',
          url: 'http://www.consilium.europa.eu/en/home/'
        },
        {
          title: 'European Commission',
          url: 'https://ec.europa.eu/'
        },
        {
          title: 'Court of Justice of the European Union',
          url: 'https://curia.europa.eu/jcms/jcms/j_6/en/'
        },
        {
          title: 'European Central Bank',
          url: 'https://www.ecb.europa.eu/home/html/index.en.html'
        },
        {
          title: 'European Court of Auditors',
          url: 'https://www.eca.europa.eu/Pages/Splash.aspx'
        },
        {
          title: 'European External Action Service',
          url: 'https://eeas.europa.eu/headquarters/headquarters-homepage_en'
        },
        {
          title: 'European Economic and Social Committee',
          url: 'https://www.eesc.europa.eu/'
        },
        {
          title: 'European Committee of the Regions',
          url: 'https://cor.europa.eu/'
        },
        {
          title: 'European Investment Bank',
          url: 'http://www.eib.org/'
        },
        {
          title: 'European Ombudsman',
          url: 'https://www.ombudsman.europa.eu/en/home.faces'
        },
        {
          title: 'European Data Protection Supervisor',
          url: 'https://edps.europa.eu/'
        },
        {
          title: 'European Personnel Selection Office',
          url: 'https://epso.europa.eu/home_en'
        },
        {
          title: 'Publications Office of the European Union',
          url: 'https://publications.europa.eu/en/home'
        },
        {
          title: 'Agencies',
          url: 'https://europa.eu/european-union/about-eu/agencies_en'
        }
      ]
    }
  ];
  const bottomContent = [
    {
      title: 'Work for the European Union',
      url: 'https://europa.eu/european-union/about-eu/working_en'
    },
    {
      title: 'Cookies',
      url: 'https://europa.eu/european-union/abouteuropa/cookies_en'
    },
    {
      title: 'Reportnet 3.0 Data Privacy Notice',
      url: ''
    },
    {
      title: 'Legal notice',
      url: 'https://europa.eu/european-union/abouteuropa/cookies_en'
    },
    {
      title: 'Language policy',
      url: 'https://europa.eu/european-union/abouteuropa/language-policy_en'
    },
    {
      title: 'Web accessibility',
      url: 'https://europa.eu/european-union/abouteuropa/accessibility_en'
    }
  ];
  return (
    <Fragment>
      <div className={styles.footer} style={{ marginLeft: leftMargin, transition: '0.5s' }}>
        <div className="rep-container">
          <div className={styles.footerTop}>
            {topContent.map(block => {
              return (
                <div className={styles.contentBlock}>
                  <h3>{block.blockTitle}</h3>
                  <div className={styles.linksWrapper}>
                    {block.content.map(blockContent => {
                      return (
                        <p>
                          <a href={blockContent.url}>{blockContent.title}</a>
                        </p>
                      );
                    })}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
        <div className={styles.footerBottom}>
          <div className="rep-container">
            {bottomContent.map(link => {
              return <a href={link.url}>{link.title}</a>;
            })}
          </div>
        </div>
      </div>
    </Fragment>
  );
};
