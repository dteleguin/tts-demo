<template>
  <v-app>
    <v-toolbar app>
      <v-toolbar-title class="headline">
        <span>{{ welcome }}</span>
      </v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn flat v-on:click="login" v-if="this.$store.state.keycloak && !this.$store.state.keycloak.authenticated">
        <span class="mr-2">Login</span>
      </v-btn>
      <v-btn flat v-on:click="logout" v-if="this.$store.state.keycloak && this.$store.state.keycloak.authenticated">
        <span class="mr-2">Logout</span>
      </v-btn>
    </v-toolbar>

    <v-content>
      <v-container fluid>
        <code>GET </code>
        <v-btn color="success" v-on:click="onHello">hello</v-btn>
      </v-container>
      <template v-for="(child, index) in alerts">
        <v-alert value="true" v-bind:type="child.type" transition="fade-transition" dismissible>{{ child.text }}</v-alert>
      </template>
    </v-content>
    <v-footer></v-footer>
  </v-app>
</template>

<script>
/* eslint-disable */
import { mapGetters } from 'vuex'
import AxiosKeycloak from 'axios-keycloak'
import Vue from 'vue'

export default {
  name: 'App',
  components: {
  },
  data () {
    return {
        alerts: []
    }
  },
  computed: {
    ...mapGetters([
      'welcome'
    ])
  },
  methods: {
    login() {
        this.$store.state.keycloak.login();
    },
    logout() {
        this.$store.state.keycloak.logout();
    },
    onHello() {
        this.doGet("hello");
    },
    doGet(url, noauth) {

        var config = {
            baseURL: "/"
        }

        var axios = noauth ?
        AxiosKeycloak.axios.create(config) :
        this.$store.state.keycloak.createAxiosInstance(config);

        axios.get(url)
            .then(response => {
                console.log(response)
                this.alerts.push({
                    type: response.status == 200 ? 'success' : 'warning',
                    text: `HTTP ${response.status} ${response.statusText} => ${response.data}`
                })
            })
            .catch(error => {
                console.log("Error! " + error)
                this.alerts.push({
                    type: 'error',
                    text: error.toString()
                })
            });
    },
    doPost(url, data) {

        var config = {
            baseURL: "http://edge:8082/"
        }

        var axios = this.$store.state.keycloak.createAxiosInstance(config);

        axios.post(url, data)
            .then(response => {
                console.log(response)
                this.alerts.push({
                    type: response.status == 200 ? 'success' : 'warning',
                    text: `HTTP ${response.status} ${response.statusText} => ${response.data.message}`
                })
            })
            .catch(error => {
                console.log("Error! " + error)
                this.alerts.push({
                    type: 'error',
                    text: error.toString()
                })
            });
    }
  }
}
</script>
