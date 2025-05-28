import { message } from 'ant-design-vue';
import axios from 'axios';

// 创建 axios 实例
const instance = axios.create(
    {
        baseURL: 'http://localhost:8501',
        timeout: 30000,
        withCredentials: true, // 跨域请求时发送 cookies
    }
);

// 请求拦截器框架
instance.interceptors.request.use(
    (config) => {
        // 在发送请求前可添加逻辑，如设置请求头
        return config;
    },
    (error) => {
        // 处理请求错误逻辑
        return Promise.reject(error);
    }
);

// 响应拦截器框架
instance.interceptors.response.use(
    (response) => {
        const { data } = response;
        if (data.code === 40100) {
            // 根据用户行为判断是不是需要用户去登录
            if (
                !response.request.responseURL.includes('/get/login') &&
                !window.location.pathname.includes('/login')
            ) {
                console.log('未登录');
                window.location.href = `/login?redirect=${getFullPath()}`;
            }
        }
        return response;
    },
    (error) => {
        return Promise.reject(error);
    }
);

const getFullPath = () => {
    const uri = window.location.href;
    const prefix = uri.indexOf(window.location.hostname + ":" + window.location.port); 
    return window.location.href.substring(prefix + (window.location.hostname + ":" + window.location.port).length);
}

export default instance;
