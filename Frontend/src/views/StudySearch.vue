<template>
  <v-container>
    <div v-if="gpList == 0" class="text-center my-10">
      <h1>검색 결과가 없습니다!</h1>
      <h3>새로운 스터디를 등록해보세요 :)</h3>
    </div>
    <div class="mx-auto">
      <div v-if="gpList != 0" class="my-10 text-center">
        <h2>검색결과</h2>
      </div>
      <v-row v-for="group in gpList" :key="group.gpNo">
        <v-card height="160" width="80%" @click="enterStudy(group)" class="mx-auto my-2">
          <div class="d-flex flex-no-wrap">
            <div width="200" class="d-flex align-stretch align-center">
              <v-img
                class="ma-2 d-none d-sm-block"
                :src="group.gpImg != null?($store.state.comm.baseUrl + '/image/group' + group.gpImg):gpImgDefault"
                max-width="200"
                max-height="140"
                contain
              />
            </div>
            <div align="left" class="text-truncate align-center ml-3">
              <v-card-subtitle
                class="text-truncate text-h6 d-block pt-2 pb-1 font-weight-bold"
                color="#000000"
              >
                <span>{{group.gpNm}}</span>
              </v-card-subtitle>
              <v-card-subtitle class="text-truncate py-0" v-text="group.gpIntro"></v-card-subtitle>
              <v-card-text class="my-0 pb-0 pt-1">
                <p class="body-2" v-if="!group.gpPublic">
                  <v-icon small color="#616161">mdi-lock</v-icon>
                  <span color="#616161">비공개 스터디</span>
                </p>
                <p v-else>
                  <span color="light-blue darken-2">공개 스터디</span>
                </p>
              </v-card-text>
              <div class="text-truncate mb-2">
                <v-chip
                  class="ma-2"
                  small
                  color="#F5F5F5"
                  v-for="tag in group.gpTag"
                  :key="tag"
                ># {{tag}} &nbsp;&nbsp;</v-chip>
              </div>
            </div>
          </div>
        </v-card>
      </v-row>
      <v-dialog v-model="dialog" width="500">
        <v-card>
          <v-card-title class="headline grey lighten-4">
            <b>{{ selectedGroup.gpNm }}에 가입하시겠습니까?</b>
          </v-card-title>
          <v-textarea v-model="message" color="teal" class="mx-5" v-if="!selectedGroup.gpPublic">
            <template v-slot:label>
              <div class="px-5">
                가입신청 메세지를 작성해 보세요!
              </div>
            </template>
          </v-textarea>

          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" text @click="signUpGroup(selectedGroup.gpNo)">신청하기</v-btn>
            <v-btn text @click="dialog = false">닫기</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </div>
  </v-container>
</template>

<script>
import axios from "axios";
import { mapState } from "vuex";
import querystring from "querystring";

export default {
  name: "StudySearch",
  data() {
    return {
      selectedGroup: { gpNo: 0, gpNm: "", gpIntro: "" },
      dialog: false,
      message: "",
      gpImgDefault: this.$store.state.comm.baseUrl + "/image/group/default.png"
    };
  },
  computed: {
    keyword() {
      return this.$store.state.sg.keyword;
    },
    gpList() {
      return this.$store.state.sg.searchedGroups;
    }
  },
  mounted() {
    this.getCategories();

    this.$vuetify.goTo(0, {
      duration: 100,
      offset: 0
    })
  },
  methods: {
    async enterStudy(group) {
      if (!this.$store.state.auth.isLogin) {
        alert("로그인이 필요합니다!");
        this.$router.push({ name: "Login" });
        return;
      }

      try {
        const joinCkUrl = "/study/user/joinck/" + group.gpNo;
        const joinRes = await axios.get(joinCkUrl);
        const joinCk = joinRes.data.object;

        if (joinCk) {
          this.$router.push("/study/" + group.gpNo);
        } else {
          const reqCkUrl = "/study/user/reqck/" + group.gpNo;
          const reqRes = await axios.get(reqCkUrl);
          const reqCk = reqRes.data.object;

          if (reqCk) alert("아직 가입 승인 대기중인 그룹입니다");
          else {
            this.dialog = true;
            this.selectedGroup = group;
          }
        }
      } catch (err) {
        console.error(err);
      }
    },
    async signUpGroup(gpNo) {
      const apiUrl = "/study/user/req?gpNo=" + gpNo;
      const msg = {
        reqMsg: this.message
      };
      try {
        const res = await axios.post(
          apiUrl,
          querystring.stringify({ reqMsg: this.message })
        );
        console.log(res);
        this.dialog = false;
        this.message = "";
      } catch (err) {
        console.error(err);
      }
    }
  }
};
</script>
