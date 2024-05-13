<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form-model layout="inline" :model="queryParam">
        <a-row :gutter="10">
          <a-col :md="6" :sm="12">
            <a-form-model-item label="机房"  style="margin-left:8px">
              <j-search-select-tag placeholder="请选择机房查询" v-model="queryParam.computerRoomId" dict="computer_room,name,id"></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :md="6" :sm="12">
            <a-form-model-item label="数据库IP"  style="margin-left:8px">
              <a-input placeholder="请输入IP" v-model="queryParam.ip" ></a-input>
            </a-form-model-item>
          </a-col>

          <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
            <a-col :md="6" :sm="24">
             <a-button type="primary" @click="searchQuery" icon="search" style="margin-left: 18px">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
               <a-button type="primary" @click="handleAdd2" icon="plus" style="margin-left: 8px">新增</a-button>
              <a-button type="primary" @click="sendPassword" icon="plus" style="margin-left: 8px">发送密码至邮箱</a-button>
            </a-col>
          </span>
        </a-row>
      </a-form-model>
    </div>

    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>

      <a-table
        ref="table"
        size="middle"
        :scroll="{x:true}"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        class="j-table-force-nowrap"
        @change="handleTableChange">

        <template slot="htmlSlot" slot-scope="text">
          <div v-html="text"></div>
        </template>
        <template slot="imgSlot" slot-scope="text">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无图片</span>
          <img v-else :src="getImgView(text)" height="25px" alt="" style="max-width:80px;font-size: 12px;font-style: italic;"/>
        </template>
        <template slot="fileSlot" slot-scope="text">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无文件</span>
          <a-button
            v-else
            :ghost="true"
            type="primary"
            icon="download"
            size="small"
            @click="uploadFile(text)">
            下载
          </a-button>
        </template>

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit2(record)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDetail2(record)">详情</a>
          <a-divider type="vertical" />
          <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
            <a>删除</a>
          </a-popconfirm>
<!--          <a-divider type="vertical" />-->
<!--          <a @click="resetPwd(record)">重置密码</a>-->
          <a-divider type="vertical" />
          <a @click="userlist(record)">用户列表</a>
        </span>

      </a-table>
    </div>

    <DatasourcePropModal ref="modalForm" @ok="modalFormOk"></DatasourcePropModal>
    <DbUserModal ref="modalForm2" @ok="modalFormOk"></DbUserModal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import DatasourcePropModal from './modules/DatasourcePropModal'
  import DbUserModal from './modules/DbUserModal'
  import {httpAction} from "@api/manage";


  export default {
    name: "ProjectList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      DatasourcePropModal,
      DbUserModal
    },
    data () {
      return {
        description: 'adad管理页面',
        // 表头
        columns: [
          {
            title:'机房',
            align:"center",
            dataIndex: 'computerRoomName'
          },
          {
            title:'业务名称',
            align:"center",
            dataIndex: 'businessName'
          },
          {
            title:'数据库IP',
            align:"center",
            dataIndex: 'ip'
          },{
            title:'端口',
            align:"center",
            dataIndex: 'port'
          },
          ,{
            title:'是否k8s',
            align:"center",
            dataIndex: 'isk8s_dictText',
            customCell:  (record) =>{
              console.log(record)
              if(record.isk8s_dictText==='是'){
                return {
                  style: {
                    'color':'green'
                  }
                }
              }
            }
          },
          {
            title:'用户名',
            align:"center",
            dataIndex: 'user'
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
          list: "/datasource/list",
          delete: "/datasource/delete",
          deleteBatch: "/datasource/deleteBatch"
        },
        dictOptions:{},
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
      resetPwd(record){
        let that = this;
        this.$confirm({
          title: '提示',
          content: '确定要重置密码吗 ?',
          onOk() {
            httpAction("/datasource/resetPwdDatasourceProp",record,"post").then((res)=>{
              if(res.success){
                that.$message.success("密码重置成功，最新密码已发送邮箱");
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
      userlist(record){
        this.$refs.modalForm2.edit(record);
        this.$refs.modalForm2.title="用户列表";
        this.$refs.modalForm2.disableSubmit = false;
      },
      handleAdd2(){
        this.$refs.modalForm.add();
        this.$refs.modalForm.title = "新增";
        this.$refs.modalForm.disableSubmit = false;
      },
      handleEdit2(record){
        this.$refs.modalForm.edit(record,"edit");
        this.$refs.modalForm.title = "编辑";
        this.$refs.modalForm.disableSubmit = false;
      },
      handleDetail2(record){
        this.$refs.modalForm.edit(record,"detail");
        this.$refs.modalForm.title="详情";
        this.$refs.modalForm.disableSubmit = true;
      },
      sendPassword(){
        let that = this;
        httpAction("/datasource/sendPassword", {},"post").then((res)=>{
          if(res.success){
            that.$message.success("密码发送成功");
          }else{
            that.$message.warning(res.message);
          }
        });
      }


    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>