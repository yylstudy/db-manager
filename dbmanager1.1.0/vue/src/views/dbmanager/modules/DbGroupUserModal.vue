<template>
  <j-modal
    :title="title"
    :width="width"
    :visible="visible"
    switchFullscreen
    @ok="handleOk"
    :okButtonProps="{ class:{'jee-hidden': disableSubmit} }"
    @cancel="handleCancel"
    :footer="null"
    cancelText="关闭">
    <DbGroupUserList ref="realForm" @ok="submitCallback" :disabled="disableSubmit" normal></DbGroupUserList>
  </j-modal>
</template>

<script>
  import DbGroupUserList from './DbGroupUserList'
  export default {
    name: "DbGroupUserModal",
    components: {
      DbGroupUserList
    },
    data () {
      return {
        title:'',
        width:1300,
        visible: false,
        disableSubmit: false
      }
    },
    methods: {
      add () {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.show();
        })
      },
      edit (record) {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.show(record);
        })
      },
      close () {
        this.$emit('close');
        this.visible = false;
      },
      handleOk () {
        this.$refs.realForm.submitForm();
      },
      submitCallback(){
        this.$emit('ok');
        this.visible = false;
      },
      handleCancel () {
        this.close()
      }
    }
  }
</script>