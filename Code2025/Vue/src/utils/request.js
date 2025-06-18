import axios from 'axios';
import {ElMessage} from 'element-plus';
import router from "@/router/index.js";

const request = axios.create({
    baseURL: "http://localhost:9999",
    timeout: 30000  //后台接口的超时时间
})

//request拦截器
//可以自请求发送前对请求统一做一些处理
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    //统一的请求头token
    let user = JSON.parse(localStorage.getItem('CodeUser') || "{}")
    config.headers['token'] = user.token
    return config
}, error => {
    return Promise.reject(error)
});


//返回的数据response拦截器
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res.code === '401') {
            ElMessage.error(res.msg)
            router.push("/login")
        } else {
            return res
        }
    },//错误拦截和提示
    error => {
        if (error.response.status === 404) {
            ElMessage.error('没找到请求接口')
        } else if (error.response.status === 500) {
            ElMessage.error("系统异常，请查看后端信息")
        } else {
            console.error(error.message)
        }
        return Promise.reject(error)
    }
)

export default request