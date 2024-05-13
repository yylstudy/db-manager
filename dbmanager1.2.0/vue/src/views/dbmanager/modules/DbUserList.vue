<template>
  <a-card >
<!--    <div class="table-page-search-wrapper">-->
<!--      <a-form-model layout="inline" :model="queryParam">-->
<!--        <a-row :gutter="10">-->
<!--          <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">-->
<!--            <a-col :md="6" :sm="24">-->
<!--               <a-button type="primary" @click="handleAdd2" icon="plus" style="margin-left: 8px">新增</a-button>-->
<!--            </a-col>-->
<!--          </span>-->
<!--        </a-row>-->
<!--      </a-form-model>-->
<!--    </div>-->

    <!-- table区域-begin -->
<!--    <div>-->
<!--      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">-->
<!--        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项-->
<!--        <a style="margin-left: 24px" @click="onClearSelected">清空</a>-->
<!--      </div>-->

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
        @change="handleTableChange">

        <template slot="htmlSlot" slot-scope="text">
          <div v-html="text"></div>
        </template>

        <span slot="action" slot-scope="text, record">

<!--          <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete2(record)">-->
<!--            <a>删除</a>-->
<!--          </a-popconfirm>-->
          <a @click="showDetail(record)">查看</a>
<!--          <a-divider type="vertical"  v-show="record.localUser"/>-->
<!--          <a @click="grantPrivileges(record)" v-show="record.localUser">授权</a>-->
<!--          <a-divider type="vertical"  v-show="!record.localUser"/>-->
<!--          <a @click="initUser(record)" v-show="!record.localUser">用户初始化</a>-->
<!--          <a-divider type="vertical"  v-show="record.localUser"/>-->
<!--          <a @click="resetPassword(record)" v-show="record.localUser">重置密码</a>-->
        </span>

      </a-table>
    <MysqlUserModal ref="modalForm" @ok="modalFormOk"></MysqlUserModal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import MysqlUserModal from './MysqlUserModal'
  import {httpAction} from "@api/manage";


  export default {
    name: "DbUserList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      MysqlUserModal
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
            title:'可访问ip',
            align:"center",
            dataIndex: 'host'
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
          list: "/datasource/userList",
          delete: "/datasource/deleteUser",
        },
        dictOptions:{},
        propId:""
      }
    },
    created() {
    },
    computed: {
      importExcelUrl: function(){
        return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
      },
    },
    methods: {
      initDictConfig(){
      },
      show(record1){
        this.url.list = "/datasource/userList?id="+record1.id
        this.searchQuery();
        this.propId = record1.id
      },
      handleAdd2(){
        this.$refs.modalForm.add(this.propId);
        this.$refs.modalForm.title = "新增";
        this.$refs.modalForm.disableSubmit = false;
      },
      handleDelete2(record){
        let that = this;
        record.propId = this.propId;
        httpAction("/datasource/deleteUser",record,"post").then((res)=>{
          if(res.success){
            that.$message.success("删除成功");
            that.searchQuery();
          }else{
            that.$message.warning(res.message);
          }
        });
      },
      showDetail(record){
        this.$refs.modalForm.edit(this.propId,record,"detail");
        this.$refs.modalForm.title = "查看";
        this.$refs.modalForm.disableSubmit = true;
      },
      grantPrivileges(record){
        this.$refs.modalForm.edit(this.propId,record,"edit");
        this.$refs.modalForm.title = "授权";
        this.$refs.modalForm.disableSubmit = false;
      },
      initUser(record){
        this.$refs.modalForm.edit(this.propId,record,"init");
        this.$refs.modalForm.title = "初始化";
        this.$refs.modalForm.disableSubmit = false;
      },
      resetPassword(record){
        let that = this;
        this.$confirm({
          title: '提示',
          content: '确定要重置密码吗 ?',
          onOk() {
            record.propId = that.propId;
            httpAction("/datasource/resetPwdMysqlUser",record,"post").then((res)=>{
              if(res.success){
                that.$message.success("密码重置成功，最新密码已发送邮箱");
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