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
    <MysqlGroupUserForm ref="realForm" @ok="submitCallback" :disabled="disableSubmit" normal></MysqlGroupUserForm>
  </j-modal>
</template>

<script>
  import MysqlGroupUserForm from './MysqlGroupUserForm'
  export default {
    name: "MysqlGroupUserModal",
    components: {
      MysqlGroupUserForm
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