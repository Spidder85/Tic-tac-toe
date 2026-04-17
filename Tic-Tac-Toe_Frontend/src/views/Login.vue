<template>
  <section class="form-page">
    <form class="form" @submit.prevent="loginUser">
      <label class="form__field">
        <span>Логин</span>
        <input
          v-model.trim="login"
          type="text"
          autocomplete="username"
          :disabled="loading"
        />
      </label>

      <label class="form__field">
        <span>Пароль</span>
        <input
          v-model="password"
          :type="passwordInputType"
          autocomplete="current-password"
          :disabled="loading"
        />
      </label>

      <label class="checkbox">
        <input
          v-model="showPassword"
          type="checkbox"
          :disabled="loading"
        />
        <span>Показать пароль</span>
      </label>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Вход...' : 'Войти' }}
      </button>

      <button type="button" :disabled="loading" @click="goToRegister">
        Регистрация
      </button>

      <p v-if="errorMessage" class="error">
        {{ errorMessage }}
      </p>
    </form>
  </section>
</template>

<script>
import { getErrorMessage } from '../services/api.js'

export default {
  name: 'Login',

  data() {
    return {
      login: '',
      password: '',
      showPassword: false,
      loading: false,
      errorMessage: ''
    }
  },

  computed: {
    passwordInputType() {
      return this.showPassword ? 'text' : 'password'
    }
  },

  methods: {
    validateForm() {
      if (!this.login) {
        return 'Введите логин'
      }

      if (!this.password) {
        return 'Введите пароль'
      }

      return ''
    },

    async loginUser() {
      this.errorMessage = ''

      const validationError = this.validateForm()

      if (validationError) {
        this.errorMessage = validationError
        return
      }

      this.loading = true

      try {
        await this.$store.dispatch('login', {
          login: this.login,
          password: this.password
        })

        this.$router.push('/games')
      } catch (error) {
        this.errorMessage = getErrorMessage(error, 'Ошибка авторизации')
      } finally {
        this.loading = false
      }
    },

    goToRegister() {
      this.$router.push('/register')
    }
  }
}
</script>
