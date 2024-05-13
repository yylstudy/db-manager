<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="任务类型" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="taskType">
              <j-dict-select-tag :trigger-change="true" @change="changeTaskType" placeholder="请选择备份类型" dictCode="task_type" v-model="model.taskType" ></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" v-show="showConfig">
            <a-form-model-item :label="configName" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="configId">
              <j-search-select-tag  v-model="model.configId"   :dict="configExpression" placeholder="请选择配置" ></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="Cron*" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="jobCron">
              <a-input style="width: 100%" v-model="model.jobCron" placeholder="请输入Cron" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="参数" :labelCol="labelCol" :wrapperCol="wrapperCol"  prop="param">
              <a-input style="width: 100%" v-model="model.param" placeholder="请输入参数" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col v-if="showFlowSubmitButton" :span="24" style="text-align: center">
            <a-button @click="submitForm">提 交</a-button>
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

  export default {
    name: "JobInfoHisForm",
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
          name:[ { required: true, message: '请输入任务名称!' },],
          taskType:[ { required: true, message: '请选择备份类型!' },],
          configId:[ { required: true, message: '请选择配置!' },],
          jobCron:[ { required: true, message: '请输入Cron!' },],
          // param:[ { required: true, message: '请输入参数!' },],
        },
        url: {
          add: "/jobinfoHis/add",
          edit: "/jobinfoHis/edit",
          queryById: "/jobinfoHis/queryById"
        },
        configExpression:"",
        configName:"xxx",
        showConfig:false
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
        if(record){
          let ss = Object.assign({}, record)
          this.changeTaskType(ss.taskType)
        }
        this.model = record?Object.assign({}, record):this.model;
        this.id = record?record.id:'';
        this.visible = true;

      },
      changeTaskType(selectedValue){
        if(selectedValue==null||selectedValue==''||selectedValue==undefined){
          this.showConfig = false;
          return ;
        }
        this.showConfig = true;
        // this.form.setFieldsValue({taskType:""});
        this.model.configId="";
        if(selectedValue=='1'){
          this.configName="备份规则"
          this.configExpression='backup_config,name,id,status=1';
        }else if(selectedValue=='2'){
          this.configName="清理规则"
          this.configExpression='clear_data_config,name,id';
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
            that.confirmLoading = true;
            let httpurl = '';
            let method = '';
            if(!this.id){
              httpurl+=this.url.add;
              method = 'post';
            }else{
              httpurl+=this.url.edit;
              method = 'put';
            }
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
