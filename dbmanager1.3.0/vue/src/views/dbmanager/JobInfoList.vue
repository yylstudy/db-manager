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
            <a-form-model-item label="任务类型"  style="margin-left:8px">
              <j-dict-select-tag :trigger-change="true"  placeholder="请选择备份类型"
                                 dictCode="task_type" v-model="queryParam.taskType" ></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :md="6" :sm="12">
            <a-form-model-item label="数据库IP"  style="margin-left:8px">
              <a-input placeholder="请输入IP" v-model="queryParam.mysqlSshHost" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :md="6" :sm="12">
            <a-form-model-item label="任务状态"  style="margin-left:8px">
              <j-dict-select-tag :trigger-change="true" placeholder="请选择任务状态查询" dictCode="trigger_status" v-model="queryParam.triggerStatus" ></j-dict-select-tag>
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
          <a @click="handleEdit(record)">编辑</a>

          <a-divider type="vertical" />
          <a-dropdown>
            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a @click="executeOneTime(record)">执行一次</a>
              </a-menu-item>
              <a-menu-item v-show="record.triggerStatus==0">
                <a @click="start(record)" >启动</a>
              </a-menu-item>
              <a-menu-item v-show="record.triggerStatus==1">
                <a @click="stop(record)">停止</a>
              </a-menu-item>
              <a-menu-item>
                <a @click="getNextTriggerTime(record)">下次执行时间</a>
              </a-menu-item>
              <a-menu-item>
                <a @click="handleDetail(record)">详情</a>
              </a-menu-item>
              <a-menu-item v-show="record.triggerStatus==0">
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>

    <job-info-modal ref="modalForm" @ok="modalFormOk"></job-info-modal>
    <job-info-modal2 ref="modalForm2" @ok="modalFormOk"></job-info-modal2>
    <a-modal
      :title="nextTimeText"
      :visible="showNext"
      @cancel="cancelShowNext"
      :footer="null"
    >
      <div v-for="(item,index) in dataList" :key="index" >
        <p>{{item}}</p>
      </div>
    </a-modal>
  </a-card>
</template>

<script>
  import { httpAction, getAction } from '@/api/manage'
  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import JobInfoModal from './modules/JobInfoModal'
  import JobInfoModal2 from './modules/JobInfoModal2'

  export default {
    name: "ProjectList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      JobInfoModal,
      JobInfoModal2
    },
    data () {
      return {
        description: 'adad管理页面',
        dataList:[],
        showNext:false,
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
            title:'任务类型',
            align:"center",
            dataIndex: 'taskType_dictText'
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
          list: "/jobinfo/list",
          delete: "/jobinfo/delete",
          deleteBatch: "/jobinfo/deleteBatch"
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
      executeOneTime(record){
        this.$refs.modalForm2.title = "手动执行";
        this.$refs.modalForm2.edit(record);
      },
      getNextTriggerTime(record){
        let that = this;
        this.nextTimeText = "下次执行时间";
        this.showNext = true;
        this.dataList = [];
        httpAction("/jobinfo/nextTriggerTime?id="+record.id,{},"get").then((res)=>{
          if(res.success){
            that.dataList = res.result;
          }else{
            that.$message.warning(res.message);
          }
        });

      },
      recycleBin(){

      },
      start(record){
        let that = this;
        httpAction("/jobinfo/changeStatus?id="+record.id+"&triggerStatus=1",{},"post").then((res)=>{
          if(res.success){
            that.$message.success("启动成功");
            that.searchQuery();
          }else{
            that.$message.warning(res.message);
          }
        });

      },
      stop(record){
        let that = this;
        httpAction("/jobinfo/changeStatus?id="+record.id+"&triggerStatus=0",{},"post").then((res)=>{
          if(res.success){
            that.$message.success("停止成功");
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