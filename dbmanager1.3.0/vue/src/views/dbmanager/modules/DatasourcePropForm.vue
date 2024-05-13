<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="机房" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="computerRoomId">
              <j-search-select-tag v-model="model.computerRoomId" @change="changeComputerRoom" dict="computer_room,name,id" placeholder="请选择机房" ></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="项目" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="businessId">
              <j-search-select-tag v-model="model.businessId"  dict="v_business,business_name,id" placeholder="请选择项目" ></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="数据库IP" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="ip">
              <a-input v-model="model.ip" placeholder="请输入数据库IP"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="数据库端口" :labelCol="labelCol" :wrapperCol="wrapperCol"prop="port" >
              <a-input style="width: 100%"  v-model="model.port" placeholder="请输入数据库端口" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="数据库用户名" :labelCol="labelCol" :wrapperCol="wrapperCol"prop="user" >
              <a-input style="width: 100%"  v-model="model.user" placeholder="请输入数据库用户名" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="this.type!='detail'">
            <a-form-model-item label="数据库密码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="password" >
              <a-input style="width: 100%" type="password"  v-model="model.password" placeholder="请输入数据库密码" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="this.type=='detail'">
            <a-form-model-item label="数据库密码" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input style="width: 100%"   v-model="model.encryptPwd" ></a-input>
            </a-form-model-item>
          </a-col>

          <a-col :span="24">
            <a-form-model-item label="是否k8s" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="isk8s" >
              <j-dict-select-tag :trigger-change="true"  @change="changeIsk8s" placeholder="请选择是否k8s" dictCode="is_k8s" v-model="model.isk8s" ></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="this.showk8s">
            <a-form-model-item label="k8s配置" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="k8sConfigId" >
              <j-search-select-tag v-model="model.k8sConfigId" @change="initNamespace" dict="k8s_config,name,id" placeholder="请选择数据源" ></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="this.showk8s">
            <a-form-model-item label="namespace" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <a-select  v-model="model.namespace" placeholder="请选择namespace"  @change="initPod" prop="namespace">
                <a-select-option v-for="(item,index) in namespaceList" :key="index"  :value="item">
                  {{item}}
                </a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="this.showk8s">
            <a-form-model-item label="pod" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <a-select  v-model="model.podName" placeholder="请选择pod"  prop="podName">
                <a-select-option v-for="(item,index) in podNameList" :key="index"  :value="item">
                  {{item}}
                </a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>

          <a-col :span="24" v-show="!this.showk8s">
            <a-form-model-item label="ssh端口" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sshPort" >
              <a-input style="width: 100%"   v-model="model.sshPort" placeholder="请输入ssh端口" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="!this.showk8s">
            <a-form-model-item label="ssh用户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sshUser" >
              <a-input style="width: 100%"   v-model="model.sshUser" placeholder="请输入ssh用户" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24"  v-show="!this.showk8s">
            <a-form-model-item label="ssh密码" v-show="this.type!='detail'" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="sshPassword" >
              <a-input style="width: 100%"  type="password" v-model="model.sshPassword"  placeholder="请输入ssh密码" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="this.type=='detail'">
            <a-form-model-item label="ssh密码" v-show="this.type=='detail'" :labelCol="labelCol" :wrapperCol="wrapperCol"   >
              <a-input style="width: 100%"   v-model="model.encryptSshPassword"   ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24"  v-show="!this.showk8s">
            <a-form-model-item label="sudo用户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sudoUser" >
              <j-dict-select-tag :trigger-change="true"  placeholder="请选择是否sudo用户" dictCode="sudo_user" v-model="model.sudoUser" ></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
