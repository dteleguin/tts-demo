<template>
<v-alert
        value="true"
        type="success"
        transition="scale-transition"
      >
        {{ foo }}
      </v-alert>
</template>

<script>
  export default {
    data: () => ({
      foo: "bar"
    })
  }
</script>

<style>
</style>
