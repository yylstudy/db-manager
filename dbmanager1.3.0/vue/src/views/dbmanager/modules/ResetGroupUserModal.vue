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
    <ResetGroupUserForm ref="realForm" @ok="submitCallback" :disabled="disableSubmit" normal></ResetGroupUserForm>
  </j-modal>
</template>

<script>
  import ResetGroupUserForm from './ResetGroupUserForm'
  export default {
    name: "ResetGroupUserModal",
    components: {
      ResetGroupUserForm
    },
    data () {
      return {
        title:'',
        width:1000,
        visible: false,
        disableSubmit: false
      }
    },
    methods: {
      add (groupId) {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.show(null,groupId,"add");
        })
      },
      edit (groupId,record,type) {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.show(record,groupId,type);
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