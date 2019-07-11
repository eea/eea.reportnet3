import React, { useContext }  from 'react';
import styles from './UserCard.module.css';
import ResourcesContext from '../../../Context/ResourcesContext';

const UserCard = () => {
    const resources = useContext(ResourcesContext);

    return (    
        <div id="userProfile" className={styles.userProfileCard}>  
            <div className={styles.userProfile}>    
                <a href="#userProfilePage" title="Edit user profile">
                    <img className={styles.avatar} alt="User Profile" 
                         src="https://image.flaticon.com/icons/svg/149/149071.svg"/>
                    <h5 className={styles.userProfile}>User</h5>
                    <i className={resources.icons["logout"]}></i>
                </a>
            </div>
            {/* <div className={styles.logOut}>
                <a href="#logOut" title="Log out"><i className={resources.icons["logout"]}></i></a>
            </div> */}
        </div>
    );
}

export default React.memo(UserCard);