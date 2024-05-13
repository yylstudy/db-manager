<template>
  <a-card >
    <div class="table-page-search-wrapper">
      <a-form-model layout="inline" :model="queryParam">
        <a-row :gutter="10">
          <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
            <a-col :md="6" :sm="24">
               <a-button type="primary" @click="resetPassword" icon="reset" style="margin-left: 8px">重置密码</a-button>
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
    </div>
  </a-card>

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
    name: "ResetGroupUserForm",
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
          host:[ { required: true, message: '请输入host，多个以,分割，不限制则为%!' },],
          username:[ { required: true, message: '请输入用户名!' },],
          db:[ { required: true, message: '请输入db，多个以,分割，不限制则为*!' },],
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
            scopedSlots: { customRender: "existsLocal" }
          },
          {
            title:'本地密码与组密码一致',
            align:"center",
            dataIndex: 'passwordGroupMatch',
            scopedSlots: { customRender: "passwordGroupMatch" }
          },

          {
            title:'本地密码与实际数据库密码一致',
            align:"center",
            dataIndex: 'passwordMatch',
            scopedSlots: { customRender: "passwordMatch" }
          },
        ],
        url: {
          list:"/datasource/resetUserList",
          add: "/datasource/addUser",
          edit: "/datasource/grantPrivileges",
          queryById: "/datasource/queryById"
        },
        type: "add",
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
        that.model.username = record.username;
        that.model.host = "";
        if(record!=null&&record!=undefined&&record!=''){
          this.url.add="/datasource/grantPrivileges"
          this.$nextTick(()=>{
            postAction("/datasource/resetUserList", that.model).then((res)=>{
              if(res.success){
                this.dataSource = res.result
              }else{
                that.$message.error(res.message);
              }
            });
          })
        }
      },
      resetPassword(){
        let that = this
        if(this.selectionRows.length==0){
          this.$message.warning("请选择要重置的数据");
          return;
        }
        for(let i=0;i<this.selectionRows.length;i++){
          let selectRow = this.selectionRows[i];
          if(!selectRow.existsLocal){
            this.$message.warning("数据库："+selectRow.ip+",用户:"+selectRow.username+",host:"+selectRow.host+"本地不存在；");
            return;
          }
          if(selectRow.passwordGroupMatch&&selectRow.passwordMatch){
            this.$message.warning("数据库："+selectRow.ip+",用户:"+selectRow.username+",host:"+selectRow.host+"数据库密码已和组密码一致，无需重置");
            return;
          }
        }

        this.$confirm({
          title: '提示',
          content: '确定要重置密码吗 ?',
          onOk() {
            let data = {
              groupId: that.model.groupId,
              username: that.model.username,
              dbUsers:that.selectionRows
            }
            httpAction("/datasource/resetGroupUserPassword",data,'post').then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
              }else{
                that.$message.warning(res.message);
              }
            })
          },
          onCancel() {
          },
        });
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
              url = "/datasource/addUser";
            }else if(that.type=='init'){
              if(this.model.password==null||this.model.password==undefined||this.model.password==''){
                this.$message.warning("请输入密码");
                return;
              }
              url = "/datasource/initGroupUser"
            }else {
              that.model.list = that.dataSource
              url = "/datasource/grantGroupPrivileges"
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
