import axios from 'axios'
const BASE_URL = import.meta.env.VITE_API_PREFIX

export const request = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
})
request.interceptors.response.use(
  (res) => {
    return res.data
  },
  ({ response }) => {
    return Promise.reject(response.data)
  },
)
