<template>
  <j-modal
    :title="title"
    :width="width"
    :visible="visible"
    switchFullscreen
    @ok="handleOk"
    :okButtonProps="{ class:{'jee-hidden': disableSubmit} }"
    @cancel="handleCancel"
    cancelText="关闭">
    <MysqlUserForm ref="realForm" @ok="submitCallback" :disabled="disableSubmit" normal></MysqlUserForm>
  </j-modal>
</template>

<script>
  import MysqlUserForm from './MysqlUserForm'
  export default {
    name: "DbUserModal",
    components: {
      MysqlUserForm
    },
    data () {
      return {
        title:'',
        width:800,
        visible: false,
        disableSubmit: false
      }
    },
    methods: {
      add (propId) {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.show(null,propId,"add");
        })
      },
      edit (propId,record,type) {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.show(record,propId,type);
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