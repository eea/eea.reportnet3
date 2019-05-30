import React from 'react';
import styles from './UserCard.module.css';

const UserCard = () => {
    return (    
        <div id="userProfile" className={styles.userProfileCard}>  
            <div className={styles.userProfile}>    
                <a href="#userProfilePage" title="Edit user profile">
                    <img className={styles.avatar} alt="User Profile" src="https://image.flaticon.com/icons/svg/149/149071.svg"/>User
                </a>
            </div>
            <div className={styles.logOut}>
                <a href="#logOut" title="Log out"><i className="pi pi-power-off"></i></a>
            </div>
        </div>
    );
}

export default UserCard;