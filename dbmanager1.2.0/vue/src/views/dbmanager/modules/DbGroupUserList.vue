<template>
  <a-card >
    <div class="table-page-search-wrapper">
      <a-form-model layout="inline" :model="queryParam">
        <a-row :gutter="10">
          <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
            <a-col :md="6" :sm="24">
               <a-button type="primary" @click="handleAdd2" icon="plus" style="margin-left: 8px">新增</a-button>
            </a-col>
          </span>
        </a-row>
      </a-form-model>
    </div>
      <a-table
        ref="table"
        size="middle"
        :scroll="{x:true}"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="false"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        class="j-table-force-nowrap"
        @change="handleTableChange">

        <template slot="htmlSlot" slot-scope="text">
          <div v-html="text"></div>
        </template>

        <span slot="action" slot-scope="text, record">
          <a @click="resetGroupPwd(record)" >重置组密码</a>
          <a-divider type="vertical" />
          <a @click="showDetail(record)">查看</a>
          <a-divider type="vertical"  v-show="record.showGrant"/>
          <a @click="grantPrivileges(record)" v-show="record.showGrant">授权</a>
          <a-divider type="vertical"  v-show="record.showReset"/>
          <a @click="resetUser(record)" v-show="record.showReset">重置密码</a>
          <a-divider type="vertical"  />
          <a @click="removeUser(record)">删除用户</a>
        </span>

      </a-table>
    <MysqlGroupUserModal ref="modalForm" @ok="modalFormOk"></MysqlGroupUserModal>
    <ResetGroupUserModal ref="modalForm2" @ok="modalFormOk"></ResetGroupUserModal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import MysqlGroupUserModal from './MysqlGroupUserModal'
  import ResetGroupUserModal from './ResetGroupUserModal'
  import {httpAction} from "@api/manage";


  export default {
    name: "DbGroupUserList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      MysqlGroupUserModal,
      ResetGroupUserModal
    },
    data () {
      return {
        description: 'adad管理页面',
        // 表头
        columns: [
          {
            title:'用户名',
            align:"center",
            dataIndex: 'username'
          },
          {
            title:'数据库ip',
            align:"center",
            dataIndex: 'ip'
          },
          {
            title:'备注',
            align:"center",
            dataIndex: 'remark',
            customRender: (text, row, index) => {
              return  <span style="color:red;white-space: pre-line;">{text}</span>
            }
            // customCell:  (record) =>{
            //   return {
            //     style: {
            //       'color':'red'
            //     }
            //   }
            // }
          },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            fixed:"right",
            width:147,
            scopedSlots: { customRender: 'action' }
          }
        ],
        url: {
          list: "/datasource/userGroupList",
          delete: "/datasource/deleteUser",
        },
        dictOptions:{},
        groupId:""
      }
    },
    created() {
    },
    computed: {
    },
    methods: {
      initDictConfig(){
      },
      show(record1){
        this.url.list = "/datasource/userGroupList?groupId="+record1.id
        this.searchQuery();
        this.groupId = record1.id
      },
      handleAdd2(){
        this.$refs.modalForm.add(this.groupId);
        this.$refs.modalForm.title = "新增";
        this.$refs.modalForm.disableSubmit = false;
      },
      showDetail(record){
        this.$refs.modalForm.edit(this.groupId,record,"detail");
        this.$refs.modalForm.title = "查看";
        this.$refs.modalForm.disableSubmit = true;
      },
      grantPrivileges(record){
        this.$refs.modalForm.edit(this.groupId,record,"grantPrivileges");
        this.$refs.modalForm.title = "授权";
        this.$refs.modalForm.disableSubmit = false;
      },
      resetGroupPwd(record){
        let that = this;
        this.$confirm({
          title: '提示',
          content: '确定要重置组密码吗?',
          onOk() {
            record.groupId = that.groupId;
            httpAction("/datasource/resetGroupPwd",record,"post").then((res)=>{
              if(res.success){
                that.$message.success("组密码重置成功");
                that.searchQuery();
              }else{
                that.$message.warning(res.message);
              }
            });
          },
          onCancel() {
          },
        });
      },
      resetUser(record){
        this.$refs.modalForm2.edit(this.groupId,record,"reset");
        this.$refs.modalForm2.title = "重置密码";
        this.$refs.modalForm2.disableSubmit = false;
      },
      removeUser(record){
        let that = this;
        this.$confirm({
          title: '提示',
          content: '确定要删除用户吗，同时也会删除对应数据库中的用户?',
          onOk() {
            record.groupId = that.groupId;
            httpAction("/datasource/deleteGroupUser",record,"post").then((res)=>{
              if(res.success){
                that.searchQuery();
                that.$message.success("密码删除成功");
              }else{
                that.$message.warning(res.message);
              }
            });
          },
          onCancel() {
          },
        });
      }

    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>