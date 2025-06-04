import axios from "axios";

const socketInstance = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true,
});

