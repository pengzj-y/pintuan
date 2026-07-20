import axios from 'axios';
import { ElMessage } from 'element-plus';

const service = axios.create({
    baseURL: 'http://localhost:8091',
    timeout: 10000,
});

service.interceptors.response.use(
    (response) => {
        const res = response.data;
        if (res.code && res.code !== '0000') {
            ElMessage.error(res.info || '请求失败');
            return Promise.reject(new Error(res.info || 'Error'));
        }
        return res;
    },
    (error) => {
        ElMessage.error(error.message || '网络异常');
        return Promise.reject(error);
    }
);

export default service;
