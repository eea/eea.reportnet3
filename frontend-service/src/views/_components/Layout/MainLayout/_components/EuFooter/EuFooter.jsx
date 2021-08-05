import { config } from 'conf';

import styles from './EuFooter.module.scss';

export const EuFooter = ({ leftMargin }) => {
  const {
    footer: { topContent, bottomContent }
  } = config;
  return (
    <div className={styles.footer}>
      <div className="rep-container">
        <div className={styles.footerTop}>
          {topContent.map(block => {
            return (
              <div className={styles.contentBlock} key={block.blockTitle}>
                <h3>{block.blockTitle}</h3>
                <div className={styles.linksWrapper}>
                  {block.content.map(blockContent => {
                    return (
                      <p key={blockContent.title}>
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
            return (
              <a href={link.url} key={link.title} rel="noopener noreferrer" target="_blank">
                {link.title}
              </a>
            );
          })}
        </div>
      </div>
    </div>
  );
};
