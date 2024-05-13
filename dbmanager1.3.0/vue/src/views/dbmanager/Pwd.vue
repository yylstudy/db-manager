<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="密码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="pwd">
              <a-input v-model="model.pwd" placeholder="请输入解密的密码"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="公钥" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="publicKey">
              <a-input v-model="model.publicKey" placeholder="请输入公钥"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="解密后密码" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <a-input v-model="model.pwdd"   ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" style="text-align: center">
            <a-button @click="pwd('2')">解密</a-button>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
  import { httpAction, getAction } from '@api/manage'
  import { validateDuplicateValue } from '@/utils/util'
  import JFormContainer from '@comp/jeecg/JFormContainer'
  import JDate from '@comp/jeecg/JDate'
  import JDictSelectTag from "@comp/dict/JDictSelectTag"
  import JMultiSelectTag from '@/components/dict/JMultiSelectTag'
  
  export default {
    name: "ProjectForm",
    components: {
      JFormContainer,
      JDate,
      JDictSelectTag,
      JMultiSelectTag,
    },
    props: {
      formData: {
        type: Object,
        default: ()=>{},
        required: false
      },
      normal: {
        type: Boolean,
        default: false,
        required: false
      },
      disabled: {
        type: Boolean,
        default: false,
        required: false
      }
    },
    data () {
      return {
        model: {status:1},
        id:'',
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        confirmLoading: false,
        validatorRules: {
          pwd:[ { required: true, message: '请输入解密的密码!' },],
          publicKey:[ { required: true, message: '请输入解密的密码!' },],
        },
        url: {
          add: "/business/add",
          edit: "/business/edit",
          queryById: "/business/queryById"
        }
      }
    },
    computed: {
      formDisabled(){
        return this.disabled
      },
      disabledId(){
        return this.id?true : false;
      },
      showFlowSubmitButton(){
      }
    },
    created () {
      this.showFlowData();
    },
    methods: {
      show (record) {
        this.model = record?Object.assign({}, record):this.model;
        this.id = record?record.id:'';
        this.visible = true;
      },
      showFlowData(){
        if(this.normal === false){
        }
      },
      pwd (type) {
        const that = this;
        this.model.type=type
        // 触发表单验证
        that.$refs.form.validate(valid => {
          if (valid) {
            httpAction("datasource/encryptDecryptPwd",this.model,"post").then((res)=>{
              if(res.success){
                this.$set(this.model,'pwdd',res.result)
              }else{
                this.$set(this.model,'pwdd',"")
                that.$message.warning(res.message);
              }
            })
          }else{
            return false;
          }

        })
      },
      popupCallback(row){
        this.model = Object.assign(this.model, row);
      },
    }
  }
</script>
