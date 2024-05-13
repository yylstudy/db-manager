<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="host" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="host">
              <a-textarea v-model="model.host" placeholder="请输入host，多个以,分割，不限制则为%" :disabled="this.type=='edit'||this.type=='init'"  ></a-textarea>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="用户名" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="username" >
              <a-input style="width: 100%"  v-model="model.username" :disabled="this.type=='edit'||this.type=='init'" placeholder="请输入用户名" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="db" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="db" >
              <a-textarea style="width: 100%"   v-model="model.db" :disabled="this.type=='init'" placeholder="请输入db，多个以,分割，不限制则为*" ></a-textarea>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="this.type=='detail'||this.type=='init'">
            <a-form-model-item label="密码" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <a-input style="width: 100%" :type="this.type=='init'?'password':'text'"  v-model="model.password"  ></a-input>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
  import { httpAction, getAction,postAction } from '@api/manage'
  import { validateDuplicateValue } from '@/utils/util'
  import JFormContainer from '@comp/jeecg/JFormContainer'
  import JDate from '@comp/jeecg/JDate'
  import JDictSelectTag from "@comp/dict/JDictSelectTag"
  import JMultiSelectTag from '@/components/dict/JMultiSelectTag'
  
  export default {
    name: "MysqlUserForm",
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
          host:[ { required: true, message: '请输入host，多个以,分割，不限制则为%!' },],
          username:[ { required: true, message: '请输入用户名!' },],
          db:[ { required: true, message: '请输入db，多个以,分割，不限制则为*!' },],
        },
        url: {
          add: "/datasource/addUser",
          edit: "/datasource/grantPrivileges",
          queryById: "/datasource/queryById"
        },
        type: "add",
      }
    },
    computed: {
      formDisabled(){
        if(this.normal===false){
          if(this.formData.disabled===false){
            return false
          }else{
            return true
          }
        }
        return this.disabled
      },
      disabledId(){
        return this.id?true : false;
      },
      showFlowSubmitButton(){
        if(this.normal===false){
          if(this.formData.disabled===false){
            return true
          }else{
            return false
          }
        }else{
          return false
        }
      }
    },
    created () {
      this.showFlowData();
    },
    methods: {
      show (record,propId,type) {
        this.type = type;
        const that = this;
        that.model = record?Object.assign({}, record):that.model;
        this.id = record?record.id:'';
        this.visible = true;
        that.model.propId = propId;
        if(record!=null&&record!=undefined&&record!=''){
          this.url.add="/datasource/grantPrivileges"
          this.$nextTick(()=>{
            postAction("/datasource/getDatabase", that.model).then((res)=>{
              if(res.success){
                that.$set(that.model,'db',res.result)
              }else{
                that.$message.error(res.message);
              }
            });
            let param = record
            param.propId = propId;
            postAction("/datasource/getMysqlUser", param).then((res)=>{
              if(res.success){
                that.$set(that.model,'password',res.result.password)
              }else{
                that.$message.error(res.message);
              }
            });
          })

        }

      },
      showFlowData(){
        if(this.normal === false){
          let params = {id:this.formData.dataId};
          getAction(this.url.queryById,params).then((res)=>{
            if(res.success){
              this.edit (res.result);
            }
          });
        }
      },
      submitForm () {
        const that = this;
        // 触发表单验证
        that.$refs.form.validate(valid => {
          if (valid) {
            let httpurl = '';
            let method = '';
            let url = "";
            if(that.type=='add'){
              url = "/datasource/addUser";
            }else if(that.type=='init'){
              if(this.model.password==null||this.model.password==''||this.model.password==undefined){
                this.$message.warning("请输入密码");
                return;
              }
              url = "/datasource/initUser"
            }else {
              url = "/datasource/grantPrivileges"
            }
            that.confirmLoading = true;
            httpAction(url,this.model,'post').then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
              }else{
                if("该编号已存在!" == res.message){
                  this.model.id=""
                }
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
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
