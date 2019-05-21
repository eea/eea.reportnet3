import React from 'react';
import logo from '../../assets/images/logo.png';

const Navigation = () => {
    return (
      <div className="bootstrap">
        <nav className="navbar navbar-expand-lg navbar-light bg-light">
          <a className="navbar-brand" href=".">
            <img height="50px" src={logo} alt="Reportnet 3"></img>
          </a>
          
          <div className="collapse navbar-collapse" id="navbarSupportedContent">
            <ul className="navbar-nav mr-auto">
              <li className="nav-item active">
                <a className="nav-link" href=".">Reportnet 3</a>
              </li>
            </ul>
            <div className="my-2 my-lg-0">
              <a className="nav-link" href=".">Username<i className="pi pi-user"></i></a>
            </div>
          </div>
        </nav>
      </div>
    );
}

export default Navigation;