<template>
  <a-spin :spinning="confirmLoading">
    <p v-html="handleMsg"></p>
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
<!--          <a-col :span="24">-->
<!--            <a-form-model-item label="任务名称" :labelCol="labelCol" :wrapperCol="wrapperCol">-->
<!--              <a-input v-model="model.name" ></a-input>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->
<!--          <a-col :span="24">-->
<!--            <a-form-model-item label="业务名称" :labelCol="labelCol" :wrapperCol="wrapperCol" >-->
<!--              <a-input style="width: 100%" :min="1" v-model="model.businessName"  ></a-input>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->
<!--          <a-col :span="24">-->
<!--            <a-form-model-item label="任务类型" :labelCol="labelCol" :wrapperCol="wrapperCol" >-->
<!--              <j-dict-select-tag :trigger-change="true"  dictCode="task_type" v-model="model.taskType" ></j-dict-select-tag>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->
<!--          <a-col :span="24">-->
<!--            <a-form-model-item label="执行开始时间" :labelCol="labelCol" :wrapperCol="wrapperCol" >-->
<!--              <a-input style="width: 100%" :min="1" v-model="model.startDate"  ></a-input>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->
<!--          <a-col :span="24">-->
<!--            <a-form-model-item label="执行结束时间" :labelCol="labelCol" :wrapperCol="wrapperCol" >-->
<!--              <a-input style="width: 100%" :min="1" v-model="model.endDate"  ></a-input>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->
<!--          <a-col v-if="showFlowSubmitButton" :span="24" style="text-align: center">-->
<!--            <a-button @click="submitForm">提 交</a-button>-->
<!--          </a-col>-->

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
    name: "JobLogForm",
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
          id:[ { required: true, message: '请输入租户编号!' },]
        },
        url: {
          add: "/dbmanager/project/add",
          edit: "/dbmanager/project/edit",
          queryById: "/dbmanager/project/queryById"
        },
        handleMsg:''
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
        let that = this;
        this.model = record?Object.assign({}, record):this.model;
        this.id = record?record.id:'';
        getAction('/joblog/queryById?id='+this.id,{}).then((res)=>{
          if(res.success){
            that.handleMsg = res.result.handleMsg
          }else{
            that.$message.warning(res.message);
          }
        })
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
