import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet, userLoginUsingPost, userLogoutUsingPost, userRegisterUsingPost } from '@/backend/service/api/userController';

export const useLoginUserStore = defineStore('loginUser', () => {

  const loginUser = ref<API.LoginUserVO>({});
  const isLogin = ref(false);

  async function getLoginUser() {
    try {
      const response = await getLoginUserUsingGet();
      if (response.data.code === 0) {
        setLoginUser(response.data.data);
        isLogin.value = true;
      } 

      return response.data;
    } catch (error : any) {
      throw Error(error.message);
    }

  }

  function setLoginUser(user: API.LoginUserVO | undefined) {
    loginUser.value = user || {};
  }

  async function userLogout() {
    const response = await userLogoutUsingPost();
    if (response.data.code === 0) {
      loginUser.value = {};
      isLogin.value = false;
    } else {
      throw new Error(response.data.message);
    }
  }

  async function userLogin(e : {userAccount : string, userPassword : string} ) {
    const response = await userLoginUsingPost({
      userAccount: e.userAccount,
      userPassword: e.userPassword
    });
    if (response.data.code === 0) {
      await getLoginUser();
    }

    return response.data;
  }


  return { loginUser, isLogin, getLoginUser, setLoginUser, userLogout, userLogin }
})
