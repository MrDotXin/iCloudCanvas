import { type Router, type RouteLocationNormalized, type NavigationGuardNext, useRoute, useRouter } from 'vue-router';

import { useLoginUserStore } from '@/stores/user.ts';
import router from '@/router';
import { ACCESS_ENUM } from '@/enum/AccessEnum';
import checkAccess from './checkAccess';
let firstGetLoginUser = true; 

router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    console.log(to, from);
    const user = useLoginUserStore();
    let loginUser = user.loginUser; 
    
    if (firstGetLoginUser) {
        firstGetLoginUser = false;
        await user.getLoginUser(); 
        loginUser = user.loginUser; 
    }

    const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;
    if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
        if (!checkAccess(needAccess, loginUser, user.isLogin)) {
            if (user.isLogin) {
                next('/noAuth');
            } else {
                next('/login?redirect=' + to.fullPath)
            }
            return;
        }
    }

    next();
});
