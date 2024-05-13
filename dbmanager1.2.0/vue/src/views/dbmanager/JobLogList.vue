<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form-model layout="inline" :model="queryParam">
        <a-row :gutter="10">
          <a-col :md="6" :sm="12">
            <a-form-model-item label="执行日期"  style="margin-left:8px">
              <a-date-picker v-model="queryParam.queryDateStart" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" placeholder="请选择执行日期" />
            </a-form-model-item>
          </a-col>
          <a-col :md="6" :sm="12">
            <a-form-model-item label="机房"  style="margin-left:8px">
              <j-search-select-tag placeholder="请选择机房查询" v-model="queryParam.computerRoomId" dict="computer_room,name,id"></j-search-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :md="6" :sm="12">
            <a-form-model-item label="任务类型"  style="margin-left:8px">
              <j-dict-select-tag :trigger-change="true" @change="changeTaskType" placeholder="请选择备份类型"
                                 dictCode="task_type" v-model="queryParam.taskType" ></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
          <a-col :md="6" :sm="12">
            <a-form-model-item label="数据库IP"  style="margin-left:8px">
              <a-input placeholder="请输入IP" v-model="queryParam.mysqlSshHost" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :md="6" :sm="12">
            <a-form-model-item label="执行结果"  style="margin-left:8px">
              <j-dict-select-tag :trigger-change="true" placeholder="请选择执行结果状态查询" dictCode="handle_code" v-model="queryParam.handleCode" ></j-dict-select-tag>
            </a-form-model-item>
          </a-col>
<!--          <a-col :md="6" :sm="12" v-show="showConfig">-->
<!--            <a-form-model-item :label="configName"   prop="configId">-->
<!--              <j-search-select-tag  v-model="queryParam.configId"   :dict="configExpression" placeholder="请选择配置" ></j-search-select-tag>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->
<!--          <a-col :md="6" :sm="12">-->
<!--            <a-form-model-item label="业务"  style="margin-left:8px">-->
<!--              <j-search-select-tag placeholder="请选择业务查询" v-model="queryParam.businessId" dict="business,business_name,id"></j-search-select-tag>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->
<!--          <a-col :md="6" :sm="12">-->
<!--            <a-form-model-item label="负责人"  style="margin-left:8px">-->
<!--              <j-search-select-tag placeholder="请选择负责人查询" v-model="queryParam.userId" dict="sys_user,realname,id"></j-search-select-tag>-->
<!--            </a-form-model-item>-->
<!--          </a-col>-->

          <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
            <a-col :md="6" :sm="24">
<!--             <a-button type="primary" @click="handleAdd" icon="plus" style="margin-left: 18px">新增</a-button>-->
             <a-button type="primary" @click="searchQuery" icon="search" style="margin-left: 18px">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
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

        <span slot="action" slot-scope="text, record">
          <a @click="executeLog(record)">执行日志</a>
          <a-divider type="vertical"  v-show="record.taskType=='2'&&record.handleCode=='2'"/>
          <a @click="showIbdAndFrm(record)" v-show="record.taskType=='2'&&record.handleCode=='2'">查看ibd、frm</a>
<!--          <a-divider type="vertical" />-->
<!--          <a-dropdown>-->
<!--            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>-->
<!--            <a-menu slot="overlay">-->
<!--              <a-menu-item>-->
<!--                -->
<!--              </a-menu-item>-->
<!--&lt;!&ndash;              <a-menu-item>&ndash;&gt;-->
<!--&lt;!&ndash;                <a @click="executeLog(record)">执行日志</a>&ndash;&gt;-->
<!--&lt;!&ndash;              </a-menu-item>&ndash;&gt;-->
<!--            </a-menu>-->
<!--          </a-dropdown>-->
        </span>

      </a-table>
    <job-log-modal ref="modalForm" @ok="modalFormOk"></job-log-modal>
    <IbdfrmModal ref="modalForm2" @ok="modalFormOk"></IbdfrmModal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import JobLogModal from './modules/JobLogModal'
  import IbdfrmModal from './modules/IbdfrmModal'


  export default {
    name: "ProjectList",
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      JobLogModal,
      IbdfrmModal
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
            title:'执行开始时间',
            align:"center",
            dataIndex: 'startDate'
          },
          {
            title:'执行结束时间',
            align:"center",
            dataIndex: 'endDate'
          },
          {
            title:'执行结果',
            align:"center",
            dataIndex: 'handleCode_dictText',
            customCell:  (record) =>{
              console.log(record)
              if(record.handleCode_dictText==='成功'){
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
            title: '操作',
            dataIndex: 'action',
            align:"center",
            fixed:"right",
            width:147,
            scopedSlots: { customRender: 'action' }
          }
        ],
        url: {
          list: "/joblog/list",
          delete: "/joblog/delete",
          deleteBatch: "/joblog/deleteBatch"
        },
        dictOptions:{},
        configExpression:"",
        configName:"xxx",
        showConfig:false,
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
      changeTaskType(selectedValue){
        if(selectedValue==null||selectedValue==''||selectedValue==undefined){
          this.showConfig = false;
          return ;
        }
        this.showConfig = true;
        if(selectedValue=='1'){
          this.configName="备份规则"
          this.configExpression='backup_config,name,id';
        }else if(selectedValue=='2'){
          this.configName="清理规则"
          this.configExpression='clear_data_config,name,id';
        }
        this.$set(this.queryParam,'configId','')
      },
      executeLog(record){
        this.$refs.modalForm.edit(record);
        this.$refs.modalForm.title="执行日志";
        this.$refs.modalForm.disableSubmit = true;
      },
      showIbdAndFrm(record){
        this.$refs.modalForm2.edit(record);
        this.$refs.modalForm2.title="ibd/frm文件查看";
        this.$refs.modalForm2.disableSubmit = false;
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>