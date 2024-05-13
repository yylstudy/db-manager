<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
          <a-col :span="24" v-show="false">
            <a-form-model-item label="Cron*" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <a-input style="width: 100%" v-model="model.id"  :disabled="disabled"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="任务类型" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <j-dict-select-tag :trigger-change="true" @change="changeTaskType" placeholder="请选择备份类型" dictCode="task_type" v-model="model.taskType" disabled="disabled"></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item :label="configName"  :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <j-search-select-tag :trigger-change="true" v-model="model.configId"  :dict="configExpression" placeholder="请选择配置" disabled="disabled" ></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="Cron*" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <a-input style="width: 100%" v-model="model.jobCron" placeholder="请输入Cron" disabled="disabled"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="参数" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <a-input style="width: 100%" v-model="model.param" placeholder="请输入参数" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col v-if="showFlowSubmitButton" :span="24" style="text-align: center">
            <a-button @click="submitForm">提 交</a-button>
          </a-col>
          <a-col :span="24" v-show="showTables">
            <a-form-model-item label="清理表名" :labelCol="labelCol" :wrapperCol="wrapperCol"  >
              <p v-html="clearTables" v-show="clearTables!=null&&clearTables!=''&&clearTables!=undefined"></p>
              <p  v-show="clearTables==null||clearTables==''||clearTables==undefined"><span style="color:red">未查询到需要清理的表</span></p>
            </a-form-model-item>
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
    name: "JobInfoForm2",
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
          method:[ { required: true, message: '请输入执行方法!' },],
          businessId:[ { required: true, message: '请选择项目!' },],
          jobCron:[ { required: true, message: '请输入Cron!' },],
          param:[ { required: true, message: '请输入参数!' },],
        },
        url: {
          add: "/jobinfo/add",
          edit: "/jobinfo/edit",
          queryById: "/jobinfo/queryById"
        },
        configExpression:"",
        showTables:false,
        clearTables:"",
        configName:"xxx"
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
        const that = this;
        if(record){
          let ss = Object.assign({}, record)
          this.changeTaskType(ss.taskType)
        }
        this.model = record?Object.assign({}, record):this.model;
        if(this.model.taskType=='2'){
          this.showTables = true;
          this.clearTables = "";
          httpAction("/clearData/getClearTableNameByJobId?jobId="+this.model.id, {},"get").then((res)=>{
            if(res.success){
              let clearTables = "";
              for(let i=0;i<res.result.length;i++){
                clearTables+=res.result[i]+"<br/>"
              }
              this.clearTables = clearTables;
            }else{
              that.$message.warning(res.message);
            }
          }).finally(() => {
            that.confirmLoading = false;
          })
        }
        this.id = record?record.id:'';
        this.visible = true;
      },
      changeTaskType(selectedValue){
        this.model.configId="";
        // this.form.setFieldsValue({taskType:""});
        if(selectedValue=='1'){
          this.configName="备份规则"
          this.configExpression='backup_config,name,id';
        }else if(selectedValue=='2'){
          this.configName="清理规则"
          this.configExpression='clear_data_config,name,id';
        }
        this.$set(this.model,'configId','')
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
            httpAction("/jobinfo/executeOneTime",this.model,"post").then((res)=>{
              if(res.success){
                that.$message.success("执行成功");
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
