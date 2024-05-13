<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container >
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules" v-show="this.type!='add'">
        <a-form-model layout="inline" :model="queryParam">
          <a-row :gutter="10">
          <span style="color:red;margin-bottom: 6px;" disabled="false" class="table-page-search-submitButtons">
            <a-col :md="24" :sm="24">
             组密码：{{groupPassword?groupPassword:"未设置组密码"}}
            </a-col>
          </span>
          </a-row>
        </a-form-model>
      <a-table
        ref="table"
        size="middle"
        :scroll="{x:true}"
        bordered
        rowKey="id"
        :columns="columns"
        :pagination="false"
        :dataSource="dataSource"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        class="j-table-force-nowrap"
        @change="handleTableChange"
      >
      <span slot="existsLocal" :style="existsLocal?'color:green':'color:red'" slot-scope="existsLocal">
        {{ existsLocal ? "是":"否" }}
      </span>
        <span slot="passwordGroupMatch" :style="passwordGroupMatch?'color:green':'color:red'" slot-scope="passwordGroupMatch">
        {{ passwordGroupMatch ? "是":"否" }}
      </span>
        <span slot="passwordMatch" :style="passwordMatch?'color:green':'color:red'" slot-scope="passwordMatch">
        {{ passwordMatch ? "是":"否" }}
      </span>
        <template slot="htmlSlot" slot-scope="text">
          <div v-html="text"></div>
        </template>
      </a-table>
      </a-form-model>
      <a-form-model ref="form" :model="model" slot="detail" :rules="validatorRules" >
        <a-row>
          <a-col :span="24" v-show="this.type!='detail'&&this.type!='add'&&this.type!='grantPrivileges'">
            <a-form-model-item label="密码" :labelCol="labelCol" :wrapperCol="wrapperCol" >
              <a-input style="width: 100%" :type="this.type=='init'?'password':'text'"  v-model="model.password"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" >
            <a-form-model-item label="用户名" :labelCol="labelCol" :wrapperCol="wrapperCol" v-show="this.type=='add'" >
              <a-input style="width: 100%"  v-model="model.username"  placeholder="请输入用户名" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" >
            <a-form-model-item label="hosts" :labelCol="labelCol" :wrapperCol="wrapperCol" v-show="this.type!='detail'">
              <a-input style="width: 100%"  v-model="model.hosts"  placeholder="请输入host，多个以,分割，不限制则为%" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24" >
            <a-form-model-item label="db" :labelCol="labelCol" :wrapperCol="wrapperCol" v-show="this.type!='detail'">
              <a-input style="width: 100%"  v-model="model.dbs" placeholder="请输入db，多个以,分割，不限制则为*"  ></a-input>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>
  import { httpAction, getAction,postAction } from '@api/manage'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import JFormContainer from '@comp/jeecg/JFormContainer'
  import JDate from '@comp/jeecg/JDate'
  import JDictSelectTag from "@comp/dict/JDictSelectTag"
  import JMultiSelectTag from '@/components/dict/JMultiSelectTag'
  
  export default {
    name: "MysqlGroupUserForm",
    mixins:[JeecgListMixin, mixinDevice],
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
          // host:[ { required: true, message: '请输入host，多个以,分割，不限制则为%!' },],
          // username:[ { required: true, message: '请输入用户名!' },],
          // db:[ { required: true, message: '请输入db，多个以,分割，不限制则为*!' },],
        },
        columns: [
          {
            title:'数据库ip',
            align:"center",
            dataIndex: 'ip'
          },
          {
            title:'可访问ip',
            align:"center",
            dataIndex: 'host'
          },
          {
            title:'可访问db',
            align:"center",
            dataIndex: 'db'
          },
          {
            title:'本地用户',
            align:"center",
            dataIndex: 'existsLocal',
            scopedSlots: { customRender: "existsLocal" },
          },
          {
            title:'本地密码与组密码一致',
            align:"center",
            dataIndex: 'passwordGroupMatch',
            scopedSlots: { customRender: "passwordGroupMatch" },
          },

          {
            title:'本地密码与实际数据库密码一致',
            align:"center",
            dataIndex: 'passwordMatch',
            scopedSlots: { customRender: "passwordMatch" },
          },
        ],
        url: {
          list:"/datasource/getGroupUser",
        },
        type: null,
        groupPassword:""
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
      show (record,groupId,type) {
        this.type = type;
        const that = this;
        that.model = record?Object.assign({}, record):that.model;
        this.visible = true;
        that.model.groupId = groupId;
        that.model.host = "";
        if(record!=null&&record!=undefined&&record!=''){
          that.model.username = record.username;
          this.$nextTick(()=>{
            postAction("/datasource/getGroupUser", that.model).then((res)=>{
              if(res.success){
                that.dataSource = res.result.list
                that.$set(that.model,'dbs',res.result.allDb)
                that.$set(that.model,'hosts',res.result.allHost)
                that.groupPassword = res.result.password
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
            let url = "";
            if(that.type=='add'){
              url = "/datasource/addGroupUser";
              if(!this.model.username){
                that.$message.warning("请输入用户名");
                return;
              }
              if(!this.model.hosts){
                that.$message.warning("请输入host");
                return;
              }
              if(!this.model.dbs){
                that.$message.warning("请输入db");
                return;
              }
            }else if(that.type=='grantPrivileges'){
              url = "/datasource/grantGroupPrivileges"
              if(!this.model.hosts){
                that.$message.warning("请输入host");
                return;
              }
              if(!this.model.dbs){
                that.$message.warning("请输入db");
                return;
              }
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
