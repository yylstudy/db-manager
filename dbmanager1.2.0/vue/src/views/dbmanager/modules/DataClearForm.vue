<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="数据清理名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="name">
              <a-input v-model="model.name"   placeholder="请选择业务" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="数据源" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <el-cascader  :options="provs"  @change="initDatabase"  size="small" v-model="model.mysqlUserId"  :disabled="this.disabled"></el-cascader>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="数据库" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="dbDatabase">
              <a-select  v-model="model.dbDatabase" placeholder="请选择数据库"  >
                <a-select-option v-for="(item,index) in dataList" :key="index"  :value="item">
                  {{item}}
                </a-select-option>
              </a-select>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="清理类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="clearType">
              <j-dict-select-tag :trigger-change="true"  placeholder="请选择清理类型" dictCode="clear_type" v-model="model.clearType" ></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="ibd/frm储存目录"  :labelCol="labelCol" :wrapperCol="wrapperCol" prop="ibdFrmDir">
              <a-input v-model="model.ibdFrmDir" :disabled="isk8s"  placeholder="请输入ibd/frm储存目录"></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <div class="add_btn_wrap">
              <a-button type="primary" class="editable-add-btn" @click="handleAdd">
                新增
              </a-button>
            </div>
            <a-table class="mb10" :pagination="false" :columns="tableNameRuleColumns" :data-source="tableNameRules" :rowKey="(record, index) => {return index}">
              <span slot="tableNameRegular" slot-scope="text, record">
                <a-input v-model="record.tableNameRegular" placeholder="请输入表名正则"></a-input>
              </span>
              <span slot="containTime" slot-scope="text, record">
                <j-dict-select-tag :trigger-change="true" placeholder="请选择" dictCode="contain_time" v-model="record.containTime" ></j-dict-select-tag>
              </span>
              <span slot="timeRule" slot-scope="text, record">
                <j-dict-select-tag :disabled="record.containTime != '0'" :trigger-change="true" placeholder="请选择时间格式" dictCode="time_rule" v-model="record.timeRule" ></j-dict-select-tag>
              </span>
              <span slot="clearTimeStart" slot-scope="text, record">
                <a-input-number :parser="limitNumber" :min="0" :disabled="record.containTime != '0'" v-model="record.clearTimeStart" placeholder="请输入距当前清理开始间隔"></a-input-number>
              </span>
              <span slot="clearTimeEnd" slot-scope="text, record">
                <a-input-number :parser="limitNumber" :min="0" :disabled="record.containTime != '0'" v-model="record.clearTimeEnd" placeholder="请输入距当前清理结束间隔"></a-input-number>
              </span>
              <span slot="action" slot-scope="text, record, index">
                <a-button type="danger" class="editable-add-btn" @click="handleDelete(record, index)">
                  删除
                </a-button>
              </span>
            </a-table>
          </a-col>

          <a-col :span="24">
            <p style="color:red">备注:</p>
            <p style="color:red">1、数据清理前需要先备份ibd和frm，所以选择的数据库的ssh用户需要mysql数据目录的rx权限，存储ibd和frm目录的rwx权限</p>
            <p style="color:red">2、若表名包含时间，距当前清理开始间隔、距当前清理结束间隔意义为清理距当前时间对应时间间隔内的表数据，例：表名正则为：t_order_[0-9]{6}，
              距当前清理开始间隔值为5，距当前清理开始间隔值为3，当前时间为2021-10-08，时间格式为yyyyMM，则会清理t_order_202105、t_order_202106、
              t_order_202107的表数据</p>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
  import { httpAction, getAction } from '@api/manage'
  import JFormContainer from '@comp/jeecg/JFormContainer'
  import JDate from '@comp/jeecg/JDate'
  import JDictSelectTag from "@comp/dict/JDictSelectTag"
  import Cascader from "ant-design-vue/es/vc-cascader/Cascader";

  export default {
    name: "DataClearForm",
    components: {
      JFormContainer,
      JDate,
      JDictSelectTag,
      Cascader,
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
          name:[ { required: true, message: '请输入清理名称!' },],
          computerRoomId:[ { required: true, message: '请选择机房!' },],
          businessId:[ { required: true, message: '请选择业务!' },],
          clearType:[ { required: true, message: '请选择清理类型!' },],
          ibdFrmDir:[ { required: true, message: '请输入ibd/frm储存目录！' },],
          dbDatabase:[ { required: true, message: '请选择数据库！' },],

        },
        url: {
          add: "/clearData/add",
          edit: "/clearData/edit",
          queryById: "/clearData/queryById"
        },
        clearTime:"",
        tableNameRuleColumns: [
          {
            title: '表名正则',
            dataIndex: 'tableNameRegular',
            key: 'tableNameRegular',
            scopedSlots: { customRender: 'tableNameRegular' },
            width: "350px"
          },
          {
            title: '是否包含时间',
            dataIndex: 'containTime',
            key: 'containTime',
            scopedSlots: { customRender: 'containTime' },
            width: "100px"
          },
          {
            title: '时间格式',
            dataIndex: 'timeRule',
            key: 'timeRule',
            scopedSlots: { customRender: 'timeRule' },
            width: "130px"
          },
          {
            title: '距当前清理开始间隔',
            dataIndex: 'clearTimeStart',
            key: 'clearTimeStart',
            scopedSlots: { customRender: 'clearTimeStart' },
            width: "80px"
          },
          {
            title: '距当前清理结束间隔',
            dataIndex: 'clearTimeEnd',
            key: 'clearTimeEnd',
            scopedSlots: { customRender: 'clearTimeEnd' },
            width: "80px"
          },
          {
            title: '操作',
            key: 'action',
            scopedSlots: { customRender: 'action' },
          },
        ],
        tableNameRules: [],
        limitNumber: limitNumber,
        provs: null,
        dataList:[],
        isk8s:false,
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
      this.initMysqlUser();
    },
    methods: {
      show (record) {
        this.model = record?Object.assign({}, record):this.model;
        let dbDatabase = this.model.dbDatabase
        if(this.model.tableNameRules!=null&&this.model.tableNameRules!=undefined){
          let tableNameRules = this.model.tableNameRules;
          tableNameRules.map(item => {
            item.clearDataConfigId = this.model.id;
          });
          this.tableNameRules = tableNameRules;
        }
        this.id = record?record.id:'';
        if(record){
          let arr = []
          arr.push(this.model.propId )
          arr.push(this.model.mysqlUserId )
          this.initDatabase(arr)
          this.model.mysqlUserId = arr
          this.model.dbDatabase = dbDatabase
          // this.changeContainTime(record.containTime)
          // this.changeTimeRule(record.timeRule);
          this.checkK8s(this.model.propId);
        }
        this.visible = true;
      },
      // changeContainTime(value){
        
      // },
      // changeTimeRule(value){
      //   if(value=='1'||value=='2'){
      //     this.clearTime = "(天)";
      //   }else if(value=='3'||value=='4'){
      //     this.clearTime = "(月)";
      //   }else{
      //     this.clearTime = "";
      //   }
      // },

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
      isNull(obj){
        return obj==null||obj==''||obj==undefined
      },

      submitForm () {
        const that = this;
        // 触发表单验证
        that.$refs.form.validate(valid => {
          if (valid) {
            let isTableRequired = false; // 表格校验ok: false 
            let tipMsg = '请填写表格';
            if(this.tableNameRules==null||this.tableNameRules.length==0){
              that.$message.warning("请添加表名规则");
              return;
            }
            this.tableNameRules.map((item, index) => {
              if(item.containTime == '0'){
                if(!item.timeRule) {
                  isTableRequired = true;
                  tipMsg = "请选择时间格式";
                }else if(!item.clearTimeStart && item.clearTimeStart != 0) {
                  isTableRequired = true;
                  tipMsg = "请输入清理开始时间";
                }else if(!item.clearTimeEnd && item.clearTimeEnd != 0) {
                  isTableRequired = true;
                  tipMsg = "请输入清理结束时间";
                }else if(item.clearTimeStart < item.clearTimeEnd) {
                  isTableRequired = true;
                  tipMsg = "清理结束时间要小于等于清理开始时间";
                }
              }else{
                this.tableNameRules[index].timeRule=''
                this.tableNameRules[index].clearTimeBefore=''
              }
            });
            if(isTableRequired) {
              that.$message.warning(tipMsg);
              return false
            }
            if(this.model.mysqlUserId==null||this.model.mysqlUserId==undefined||this.model.mysqlUserId.length==0){
              that.$message.warning("请选择数据源");
              return
            }
            this.model.mysqlUserId = this.model.mysqlUserId[1]
            this.model.tableNameRules = this.tableNameRules;
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
            // console.log("httpurl save", httpurl)
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

        });

      },
      popupCallback(row){
        this.model = Object.assign(this.model, row);
      },
      initMysqlUser(){
        getAction("/datasource/getMysqlUserByLevel",{}).then((res)=>{
          this.provs = res.result;
        });
      },
      handleAdd() {
        let tableNameRuleItem = {
          clearDataConfigId: this.tableNameRules.length + 1, // TODO:获取真实的clearDataConfigId
          id: '', // 新增id为空 保存后会有id
          tableNameRegular: '',
          containTime: '1',
          timeRule: '',
          clearTimeStart: '',
          clearTimeEnd: '',
        }
        this.tableNameRules.push(tableNameRuleItem);
      },
      checkK8s(selectedValue){
        let that = this
        httpAction("/datasource/getDatasourcePropK8sConfig?id="+selectedValue,{},'get').then((res)=>{
          if(res.success){
            that.isk8s = res.result.isk8s=='1'
            if(that.isk8s){
              this.$set(this.model,'ibdFrmDir',res.result.nfsBaseDir+"/"+res.result.namespace+"_"+res.result.podName)
            }
          }else{
            that.$message.warning("查询数据库失败，请确认是否有对应权限");
          }
        })
      },
      initDatabase(selectedValue){
        let that = this
        if(selectedValue==null||selectedValue==''||selectedValue==undefined){
          return ;
        }
        this.checkK8s(selectedValue[0])
        this.$set(this.model,'dbDatabase','')
        this.dataList = []
        httpAction("/datasource/getRealDatabase?mysqlUserId="+selectedValue[1],{},'get').then((res)=>{
          if(res.success){
            that.dataList = res.result
          }else{
            that.$message.warning("查询数据库失败，请确认是否有对应权限");
          }
        })

      },
      handleDelete(record, index) {
        this.tableNameRules.splice(index, 1);
      },
    }
  }

  const limitNumber = value => {
         if (typeof value === 'string') {
          return !isNaN(Number(value)) ? value.replace(/^(0+)|[^\d]/g, '') : ''
        } else if (typeof value === 'number') {
          return !isNaN(value) ? String(value).replace(/^(0+)|[^\d]/g, '') : ''
        } else {
          return ''
        }
      }
</script>
<style scoped>
.add_btn_wrap{
  margin-bottom: 10px;
  display: flex;
  justify-content: flex-end;
}
.mb10{
  margin-bottom: 10px;
}
</style>
