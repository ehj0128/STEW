import router from "@/router";
import axios from 'axios';
import jwt from "jsonwebtoken";
import { rejects } from 'assert';
import { getBaseUrl } from '@/constants'
import store from "@/store";
import Vue from "vue";

Vue.config.productionTip = false;
const refreshInstance = axios.create()

refreshInstance.defaults.baseURL = getBaseUrl('api')

class User {
  userId: number;
  userNm: string;
  userEmail: string;
  userImg: string;
  accessToken: string;
  refreshToken: string;

  constructor() {
    this.userId = 0;
    this.userNm = "";
    this.userEmail = "";
    this.userImg = "";
    this.accessToken = "";
    this.refreshToken = "";
  }
}

// 유저 인증 정보 모듈화
export default {
    namespaced: true,
    
    state: {
      isLogin: false,
      userInfo: new User(),
    },

    getters: {
      loginStatus: (state: { isLogin: any; }) => state.isLogin,
      getUserInfo: (state: { userInfo: User; }) => state.userInfo
    },
  
    mutations: {
      loginSuccess(state: any, payload: any) {
        state.isLogin = true;
        state.userInfo = payload;
      },
  
      logoutSuccess(state: any) {
        state.isLogin = false;
        state.userInfo = new User();
      },
  
      refreshSuccess(state: any, payload: any) {
        state.userInfo.accessToken = payload;
      },

      getUserInfoByToken(state: { userInfo: any; }, payload: any) {
       state.userInfo.userId = payload.userId; 
       state.userInfo.userNm = payload.userNm;
       state.userInfo.userEmail = payload.userEmail;
       state.userInfo.userImg = payload.userImg
      }
    },
  
    actions: {
      // 로그인
      async signIn({ commit, dispatch }: any, userObj: any) {
        await axios.post('/user/signin', userObj)
          .then(res => {
            const userInfo = {
              'accessToken': res.headers.accesstoken,
              'refreshToken': res.headers.refreshtoken
            }
            //임시(userId 불러오기용)
            commit("loginSuccess", userInfo);
            dispatch("tokenInformation");
            dispatch("notice/getReqsSock", null, { root: true });
            dispatch("notice/getReqs", null, { root: true });
            dispatch("notice/getNotis", null, { root: true });
          })
          .catch(err => {
            alert("이메일과 비밀번호를 확인하세요");
            console.log(err)
          })
      },
  
      // 로그아웃
      logout({ commit, dispatch }: any) {
        axios.get('/user/logout')
          .then(res => {
            // console.log(res);
            commit("logoutSuccess");
            commit("notice/wsDisconnect", null, { root: true });
            router.push("/").catch(()=>({}));
          })
      },
  
      // 토큰 갱신
      async tokenRefresh({ state, commit }: any) {
        return new Promise((resolve, reject) => {
          const config = {
            headers: {
              "Authorization": state.userInfo.accessToken,
              "refreshToken": state.userInfo.refreshToken
            }
          }
          const origin = state.userInfo.accessToken;
          refreshInstance.get('/user/refresh', config)
            .then(res => {
              // console.log("토큰 재발급 응답");
              if(res.data.msg == 'success'){
                commit("refreshSuccess", res.headers.accesstoken);
                if (origin !== state.userInfo.accessToken) {
                  resolve();
                  //console.log(origin);
                  //console.log(state.userInfo.accessToken);
                }
              }else if(res.data.msg == 'invalid refreshToken'){
                commit("logoutSuccess");
                alert("다시 로그인해주세요");
                reject();
                router.push('/').catch(()=>({}));
              }
            })
        })
      },
  
      // accessToken 정보 확인
      tokenInformation({ state, commit }: any) {
        const token = state.userInfo.accessToken.replace("Bearer ", "");
        const decode: any = jwt.decode(token);
        if (decode) {
          const userInfo = {
            'userId': decode.userId,
            'userEmail': decode.sub,
            'userNm': decode.userNm,
            'userImg': decode.userImg
          };
          commit("getUserInfoByToken", userInfo);
        }
      },
    },
  }