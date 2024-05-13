<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="备份名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="name">
              <a-input v-model="model.name" placeholder="请输入备份名称"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="数据源" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="propId">
              <j-search-select-tag v-model="model.propId"  @change="initDatabase" dict="v_datasource_prop,name,id" placeholder="请选择数据源" ></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备份databases" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="backupDatabases">
              <a-select  v-model="model.backupDatabases" mode="multiple" placeholder="请选择备份的db,多个以,分隔，不输入默认全库备份"  >
                <a-select-option v-for="(item,index) in dataList" :key="index"  :value="item">
                  {{item}}
                </a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备份本地存储目录" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="backupPath">
              <a-input v-model="model.backupPath" placeholder="请输入备份本地存储地址"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="mysql配置文件路径" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="mysqlCnf">
              <a-input v-model="model.mysqlCnf" placeholder="请输入mysql配置文件路径"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="全量备份时间间隔(天)" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="dayBeforeFull">
              <a-input-number v-model="model.dayBeforeFull" placeholder="请输入全量备份时间间隔"></a-input-number>
            </a-form-model-item>

          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备份文件存储时间(天)" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="keepDays">
              <a-input-number v-model="model.keepDays" placeholder="请输入本地备份文件存储时间"></a-input-number>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <p style="color:red">备注:</p>
            <p style="color:red">1、备份数据库不配置时，则备份所有database</p>
            <p style="color:red">2、执行备份需要linux层面mysql数据目录rx权限、备份本地存储目录rwx权限</p>
            <p style="color:red">3、备份本地存储目录为数据源所在服务器目录</p>
            <p style="color:red">4、全量备份时间间隔为：若当前时间与上一次全量备份时间相差超过此间隔，则下次进行全量备份</p>
            <p style="color:red">5、备份文件存储时间为：仅针对已打成tar包全量和增量文件的压缩包</p>
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
    name: "BackupConfigForm",
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
          name:[ { required: true, message: '请输入备份名称!' },],
          propId:[ { required: true, message: '请选择数据源!' },],
          backupPath:[ { required: true, message: '请输入备份本地存储地址!' },],
          enableRemoteStore:[ { required: true, message: '请选择是否远程存储' },],
          mysqlCnf:[ { required: true, message: '请输入mysql配置文件路径!' },],
          dayBeforeFull:[ { required: true, message: '请输入全量备份时间间隔!' },],
          keepDays:[ { required: true, message: '请输入本地备份文件存储时间!' },],
        },
        url: {
          add: "/backup/add",
          edit: "/backup/edit",
          queryById: "/backup/queryById"
        },
        dataList:[]
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
        this.model = record?Object.assign({}, record):this.model;
        if(record){
          let ss = Object.assign({}, record)
          let backupDatabases = ss.backupDatabases
          this.initDatabase(ss.propId)
          this.model.backupDatabases = backupDatabases

        }
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
      testSsh(){
        const that = this;
        if(this.isNull(this.model.propId)){
          that.$message.error("请选择数据源");
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
      testmysql(){
        const that = this;
        if(this.isNull(this.model.mysqlSshHost)){
          that.$message.error("请输入mysql服务ssh地址");
          return
        }
        if(this.isNull(this.model.mysqlPort)){
          that.$message.error("请输入mysql端口");
          return
        }
        if(this.isNull(this.model.mysqlUser)){
          that.$message.error("请输入mysql用户名");
          return
        }
        if(this.isNull(this.model.mysqlPassword)){
          that.$message.error("请输入mysql密码密文");
          return
        }
        if(this.isNull(this.model.mysqlPasswordKey)){
          that.$message.error("请输入mysql密码秘钥");
          return
        }
        httpAction("/backup/testmysql",this.model,"post").then((res)=>{
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
      initDatabase(selectedValue){
        let that = this
        if(selectedValue==null||selectedValue==''||selectedValue==undefined){
          return ;
        }
        this.$set(this.model,'backupDatabases',[])
        this.dataList = []
        httpAction("/datasource/getRealDatabaseByPropId?propId="+selectedValue,{},'get').then((res)=>{
          if(res.success){
            that.dataList = res.result
          }else{
            that.$message.warning("查询数据库失败，请确认是否有对应权限");
          }
        })

      },
      submitForm () {
        const that = this;
        // 触发表单验证
        that.$refs.form.validate(valid => {
          if (valid) {
            if(this.model.dayBeforeFull>=this.model.keepDays){
              this.$message.error("全量备份时间间隔不能大于等于备份文件存储时间");
              return ;
            }
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
