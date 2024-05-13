<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled"  >
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules" v-show="!showPrepareResult">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="是否本地文件" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="islocalhostFile">
              <j-dict-select-tag :trigger-change="true"  default-value="0" @change="changeExecute" placeholder="请选择" dictCode="contain_time" v-model="model.islocalhostFile" ></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="还原文件" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="recoverTarFile">
              <a-input v-model="model.recoverTarFile" placeholder="请输入还原文件绝对路径"></a-input>
            </a-form-model-item>
          </a-col>

          <a-col :span="24" v-show="showSsh">
            <a-form-model-item label="mysql服务ssh地址" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="mysqlSshHost">
              <a-row>
                <a-col :span="20">
                  <a-input v-model="model.mysqlSshHost" placeholder="请输入mysql服务ssh地址"></a-input>
                </a-col>
                <a-col :span="4">
                  <a style="margin-left: 8px" @click="testSsh">测试ssh</a>
                </a-col>
              </a-row>

            </a-form-model-item>
          </a-col>

          <a-col :span="24" v-show="showSsh">
            <a-form-model-item label="mysql服务ssh端口" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="mysqlSshPort">
              <a-input v-model="model.mysqlSshPort" placeholder="请输入mysql服务ssh端口"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="showSsh">
            <a-form-model-item label="mysql服务ssh用户名" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="mysqlSshUser">
              <a-input v-model="model.mysqlSshUser" placeholder="请输入mysql服务ssh用户名"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24"  v-show="showSsh">
            <a-form-model-item label="mysql服务ssh密码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="mysqlSshPassword" >
              <a-input v-model="model.mysqlSshPassword" type="password"  placeholder="请输入mysql服务ssh密码"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="showSsh">
            <a-form-model-item label="mysql配置文件路径" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="mysqlCnf">
              <a-input v-model="model.mysqlCnf" placeholder="请输入mysql配置文件路径"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <p style="color:red">备注：还原文件仅支持.tar.gz，若未超过全备周期，请手动打包.tar.gz</p>
            <p style="color:red">还原准备成功后，会提示后续的命令操作，请勿关闭页面</p>
          </a-col>
          <a-col v-if="showFlowSubmitButton" :span="24" style="text-align: center">
            <a-button @click="submitForm">提 交</a-button>
          </a-col>
        </a-row>
      </a-form-model>
      <p v-html="prepareResult"></p>
    </j-form-container>
  </a-spin>
</template>

<script>
  import { httpAction, getAction,httpAction2 } from '@api/manage'
  import { validateDuplicateValue } from '@/utils/util'
  import JFormContainer from '@comp/jeecg/JFormContainer'
  import JDate from '@comp/jeecg/JDate'
  import JDictSelectTag from "@comp/dict/JDictSelectTag"
  
  export default {
    name: "RecoverForm",
    components: {
      JFormContainer,
      JDate,
      JDictSelectTag,
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
          recoverTarFile:[ { required: true, message: '请输入还原文件绝对路径!' },],
          islocalhostFile:[ { required: true, message: '请选择是否本机!' },],

        },
        url: {
          add: "/backup/add",
          edit: "/backup/recoverPrepare",
          queryById: "/backup/queryById"
        },
        showPrepareResult:false,
        prepareResult:"",
        showSsh:false,
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
      show (record) {
        // this.model = record?Object.assign({}, record):this.model;
        this.model.id = record?record.id:'';
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
      cancelPrepareResult(){
        this.showPrepareResult = false;
      },
      changeExecute(value){
        if(value=='0'){
          this.validatorRules.mysqlSshHost=[]
          this.validatorRules.mysqlSshPort=[ ]
          this.validatorRules.mysqlSshUser=[]
          this.validatorRules.mysqlSshPassword=[]
          this.validatorRules.mysqlCnf=[ ]
          this.showSsh = false
        }else{
          this.showSsh = true
          this.validatorRules.mysqlSshHost=[ { required: true, message: '请输入mysql服务ssh地址!' },]
          this.validatorRules.mysqlSshPort=[ { required: true, message: '请输入mysql服务ssh端口!' },]
          this.validatorRules.mysqlSshUser=[ { required: true, message: '请输入mysql服务ssh用户名!' },]
          this.validatorRules.mysqlSshPassword=[ { required: true, message: '请输入mysql服务ssh密码!' },]
          this.validatorRules.mysqlCnf=[ { required: true, message: '请输入还原文件绝对路径!' },]
        }
      },
      testSsh(){
        const that = this;
        if(this.isNull(this.model.mysqlSshHost)){
          that.$message.error("请输入mysql服务ssh地址");
          return
        }
        if(this.isNull(this.model.mysqlSshPort)){
          that.$message.error("请输入mysql服务ssh端口");
          return
        }
        if(this.isNull(this.model.mysqlSshUser)){
          that.$message.error("请输入mysql服务ssh用户名");
          return
        }
        if(this.isNull(this.model.mysqlSshPassword)){
          that.$message.error("请输入mysql服务ssh密码");
          return
        }
        httpAction("/backup/testSsh",this.model,"post").then((res)=>{
          if(res.success){
            that.$message.success(res.message);
          }else{
            that.$message.warning(res.message);
          }
        }).finally(() => {
          that.confirmLoading = false;
        })
      },
      isNull(obj){
        return obj==null||obj==''||obj==undefined
      },
      submitForm () {
        const that = this;
        // 触发表单验证
        that.$refs.form.validate(valid => {
          if (valid) {
            that.confirmLoading = true;
            httpAction2("backup/recoverPrepare",this.model,'post',1000000).then((res)=>{
              if(res.success){
                that.prepareResult = res.message
                that.disableSubmit = true;
                that.showPrepareResult = true;
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
