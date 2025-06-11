import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import axiosInstance from '../components/api/axiosInstance';

const Home = () => {
  const [redirectTo, setRedirectTo] = useState(null);

  useEffect(() => {
    axiosInstance.get(`/chat-rooms/recent`)
      .then(res => {
        const inviteCode = res.data.inviteCode;
        if (inviteCode) {
          setRedirectTo(`/chat/${inviteCode}`);
        } else {
          setRedirectTo('/blank');
        }
      })
      .catch(err => {
        const status = err.response?.status;
        if (status === 404) setRedirectTo('/blank');
        else setRedirectTo('/login');
      });
  }, []);

  if (!redirectTo) {
    return <div style={{ padding: '1rem' }}>가장 최근 채팅방으로 이동 중...</div>;
  }

  return <Navigate to={redirectTo} replace />;
};

export default Home;