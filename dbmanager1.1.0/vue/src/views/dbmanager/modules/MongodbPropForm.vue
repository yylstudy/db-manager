<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="机房" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="computerRoomId">
              <j-search-select-tag v-model="model.computerRoomId"  dict="computer_room,name,id" placeholder="请选择机房" ></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="项目" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="businessId">
              <j-search-select-tag v-model="model.businessId"  dict="v_business,business_name,id" placeholder="请选择项目" ></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备份名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="name">
              <a-input v-model="model.name" placeholder="请输入备份名称"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="mongodb IP" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="ip">
              <a-input v-model="model.ip" placeholder="请输入数据库IP"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="mongodb端口" :labelCol="labelCol" :wrapperCol="wrapperCol"prop="port" >
              <a-input style="width: 100%"  v-model="model.port" placeholder="请输入数据库端口" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="mongodb用户名" :labelCol="labelCol" :wrapperCol="wrapperCol"prop="user" >
              <a-input style="width: 100%"  v-model="model.user" placeholder="请输入数据库用户名" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="mongodb密码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="password" >
              <a-input style="width: 100%" type="password"  v-model="model.password" placeholder="请输入数据库密码" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="ssh端口" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sshPort" >
              <a-input style="width: 100%"   v-model="model.sshPort" placeholder="请输入ssh端口" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="ssh用户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sshUser" >
              <a-input style="width: 100%"   v-model="model.sshUser" placeholder="请输入ssh用户" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" >
            <a-form-model-item label="ssh密码" v-show="this.type!='detail'" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="sshPassword" >
              <a-input style="width: 100%"  type="password" v-model="model.sshPassword"  placeholder="请输入ssh密码" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="this.type=='detail'">
            <a-form-model-item label="ssh密码" v-show="this.type=='detail'" :labelCol="labelCol" :wrapperCol="wrapperCol"   >
              <a-input style="width: 100%"   v-model="model.encryptSshPassword"   ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备份路径"  :labelCol="labelCol" :wrapperCol="wrapperCol"   >
              <a-input style="width: 100%"   v-model="model.backupPath"   ></a-input>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
import {httpAction, getAction, postAction} from '@api/manage'
  import { validateDuplicateValue } from '@/utils/util'
  import JFormContainer from '@comp/jeecg/JFormContainer'
  import JDate from '@comp/jeecg/JDate'
  import JDictSelectTag from "@comp/dict/JDictSelectTag"
  import JMultiSelectTag from '@/components/dict/JMultiSelectTag'
  
  export default {
    name: "MongodbPropForm",
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
          computerRoomId:[ { required: true, message: '请选择机房!' },],
          businessId:[ { required: true, message: '请选择项目!' },],
          name:[ { required: true, message: '请输入备份名称!' },],
          ip:[ { required: true, message: '请输入mongodb IP!' },],
          port:[ { required: true, message: '请输入mongodb端口!' },],
          user:[ { required: false, message: '请输入mongodb用户名!' },],
          password:[ { required: false, message: '请输入mongodb密码!' },],
          sshPort:[ { required: true, message: '请输入ssh端口!' },],
          sshUser:[ { required: true, message: '请输入ssh用户!' },],
          sshPassword:[ { required: true, message: '请输入ssh密码!' },],
        },
        url: {
          add: "/mongodb/add",
          edit: "/mongodb/edit",
          queryById: "/mongodb/queryById"
        },
        type:"",
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
      show (record,type) {
        this.type = type;
        if(type=='edit'){
          this.validatorRules.password=[]
          this.validatorRules.sshPassword=[]
        }
        this.model = record?Object.assign({}, record):this.model;
        this.id = record?record.id:'';
        this.visible = true;
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
            if(!this.id){
              httpurl+=this.url.add;
              method = 'post';
            }else{
              httpurl+=this.url.edit;
              method = 'put';
            }
            that.confirmLoading = true;
            httpAction(httpurl,this.model,method).then((res)=>{
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
      isNull(obj){
        return obj==null||obj==''||obj==undefined
      },
      popupCallback(row){
        this.model = Object.assign(this.model, row);
      },
    }
  }
</script>
