import axios from 'axios';

const instance = axios.create({
  baseURL: 'http://localhost:5173/api/v1',
  timeout: 30000,
});

instance.interceptors.response.use(
  (response) => {
    const data = response.data;
    console.log('响应:', JSON.stringify(data, null, 2));
    if (data.code && data.code !== 200) {
      return Promise.reject(new Error(data.message || '请求失败'));
    }
    return response.data;
  },
  (error) => Promise.reject(error)
);

async function test() {
  // 1. 获取验证码
  console.log('1. 获取验证码...');
  const captchaRes = await instance.get('/captcha');
  console.log('captchaId:', captchaRes.data.captchaId);
  
  // 2. 登录
  console.log('\n2. 登录...');
  try {
    const loginRes = await instance.post('/auth/login', {
      username: 'admin',
      password: 'admin123',
      captchaId: captchaRes.data.captchaId,
      captchaCode: 'test' // 需要手动输入正确的验证码
    });
    console.log('登录成功!');
    console.log('token:', loginRes.data?.token);
    console.log('user:', loginRes.data?.user);
    console.log('permissions:', loginRes.data?.permissions);
  } catch (err) {
    console.error('登录失败:', err.message);
  }
}

test();