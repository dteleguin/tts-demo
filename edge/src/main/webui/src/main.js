/* eslint-disable */
/* global keycloak */

import Vue from 'vue'
import Vuex from 'vuex'
import './plugins/vuetify'
import App from './App.vue'
import AxiosKeycloak from 'axios-keycloak'

const keycloak = new AxiosKeycloak();

Vue.config.productionTip = false;

Vue.use(Vuex);

const store = new Vuex.Store({
    state: {
        keycloak: null,
        welcome: null
    },
    mutations: {
        keycloak(state, _keycloak) {
            state.keycloak = _keycloak;
        }
    },
    getters: {
        welcome: state => {
            if (state.keycloak && state.keycloak.authenticated)
                return "Hello " + state.keycloak.idTokenParsed.preferred_username;
        }
    }
});

var app = new Vue({
    render: h => h(App),
    store
}).$mount('#app');

keycloak.init({ onLoad: 'check-sso' })
    .success(function (authenticated) {
        store.commit('keycloak', keycloak);
    })
    .error(function() {
        console.log("Error initializing Keycloak adapter");
    });
