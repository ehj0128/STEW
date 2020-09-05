const DEV_URL = 'http://localhost:8399'
const DEPLOY_URL = 'https://i3b103.p.ssafy.io'

// 개발 <-> 배포 baseUrl
export const API_BASE_URL = DEV_URL
// export const API_BASE_URL = DEPLOY_URL

export const ACCESS_TOKEN_STRING = 'accessToken';
export const REFRESH_TOKEN_STRING = 'refreshToken';
export const TOKEN_PREFIX = 'Bearer ';

// + Provider
export const OAUTH2_REDIRECT_URI = API_BASE_URL + '/api/login/oauth2/code'

export const GOOGLE_AUTH_URL = API_BASE_URL + '/api/oauth2/authorize/google?redirect_uri=' + OAUTH2_REDIRECT_URI;
export const FACEBOOK_AUTH_URL = API_BASE_URL + '/api/oauth2/authorize/facebook?redirect_uri=' + OAUTH2_REDIRECT_URI;
export const GITHUB_AUTH_URL = API_BASE_URL + '/api/oauth2/authorize/github?redirect_uri=' + OAUTH2_REDIRECT_URI;
export const CUSTOM_AUTH_URL = API_BASE_URL + '/api/oauth2/authorization/';

export const KAKAO_LOGO_URL = "/image/social\\kakao_normal.png";
export const NAVER_LOGO_URL = "/image/social\\naver_normal.png";
export const GOOGLE_LOGO_URL = "/image/social\\google_normal.png";
export const FACEBOOK_LOGO_URL = "/image/social\\facebook_normal.png";


export function getCustomAuthUrl(provider: string){
    return CUSTOM_AUTH_URL + provider;
}

export function getBaseUrl(type: string){
    if(API_BASE_URL.match(DEV_URL)){
        return DEV_URL + '/api'
    }else if(API_BASE_URL.match(DEPLOY_URL)){
        if(type == 'img'){
            return DEPLOY_URL
        }else if(type == 'api'){
            return DEPLOY_URL + '/api'
        }
    }
}