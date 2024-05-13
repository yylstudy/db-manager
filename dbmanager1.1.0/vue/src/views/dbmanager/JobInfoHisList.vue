<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form-model layout="inline" :model="queryParam">
        <a-row :gutter="10">

          <a-col :md="6" :sm="12">
            <a-form-model-item label="业务名称" prop="userId" style="margin-left:8px">
              <j-search-select-tag placeholder="请选择业务名称查询" v-model="queryParam.businessId" dict="business,business_name,id"></j-search-select-tag>
            </a-form-model-item>
          </a-col>
<!--          <a-col :md="6" :sm="12">-->
<!--            <a-form-model-item label="业务类型" prop="userId" style="margin-left:8px">-->
<!--              <j-dict-select-tag placeholder="请选择业务类型查询" v-model="queryParam.taskType" dictCode="task_type"></j-dict-select-tag>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->
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
          <a @click="recovery(record)">恢复</a>
          <a-divider type="vertical" />
          <a @click="handleDetail(record)">详情</a>
        </span>

      </a-table>

    <JobInfoHisModal ref="modalForm" @ok="modalFormOk"></JobInfoHisModal>
  </a-card>
</template>

<script>
  import { httpAction, getAction } from '@/api/manage'
  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import JobInfoHisModal from './modules/JobInfoHisModal'

  export default {
    name: "ProjectList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      JobInfoHisModal,
    },
    data () {
      return {
        description: 'adad管理页面',
        dataList:[],
        showNext:false,
        // 表头
        columns: [
          {
            title:'业务名称',
            align:"center",
            dataIndex: 'businessName'
          },
          {
            title:'规则名称',
            align:"center",
            dataIndex: 'configName'
          },
          {
            title:'数据源',
            align:"center",
            dataIndex: 'datasourceName'
          },
          {
            title:'任务类型',
            align:"center",
            dataIndex: 'taskType_dictText'
          },
          {
            title:'cron',
            align:"center",
            dataIndex: 'jobCron'
          },
          {
            title:'状态',
            align:"center",
            dataIndex: 'triggerStatus_dictText',
            customCell:  (record) =>{
              console.log(record)
              if(record.triggerStatus_dictText==='运行'){
                return {
                  style: {
                    'color':'green'
                  }
                }
              }else{
                return {
                  style: {
                    'color':'red'
                  }
                }
              }

            }
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
          list: "/jobinfoHis/list",
          delete: "/jobinfoHis/delete",
          deleteBatch: "/jobinfoHis/deleteBatch"
        },
        dictOptions:{},
        nextTimeText:""
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
      recovery(record){
        let that = this;
        httpAction("/jobinfoHis/recovery?id="+record.id,{},"post").then((res)=>{
          if(res.success){
            that.$message.success("启动成功");
            that.searchQuery();
          }else{
            that.$message.warning(res.message);
          }
        });

      },
      cancelShowNext(){
        this.showNext = false;
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>