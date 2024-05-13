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
            <a-form-model-item label="数据源"  style="margin-left:8px">
              <a-input placeholder="请输入数据源" v-model="queryParam.mysqlSshHost" ></a-input>
            </a-form-model-item>
          </a-col>
          <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
            <a-col :md="6" :sm="24">
             <a-button type="primary" @click="searchQuery" icon="search" style="margin-left: 18px">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
              <a-button type="primary" @click="handleAdd" icon="plus" style="margin-left: 8px">新增</a-button>
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
          <a @click="handleEdit(record)">编辑</a>

          <a-divider type="vertical" />
          <a-dropdown>
            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>
            <a-menu slot="overlay">
               <a-menu-item>
                <a @click="checkSoft(record)">校验</a>
              </a-menu-item>
              <a-menu-item>
                <a @click="recover(record)">还原准备</a>
              </a-menu-item>
              <a-menu-item>
                <a @click="handleDetail(record)">详情</a>
              </a-menu-item>
              <a-menu-item>
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>
    </div>

    <BackupConfigModal ref="modalForm" @ok="modalFormOk"></BackupConfigModal>
    <RecoverModal ref="modalForm2" @ok="modalFormOk"></RecoverModal>
    <a-modal
      title="检测情况"
      :visible="showInstall"
      @cancel="cancelShowInstall"
      :footer="null"
    >
      <p v-show="xtrabackupSuccess=='true'" style="color:green">1、已安装xtrabackup</p>
      <p v-show="xtrabackupSuccess=='false'"><a @click="installXtrabackup(dataJson)"  style="color:red">1、未安装xtrabackup，点击安装</a></p>
      <p v-show="xtrabackupSuccess=='ing'" style="color:red">1、正在安装xtrabackup，请勿关闭页面</p>
      <p v-show="qpressSuccess=='true'" style="color:green">2、已安装qpress</p>
      <p v-show="qpressSuccess=='false'"><a @click="installQpress(dataJson)"  style="color:red">2、未安装qpress，点击安装</a></p>
      <p v-show="qpressSuccess=='ing'" style="color:red">2、正在安装qpress，请勿关闭页面</p>
      <p v-show="dirSuccess" style="color:green">3、备份目录校验成功</p>
      <p v-show="!dirSuccess"style="color:red">3、备份目录不存在，请先创建备份目录</p>
      <p v-show="connectionSuccess" style="color:green">4、mysql配置成功</p>
      <p v-show="!connectionSuccess" style="color:red">4、mysql配置错误，请检查</p>
<!--      <p  style="color:red">注意：xtrabackup和qpress安装包均下载至/tmp目录，所以需要/usr/bin和/tmp的rwx权限</p>-->
    </a-modal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import BackupConfigModal from './modules/BackupConfigModal'
  import RecoverModal from './modules/RecoverModal'
  import {httpAction,httpAction2} from "@api/manage";


  export default {
    name: "ProjectList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      BackupConfigModal,
      RecoverModal
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
            title:'备份名称',
            align:"center",
            dataIndex: 'name'
          },{
            title:'数据源',
            align:"center",
            dataIndex: 'datasourceName'
          },
          {
            title:'全量备份时间间隔',
            align:"center",
            dataIndex: 'dayBeforeFull'
          },
          {
            title:'本地备份文件存储时间',
            align:"center",
            dataIndex: 'keepDays'
          },
          {
            title:'软件安装状态',
            align:"center",
            dataIndex: 'status_dictText'
          },

          {
            title:'创建时间',
            align:"center",
            dataIndex: 'createTime'
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
          list: "/backup/list",
          delete: "/backup/delete",
          deleteBatch: "/backup/deleteBatch"
        },
        dictOptions:{},
        detail:"",
        showInstall:false,
        xtrabackupSuccess:'',
        qpressSuccess:false,
        dirSuccess:false,
        connectionSuccess:false,
        dataJson:{}
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
      recover(record){
        this.$refs.modalForm2.edit(record);
        this.$refs.modalForm2.title = "还原配置";
        this.$refs.modalForm2.disableSubmit = false;
      },
      cancelShowInstall(){
        this.showInstall = false;
      },
      installXtrabackup(data){
        let that = this;
        this.xtrabackupSuccess = 'ing'
        httpAction2("/backup/installSoftware?id="+data.id+"&type=xtrabackup",{},"post",1000000).then((res)=>{
          if(res.success){
            that.xtrabackupSuccess = 'true'
            this.searchQuery()
            // that.cancelShowInstall();
          }else{
            that.xtrabackupSuccess = 'false'
            that.$message.warning(res.message);
          }
        });

      },
      installQpress(data){
        let that = this;
        this.qpressSuccess='ing'
        httpAction2("/backup/installSoftware?id="+data.id+"&type=qpress",{},"post",1000000).then((res)=>{
          if(res.success){
            that.qpressSuccess = 'true'
            this.searchQuery()
            // that.cancelShowInstall();
          }else{
            that.qpressSuccess = 'false'
            that.$message.warning(res.message);
          }
        });
      },
      checkSoft(record){
        this.dataJson = record
        let that = this;
        httpAction("/backup/checkSoftInstall?id="+record.id,{},"post").then((res)=>{
          if(res.success){
            this.showInstall = true;
            this.xtrabackupSuccess = ''+res.result[0]
            this.qpressSuccess = ''+res.result[1]
            this.dirSuccess = res.result[2]
            this.connectionSuccess = res.result[3]
            that.searchQuery();
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