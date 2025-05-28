import ACCESS_ENUM from "@/enum/AccessEnum";

export default function checkAccess(require : string, user : API.LoginUserVO, isLogin : boolean) {

    if (require === ACCESS_ENUM.ADMIN) {
        if (!isLogin || user.userRole !== ACCESS_ENUM.ADMIN) {
            return false;
        }
    } else if (require === ACCESS_ENUM.USER) {
        if (!isLogin) {
            return false;
        }
    }

    return true;
}