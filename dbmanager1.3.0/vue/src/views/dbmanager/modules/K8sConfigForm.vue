<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="k8s集群名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="name">
              <a-input v-model="model.name" placeholder="请输入k8s集群名称"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" >
            <a-form-item label="k8s配置文件"   :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-upload  class=""
                         :action="uploadAction"
                         :multiple="true"
                         listType="text"
                         v-decorator="['testFile']"
                         :headers="headers"
                         :fileList="testFileList"
                         @change="testHandleChange"
                         @download="testFileDownload"
                         :showUploadList="{
                            showRemoveIcon: !formDisabled,
                            showDownloadIcon: true
                          }"
              >
                <a-icon type="plus" />
              </a-upload>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>
<script>

  import { httpAction, getAction } from '@/api/manage'
  import { validateDuplicateValue } from '@/utils/util'
  import { ACCESS_TOKEN } from "@/store/mutation-types"
  import Vue from "vue";
  export default {
    name: 'K8sConfigForm',
    components: {
    },
    props: {
      //表单禁用
      disabled: {
        type: Boolean,
        default: false,
        required: false
      }
    },
    data () {
      return {
        model:{
         },
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
          name:[ { required: true, message: '请输入k8s集群名称!' },],
          nfsSshIp:[ { required: true, message: '请输入nfs ip!' },],
          nfsSshPort:[ { required: true, message: '请输入nfs端口!' },],
          nfsSshUser:[ { required: true, message: '请输入nfs用户!' },],
          nfsSshPassword:[ { required: true, message: '请输入nfs密码!' },],
          nfsBaseDir:[ { required: true, message: '请输入nfs数据基础目录!' },],
        },
        url: {
          fileLogoUpload: window._CONFIG['domianURL']+"/upload?businessType=1001",
          add: "/k8sconfig/add",
          edit: "/k8sconfig/edit",
          queryById: "/k8sconfig/queryById",
          getAttachment: "/selectByBusinessIdAndType",
          download: window._CONFIG['domianURL']+"/download?attachmentId="
        },
        testFileList: [],
        headers: {
          authorization: 'authorization-text',
        },
      }
    },
    computed: {
      formDisabled(){
        return this.disabled
      },
      uploadAction:function () {
        // 上传接口
        return this.url.fileLogoUpload;
      },
    },
    created () {
      const token =  Vue.ls.get(ACCESS_TOKEN);
      this.headers = {"X-Access-Token":token}
       //备份model原始值
      this.modelDefault = JSON.parse(JSON.stringify(this.model));

    },
    methods: {
      add () {
        this.edit(this.modelDefault);
      },
      edit (record) {
        this.model = Object.assign({}, record);
        this.visible = true;
        this.$nextTick(() => {
          if(!this.isNull(this.model)&&!this.isNull(this.model.id)){
            let token = Vue.ls.get(ACCESS_TOKEN)
            getAction(this.url.getAttachment+"?businessId="+record.id+"&businessType=1001",{}).then((res)=>{
              for(let i=0;i<res.length;i++){
                let urls = this.url.download+res[i].id+"&X-Access-Token="+token;
                this.testFileList.push({
                  uid: i,
                  name: res[i].rawFileName,
                  status: 'done',
                  url: urls,
                  response: {
                    attachmentId: res[i].id,
                  }
                });
              }
            });
          }

        });
      },
      testHandleChange({ fileList }){
        this.testFileList = fileList;
      },
      testFileDownload(file){
        let token = Vue.ls.get(ACCESS_TOKEN)
        let urls = file.url+"&X-Access-Token="+token;
        window.open(urls);
      },
      isNull(obj){
        if(obj==null||obj==''||obj==undefined){
          return true;
        }
        return false;
      },
      submitForm () {
        const that = this;
        // 触发表单验证
        this.$refs.form.validate(valid => {
          if (valid) {
            let httpurl = '';
            let method = '';
            if(!this.model.id){
              httpurl+=this.url.add;
              method = 'post';
            }else{
              httpurl+=this.url.edit;
               method = 'put';
            }
            if(that.testFileList.length==0||that.testFileList.length>1){
              that.$message.error("请上传一个配置文件");
              return;
            }
            let attachmentIds = [];
            for(let i=0;i<that.testFileList.length;i++){
              let attachmentId = that.testFileList[i].response.attachmentId;
              attachmentIds.push(attachmentId);
            }
            that.confirmLoading = true;
            this.model.attachementIds = attachmentIds;
            httpAction(httpurl,this.model,method).then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
              }else{
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
            })
          }
         
        })
      },
    }
  }
</script>