<!--          <a-col :span="24" v-show="this.type=='add'">-->
<!--            <div class="add_btn_wrap">-->
<!--              <a-button type="primary" class="editable-add-btn" @click="initUser">-->
<!--                初始化-->
<!--              </a-button>-->
<!--            </div>-->
<!--          </a-col>-->

          <a-col :span="24" v-show="!this.isNull(this.initUserList)&&this.initUserList.length>0">
            <a-table class="mb10" :pagination="false" :columns="initUserColumns" :data-source="initUserList" :rowKey="(item, index) => {return index}">
              <span slot="username" slot-scope="text, item">
                <a-input v-model="item.username" :disabled="true" ></a-input>
              </span>
              <span slot="host" slot-scope="text, item">
                <a-input v-model="item.host" :disabled="true" ></a-input>
              </span>
              <span slot="password" slot-scope="text, item">
                <a-input v-model="item.password" type="password" placeholder="请输入密码"></a-input>
              </span>
            </a-table>
          </a-col>

          <a-col :span="24" v-if="showRemoteStore">
            <a-form-model-item label="开启远程存储" :labelCol="labelCol"   :wrapperCol="wrapperCol" prop="enableRemoteStore">
              <j-dict-select-tag   placeholder="请选择是否远程存储（备份、清理数据上传至机房sftp服务器）"
                                 dictCode="enable_remote_store" v-model="model.enableRemoteStore" ></j-dict-select-tag>
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
    name: "DatasourcePropForm",
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
          ip:[ { required: true, message: '请输入数据库IP!' },],
          port:[ { required: true, message: '请输入数据库端口!' },],
          user:[ { required: true, message: '请输入数据库用户名!' },],
          password:[ { required: true, message: '请输入数据库密码!' },],
          sshPort:[ { required: true, message: '请输入ssh端口!' },],
          sshUser:[ { required: true, message: '请输入ssh用户!' },],
          sshPassword:[ { required: true, message: '请输入ssh密码!' },],
          sudoUser:[ { required: true, message: '请选择是否sudo用户!' },],

          k8sConfigId:[ { required: this.showk8s, message: '请选择k8s配置!' },],
          namespace:[ { required: this.showk8s, message: '请输入namespace!' },],
          podName:[ { required: this.showk8s, message: '请输入pod名称!' },],
          isk8s:[ { required: true, message: '请选择是否k8s!' },],
          enableRemoteStore:[ { required: true, message: '请选择是否远程存储（备份、清理数据上传至机房sftp服务器）!' },],
        },
        url: {
          add: "/datasource/add",
          edit: "/datasource/edit",
          queryById: "/datasource/queryById"
        },
        initUserList:[],
        type:"",
        showk8s:false,
        showRemoteStore: false,
        namespaceList:[],
        podNameList:[],
        initUserColumns: [
          {
            title: '用户名',
            dataIndex: 'username',
            key: 'username',
            scopedSlots: { customRender: 'username' },
            width: "100px"
          },
          {
            title: '可访问IP',
            dataIndex: 'host',
            key: 'host',
            scopedSlots: { customRender: 'host' },
            width: "100px"
          },
          {
            title: '密码',
            dataIndex: 'password',
            key: 'password',
            scopedSlots: { customRender: 'password' },
            width: "130px"
          },
        ],
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
          this.changeComputerRoom(record.computerRoomId)
          this.changeIsk8s(record.isk8s);
        }
        this.model = record?Object.assign({}, record):this.model;
        this.id = record?record.id:'';
        this.visible = true;
        if(type=='edit'&&record.isk8s==1){
          this.initNamespace(record.k8sConfigId)
          this.initPod(record.namespace)
          this.model.namespace = record.namespace;
          this.model.podName = record.podName;
        }
      },
      initUser () {
        const that = this;
        that.$refs.form.validate(valid => {
          if (valid) {
            postAction("/datasource/queryInitUser",that.model).then((res)=>{
              if(res.success){
                this.initUserList = res.result
              }
            });
          }
        })
      },
      changeComputerRoom(selectedValue){
        let that = this
        httpAction("/computerRoom/getById?id="+selectedValue,{},'get').then((res)=>{
          if(res.success){
            that.showRemoteStore = res.result.enableRemoteStore==1?true:false
          }
        })
      },
      changeIsk8s(selectedValue){
        if(selectedValue==null||selectedValue==''||selectedValue==undefined){
          return ;
        }
        if(selectedValue=='0'){
          this.showk8s = false
          this.$set(this.model,'k8sConfigId',"")
          this.$set(this.model,'namespace',"")
          this.$set(this.model,'podName',"")
          this.namespaceList = []
          this.podNameList = []
          // this.validatorRules.sshPassword = [ { required: !this.showk8s, message: '请输入ssh密码!' },]
        }else{
          this.showk8s = true
          this.$set(this.model,'sshPort',"")
          this.$set(this.model,'sshUser',"")
          this.$set(this.model,'sshPassword',"")
        }
        this.validatorRules.sshPort = [ { required: !this.showk8s, message: '请输入ssh端口!' },]
        this.validatorRules.sshUser = [ { required: !this.showk8s, message: '请输入ssh用户!' },]
        this.validatorRules.sudoUser = [ { required: !this.showk8s, message: '请选择是否sudo用户!' },]
        if(this.type=='edit'){
          this.validatorRules.sshPassword = [ ]
        }else{
          this.validatorRules.sshPassword = [ { required: !this.showk8s, message: '请输入ssh密码!' },]
        }
        this.validatorRules.k8sConfigId = [ { required: this.showk8s, message: '请选择k8s配置!' },]
        this.validatorRules.namespace = [ { required: this.showk8s, message: '请输入namespace!' },]
        this.validatorRules.podName = [ { required: this.showk8s, message: '请输入pod名称!' },]

      },
      initNamespace(selectedValue){
        let that = this
        if(selectedValue==null||selectedValue==''||selectedValue==undefined){
          return ;
        }
        this.$set(this.model,'namespace','')
        this.$set(this.model,'podName','')
        this.namespaceList = []
        this.podNameList = []
        httpAction("/datasource/getNamespace?k8sConfigId="+selectedValue,{},'get').then((res)=>{
          if(res.success){
            that.namespaceList = res.result
          }else{
            that.$message.warning("获取namespace失败，请检查k8s配置文件是否正确");
          }
        })

      },
      initPod(selectedValue){
        let that = this
        if(selectedValue==null||selectedValue==''||selectedValue==undefined){
          return ;
        }
        this.$set(this.model,'podName','')
        this.podNameList = []
        httpAction("/datasource/getPod?k8sConfigId="+this.model.k8sConfigId+"&namespace="+selectedValue,{},'get').then((res)=>{
          if(res.success){
            that.podNameList = res.result
          }else{
            that.$message.warning("获取pod失败，请检查k8s配置文件是否正确");
          }
        })

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
            this.model.initUser = this.initUserList
            if(!this.isNull(this.model.initUser)){
              for(let i=0;i<this.initUserList.length;i++){
                let initUser = this.initUserList[i]
                if(this.isNull(initUser.password)){
                  that.$message.error("请输入密码");
                  return;
                }
              }
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
