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
              <a-input placeholder="请输入IP" v-model="queryParam.mysqlSshHost" ></a-input>
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
                <a @click="showClearName(record)">查看清理表名</a>
              </a-menu-item>
<!--               <a-menu-item>-->
<!--                <a @click="backupIbdAndFrm(record)">idb、frm备份</a>-->
<!--              </a-menu-item>-->
              <a-menu-item>
                <a @click="handleDetail(record)">详情</a>
              </a-menu-item>
              <a-menu-item>
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>
<!--              <a-menu-item>-->
<!--                <a @click="executeLog(record)">执行日志</a>-->
<!--              </a-menu-item>-->
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>
    <data-clear-modal ref="modalForm" @ok="modalFormOk"></data-clear-modal>
    <a-modal
      title="清理表名"
      :visible="showClear"
      @cancel="cancelShowClear"
      :footer="null"
    >
      <div v-show="dataList==null||dataList.length==0" >
        <span style="color:red">未查询到需要清理的表</span>
      </div>
      <div v-for="(item,index) in dataList" :key="index" >
        <p>{{item}}</p>
      </div>
    </a-modal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import DataClearModal from './modules/DataClearModal'
  import {httpAction} from "@api/manage";


  export default {
    name: "ProjectList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      DataClearModal
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
            title:'数据清理名称',
            align:"center",
            dataIndex: 'name'
          },
          {
            title:'数据源',
            align:"center",
            dataIndex: 'datasourceName'
          },
          // {
          //   title:'表名正则',
          //   align:"center",
          //   dataIndex: 'tableNameRegular'
          // },
          // {
          //   title:'表名包含时间',
          //   align:"center",
          //   dataIndex: 'containTime_dictText'
          // },
          // {
          //   title:'时间格式',
          //   align:"center",
          //   dataIndex: 'timeRule_dictText'
          // },
          // {
          //   title:'距当前清理时间',
          //   align:"center",
          //   dataIndex: 'clearTimeBefore'
          // },
          {
            title:'清理类型',
            align:"center",
            dataIndex: 'clearType_dictText'
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
          list: "/clearData/list",
          delete: "/clearData/delete",
          deleteBatch: "/clearData/deleteBatch"
        },
        dataList:[],
        dictOptions:{},
        showClear:false
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
      cancelShowClear(){
        this.showClear = false;
      },
      showClearName(record){
        let that = this;
        this.showClear = true;
        this.dataList = [];
        httpAction("/clearData/getClearTableName?id="+record.id,{},"get").then((res)=>{
          if(res.success){
            that.dataList = res.result;
          }else{
            that.$message.warning(res.message);
          }
        });

      },
      testConnection(record){
        let that = this;
        httpAction("/clearData/testConnection?id="+record.id,{},"post").then((res)=>{
          if(res.success){
            that.$message.success(res.message);
          }else{
            that.$message.warning(res.message);
          }
        });

      },
      executeLog(record){
        this.$refs.modalForm.edit(record);
        this.$refs.modalForm.title="执行日志";
        this.$refs.modalForm.disableSubmit = true;
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>