import React from 'react';
import Header from './components/header';
import Sidebar from './components/SideBar';
import { Outlet } from 'react-router-dom';

const Layout = () => {
  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Header />

      <div style={{ flex: 1, display: 'flex', minHeight: 0 }}>
        <Sidebar />
        <div
          style={{
            flex: 1,
            overflow: 'auto',
            backgroundColor: '#e0e0e0',
          }}
        >
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default Layout;