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
    <DatasourcePropForm ref="realForm" @ok="submitCallback" :disabled="disableSubmit" normal></DatasourcePropForm>
  </j-modal>
</template>

<script>
  import DatasourcePropForm from './DatasourcePropForm'
  export default {
    name: "DatasourcePropModal",
    components: {
      DatasourcePropForm
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
      add () {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.show(null,"add");
        })
      },
      edit (record,type) {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.show(record,type);
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